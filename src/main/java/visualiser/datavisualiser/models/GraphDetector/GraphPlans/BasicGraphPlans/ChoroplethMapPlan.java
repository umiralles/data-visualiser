package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GoogleChart.ChartType;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryKey;

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
                          List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        super(k1, orderedMandAtts, orderedOptionalAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new ChoroplethMapPlan(null, null, null);
    }

    @Override
    public BasicGraphPlan getInstance(PrimaryKey k1,
                                      List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
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
    public ChartType getGoogleChartType() {
        return ChartType.CHOROPLETH_MAP;
    }
}
