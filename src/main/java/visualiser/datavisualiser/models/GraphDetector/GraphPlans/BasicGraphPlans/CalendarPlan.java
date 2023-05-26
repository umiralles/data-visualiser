package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GoogleChart.ChartType;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.List;

public class CalendarPlan extends BasicGraphPlan {

    private static final String planName = "Calendar";
    private static final int kLowerLim = 1;
    private static final int kUpperLim = -1;
    private static final AttributeType kType = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.TEMPORAL);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private CalendarPlan(Attribute kAtt, List<Attribute> orderedAtts) {
        super(kAtt, orderedAtts);
    }

    public static BasicGraphPlan getDummyInstance() {
        return new CalendarPlan(null, null);
    }

    @Override
    public BasicGraphPlan getInstance(Attribute kAtt, List<Attribute> orderedAtts) {
        return new CalendarPlan(kAtt, orderedAtts);
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
        return ChartType.CALENDAR;
    }
}
