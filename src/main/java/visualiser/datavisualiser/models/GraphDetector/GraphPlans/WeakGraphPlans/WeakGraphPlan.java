package visualiser.datavisualiser.models.GraphDetector.GraphPlans.WeakGraphPlans;

import visualiser.datavisualiser.models.DataTable.Column;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.DataTable.DataType;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.*;

public abstract class WeakGraphPlan extends GraphPlan {

    // Weak entity attribute
    private final PrimaryKey k1;
    // Owner entity attribute
    private final PrimaryKey k2;
    // Attributes from weak entity
    private final List<GraphAttribute> orderedMandatoryAtts;
    private final List<GraphAttribute> orderedOptionalAtts;

    WeakGraphPlan(PrimaryKey k1, PrimaryKey k2, List<GraphAttribute> orderedMandatoryAtts, List<GraphAttribute> orderedOptionalAtts) {
        this.k1 = k1;
        this.k2 = k2;
        this.orderedMandatoryAtts = orderedMandatoryAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    @Override
    public String getName() {
        return k1.getTable() + " - " + k2.getTable();
    }

    protected PrimaryKey getK1() {
        return k1;
    }

    protected PrimaryKey getK2() {
        return k2;
    }

    protected List<GraphAttribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    protected List<GraphAttribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static WeakGraphPlan getDummyInstance() {
        return null;
    }

    public abstract WeakGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                              List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts);

    @Override
    public List<GraphAttribute> getAllOrderedAttributes() {
        List<GraphAttribute> allAtts = new ArrayList<>(orderedMandatoryAtts);
        allAtts.addAll(orderedOptionalAtts);
        return allAtts;
    }

    public abstract String getPlanName();

    public abstract int getK1LowerLim();

    public abstract int getK1UpperLim();

    public abstract AttributeType getK1Type();

    public abstract int getK2LowerLim();

    public abstract int getK2UpperLim();

    public abstract AttributeType getK2Type();

    public abstract boolean mustBeComplete();

    public abstract List<AttributeType> getMandatories();
    public abstract List<AttributeType> getOptionals();

//    // TODO: add complete stuff
//    public Set<GraphPlan> fitAttributesToPlan(Attribute k1Att, Attribute k2Att, List<Attribute> unorderedAtts) {
//        Set<GraphPlan> plans = new HashSet<>();
//        List<List<Attribute>> possibleOrders = orderAttributesByType(k1Att, k2Att, unorderedAtts);
//
//        for (List<Attribute> possibleOrder : possibleOrders) {
//            plans.add(getInstance(k1Att, k2Att, possibleOrder));
//        }
//
//        return plans;
//    }

    public boolean fitKTypes(PrimaryKey k1, PrimaryKey k2) {
        Set<PrimaryAttribute> k1Atts = new HashSet<>(k1.getPAttributes());
        Set<PrimaryAttribute> k2Atts = new HashSet<>(k2.getPAttributes());

        // check if one primary key contains the other. If so, remove the
        //  similar attributes from the larger key
        if (!k1.equals(k2)) {
            // There should be two primary keys
            List<List<PrimaryAttribute>> sharedAtts = k1.sharedAttributes(k2);
            if (k1Atts.size() > k2Atts.size()) {
                List<PrimaryAttribute> zeroSharedAtts = sharedAtts.get(0);
                zeroSharedAtts.forEach(k1Atts::remove);
            } else if (k2Atts.size() > k1Atts.size()) {
                List<PrimaryAttribute> oneSharedAtts = sharedAtts.get(1);
                oneSharedAtts.forEach(k2Atts::remove);
            }
        }

        boolean k1Correct;
        if (k1Atts.size() == 1) {
            // If there is one primary attribute then use that to check the type
            k1Correct = k1Atts.stream().findFirst().get().getDBType().getAttType().isType(getK1Type());
        } else {
            // If there are more than one primary attributes, the AttributeType is equivalent to LEXICAL
            k1Correct = AttributeType.LEXICAL.isType(getK1Type());
        }

        if (k1Correct) {
            if (k2Atts.size() == 1) {
                // If there is one primary attribute then use that to check the type
                return k2Atts.stream().findFirst().get().getDBType().getAttType().isType(getK2Type());
            }

            // If there are more than one primary attributes, the AttributeType is equivalent to LEXICAL
            return AttributeType.LEXICAL.isType(getK2Type());
        }

        return false;
    }

