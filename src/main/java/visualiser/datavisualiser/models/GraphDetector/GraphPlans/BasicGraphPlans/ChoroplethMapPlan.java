package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleGeoChart;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;

import java.util.Collections;
import java.util.List;

public class ChoroplethMapPlan extends BasicGraphPlan {

    private static final String planName = "Choropleth Map";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = -1;
    private static final AttributeType kType = AttributeType.GEOGRAPHICAL;
    private static final List<AttributeType> mandatories = List.of(AttributeType.COLOUR);
    private static final List<AttributeType> optionals = Collections.emptyList();

    private ChoroplethMapPlan(PrimaryKey k1,
                              List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new ChoroplethMapPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts) {
        return new ChoroplethMapPlan(k1, orderedMandAtts, orderedOptionalAtts);
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
        String colourId = getOrderedMandatoryAtts().get(0).attribute().toString();
        boolean useMarkers = !getK1().getTable().contains("country") && !getK1().getTable().contains("continent");

        return new GoogleGeoChart(dataTable, getK1().toString(), colourId, useMarkers);
    }
}
