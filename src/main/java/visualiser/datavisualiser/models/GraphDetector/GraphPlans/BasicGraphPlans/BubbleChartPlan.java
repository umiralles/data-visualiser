package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GoogleChart.ChartType;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryKey;

import java.util.List;

public class BubbleChartPlan extends BasicGraphPlan {

    private static final String planName = "Bubble Chart";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = -1;
    private static final AttributeType kType = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR, AttributeType.SCALAR, AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private BubbleChartPlan(PrimaryKey k1,
                          List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new BubbleChartPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        return new BubbleChartPlan(k1, orderedMandAtts, orderedOptionalAtts);
    }

    @Override
    public String getPlanName() {return planName;}

    @Override
    public int getKLowerLim() {
        return kLowerLim;
    }

    @Override
    public int getKUpperLim() {
        return kUpperLim;
    }

    @Override
    public AttributeType getKType() {
        return kType;
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
    public ChartType getGoogleChartType() {
        return ChartType.BUBBLE_CHART;
    }
}
