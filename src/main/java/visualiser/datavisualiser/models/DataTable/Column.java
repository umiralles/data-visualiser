package visualiser.datavisualiser.models.DataTable;

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

    public Column(DataType type, String id) {
        this(type, id, id, null, null);
    }

    public DataType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() { return label; }

    public String getPattern() {
        return pattern;
    }

    public JSONObject getProperties() {
        return properties;
    }
}