    public Set<GraphPlan> fitAttributesToPlan(PrimaryKey k1, PrimaryKey k2, List<Attribute> unorderedAtts) {
        int attSize = unorderedAtts.size();
        int smallestAttsSize = getMandatories().size();
        int largestAttsSize = smallestAttsSize + getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize) {
            return Collections.emptySet();
        }

        Set<GraphPlan> plans = new HashSet<>();
        List<List<GraphAttribute>> possibleOrders = findMandatoryAndOptionalAttsOrder(unorderedAtts, getMandatories(), getOptionals());

        int numMandatories = getMandatories().size();
        for (List<GraphAttribute> possibleOrder : possibleOrders) {
            List<GraphAttribute> possibleMandOrder = possibleOrder.subList(0, numMandatories);
            List<GraphAttribute> possibleOptOrder = possibleOrder.subList(numMandatories, possibleOrder.size());

            plans.add(getInstance(k1, k2, possibleMandOrder, possibleOptOrder));
        }

        return plans;
    }

    // groupsId:        e.g. city
    // groupTitlesId:   e.g. year
    // valuesId:        e.g. population
    protected static DataTable shiftDataViaGroups(DataTable dataTable, String groupsId, String groupTitlesId, String valuesId) {
        int groupsIdx = -1;
        int groupTitlesIdx = -1;
        int valuesIdx = -1;
        for (int i = 0; i < dataTable.columns().size(); i++) {
            Column col = dataTable.columns().get(i);
            if (col.id().equals(groupsId)) {
                groupsIdx = i;
            } else if (col.id().equals(groupTitlesId)) {
                groupTitlesIdx = i;
            } else if (col.id().equals(valuesId)) {
                valuesIdx = i;
            }
        }

        if (groupsIdx == -1 || groupTitlesIdx == -1 || valuesIdx == -1) {
            throw new IllegalArgumentException("WeakGraphPlan.shiftDataViaGroups: dataTable does not apply to " +
                    "plan with attributes " + groupsId + " and " + groupTitlesId);
        }

        Map<String, Set<String>> linesToLineVals = new HashMap<>();
        Set<String> possibleLineVals = new HashSet<>();
        for (List<DataCell> row : dataTable.rows()) {
            String lineName = row.get(groupsIdx).value();
            String lineVal = row.get(groupTitlesIdx).value();

            if (linesToLineVals.containsKey(lineName)) {
                linesToLineVals.get(lineName).add(lineVal);
            } else {
                linesToLineVals.put(lineName, new HashSet<>(Collections.singleton(lineVal)));
            }

            possibleLineVals.add(lineVal);
        }

        List<Column> newCols = new ArrayList<>();
        newCols.add(dataTable.columns().get(groupTitlesIdx)); // year

        List<String> groups = new ArrayList<>(linesToLineVals.keySet());
        DataType valuesType = dataTable.columns().get(valuesIdx).type();
        for (String group : groups) {
            newCols.add(new Column(valuesType, group));
        }

        List<String> uniqueLineVals = new ArrayList<>(possibleLineVals);

        // initialise newRows to have null DataCells
        List<List<DataCell>> newRows = new ArrayList<>();
        DataCell nullCell = new DataCell(null, valuesType);
        for (String lineVal : uniqueLineVals) {
            List<DataCell> nullRow = new ArrayList<>();
            newCols.forEach(col -> nullRow.add(nullCell));
            nullRow.remove(0);
            nullRow.add(0, new DataCell(lineVal, dataTable.columns().get(groupTitlesIdx).type()));
            newRows.add(nullRow);
        }

        // Add x axis vals
        for (List<DataCell> row : dataTable.rows()) {
            String group = row.get(groupsIdx).value();
            String groupTitle = row.get(groupTitlesIdx).value();
            DataCell valueVal = row.get(valuesIdx);

            int groupIdx = groups.indexOf(group) + 1;
            int groupTitleIdx = uniqueLineVals.indexOf(groupTitle);
            newRows.get(groupTitleIdx).remove(groupIdx);
            newRows.get(groupTitleIdx).add(groupIdx, valueVal);
        }

        return new DataTable(newCols, newRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, k2, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
