package visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans;

import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryKey;

import java.util.List;

public class CirclePackingPlan extends OneManyGraphPlan {

    private static final String planName = "Circle Packing";
    private static final int k1LowerLim = 1;
    private static final int k1UpperLim = 100;
    private static final AttributeType k1Type = AttributeType.ANY;
    private static final int k2LowerLim = 1;
    private static final int k2UpperLim = 100;
    private static final AttributeType k2Type = AttributeType.ANY;
    private static final List<AttributeType> mandatories = List.of(AttributeType.SCALAR);
    private static final List<AttributeType> optionals = List.of(AttributeType.COLOUR);

    private CirclePackingPlan(PrimaryKey k1, PrimaryKey k2,
                              List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        super(k1, k2, orderedMandAtts, orderedOptionalAtts);
    }

    public static OneManyGraphPlan getDummyInstance() {
        return new CirclePackingPlan(null, null, null, null);
    }

    @Override
    public OneManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                        List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        return new CirclePackingPlan(k1, k2, orderedMandAtts, orderedOptionalAtts);
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
