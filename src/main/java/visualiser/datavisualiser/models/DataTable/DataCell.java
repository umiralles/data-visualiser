package visualiser.datavisualiser.models.DataTable;

import org.json.JSONObject;

import java.sql.Timestamp;

public record DataCell(String value, DataType type, String valueFormat, JSONObject properties) implements Comparable<DataCell> {

    // As described in 'rows' properties
    //  https://developers.google.com/chart/interactive/docs/reference#dataparam

    // value:   - when null, the cell is null
    //          - the string representation of the value
    // type:    - the value's Google Charts type

    public DataCell(String value, DataType type) {
        this(value, type, null, new JSONObject());
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public int compareTo(DataCell otherCell) {
        // Not comparable
        if (!type.equals(otherCell.type)) {
            return 0;
        }

        return switch (type) {
            case BOOLEAN -> Boolean.compare(Boolean.parseBoolean(value), Boolean.parseBoolean(otherCell.value));
            case INT, DOUBLE, FLOAT -> Float.compare(Float.parseFloat(value), Float.parseFloat(otherCell.value));
            case STRING -> value.compareTo(otherCell.value);
            case DATE, DATETIME, TIMEOFDAY -> Timestamp.valueOf(value).compareTo(Timestamp.valueOf(otherCell.value));
        };
    }
}
