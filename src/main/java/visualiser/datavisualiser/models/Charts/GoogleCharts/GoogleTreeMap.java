package visualiser.datavisualiser.models.Charts.GoogleCharts;

import visualiser.datavisualiser.models.Charts.GoogleChart;
import visualiser.datavisualiser.models.Charts.GoogleDataType;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.DataTable.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoogleTreeMap extends GoogleChart {

    public GoogleTreeMap(DataTable dataTable, String childLabelId, String parentLabelId, String nodeSizeId,
                         String colourId) {
        super(dataTable);

        if (colourId != null) {
            reOrderData(List.of(childLabelId, parentLabelId, nodeSizeId, colourId));
            // Shows colour scale
            addOption("showScale", true);
        } else {
            reOrderData(List.of(childLabelId, parentLabelId, nodeSizeId));
        }

        // Add parent labels to child label column (to be a root in the graph)
        int nodeSizeIdx = IntStream.range(0, dataTable.columns().size()).dropWhile(idx -> !dataTable.columns().get(idx).id().equals(nodeSizeId)).findFirst().getAsInt();
        DataType nodeSizeDataType = dataTable.columns().get(nodeSizeIdx).type();
        String nullNodeSizeVal = GoogleDataType.getNameFromDataType(nodeSizeDataType).equals("number") ? "0" : "";
        DataType colourDataType = null;
        String nullColourVal = null;
        if (colourId != null) {
            int colourIdx = IntStream.range(0, dataTable.columns().size()).dropWhile(idx -> !dataTable.columns().get(idx).id().equals(colourId)).findFirst().getAsInt();
            colourDataType = dataTable.columns().get(colourIdx).type();
            nullColourVal = GoogleDataType.getNameFromDataType(colourDataType).equals("number") ? "0" : "";
        }

        List<DataCell> rootRow = new ArrayList<>();
        rootRow.add(new DataCell(parentLabelId, DataType.STRING));
        rootRow.add(new DataCell(null, DataType.STRING));
        rootRow.add(new DataCell(nullNodeSizeVal, nodeSizeDataType));
        if (colourId != null) {
            rootRow.add(new DataCell(nullColourVal, colourDataType));
        }

        List<List<DataCell>> newRows = new ArrayList<>();
        newRows.add(rootRow);

        int parentIdx = IntStream.range(0, dataTable.columns().size()).dropWhile(idx -> !dataTable.columns().get(idx).id().equals(parentLabelId)).findFirst().getAsInt();
        for (String parentVal : dataTable.rows().stream().map(row -> row.get(parentIdx).value()).collect(Collectors.toSet())) {
            List<DataCell> newRow = new ArrayList<>();
            newRow.add(new DataCell(parentVal, DataType.STRING));
            newRow.add(new DataCell(parentLabelId, DataType.STRING));
            newRow.add(new DataCell(nullNodeSizeVal, nodeSizeDataType));
            if (colourId != null) {
                newRow.add(new DataCell(nullColourVal, colourDataType));
            }

            newRows.add(newRow);
        }

        addRows(newRows);

        addTitle("Tree Map");
    }

    public GoogleTreeMap(DataTable dataTable, String childLabelId, String parentLabelId, String nodeSizeId) {
        this(dataTable, childLabelId, parentLabelId, nodeSizeId, null);
    }

    @Override
    protected String getId() {
        return "TreeMap";
    }
}
