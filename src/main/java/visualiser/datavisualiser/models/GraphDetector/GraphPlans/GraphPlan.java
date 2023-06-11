package visualiser.datavisualiser.models.GraphDetector.GraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class GraphPlan {
    public abstract String getName();

    public abstract List<GraphAttribute> getAllOrderedAttributes();

//      TODO: make abstract again
//    public abstract Chart getChart(DataTable dataTable);

    public Chart getChart(DataTable dataTable) {
        System.out.println("tried to print chart " + getClass().getName() + " " + getName());

        if (getAllOrderedAttributes().isEmpty()) {
            return null;
        }

        System.out.print(" with atts:");
        for (GraphAttribute att : getAllOrderedAttributes()) {
            System.out.println(att.attribute().toString());
        }

        return null;
    }

    protected static List<List<GraphAttribute>> findMandatoryAndOptionalAttsOrder(List<Attribute> atts,
                                                                                  List<AttributeType> mandatories,
                                                                                  List<AttributeType> optionals) {
        int attSize = atts.size();
        int numOptionals = attSize - mandatories.size();
        if (optionals.size() < numOptionals) {
            // There are more optionals needed than the graph type allows
            return Collections.emptyList();
        }

        List<List<Integer>> possMandOrders = new ArrayList<>();
        possMandOrders.add(Collections.emptyList());

        if (!mandatories.isEmpty()) {
            // e.g. returning [[0,1],[0]] means that the 0th mandatory type for the graph matches the 0th and 1st attribute
            //                                   and the 1st mandatory type for the graph matches the 0th attribute
            List<List<Integer>> possMandatories = findMatchingMandatoryTypes(mandatories, atts);
            if (possMandatories.isEmpty()) {
                return Collections.emptyList();
            }

            // e.g. for possOrder [[1, 0]] this means that the only possible combination would be to have the
            //          0th mandatory type be the 1st attribute
            //          1st mandatory type be the 0th attribute
            //  All orders are of size mandatories.size()
            possMandOrders = findPossibleOrdersOfAttributes(possMandatories, false);
        }

        if (possMandOrders.size() == 0) {
            return Collections.emptyList();
        }

        // This will be a list of possible orders which are of the format:
        //     possibleOrder[0] = possible mandatory order
        //     possibleOrder[1] = possible optionals order
        List<List<List<Integer>>> possOrders = new ArrayList<>();
        if (numOptionals == 0) {
            // Add an empty list to represent optionals for each mandatory order
            possMandOrders.forEach(possMandOrder -> possOrders.add(new ArrayList<>(List.of(possMandOrder, new ArrayList<>()))));
        } else {
            // Check for optional attributes
            for (List<Integer> possMandOrder : possMandOrders) {
                List<List<Integer>> possOptionals = findMatchingOptionalTypes(optionals, possMandOrder, atts);

                List<List<Integer>> possOptOrders = findPossibleOrdersOfAttributes(possOptionals, true);
                if (possOptOrders.size() == 0) {
                    // this mandatory order of attributes is not possible, since there is no fit for remaining attributes
                    continue;
                }

                for (List<Integer> possOptOrder : possOptOrders) {
                    List<List<Integer>> possOrder = List.of(possMandOrder, possOptOrder);
                    possOrders.add(possOrder);
                }
            }
        }

        // Convert possible orders indexes to GraphAttributes
        //  Every possible order should be of the same length
        return possOrders.stream().map(possOrder -> {
            List<Integer> possMandOrder = possOrder.get(0);
            List<Integer> possOptOrder = possOrder.get(1);

            List<GraphAttribute> possAttsOrder = new ArrayList<>(mandatories.size() + optionals.size());
            IntStream.range(0, mandatories.size()).forEach(mandTypeIdx -> {
                int attIdx = possMandOrder.get(mandTypeIdx);
                possAttsOrder.add(new GraphAttribute(atts.get(attIdx), mandatories.get(mandTypeIdx), false));
            });

            if (possOptOrder.isEmpty()) {
                optionals.forEach(optType -> possAttsOrder.add(new GraphAttribute(null, optType, true)));
                return possAttsOrder;
            }

            IntStream.range(0, optionals.size()).forEach(optTypeIdx -> {
                Integer attIdx = possOptOrder.get(optTypeIdx);
                AttributeType optAttType = optionals.get(optTypeIdx);

                // optional indices are nullable
                if (attIdx == null) {
                    possAttsOrder.add(new GraphAttribute(null, optAttType, true));
                    return;
                }

                possAttsOrder.add(new GraphAttribute(atts.get(attIdx), optAttType, true));
            });

            return possAttsOrder;
        }).collect(Collectors.toList());
    }

    // e.g. returning [[0,1],[0]] means that the 0th mandatory type for the graph matches the 0th and 1st attribute
    //                                   and the 1st mandatory type for the graph matches the 0th attribute
    private static List<List<Integer>> findMatchingMandatoryTypes(List<AttributeType> mandatories,
                                                                  List<Attribute> atts) {
        List<List<Integer>> possMandatories = new ArrayList<>();
        for (AttributeType mandType : mandatories) {
            List<Integer> possIdxs = AttributeType.findMatchingIndices(mandType, atts);
            if (possIdxs.isEmpty()) {
                return Collections.emptyList();
            }

            possMandatories.add(possIdxs);
        }

        return possMandatories;
    }

    private static List<List<Integer>> findMatchingOptionalTypes(List<AttributeType> optionalTypes,
                                                                 List<Integer> orderOfMandatories,
                                                                 List<Attribute> atts) {
        // Remove attributes that are already a part of this order
        List<Attribute> optionalAtts = new ArrayList<>(atts);
        List<Integer> possMandOrderReverse = new ArrayList<>(orderOfMandatories);
        possMandOrderReverse.sort(Comparator.reverseOrder());
        for (Integer idx : possMandOrderReverse) {
            optionalAtts.remove((int) idx);
        }

        List<List<Integer>> possOptionals = new ArrayList<>();
        for (AttributeType optType : optionalTypes) {
            List<Integer> possIdxs = AttributeType.findMatchingIndices(optType, optionalAtts);
            possOptionals.add(possIdxs.stream().map(idx -> atts.indexOf(optionalAtts.get(idx))).collect(Collectors.toList()));
        }

        return possOptionals;
    }

    // e.g. (extending above) returning[[1, 0]] this means that the only possible combination would be to have the
    //          0th mandatory type be the 1st attribute
    //          1st mandatory type be the 0th attribute
    private static List<List<Integer>> findPossibleOrdersOfAttributes(List<List<Integer>> indicesList,
                                                                      boolean areOptional) {
        // Only usable for optionals
        Set<Integer> uniqueIdxs = null;
        if (areOptional) {
            // highest index represents a null value
            // all possible integers provided can be null
            uniqueIdxs = indicesList.stream().flatMap(List::stream).collect(Collectors.toSet());
            indicesList.forEach(list -> list.add(null));
        }

        List<List<Integer>> combinations = new ArrayList<>();
        // initialize an array of indices
        // indicesList.size() is the total number of mandatories/optionals
        int[] indices = new int[indicesList.size()];

        // Loop through all possible combinations of indices
        while (true) {
            List<Integer> combination = new ArrayList<>();
            for (int i = 0; i < indicesList.size(); i++) {
                // Generate this combination
                combination.add(indicesList.get(i).get(indices[i]));
            }

            // Check if all indices are unique
            Set<Integer> uniques = new HashSet<>(combination);
            if (areOptional) {
                // Check that all possible indices are included, and only nulls are duplicated
                List<Integer> combinationCopy = new ArrayList<>(combination);
                List<Integer> nullList = new ArrayList<>();
                nullList.add(null);
                combinationCopy.removeAll(nullList);

                Set<Integer> combCopyUniques = new HashSet<>(combinationCopy);
                if (combinationCopy.size() == combCopyUniques.size()
                        && uniques.containsAll(uniqueIdxs)) {
                    combinations.add(combination);
                }
            } else if (combination.size() == uniques.size()) {
                combinations.add(combination);
            }

            // Increment the indices
            int j = indicesList.size() - 1;
            while (j >= 0 && indices[j] == indicesList.get(j).size() - 1) {
                indices[j] = 0;
                j--;
            }
            if (j < 0) {
                // No more combinations
                break;
            }
            indices[j]++;
        }
        return combinations;
    }
}