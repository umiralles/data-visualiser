package visualiser.datavisualiser.models.Charts.GoogleCharts;

import org.json.JSONObject;
import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.List;

public class GoogleScatterChart extends GoogleChart {

    private final static int BUBBLE_SIZE = 5;

    public GoogleScatterChart(DataTable dataTable, String labelId, String yAxisId, String xAxisId, String colourId) {
        super(dataTable);

        if (colourId != null) {
            reOrderData(List.of(labelId, yAxisId, xAxisId, colourId));
            addColourAxis();
        } else {
            reOrderData(List.of(labelId, yAxisId, xAxisId));
            hideColourAxis();
        }

        addTitle("Scatter Chart");
        addHAxis(yAxisId);
        addVAxis(xAxisId);

        // Add text defaults
        addOption("bubble", new JSONObject()
                .put("textStyle", new JSONObject()
                        .put("fontSize", 10)
                        .put("fontName", "Times Roman")));

        addSizeAxis(5, 5);
    }

    public GoogleScatterChart(DataTable dataTable, String labelId, String yAxisId, String xAxisId) {
        this(dataTable, labelId, yAxisId, xAxisId, null);
    }

    @Override
    protected String getId() {
        return "BubbleChart";
    }
}
