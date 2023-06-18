package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleBarChart extends GoogleChart {
    public GoogleBarChart(DataTable unorderedData, String xAxisId, List<String> yAxisIds) {
        super(unorderedData);

        List<String> newOrder = new ArrayList<>(List.of(xAxisId));
        newOrder.addAll(yAxisIds);
        reOrderData(newOrder);
        addOption("isStacked", true);

        addVAxis(xAxisId);

        addTitle("Bar Chart");
    }

    public GoogleBarChart(DataTable unorderedData, String xAxisId, String yAxisId) {
        super(unorderedData, List.of(xAxisId, yAxisId));

        addHAxis(yAxisId);
        addVAxis(xAxisId);

        addTitle("Bar Chart");
    }

    // Used directly for google_chart.html to determine the correct function to use when generating graph
    @Override
    protected String getId() {
        return "BarChart";
    }
}
