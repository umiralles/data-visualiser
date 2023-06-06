package visualiser.datavisualiser.models.DataTable;

import org.json.JSONObject;

public record Column(DataType type, String id, String label, String role, String pattern, JSONObject properties) {

    // As described in 'cols' properties
    //  https://developers.google.com/chart/interactive/docs/reference#dataparam

    public Column(DataType type, String id, String label, String role) {
        this(type, id, label, role, null, null);
    }

    public Column(DataType type, String id, String label) {
        this(type, id, label, null, null, null);
    }

    public Column(DataType type, String id) {
        this(type, id, id, null, null, null);
    }
}
