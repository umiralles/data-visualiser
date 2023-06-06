package visualiser.datavisualiser.models.DataTable;


import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public record DataTable(List<Column> columns, List<List<DataCell>> rows) {

    public DataTable {
        // Check each row has the correct length and type
        // TODO: needs error message
        for (List<DataCell> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < row.size(); i++) {
                DataCell rowCell = row.get(i);
                Column column = columns.get(i);

                if (!rowCell.type().equals(column.type())) {
                    throw new IllegalArgumentException();
                }
            }
        }

    }

    // The list of ids must already be contained in the datatable
    public static DataTable getWithReOrderedColumns(DataTable dataTable, List<String> newOrderIds) {
        // Remove any null values
        List<String> ids = new ArrayList<>(newOrderIds);
        ids.removeAll(Collections.singleton(null));
        Set<String> cols = dataTable.columns.stream().map(Column::id).collect(Collectors.toSet());

        if (!cols.containsAll(ids)) {
            // TODO: error
            System.out.println("Tried to reorder columns with incorrect ids. columnIds: ");
            cols.forEach(col -> System.out.print(col + " "));
            System.out.print("newIds: ");
            ids.forEach(id -> System.out.print(id + " "));
            return null;
        }

        // Create appropriately sized new arrays
        List<Column> newColumns = new ArrayList<>();
        dataTable.columns.forEach(col -> newColumns.add(null));
        List<List<DataCell>> newRows = new ArrayList<>();
        dataTable.rows.forEach(row -> {
            List<DataCell> newRow = new ArrayList<>();
            dataTable.columns.forEach(col -> newRow.add(null));
            newRows.add(newRow);
        });

        for (int oldIdx = 0; oldIdx < dataTable.columns.size(); oldIdx++) {
            String oldId = dataTable.columns.get(oldIdx).id();

            if (!ids.contains(oldId)) {
                return null;
            }

            // Find new placement of the old attribute
            int newIdx = ids.indexOf(oldId);

            // Place into the new Index
            newColumns.set(newIdx, dataTable.columns.get(oldIdx));
            for (int rowIdx = 0; rowIdx < dataTable.rows.size(); rowIdx++) {
                DataCell oldCell = dataTable.rows.get(rowIdx).get(oldIdx);
                newRows.get(rowIdx).set(newIdx, oldCell);
            }
        }

        return new DataTable(newColumns, newRows);
    }

    public static DataTable getWithNewColumn(DataTable dataTable, Column column, List<DataCell> dataCells) {
        List<Column> newColumns = new ArrayList<>(dataTable.columns);
        List<List<DataCell>> newRows = new ArrayList<>();

        newColumns.add(column);

        for (int i = 0; i < dataTable.rows.size(); i++) {
            DataCell newCell = dataCells.get(i);

            if (!newCell.type().equals(column.type())) {
                throw new IllegalArgumentException();
            }

            List<DataCell> newRow = new ArrayList<>(dataTable.rows.get(i));
            newRow.add(newCell);
            newRows.add(newRow);
        }

        return new DataTable(newColumns, newRows);
    }

    public List<String> getHexColoursFromId(String id, Color startColour, Color endColour) {
        List<String> ids = columns.stream().map(Column::id).toList();

        if (!ids.contains(id)) {
            return null;
        }

        int colourIdx = ids.indexOf(id);

        Column colourCol = columns.get(colourIdx);
        List<String> colourVals = new ArrayList<>();
        rows.forEach(row -> colourVals.add(row.get(colourIdx).value()));

        List<String> hexColours = null;
        switch (colourCol.type()) {
            case BOOLEAN -> {
                String trueColour = "#" + colorToString(startColour);
                String falseColour = "#" + colorToString(endColour);
                hexColours = colourVals.stream()
                        .map(Boolean::valueOf)
                        .map(bool -> {
                            if (bool) {
                                return trueColour;
                            }
                            return falseColour;
                        }).toList();
            }
            case STRING, DATE ->
                // Assign value based on sorted order
                    hexColours = convertIntegersToHexColors(getStringValues(colourVals), startColour, endColour);
            case INT -> {
                List<Float> floatList = colourVals.stream().map(intStr -> Integer.valueOf(intStr).floatValue()).toList();
                hexColours = convertFloatsToHexColors(floatList, startColour, endColour);
            }
            case DOUBLE, FLOAT -> {
                List<Float> floatList = colourVals.stream().map(Float::valueOf).toList();
                hexColours = convertFloatsToHexColors(floatList, startColour, endColour);
            }
            default -> System.out.println(
                    "DataTable.convertToHexColour: Tried to convert unsupported type " + colourCol.type() + " to hex colour");
        }

        return hexColours;
    }

    public List<String> getHexColoursFromId(String id) {
        return getHexColoursFromId(id, Color.YELLOW, Color.RED);
    }

    private static String colorToString(Color color) {
        return String.format("%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private static List<Integer> getStringValues(List<String> stringList) {
        Map<String, Integer> stringToValueMap = new TreeMap<>();
        Map<String, Integer> stringCounter = new HashMap<>();

        for (String str : stringList) {
            stringCounter.put(str, stringCounter.getOrDefault(str, 0) + 1);
            stringToValueMap.put(str + "_" + stringCounter.get(str), stringToValueMap.size() + 1);
        }

        List<Integer> valueList = new ArrayList<>();
        for (String str : stringList) {
            valueList.add(stringToValueMap.get(str + "_" + stringCounter.get(str)));
        }

        return valueList;
    }

    private static List<String> convertIntegersToHexColors(List<Integer> integerList, Color startColor, Color endColor) {
        List<Float> floatList = integerList.stream().map(Integer::floatValue).toList();
        return convertFloatsToHexColors(floatList, startColor, endColor);
    }

    private static List<String> convertFloatsToHexColors(List<Float> floatsList, Color startColor, Color endColor) {
        List<String> hexColorList = new ArrayList<>();

        // Calculate the hue difference between start and end colors
        double startHue = startColor.getHue();
        double endHue = endColor.getHue();
        double hueDiff = endHue - startHue;

        // Calculate the increment for each integer
        float maxVal = Collections.max(floatsList);
        float minVal = Collections.min(floatsList);
        double increment = hueDiff / (maxVal - minVal);

        // Convert each integer to a hex color
        for (float floatVal : floatsList) {
            // Calculate the hue based on the integer value
            double hue = startHue + ((floatVal - minVal) * increment);

            // Create the Color object with constant saturation and brightness, and convert it to a hex string
            Color color = Color.hsb(hue, 1.0, 1.0);
            String hexColor = "#" + colorToString(color);

            hexColorList.add(hexColor);
        }

        return hexColorList;
    }
}
