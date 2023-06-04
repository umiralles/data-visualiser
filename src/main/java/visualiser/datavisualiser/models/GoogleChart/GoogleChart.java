package visualiser.datavisualiser.models.GoogleChart;

import javafx.scene.paint.Color;
import org.json.JSONObject;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GoogleChart {

    private final String divName;
    private final DataTable dataTable;

    private JSONObject options;
    private ChartType type;

    private JSONObject chartJson = null;

    public GoogleChart(String divName, DataTable dataTable, JSONObject options, ChartType type) {
        this.divName = Objects.requireNonNull(divName);
        this.dataTable = Objects.requireNonNull(dataTable);
        this.options = Objects.requireNonNull(options);
        this.type = Objects.requireNonNull(type);
    }

    public GoogleChart(String divName, List<Column> columns, List<List<DataCell>> rows,
                       JSONObject options, ChartType type) {
        this(divName, new DataTable(columns, rows), options, type);
    }

    public GoogleChart(String divName, DataTable dataTable, HashMap<String, String> options, ChartType type) {
        this(divName, dataTable, new JSONObject(options), type);
    }

    public GoogleChart(String divName, DataTable dataTable, ChartType type) {
        this(divName, dataTable, new JSONObject(), type);
    }

    public GoogleChart(String divName, List<Column> columns, List<List<DataCell>> rows,
                       HashMap<String, String> options, ChartType type) {
        this(divName, new DataTable(columns, rows), new JSONObject(options), type);
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public void setOptions(String title, int width, int height) {
        JSONObject newOptions = Objects.requireNonNullElseGet(options, JSONObject::new);

        newOptions.put("title", title)
                .put("width", width)
                .put("height", height);

        setOptions(newOptions);
    }

    public void addColourAxis(String startColour, String endColour) {
        options.put("colorAxis",
                new JSONObject().put("colors", new String[]{startColour, endColour}));
    }

    public void addColourAxis() {
        addColourAxis("yellow", "red");
    }

    public void setType(ChartType type) {
        this.type = type;
    }

    public JSONObject getJson() {
        if (chartJson != null) {
            return chartJson;
        }

        JSONObject data = new JSONObject();

        data.put("div_name", divName)
                .put("data_table", dataTable.generateJSON())
                .put("options", options)
                .put("chart_type", type.getName());

        this.chartJson = data;
        return data;
    }

    public void writeJson(String fileName, JSONObject json) {
        try {
            // Creates a FileWriter
            FileWriter file = new FileWriter(fileName);

            // Creates a BufferedWriter
            BufferedWriter output = new BufferedWriter(file);

            // Writes the string to the file
            output.write(json.toString());

            // Closes the writer
            output.close();
        } catch (IOException e) {
            System.out.println("Failed to write chart.");
            e.getStackTrace();
        }
    }

    public void writeJson(String fileName) {
        writeJson(fileName, getJson());
    }

    public void convertToHexColour(Attribute attribute, Color startColour, Color endColour) {
        List<String> hexColours = dataTable.convertToHexColour(attribute, startColour, endColour);
        options.put("colors", hexColours.toArray(new String[0]));
    }

    public void convertToHexColour(Attribute attribute) {
        convertToHexColour(attribute, Color.YELLOW, Color.RED);
    }
}
