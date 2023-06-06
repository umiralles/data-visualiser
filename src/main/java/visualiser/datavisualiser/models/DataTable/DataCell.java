package visualiser.datavisualiser.models.DataTable;

import org.json.JSONObject;

public class DataCell {

    // As described in 'rows' properties
    //  https://developers.google.com/chart/interactive/docs/reference#dataparam

    // value:   - when null, the cell is null
    //          - the string representation of the value
    // type:    - the value's Google Charts type
    private final String value;

    private final DataType type;
    private final String valueFormat;
    private final JSONObject properties;

    public DataCell(String value, DataType type, String valueFormat, JSONObject properties) {
        this.value = value;
        this.type = type;
        this.valueFormat = valueFormat;
        this.properties = properties;
    }

    public DataCell(String value, DataType type, String valueFormat) {
        this(value, type, valueFormat, null);
    }

    public DataCell(String value, DataType type) {
        this(value, type, null, null);
    }

    public String getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public JSONObject getProperties() {
        return properties;
    }
}
