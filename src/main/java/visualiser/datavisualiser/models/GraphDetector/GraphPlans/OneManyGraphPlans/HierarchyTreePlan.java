package visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleOrgChart;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.Collections;
import java.util.List;

public class HierarchyTreePlan extends OneManyGraphPlan {

    private static final String planName = "Hierarchy Tree";
    private static final int k1LowerLim = 1;
    private static final int k1UpperLim = 100;
    private static final AttributeType k1Type = AttributeType.ANY;
    private static final int k2LowerLim = 1;
    private static final int k2UpperLim = 100;
    private static final AttributeType k2Type = AttributeType.ANY;
    private static final List<AttributeType> mandatories = Collections.emptyList();
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private HierarchyTreePlan(PrimaryKey k1, PrimaryKey k2,
                              List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, k2, orderedMandAtts, orderedOptionalAtts);
    }

    public static OneManyGraphPlan getDummyInstance() {
        return new HierarchyTreePlan(null, null, null, null);
    }

    @Override
    public OneManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                        List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new HierarchyTreePlan(k1, k2, orderedMandAtts, orderedOptionalAtts);
    }

    @Override
    public String getPlanName() {return planName;}

    @Override
    public int getK1LowerLim() {
        return k1LowerLim;
    }

    @Override
    public int getK1UpperLim() {
        return k1UpperLim;
    }

    @Override
    public AttributeType getK1Type() {
        return k1Type;
    }

    @Override
    public int getK2PerK1LowerLim() {
        return k2LowerLim;
    }

    @Override
    public int getK2PerK1UpperLim() {
        return k2UpperLim;
    }

    @Override
    public AttributeType getK2Type() {
        return k2Type;
    }

    @Override
    public List<AttributeType> getMandatories() {
        return mandatories;
    }

    @Override
    public List<AttributeType> getOptionals() {
        return optionals;
    }

    @Override
    public Chart getChart(DataTable dataTable) {
        String colourId = null;
        if (!getOrderedOptionals().isEmpty() && getOrderedOptionals().get(0).attribute() != null) {
            colourId = getOrderedOptionals().get(0).attribute().toString();
        }

        return new GoogleOrgChart(dataTable, getChildKey().toString(), getParentKey().toString(), colourId);
    }
}
