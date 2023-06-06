package visualiser.datavisualiser.models.DataTable;

import org.json.JSONObject;

public record DataCell(String value, DataType type, String valueFormat, JSONObject properties) {

    // As described in 'rows' properties
    //  https://developers.google.com/chart/interactive/docs/reference#dataparam

    // value:   - when null, the cell is null
    //          - the string representation of the value
    // type:    - the value's Google Charts type

    public DataCell(String value, DataType type) {
        this(value, type, null, null);
    }
}
