package visualiser.datavisualiser.models.GraphDetector.GraphPlans;

import visualiser.datavisualiser.models.GoogleChart.ChartType;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.*;
import java.util.stream.Collectors;

 public abstract class GraphPlan {

     public abstract String getOrderedAttributesRepresentation();
     public abstract List<String> getOrderedColumnNames();

     // Can be overwritten
     public ChartType getGoogleChartType() {
         return ChartType.NOT_SUPPORTED;
     }

     // TODO:
     //  > have some way of determining the VisSchemaPattern
     //  > then match the GraphPlan to the correct one here?
     //  > return a GraphPlan which can then be used to generate the graph

     protected static List<List<Attribute>> findMandatoryAndOptionalAttsOrder(List<Attribute> atts,
                                                                              List<AttributeType> mandatories,
                                                                              List<AttributeType> optionals) {
         int attSize = atts.size();
         // e.g. returning [[0,1],[0]] means that the 0th mandatory type for the graph matches the 0th and 1st attribute
         //                                   and the 1st mandatory type for the graph matches the 0th attribute
         List<List<Integer>> possMandatories = findMatchingMandatoryTypes(mandatories, atts);
         if (possMandatories.isEmpty()) {
             return Collections.emptyList();
         }

         int numOptionals = attSize - mandatories.size();
         if (optionals.size() < numOptionals) {
             // There are more optionals needed than the graph type allows
             return Collections.emptyList();
         }

         // e.g. for possOrder [[1, 0]] this means that the only possible combination would be to have the
         //          0th mandatory type be the 1st attribute
         //          1st mandatory type be the 0th attribute
         //  All orders are of size mandatories.size()
         List<List<Integer>> possOrders = findPossibleOrdersOfAttributes(possMandatories);
         if (possOrders.size() == 0) {
             return Collections.emptyList();
         }

         // Check for optional attributes
         if (numOptionals > 0) {
             List<List<Integer>> possMandOrders = possOrders;

             possOrders = new ArrayList<>();
             for (List<Integer> possMandOrder : possMandOrders) {
                 List<List<Integer>> possOptionals = findMatchingOptionalTypes(optionals, possMandOrder, atts);

                 List<List<Integer>> possOptOrders = findPossibleOrdersOfAttributes(possOptionals);
                 if (possOptOrders.size() == 0) {
                     // this mandatory order of attributes is not possible, since there is no fit for remaining attributes
                     continue;
                 }

                 for (List<Integer> possOptOrder : possOptOrders) {
                     ArrayList<Integer> possOrder = new ArrayList<>();
                     possOrder.addAll(possMandOrder);
                     possOrder.addAll(possOptOrder);

                     possOrders.add(possOrder);
                 }
             }
         }

         // Convert possible orders indexes to Attributes
         return possOrders.stream().map(idxs -> idxs.stream().map(atts::get).collect(Collectors.toList())).collect(Collectors.toList());
     }

     // e.g. returning [[0,1],[0]] means that the 0th mandatory type for the graph matches the 0th and 1st attribute
     //                                   and the 1st mandatory type for the graph matches the 0th attribute
     private static List<List<Integer>> findMatchingMandatoryTypes(List<AttributeType> mandatories,
                                                                   List<Attribute> atts) {
         List<List<Integer>> possMandatories = new ArrayList<>();
         for (AttributeType mandType : mandatories) {
             List<Integer> possIdxs = AttributeType.findMatchingIndices(mandType, atts);
             if (possIdxs.size() == 0) {
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
         ArrayList<Attribute> optionalAtts = new ArrayList<>(atts);
         ArrayList<Integer> possMandOrderReverse = new ArrayList<>(orderOfMandatories);
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
     private static List<List<Integer>> findPossibleOrdersOfAttributes(List<List<Integer>> indicesList) {
         List<List<Integer>> combinations = new ArrayList<>();
         int[] indices = new int[indicesList.size()]; // initialize an array of indices

         // Loop through all possible combinations of indices
         while (true) {
             List<Integer> combination = new ArrayList<>();
             for (int i = 0; i < indicesList.size(); i++) {
                 // Generate this combination
                 combination.add(indicesList.get(i).get(indices[i]));
             }

             // Check if all indices are unique
             if (combination.size() == new HashSet<>(combination).size()) {
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