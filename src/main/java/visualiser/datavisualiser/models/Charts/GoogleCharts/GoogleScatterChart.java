package visualiser.datavisualiser.models.Charts.GoogleCharts;

import org.json.JSONObject;
import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.List;

public class GoogleScatterChart extends GoogleChart {

    private final static int BUBBLE_SIZE = 5;

    public GoogleScatterChart(DataTable dataTable, String labelId, String xAxisId, String yAxisId, String colourId) {
        super(dataTable);

        if (colourId != null) {
            String bubbleSizeId = "bubbleSize";
            addConstantColumn(bubbleSizeId, BUBBLE_SIZE);
            reOrderData(List.of(labelId, xAxisId, yAxisId, colourId, bubbleSizeId));
            addColourAxis();
        } else {
            reOrderData(List.of(labelId, xAxisId, yAxisId));
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

    public GoogleScatterChart(DataTable dataTable, String labelId, String xAxisId, String yAxisId) {
        this(dataTable, labelId, xAxisId, yAxisId, null);
    }

    @Override
    protected String getId() {
        return "BubbleChart";
    }
}
