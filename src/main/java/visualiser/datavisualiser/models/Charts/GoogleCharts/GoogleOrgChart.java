package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.DataTable.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GoogleOrgChart extends GoogleChart {

    // parentLabelId, colourId and tooltipLabelId are all optional
    public GoogleOrgChart(DataTable dataTable, String childLabelId, String colourId, String parentLabelId, String tooltipLabelId) {
        super(dataTable);

        List<String> newOrderOfData = new ArrayList<>();
        if (parentLabelId != null && tooltipLabelId != null) {
            newOrderOfData.addAll(List.of(childLabelId, parentLabelId, tooltipLabelId));
        } else if (parentLabelId != null) {
            newOrderOfData.addAll(List.of(childLabelId, parentLabelId));
        } else {
            newOrderOfData.add(childLabelId);
        }

        if (colourId != null) {
//            addOption("allowHtml", true);
//            convertToColourCellProperties(colourId);
            List<String> rowColours = dataTable.getHexColoursFromId(colourId);
            addOption("data-vis-row-colour", rowColours);
        }

        reOrderData(newOrderOfData);

        addTitle("Org Chart");
    }

    public GoogleOrgChart(DataTable dataTable, String childLabelId, String parentLabelId, String colourId) {
        this(dataTable, childLabelId, colourId, parentLabelId, null);
    }

    @Override
    protected String getId() {
        return "OrgChart";
    }
}
