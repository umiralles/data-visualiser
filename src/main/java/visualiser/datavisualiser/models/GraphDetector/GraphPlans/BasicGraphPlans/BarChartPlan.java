package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleBarChart;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.Collections;
import java.util.List;

public class BarChartPlan extends BasicGraphPlan {

    private static final String planName = "Bar Chart";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = 100;
    private static final AttributeType kType = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR);
    private static final List<AttributeType> optionals = Collections.emptyList();

    private BarChartPlan(PrimaryKey k1,
                         List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new BarChartPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new BarChartPlan(k1, orderedMandAtts, orderedOptionalAtts);
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
    public Chart getChart(DataTable dataTable) {
        String yAtt = getOrderedMandatoryAtts().get(0).attribute().toString();
        return new GoogleBarChart(dataTable, getK1().toString(), yAtt);
    }
}
