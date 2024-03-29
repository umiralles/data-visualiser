package visualiser.datavisualiser.models.Charts.GoogleCharts;

import org.json.JSONObject;
import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleMaterialColumnChart extends GoogleChart {
    public GoogleMaterialColumnChart(DataTable unorderedData, String xAxisId, List<String> yAxisIds) {
        super(unorderedData);

        List<String> newOrder = new ArrayList<>(List.of(xAxisId));
        newOrder.addAll(yAxisIds);
        reOrderData(newOrder);

        addOption("chart", new JSONObject().put("title", "Column Chart"));
    }

    // Used directly for google_chart.html to determine the correct function to use when generating graph
    @Override
    protected String getId() {
        return "Bar";
    }
}
