package visualiser.datavisualiser.models.GoogleChart;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class DataTable {

    private final List<Column> columns;
    private final List<List<DataCell>> rows;

    public DataTable(List<Column> columns, List<List<DataCell>> rows) throws IllegalArgumentException {

        // Check each row has the correct length and type
        // TODO: needs error message
        for (List<DataCell> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < row.size(); i++) {
                DataCell rowCell = row.get(i);
                Column column = columns.get(i);

                if (!rowCell.getType().equals(column.getType())) {
                    throw new IllegalArgumentException();
                }
            }
        }

        this.columns = columns;
        this.rows = rows;
    }

    public JSONObject generateJSON() {

        JSONArray columnsJson = new JSONArray();

        for (Column column : columns) {
            columnsJson.put(column.generateJSON());
        }

        JSONArray rowsJson = new JSONArray();

        for (List<DataCell> row : rows) {

            JSONArray rowJson = new JSONArray();
            boolean nullRow = false;
            for (DataCell cell : row) {
                JSONObject cellJson = cell.generateJSON();

                if (cellJson == null) {
                    nullRow = true;
                    break;
                }

                rowJson.put(cellJson);
            }

            if (!nullRow) {
                rowsJson.put(new JSONObject().put("c", rowJson));
            }
        }

        return new JSONObject()
                .put("cols", columnsJson)
                .put("rows", rowsJson);
    }

    public DataTable reOrderColumns(List<String> newOrder) {
        if (newOrder.size() != columns.size()) {
            // TODO: error
            return null;
        }

        // TODO: maybe use hashmaps for columns
        List<Column> newColumns = new ArrayList<>();
        List<List<DataCell>> newRows = new ArrayList<>();
        rows.forEach(row -> newRows.add(new ArrayList<>()));

        for (String columnName : newOrder) {
            OptionalInt opIdx = IntStream.range(0, columns.size())
                    .filter(i -> columns.get(i).getLabel().equals(columnName)).findFirst();

            if (opIdx.isEmpty()) {
                // TODO: illegal argument
                return null;
            }

            newColumns.add(columns.get(opIdx.getAsInt()));
            IntStream.range(0, newRows.size()).forEach(rowIdx -> newRows.get(rowIdx).add(rows.get(rowIdx).get(opIdx.getAsInt())));
        }

        return new DataTable(newColumns, newRows);
    }
}
