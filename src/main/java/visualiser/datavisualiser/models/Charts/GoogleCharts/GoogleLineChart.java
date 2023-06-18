package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleLineChart extends GoogleChart {
    public GoogleLineChart(DataTable dataTable, String xAxis, String yAxisLabel, List<String> yAxes) {
        super(dataTable);

        List<String> newOrder = new ArrayList<>(List.of(xAxis));
        newOrder.addAll(yAxes);
        reOrderData(newOrder);

        addOption("interpolateNulls", true);
        addOption("pointShape", "circle");
        addOption("pointSize", 4);

        // TODO: v axes title?

        addTitle("Line Chart");
    }

    @Override
    protected String getId() {
        return "LineChart";
    }
}
