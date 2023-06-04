package visualiser.datavisualiser.models.GraphDetector.GraphPlans.WeakGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.Collections;
import java.util.List;

public class StackedBarChartPlan extends WeakGraphPlan {

    private static final String planName = "Stacked Bar Chart";
    private static final int k1LowerLim = 1;
    private static final int k1UpperLim = 20;
    private static final AttributeType k1Type = AttributeType.ANY;
    private static final int k2LowerLim = 1;
    private static final int k2UpperLim = 20;
    private static final AttributeType k2Type = AttributeType.ANY;
    private static final boolean complete = true;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR);
    private static final List<AttributeType> optionals = Collections.emptyList();

    private StackedBarChartPlan(PrimaryKey k1, PrimaryKey k2,
                                List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, k2, orderedMandAtts, orderedOptionalAtts);
    }

    public static WeakGraphPlan getDummyInstance() {
        return new StackedBarChartPlan(null, null, null, null);
    }

    @Override
    public WeakGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                     List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new StackedBarChartPlan(k1, k2, orderedMandAtts, orderedOptionalAtts);
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
    public boolean isCompleteRelationship() {
        return complete;
    }

    @Override
    public List<AttributeType> getMandatories() {
        return mandatories;
    }

    @Override
    public List<AttributeType> getOptionals() {
        return optionals;
    }
}
