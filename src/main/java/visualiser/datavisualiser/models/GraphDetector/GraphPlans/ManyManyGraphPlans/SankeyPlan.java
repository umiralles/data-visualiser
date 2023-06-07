package visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleSankeyDiagram;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.List;

public class SankeyPlan extends ManyManyGraphPlan {

    private static final String planName = "Sankey";
    private static final int k1LowerLim = 1;
    private static final int k1UpperLim = 20;
    private static final AttributeType k1Type = AttributeType.ANY;
    private static final int k2LowerLim = 1;
    private static final int k2UpperLim = 20;
    private static final AttributeType k2Type = AttributeType.ANY;
    private static final boolean reflexive = false;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private SankeyPlan(PrimaryKey k1, PrimaryKey k2,
                       List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, k2, orderedMandAtts, orderedOptionalAtts);
    }

    public static ManyManyGraphPlan getDummyInstance() {
        return new SankeyPlan(null, null, null, null);
    }

    @Override
    public ManyManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                         List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new SankeyPlan(k1, k2, orderedMandAtts, orderedOptionalAtts);
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
    public int getK2LowerLim() {
        return k2LowerLim;
    }

    @Override
    public int getK2UpperLim() {
        return k2UpperLim;
    }

    @Override
    public AttributeType getK2Type() {
        return k2Type;
    }

    @Override
    public boolean isReflexiveRelationship() {
        return reflexive;
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
        String valueId = getOrderedMandatories().get(0).attribute().toString();
        String colourId = null;
        if (!getOrderedOptionals().isEmpty() && getOrderedOptionals().get(0).attribute() != null) {
            colourId = getOrderedOptionals().get(0).attribute().toString();
        }

        return new GoogleSankeyDiagram(dataTable, getK1().toString(), getK2().toString(), valueId, colourId);
    }
}
