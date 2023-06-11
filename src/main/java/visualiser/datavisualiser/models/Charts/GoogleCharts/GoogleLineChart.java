package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleLineChart extends GoogleChart {
    public GoogleLineChart(DataTable dataTable, String xAxis, List<String> yAxes) {
        super(dataTable);

        List<String> newOrder = new ArrayList<>(List.of(xAxis));
        newOrder.addAll(yAxes);
        reOrderData(newOrder);

        addTitle("Line Chart");
    }

    @Override
    protected String getId() {
        return "LineChart";
    }
}
