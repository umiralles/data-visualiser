package visualiser.datavisualiser.models.GoogleChart;

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

    public JSONObject generateJSON() {
        if (value == null || value.isBlank() || value.equals("null")) {
            return null;
        }

        JSONObject cellJson = new JSONObject();

        switch (type) {
            case BOOLEAN -> cellJson.put("v", Boolean.valueOf(value));
            case INT -> cellJson.put("v", Integer.valueOf(value));
            case DOUBLE -> cellJson.put("v", Double.valueOf(value));
            case FLOAT -> cellJson.put("v", Float.valueOf(value));
            case STRING, DATE, DATETIME, TIMEOFDAY -> cellJson.put("v", value);
        }

        if (valueFormat != null && !valueFormat.isBlank()) {
            cellJson.put("f", valueFormat);
        }

        if (properties != null && properties.length() > 0) {
            cellJson.put("p", properties);
        }

        return cellJson;
    }
}
