package visualiser.datavisualiser.models.Charts;

import javafx.concurrent.Worker;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;
import visualiser.datavisualiser.models.DataTable.Column;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.DataTable.DataType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class GoogleChart implements Chart {
    private final static String DEFAULT_DIV = "google_chart_div";

    private final String divName;

    private DataTable dataTable;
    private JSONObject options;
    private JSONObject chartJson = null;

    protected GoogleChart(String divName, DataTable dataTable, JSONObject options) {
        this.divName = Objects.requireNonNull(divName);
        this.dataTable = Objects.requireNonNull(dataTable);
        this.options = Objects.requireNonNull(options);
    }

    protected GoogleChart(String divName, DataTable dataTable, List<String> newDataTableOrderIds, JSONObject options) {
        this(divName, dataTable.reOrderColumns(newDataTableOrderIds), options);
    }

    protected GoogleChart(String divName, DataTable dataTable, List<String> newDataTableOrderIds) {
        this(divName, dataTable, newDataTableOrderIds, new JSONObject());
    }

    protected GoogleChart(DataTable dataTable, List<String> newDataTableOrderIds) {
        this(DEFAULT_DIV, dataTable, newDataTableOrderIds);
    }

    protected GoogleChart(DataTable dataTable) {
        this(DEFAULT_DIV, dataTable, new JSONObject());
    }

    protected abstract String getId();

    public JSONObject getJson() {
        if (chartJson != null) {
            return chartJson;
        }

        JSONObject data = new JSONObject();

        data.put("div_name", divName)
                .put("data_table", generateTableJSON(dataTable))
                .put("options", options)
                .put("chart_type", getId());

        this.chartJson = data;
        return data;
    }

    private JSONObject generateTableJSON(DataTable dataTable) {
        List<Column> columns = dataTable.getColumns();
        List<List<DataCell>> rows = dataTable.getRows();

        JSONArray columnsJson = new JSONArray();

        for (Column column : columns) {
            columnsJson.put(generateColumnJSON(column));
        }

        JSONArray rowsJson = new JSONArray();

        for (List<DataCell> row : rows) {

            JSONArray rowJson = new JSONArray();
            boolean nullRow = false;
            for (DataCell cell : row) {
                JSONObject cellJson = generateCellJSON(cell);

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

    private JSONObject generateColumnJSON(Column column) {
        DataType type = column.getType();
        String id = column.getId();
        String label = column.getLabel();
        String pattern = column.getPattern();
        JSONObject properties = column.getProperties();

        JSONObject columnJson = new JSONObject().put("type", GoogleDataType.getNameFromDataType(type));

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

    private JSONObject generateCellJSON(DataCell cell) {
        DataType type = cell.getType();
        String value = cell.getValue();
        String valueFormat = cell.getValueFormat();
        JSONObject properties = cell.getProperties();

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

    @Override
    public void setSize(int width, int height) {
        options.put("width", width);
        options.put("height", height);
    }

    @Override
    public void showChart(WebView webView) {
        WebEngine engine = webView.getEngine();
        engine.load(getClass().getResource("google_chart.html").toExternalForm());

        // Inject the JSON data into the WebView after the page finishes loading
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                String jsonString = getJson().toString().replace("'", "\\'");

                engine.executeScript("var globalJson = " + jsonString + ";");
                engine.executeScript("drawChart('" + jsonString + "');");
            }
        });
    }

    protected boolean reOrderData(List<String> newIdsOrder) {
        DataTable newDataTable = dataTable.reOrderColumns(newIdsOrder);

        if (newDataTable != null) {
            this.dataTable = dataTable.reOrderColumns(newIdsOrder);
            return true;
        }

        return false;
    }

    // Adds a new column where every value is the value of 'constant' and the column id is 'newColId'
    protected void addConstantColumn(String newColId, int constant) {
        List<Column> newCols = new ArrayList<>(dataTable.getColumns());
        List<List<DataCell>> newRows = new ArrayList<>();
        dataTable.getRows().forEach(row -> newRows.add(new ArrayList<>(row)));

        newCols.add(new Column(DataType.INT, newColId));

        String value = String.valueOf(constant);
        for (List<DataCell> row : newRows) {
            row.add(new DataCell(value, DataType.INT));
        }

        this.dataTable = new DataTable(newCols, newRows);
    }

    protected void addOption(String key, Object option) {
        options.put(key, option);
    }

    protected void addTitle(String title) {
        options.put("title", title);
    }

    protected void addHAxis(String title) {
        options.put("hAxis", new JSONObject().put("title", title));
    }

    protected void addVAxis(String title) {
        options.put("vAxis", new JSONObject().put("title", title));
    }

    protected void addColourAxis(String startColour, String endColour) {
        options.put("colorAxis",
                new JSONObject().put("colors", new String[]{startColour, endColour}));
    }

    protected void addColourAxis() {
        addColourAxis("yellow", "red");
    }

    protected void convertToHexColour(Attribute attribute, Color startColour, Color endColour) {
        List<String> hexColours = dataTable.convertToHexColour(attribute.toString(), startColour, endColour);
        options.put("colors", hexColours.toArray(new String[0]));
    }

    protected void convertToHexColour(Attribute attribute) {
        convertToHexColour(attribute, Color.YELLOW, Color.RED);
    }

    protected void addSizeAxis(int maxSize, int minSize) {
        options.put("sizeAxis", new JSONObject()
                .put("maxSize", maxSize)
                .put("minSize", minSize));
    }
}
