package visualiser.datavisualiser.models.GraphDetector.GraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;

import java.util.Objects;

public record GraphAttribute(Attribute attribute, AttributeType typeInGraph, boolean optional) {
    // attribute:    The attribute for the graph
    // typeInGraph:  The chosen type as a part of the graph
    // optional:     True if the type is optional for the graph
    public GraphAttribute {
        // Optional GraphAttributes can be null
        if (!optional) {
            Objects.requireNonNull(attribute);
        }
        Objects.requireNonNull(typeInGraph);
    }

    public boolean isAttribute(Attribute att) {
        return attribute.equals(att);
    }
}
