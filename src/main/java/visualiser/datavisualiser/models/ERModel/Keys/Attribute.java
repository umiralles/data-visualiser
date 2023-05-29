package visualiser.datavisualiser.models.ERModel.Keys;

import visualiser.datavisualiser.models.ERModel.DBType;

import java.util.Objects;

public class Attribute {

    private final String table;
    private final String column;
    private final DBType type;

    public Attribute(String table, String column, DBType type) {
        this.table = table;
        this.column = column;
        this.type = type;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public DBType getDBType() {
        return type;
    }

    // isAttribute is true if the given attribute shares the same name and table as this attribute
    public boolean isAttribute(String table, String column) {
        return table.equals(getTable()) && column.equals(getColumn());
    }

    public boolean isAttribute(Attribute attribute) {
        return table.equals(attribute.getTable()) && column.equals(attribute.getColumn());
    }

    @Override
    public String toString() {
        return getTable() + "." + getColumn();
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, column);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute eq)) {
            return false;
        }

        return table.equals(eq.table) && column.equals(eq.column);
    }
}
