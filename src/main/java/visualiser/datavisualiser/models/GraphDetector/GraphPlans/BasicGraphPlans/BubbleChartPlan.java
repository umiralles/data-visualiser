package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleBubbleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.List;

public class BubbleChartPlan extends BasicGraphPlan {

    private static final String planName = "Bubble Chart";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = -1;
    private static final AttributeType kType = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR, AttributeType.SCALAR, AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private BubbleChartPlan(PrimaryKey k1,
                            List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new BubbleChartPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
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
    public Chart getChart(DataTable unprocessedData) {
        String labelId = getK1().toString();
        String xAxisId = getOrderedMandatoryAtts().get(0).attribute().toString();
        String yAxisId = getOrderedMandatoryAtts().get(1).attribute().toString();
        String bubbleSizeId = getOrderedMandatoryAtts().get(2).attribute().toString();

        String colourId = null;
        if (!getOrderedMandatoryAtts().isEmpty() && getOrderedOptionalAtts().get(0).attribute() != null) {
            colourId = getOrderedOptionalAtts().get(0).attribute().toString();
        }

        return new GoogleBubbleChart(unprocessedData, labelId, xAxisId, yAxisId, colourId, bubbleSizeId);
    }
}
