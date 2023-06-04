package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GoogleChart.ChartType;
import visualiser.datavisualiser.models.GoogleChart.DataTable;
import visualiser.datavisualiser.models.GoogleChart.GoogleChart;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.List;
import java.util.Optional;

public class ScatterDiagramPlan extends BasicGraphPlan {

    private static final String planName = "Scatter Diagram";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = -1;
    private static final AttributeType kType = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR, AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private ScatterDiagramPlan(PrimaryKey k1,
                               List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new ScatterDiagramPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new ScatterDiagramPlan(k1, orderedMandAtts, orderedOptionalAtts);
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
    public GoogleChart getChart(DataTable unprocessedData, int width, int height) {
        GoogleChart chart = super.getChart(unprocessedData, width, height);

        // Are colours needed?getChart
        Optional<GraphAttribute> optColourAtt = getOrderedOptionals().stream()
                .dropWhile(opt -> opt.typeInGraph() != AttributeType.COLOUR || opt.attribute() == null).findFirst();
        optColourAtt.ifPresent(graphAttribute -> chart.convertToHexColour(graphAttribute.attribute()));

        return chart;
    }

    @Override
    public ChartType getGoogleChartType() {
        return ChartType.SCATTER_DIAGRAM;
    }
}
