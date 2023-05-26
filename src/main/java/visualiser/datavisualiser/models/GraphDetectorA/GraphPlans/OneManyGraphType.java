package visualiser.datavisualiser.models.GraphDetectorA.GraphPlans;

import visualiser.datavisualiser.models.RelationalModel.AttributeType;

import java.util.Collections;
import java.util.List;

public enum OneManyGraphType {
    TREE_MAP(1, 100, AttributeType.ANY, 1, 100, AttributeType.ANY,
            List.of(AttributeType.SCALAR), List.of(AttributeType.COLOUR)),
    HIERARCHY_TREE(1, 100, AttributeType.ANY, 1, 100, AttributeType.ANY,
            Collections.emptyList(), List.of(AttributeType.COLOUR)),
    CIRCLE_PACKING(1, 100, AttributeType.ANY, 1, 100, AttributeType.ANY,
            List.of(AttributeType.SCALAR), List.of(AttributeType.COLOUR));

    // If either lowerLim or upperLim are -1, then there is no limit
    private final int k1LowerLim;
    private final int k1UpperLim;
    private final AttributeType k1Type;

    private final int k2PerK1LowerLim;
    private final int k2PerK1UpperLim;
    private final AttributeType k2Type;

    private final List<AttributeType> mandatories;
    private final List<AttributeType> optionals;

    OneManyGraphType(int k1LowerLim, int k1UpperLim, AttributeType k1Type,
                     int k2PerK1LowerLim, int k2PerK1UpperLim, AttributeType k2Type,
                     List<AttributeType> mandatories, List<AttributeType> optionals) {
        this.k1LowerLim = k1LowerLim;
        this.k1UpperLim = k1UpperLim;
        this.k1Type = k1Type;
        this.k2PerK1LowerLim = k2PerK1LowerLim;
        this.k2PerK1UpperLim = k2PerK1UpperLim;
        this.k2Type = k2Type;
        this.mandatories = mandatories;
        this.optionals = optionals;
    }

    public int getK1LowerLim() {
        return k1LowerLim;
    }

    public int getK1UpperLim() {
        return k1UpperLim;
    }

    public AttributeType getK1Type() {
        return k1Type;
    }

    public int getK2PerK1LowerLim() {
        return k2PerK1LowerLim;
    }

    public int getK2PerK1UpperLim() {
        return k2PerK1UpperLim;
    }

    public AttributeType getK2Type() {
        return k2Type;
    }

    public List<AttributeType> getMandatories() {
        return mandatories;
    }

    public List<AttributeType> getOptionals() {
        return optionals;
    }
}
