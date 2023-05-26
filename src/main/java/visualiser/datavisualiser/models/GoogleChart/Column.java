package visualiser.datavisualiser.models.GoogleChart;

import org.json.JSONObject;

public class Column {

    // As described in 'cols' properties
    //  https://developers.google.com/chart/interactive/docs/reference#dataparam
    private final DataType type;
    private final String id;
    private final String label;
    private final String pattern;
    private final JSONObject properties;

    public Column(DataType type, String id, String label, String pattern, JSONObject properties) {
        this.type = type;
        this.id = id;
        this.label = label;
        this.pattern = pattern;
        this.properties = properties;
    }

    public Column(DataType type, String id, String label) {
        this(type, id, label, null, null);
    }

    public Column(DataType type, String label) {
        this(type, null, label, null, null);
    }

    public Column(DataType type) {
        this(type, null, null, null, null);
    }

    public DataType getType() {
        return type;
    }

    public String getLabel() { return label; }

    public JSONObject generateJSON() {

        JSONObject columnJson = new JSONObject().put("type", type.getName());

        if (id != null && !id.isBlank()) {
            columnJson.put("id", id);
        }

        if (label != null && !label.isBlank()) {
            columnJson.put("label", label);
        }

        if (pattern != null && !pattern.isBlank()) {
            columnJson.put("pattern", pattern);
        }

        if (properties != null && properties.length() > 0) {
            columnJson.put("p", properties);
        }

        return columnJson;
    }
}
