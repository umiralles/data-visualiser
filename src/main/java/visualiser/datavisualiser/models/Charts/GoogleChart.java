package visualiser.datavisualiser.models.Charts;

import javafx.beans.value.ChangeListener;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this(divName, DataTable.getWithReOrderedColumns(dataTable, newDataTableOrderIds), options);
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
//        if (chartJson != null) {
//            return chartJson;
//        }

        JSONObject data = new JSONObject();

        data.put("div_name", divName)
                .put("data_table", generateTableJSON(dataTable))
                .put("options", options)
                .put("chart_type", getId());

        this.chartJson = data;
        return data;
    }

    private JSONObject generateTableJSON(DataTable dataTable) {
        List<Column> columns = dataTable.columns();
        List<List<DataCell>> rows = dataTable.rows();

        JSONArray columnsJson = new JSONArray();

        for (Column column : columns) {
            columnsJson.put(generateColumnJSON(column));
        }

        JSONArray rowsJson = new JSONArray();

        for (List<DataCell> row : rows) {

            JSONArray rowJson = new JSONArray();
            for (DataCell cell : row) {
                JSONObject cellJson = generateCellJSON(cell);

                rowJson.put(cellJson);
            }

            rowsJson.put(new JSONObject().put("c", rowJson));
        }

        return new JSONObject()
                .put("cols", columnsJson)
                .put("rows", rowsJson);
    }

    private JSONObject generateColumnJSON(Column column) {
        DataType type = column.type();
        String id = column.id();
        String label = column.label();
        String role = column.role();
        String pattern = column.pattern();
        JSONObject properties = column.properties();

        JSONObject columnJson = new JSONObject().put("type", GoogleDataType.getNameFromDataType(type));

        if (id != null && !id.isBlank()) {
            columnJson.put("id", id);
        }

        if (label != null && !label.isBlank()) {
            columnJson.put("label", label);
        }

        if (role != null && !role.isBlank()) {
            columnJson.put("role", role);
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
        DataType type = cell.type();
        String value = cell.value();
        String valueFormat = cell.valueFormat();
        JSONObject properties = cell.properties();

        if (value == null) {
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
    public ChangeListener<? super Worker.State> showChart(WebView webView, ChangeListener<? super Worker.State> oldListener) {
        WebEngine engine = webView.getEngine();
        engine.load(getClass().getResource("google_chart.html").toExternalForm());

        // Inject the JSON data into the WebView after the page finishes loading
        if (oldListener != null) {
            engine.getLoadWorker().stateProperty().removeListener(oldListener);
        }

        ChangeListener<? super Worker.State> newListener = (observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                String jsonString = getJson().toString().replace("'", "\\'");

                engine.executeScript("var globalJson = " + jsonString + ";");
                engine.executeScript("drawChart('" + jsonString + "');");
            }
        };

        engine.getLoadWorker().stateProperty().addListener(newListener);
        return newListener;
    }

    protected boolean reOrderData(List<String> newIdsOrder) {
        DataTable newDataTable = DataTable.getWithReOrderedColumns(dataTable, newIdsOrder);

        if (newDataTable != null) {
            this.dataTable = DataTable.getWithReOrderedColumns(dataTable, newIdsOrder);
            return true;
        }

        return false;
    }

    protected void addColumn(Column newCol, List<DataCell> newColVals) {
        this.dataTable = DataTable.getWithNewColumn(dataTable, newCol, newColVals);
    }

    // Adds a new column where every value is the value of 'constant' and the column id is 'newColId'
    protected void addConstantColumn(String newColId, int constant) {
        Column newCol = new Column(DataType.INT, newColId);
        String newVal = String.valueOf(constant);
        List<DataCell> newColVals =
                Stream.generate(() -> new DataCell(newVal, DataType.INT))
                        .limit(dataTable.rows().size()).toList();

        this.dataTable = DataTable.getWithNewColumn(dataTable, newCol, newColVals);
    }

    protected void addRows(List<List<DataCell>> newRows) {
        dataTable.rows().addAll(newRows);
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

    protected void hideColourAxis() {
        options.put("colorAxis",
                new JSONObject().put("legend",
                        new JSONObject().put("position", "none")));
    }

    // DOES NOT REMOVE THE CHOSEN COLUMN FROM THE DATATABLE
    protected void addColorsOption(String colourId, Color startColour, Color endColour) {
        List<String> hexColours = dataTable.getHexColoursFromId(colourId, startColour, endColour);
        if (hexColours == null) {
            // TODO: error
            System.out.println("Tried to add colours option to " + colourId);
            return;
        }

        options.put("colors", hexColours.toArray(new String[0]));
    }

    protected void addColorsOption(String colourId) {
        addColorsOption(colourId, Color.YELLOW, Color.RED);
    }

    protected void convertToColourStyleColumn(String colourId, Color startColour, Color endColour) {
        List<String> hexColours = dataTable.getHexColoursFromId(colourId, startColour, endColour);
        if (hexColours == null) {
            // TODO: error
            System.out.println("Tried to convert " + colourId + " to colour style column");
            return;
        }

        List<String> currOrder = dataTable.columns().stream().map(Column::id).collect(Collectors.toCollection(ArrayList::new));
        currOrder.remove(colourId);
        DataTable reOrdered = DataTable.getWithReOrderedColumns(dataTable, currOrder);
        if (reOrdered == null) {
            // TODO: error
            System.out.println("Tried to reorder while converting " + colourId + " to colour style column");
            return;
        }

        List<DataCell> styleColumn = hexColours.stream().map(hex -> new DataCell("color: " + hex, DataType.STRING)).toList();

        this.dataTable = DataTable.getWithNewColumn(reOrdered, generateStyleColumn("style"), styleColumn);
    }

    protected void convertToColourStyleColumn(String colourId) {
        convertToColourStyleColumn(colourId, Color.YELLOW, Color.RED);
    }

    protected void convertToColourCellProperties(String colourId) {
        convertToColourCellProperties(colourId, Color.YELLOW, Color.RED);
    }

    protected void convertToColourCellProperties(String colourId, Color startColour, Color endColour) {
        List<String> hexColours = dataTable.getHexColoursFromId(colourId, startColour, endColour);
        for (int i = 0; i < dataTable.rows().size(); i++) {
            List<DataCell> row = dataTable.rows().get(i);
            String hexColour = hexColours.get(i);
            for (DataCell cell : row) {
                cell.setProperty("style", "background-color:" + hexColour + " !important;");
//                cell.setProperty("style", "border: 1px solid green;");
            }
        }
    }

//    protected void convertToAnnotationColumn(String annotationId) {
//        Optional<Column> annoColOpt = dataTable.columns().stream().dropWhile(col -> !col.id().equals(annotationId)).findFirst();
//        if (annoColOpt.isEmpty()) {
//            return;
//        }
//
//        Column oldCol = annoColOpt.get();
//        this.dataTable = DataTable.getWithReplacedColumnData(dataTable, annotationId,
//                new Column(oldCol.type(), oldCol.id(), oldCol.label(), "annotation", oldCol.pattern(), oldCol.properties()));
//    }

    protected void addSizeAxis(int maxSize, int minSize) {
        options.put("sizeAxis", new JSONObject()
                .put("maxSize", maxSize)
                .put("minSize", minSize));
    }

    protected static Column generateStyleColumn(String id) {
        return new Column(DataType.STRING, id, id, "style");
    }

    protected void removeDuplicatesInColumn(int idx) {
        // Remove duplicates for the indexed column
        Set<String> seenVals = new HashSet<>();
        List<Integer> idxsToRemove = new ArrayList<>();
        for (int i = dataTable.rows().size() -1; i >= 0; i--) {
            String rowVal = dataTable.rows().get(i).get(idx).value();
            if (seenVals.contains(rowVal)) {
                idxsToRemove.add(i);
                continue;
            }

            seenVals.add(rowVal);
        }

        for (Integer toRemove : idxsToRemove) {
            dataTable.rows().remove((int) toRemove);
        }
    }
}
