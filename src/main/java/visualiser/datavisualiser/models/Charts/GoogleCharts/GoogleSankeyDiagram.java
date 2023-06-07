package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.List;

public class GoogleSankeyDiagram extends GoogleChart {

    public GoogleSankeyDiagram(DataTable dataTable, String sourceId, String destinationId,
                               String valueId, String colourId) {
        super(dataTable);

        if (colourId != null) {
            convertToColourStyleColumn(colourId);
            reOrderData(List.of(sourceId, destinationId, valueId, colourId));
        } else {
            reOrderData(List.of(sourceId, destinationId, valueId));
        }
    }

    public GoogleSankeyDiagram(DataTable dataTable, String sourceId, String destinationId,
                               String valueId) {
        this(dataTable, sourceId, destinationId, valueId, null);
    }

    @Override
    protected String getId() {
        return "Sankey";
    }
}
