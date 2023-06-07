package visualiser.datavisualiser.models.DataTable;


import javafx.scene.paint.Color;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;

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

    public static DataTable getWithNewColumn(DataTable dataTable, Column column, List<DataCell> columnVals) {
        List<Column> newColumns = new ArrayList<>(dataTable.columns);
        List<List<DataCell>> newRows = new ArrayList<>();

        newColumns.add(column);

        for (int i = 0; i < dataTable.rows.size(); i++) {
            DataCell newCell = columnVals.get(i);

            if (!newCell.type().equals(column.type())) {
                throw new IllegalArgumentException();
            }

            List<DataCell> newRow = new ArrayList<>(dataTable.rows.get(i));
            newRow.add(newCell);
            newRows.add(newRow);
        }

        return new DataTable(newColumns, newRows);
    }

    public static DataTable getWithLimit(DataTable dataTable, ERModel rm, Relationship rel, String k1Id, int k1Limit, String k2Id, int k2Limit) {
        if (k1Limit < 0) {
            // No limit (k2 should not be limited without also limiting k1)
            return dataTable;
        }

        // Just k1 Limit
        if (k2Id == null || k2Limit < 0 || rel == null) {
            if (dataTable.rows.size() < k1Limit) {
                // Already under limit
                return dataTable;
            }

            Optional<Column> k1Col = dataTable.columns.stream().dropWhile(col -> col.id().equals(k1Id)).findFirst();
            if (k1Col.isEmpty()) {
                // error
                System.out.println("Tried to limit with incorrect ids: " + k1Id);
                return null;
            }

            int k1Idx = dataTable.columns.indexOf(k1Col.get());
            Set<String> k1Vals = new HashSet<>();
            for (List<DataCell> row : dataTable.rows) {
                k1Vals.add(row.get(k1Idx).value());
            }

            if (k1Vals.size() < k1Limit) {
                // Already under limit
                return dataTable;
            }

            Set<String> allowedK1s = new HashSet<>(k1Vals.stream().toList().subList(0, k1Limit));
            List<List<DataCell>> newRows = new ArrayList<>();
            for (List<DataCell> oldRow : dataTable.rows) {
                if (allowedK1s.contains(oldRow.get(k1Idx).value())) {
                    newRows.add(new ArrayList<>(oldRow));
                }
            }

            return new DataTable(dataTable.columns, newRows);
        }

        // Find indexes
        int k1Idx = -1;
        int k2Idx = -1;
        for (int i  = 0; i < dataTable.columns.size(); i++) {
            if (dataTable.columns.get(i).id().equals(k1Id)) {
                k1Idx = i;
            } else if (dataTable.columns.get(i).id().equals(k2Id)) {
                k2Idx = i;
            }
        }

        if (k1Idx == -1 || k2Idx == -1) {
            // error
            System.out.println("Tried to limit with incorrect ids: " + k1Id + " " + k2Id);
            return null;
        }

        // Collect values
        Map<String, Set<String>> k1ToK2s = new HashMap<>();
        Set<String> allK2Vals = new HashSet<>();
        for (List<DataCell> row : dataTable.rows) {
            String k1Val = row.get(k1Idx).value();
            String k2Val = row.get(k2Idx).value();

            if (!k1ToK2s.containsKey(k1Val)) {
                k1ToK2s.put(k1Val, new HashSet<>());
            }

            k1ToK2s.get(k1Val).add(k2Val);
            allK2Vals.add(k2Val);
        }

        // Find which rows to keep
        Set<String> k1sToKeep = k1ToK2s.keySet();
        if (k1ToK2s.keySet().size() >= k1Limit) {
            k1sToKeep = new HashSet<>(k1ToK2s.keySet().stream().toList().subList(0, k1Limit));
        }

        List<List<DataCell>> newRows = new ArrayList<>();
        if ((rel instanceof BinaryRelationship) && !((BinaryRelationship) rel).isWeakRelationship(rm)) {
            // OneMany relationship
            Map<String, Set<String>> k1ToK2sToKeep = new HashMap<>();
            for (String k1 : k1sToKeep) {
                Set<String> k2sSet = k1ToK2s.get(k1);
                if (k2sSet.size() > k2Limit) {
                    k2sSet =new HashSet<>(k2sSet.stream().toList().subList(0, k2Limit));
                }

                k1ToK2sToKeep.put(k1, k2sSet);
            }

            for (List<DataCell> row : dataTable.rows) {
                String rowK1 = row.get(k1Idx).value();
                String rowK2 = row.get(k2Idx).value();

                if (k1ToK2sToKeep.containsKey(rowK1)
                        && k1ToK2sToKeep.get(rowK1).contains(rowK2)) {
                    newRows.add(new ArrayList<>(row));
                }
            }

        } else {
            // All other relationships
            Set<String> k2sToKeep = allK2Vals;
            if (k2sToKeep.size() >= k2Limit) {
                k2sToKeep = new HashSet<>(allK2Vals.stream().toList().subList(0, k2Limit));
            }

            for (List<DataCell> row : dataTable.rows) {
                String rowK1 = row.get(k1Idx).value();
                String rowK2 = row.get(k2Idx).value();

                if (k1sToKeep.contains(rowK1) && k2sToKeep.contains(rowK2)) {
                    newRows.add(new ArrayList<>(row));
                }
            }
        }

        return new DataTable(dataTable.columns, newRows);
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
