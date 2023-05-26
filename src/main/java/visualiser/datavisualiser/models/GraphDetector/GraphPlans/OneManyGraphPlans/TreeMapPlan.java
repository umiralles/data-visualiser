package visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans;

import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.List;

public class TreeMapPlan extends OneManyGraphPlan {

    private static final String planName = "Tree Map";
    private static final int k1LowerLim = 1;
    private static final int k1UpperLim = 100;
    private static final AttributeType k1Type = AttributeType.ANY;
    private static final int k2LowerLim = 1;
    private static final int k2UpperLim = 100;
    private static final AttributeType k2Type = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private TreeMapPlan(Attribute k1Att, Attribute k2Att, List<Attribute> orderedAtts) {
        super(k1Att, k2Att, orderedAtts);
    }

    public static OneManyGraphPlan getDummyInstance() {
        return new TreeMapPlan(null, null, null);
    }

    @Override
    public OneManyGraphPlan getInstance(Attribute k1Att, Attribute k2Att, List<Attribute> orderedAtts) {
        return new TreeMapPlan(k1Att, k2Att, orderedAtts);
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
}
