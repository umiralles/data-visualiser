package visualiser.datavisualiser.models.GraphDetector;

import visualiser.datavisualiser.models.ERModel.Keys.Attribute;

import java.util.Objects;

public record InputAttribute(String table, String column) {
    public InputAttribute {
        Objects.requireNonNull(table);
        Objects.requireNonNull(column);
    }

    public InputAttribute(Attribute a) {
        this(a.getTable(), a.getColumn());
    }
}
