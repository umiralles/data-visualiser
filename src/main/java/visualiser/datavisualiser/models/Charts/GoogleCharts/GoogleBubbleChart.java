package visualiser.datavisualiser.models.Charts.GoogleCharts;

import org.json.JSONObject;
import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.List;

public class GoogleBubbleChart extends GoogleChart {

    private final static int CONST_COLOUR = 0;

    // For this constructor, colourId or bubbleSizeId may be null, since they are optional columns
    public GoogleBubbleChart(DataTable dataTable, String labelId, String xAxisId, String yAxisId,
                             String colourId, String bubbleSizeId) {
        super(dataTable);

        if (colourId == null && bubbleSizeId != null) {
            colourId = "bubbleColourId";
            addConstantColumn(colourId, CONST_COLOUR);
            if (!reOrderData(List.of(labelId, xAxisId, yAxisId, colourId, bubbleSizeId))) {
                throw new IllegalArgumentException("GoogleBubbleChart: Ids were not correct (one of " + labelId
                        + ", " + xAxisId + ", " + yAxisId + ", " + bubbleSizeId);
            }
            hideColourAxis();

        } else if (colourId == null) {
            if (!reOrderData(List.of(labelId, xAxisId, yAxisId))) {
                throw new IllegalArgumentException("GoogleBubbleChart: Ids were not correct (one of " + labelId
                        + ", " + xAxisId + ", " + yAxisId);
            }
            hideColourAxis();

        } else if (bubbleSizeId == null) {
            if (!reOrderData(List.of(labelId, xAxisId, yAxisId, colourId))) {
                throw new IllegalArgumentException("GoogleBubbleChart: Ids were not correct (one of " + labelId
                        + ", " + xAxisId + ", " + yAxisId + ", " + colourId);
            }

        } else {
            if (!reOrderData(List.of(labelId, xAxisId, yAxisId, colourId, bubbleSizeId))) {
                throw new IllegalArgumentException("GoogleBubbleChart: Ids were not correct (one of " + labelId
                        + ", " + xAxisId + ", " + yAxisId + ", " + colourId + ", " + bubbleSizeId);
            }
        }

        addTitle("Bubble Chart");
        addHAxis(xAxisId);
        addVAxis(yAxisId);

        // Add text defaults
        addOption("bubble", new JSONObject()
                .put("textStyle", new JSONObject()
                        .put("fontSize", 10)
                        .put("fontName", "Times Roman")));
    }

    public GoogleBubbleChart(DataTable dataTable, String labelId, String xAxisId, String yAxisId) {
        this(dataTable, labelId, xAxisId, yAxisId, null, null);
    }

    public GoogleBubbleChart(DataTable dataTable, String labelId, String xAxisId, String yAxisId, String bubbleSizeId) {
        this(dataTable, labelId, xAxisId, yAxisId, null, bubbleSizeId);
    }

    @Override
    protected String getId() {
        return "BubbleChart";
    }
}
