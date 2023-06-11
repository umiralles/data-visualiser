package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.List;

public class GoogleGeoChart extends GoogleChart {

    // Set up for a GeoChart in 'markers' mode
    public GoogleGeoChart(DataTable dataTable, String locationId, String colourId, boolean useMarkers) {
        super(dataTable, List.of(locationId, colourId));

        if (useMarkers) {
            addOption("displayMode", "markers");
        }
//        addOption("region", "FR");
        addColourAxis("violet", "red");
    }

    public GoogleGeoChart(DataTable dataTable, String locationId, boolean useMarkers) {
        super(dataTable, List.of(locationId));
        if (useMarkers) {
            addOption("displayMode", "markers");
        }
    }

    @Override
    protected String getId() {
        return "GeoChart";
    }
}
