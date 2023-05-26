package visualiser.datavisualiser.models.GraphDetectorA.GraphPlans;

import visualiser.datavisualiser.models.RelationalModel.AttributeType;

import java.util.List;

public enum BasicGraphType {
    // TODO: 'note that all of the visualisations may have additional TEMPORAL SCALARS (time sliders) or
    //  DISCRETE SCALARS (paging options)
    // > there is only ever one optional, but multiple optionals are supported
    BARCHART(1, 100, AttributeType.ANY, List.of(AttributeType.SCALAR)),
    CALENDAR(1, -1, AttributeType.ANY, List.of(AttributeType.TEMPORAL), List.of(AttributeType.COLOUR)),
    SCATTER_DIAGRAM(1, -1, AttributeType.ANY, List.of(AttributeType.SCALAR, AttributeType.SCALAR), List.of(AttributeType.COLOUR)),
    BUBBLE_CHART(1, -1, AttributeType.ANY, List.of(AttributeType.SCALAR, AttributeType.SCALAR, AttributeType.SCALAR), List.of(AttributeType.COLOUR)),
    CHOROPLETH_MAP(1, -1, AttributeType.GEOGRAPHICAL, List.of(AttributeType.COLOUR)),
    WORD_CLOUD(1, -1, AttributeType.LEXICAL, List.of(AttributeType.SCALAR), List.of(AttributeType.COLOUR));

    // TODO: check for limit (probs not here tho)
    // If either lowerLim or upperLim are -1, then there is no limit
    private final int kLowerLim;
    private final int kUpperLim;
    private final AttributeType kType;
    private final List<AttributeType> mandatories;
    private final List<AttributeType> optionals;

    BasicGraphType(int kLowerLim, int kUpperLim, AttributeType kType, List<AttributeType> mandatories, List<AttributeType> optionals) {
        this.kLowerLim = kLowerLim;
        this.kUpperLim = kUpperLim;
        this.kType = kType;
        this.mandatories = mandatories;
        this.optionals = optionals;
    }

    BasicGraphType(int kLowerLim, int kUpperLim, AttributeType kType, List<AttributeType> mandatories) {
        this(kLowerLim, kUpperLim, kType, mandatories, List.of());
    }

    public int getkLowerLim() {
        return kLowerLim;
    }

    public int getkUpperLim() {
        return kUpperLim;
    }

    public AttributeType getkType() {
        return kType;
    }

    public List<AttributeType> getMandatories() {
        return mandatories;
    }

    public List<AttributeType> getOptionals() {
        return optionals;
    }
}
