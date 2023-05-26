package visualiser.datavisualiser.models.GraphDetectorA;

import java.util.Objects;

public record InputAttribute(String table, String column) {
    public InputAttribute {
        Objects.requireNonNull(table);
        Objects.requireNonNull(column);
    }
}
