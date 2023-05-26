package visualiser.datavisualiser.models.GraphDetectorA.GraphPlans;

import visualiser.datavisualiser.models.RelationalModel.AttributeType;

import java.util.ArrayList;
import java.util.List;

public enum WeakGraphType {
    LINE_CHART(1, 20, AttributeType.ANY, 1, -1, AttributeType.SCALAR,
            false, List.of(AttributeType.SCALAR), List.of(AttributeType.SCALAR)),
    STACKED_BAR_CHART(1, 20, AttributeType.ANY, 1, 20, AttributeType.ANY,
            true, List.of(AttributeType.SCALAR)),
    GROUPED_BAR_CHART(1, 20, AttributeType.ANY, 1, 20, AttributeType.ANY,
            false, List.of(AttributeType.SCALAR)),
    SPIDER_CHART(3, 10, AttributeType.ANY, 1, 20, AttributeType.ANY,
            true, List.of(AttributeType.SCALAR));

    // If either lowerLim or upperLim are -1, then there is no limit
    private final int k1LowerLim;
    private final int k1UpperLim;
    private final AttributeType k1Type;

    private final int k2LowerLim;
    private final int k2UpperLim;
    private final AttributeType k2Type;

    private final boolean complete;

    private final List<AttributeType> mandatories;
    private final List<AttributeType> optionals;

    WeakGraphType(int k1LowerLim, int k1UpperLim, AttributeType k1Type,
                  int k2LowerLim, int k2UpperLim, AttributeType k2Type,
                  boolean complete, List<AttributeType> mandatories, List<AttributeType> optionals) {
        this.k1LowerLim = k1LowerLim;
        this.k1UpperLim = k1UpperLim;
        this.k1Type = k1Type;
        this.k2LowerLim = k2LowerLim;
        this.k2UpperLim = k2UpperLim;
        this.k2Type = k2Type;
        this.complete = complete;
        this.mandatories = mandatories;
        this.optionals = optionals;
    }

    WeakGraphType(int k1LowerLim, int k1UpperLim, AttributeType k1Type,
                  int k2LowerLim, int k2UpperLim, AttributeType k2Type,
                  boolean complete, List<AttributeType> mandatories) {
        this(k1LowerLim, k1UpperLim, k1Type, k2LowerLim, k2UpperLim, k2Type, complete, mandatories, new ArrayList<>());
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

    public int getK2LowerLim() {
        return k2LowerLim;
    }

    public int getK2UpperLim() {
        return k2UpperLim;
    }

    public AttributeType getK2Type() {
        return k2Type;
    }

    public boolean isComplete() {
        return complete;
    }

    public List<AttributeType> getMandatories() {
        return mandatories;
    }

    public List<AttributeType> getOptionals() {
        return optionals;
    }
}
