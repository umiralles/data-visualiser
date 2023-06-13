package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleOrgChart extends GoogleChart {

    // parentLabelId, colourId are all optional
    public GoogleOrgChart(DataTable dataTable, String childLabelId, String parentLabelId, String colourId) {
        super(dataTable);

        List<String> newOrderOfData = new ArrayList<>();
        if (parentLabelId != null && colourId != null) {
            newOrderOfData.addAll(List.of(childLabelId, parentLabelId, colourId));
        } else if (parentLabelId != null) {
            newOrderOfData.addAll(List.of(childLabelId, parentLabelId));
        } else {
            newOrderOfData.add(childLabelId);
        }

        if (colourId != null) {
            addOption("allowHtml", true);
//            convertToColourCellProperties(colourId);
            List<String> rowColours = dataTable.getHexColoursFromId(colourId);
            addOption("dataVisRowColours", rowColours);
            addColumnValuePrefix(colourId, colourId + ": ");
        }

        reOrderData(newOrderOfData);

        addTitle("Org Chart");
    }

    @Override
    protected String getId() {
        return "OrgChart";
    }
}
