package visualiser.datavisualiser.models.GoogleChart;


import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.*;
import java.util.stream.IntStream;

public class DataTable {

    // Must Always match the order of columns and rows. Is nullable
    private final List<Attribute> attributeOrder;
    private final List<Column> columns;
    private final List<List<DataCell>> rows;

    public DataTable(List<Attribute> attributeOrder, List<Column> columns, List<List<DataCell>> rows) throws IllegalArgumentException {
        if ((attributeOrder != null && attributeOrder.size() != columns.size())
                // attribute order should not have null attributes
                || Objects.requireNonNull(attributeOrder).stream().dropWhile(Objects::nonNull).findFirst().isPresent()) {
            throw new IllegalArgumentException();
        }

        // Check each row has the correct length and type
        // TODO: needs error message
        for (List<DataCell> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < row.size(); i++) {
                DataCell rowCell = row.get(i);
                Column column = columns.get(i);

                if (!rowCell.getType().equals(column.getType())) {
                    throw new IllegalArgumentException();
                }

                // TODO: check attribute order types?
//                if (attributeOrder != null) {
//                    GraphAttribute attribute = attributeOrder.get(i);
//                    if (!attribute.typeInGraph())
//                }
            }
        }

        this.attributeOrder = attributeOrder;
        this.columns = columns;
        this.rows = rows;
    }

    public DataTable(List<Column> columns, List<List<DataCell>> rows) throws IllegalArgumentException {
        this(null, columns, rows);
    }

    public JSONObject generateJSON() {

        JSONArray columnsJson = new JSONArray();

        for (Column column : columns) {
            columnsJson.put(column.generateJSON());
        }

        JSONArray rowsJson = new JSONArray();

        for (List<DataCell> row : rows) {

            JSONArray rowJson = new JSONArray();
            boolean nullRow = false;
            for (DataCell cell : row) {
                JSONObject cellJson = cell.generateJSON();

                if (cellJson == null) {
                    nullRow = true;
                    break;
                }

                rowJson.put(cellJson);
            }

            if (!nullRow) {
                rowsJson.put(new JSONObject().put("c", rowJson));
            }
        }

        return new JSONObject()
                .put("cols", columnsJson)
                .put("rows", rowsJson);
    }

    // The list of attributes must already be contained in the datatable
    public DataTable reOrderColumns(List<Attribute> newOrder) {
        if (newOrder.size() != columns.size() || attributeOrder == null) {
            // TODO: error
            return null;
        }

        // Create appropriately sized new arrays
        List<Attribute> newGraphAttOrder = new ArrayList<>();
        columns.forEach(col -> newGraphAttOrder.add(null));
        List<Column> newColumns = new ArrayList<>();
        columns.forEach(col -> newColumns.add(null));
        List<List<DataCell>> newRows = new ArrayList<>();
        rows.forEach(row -> {
            List<DataCell> newRow = new ArrayList<>();
            columns.forEach(col -> newRow.add(null));
            newRows.add(newRow);
        });

        for (int i = 0; i < columns.size(); i++) {
            int oldIdx = i;
            Attribute oldAtt = attributeOrder.get(oldIdx);

            // Find new placement of the old attribute
            int newIdx = newOrder.indexOf(oldAtt);

            // Place into the new Index
            newGraphAttOrder.set(newIdx, oldAtt);
            newColumns.set(newIdx, columns.get(oldIdx));
            IntStream.range(0, rows.size()).forEach(rowIdx -> {
                DataCell oldCell = rows.get(rowIdx).get(oldIdx);
                newRows.get(rowIdx).set(newIdx, oldCell);
            });
        }

        return new DataTable(newGraphAttOrder, newColumns, newRows);
    }

    public DataTable reOrderColumnsViaLabels(List<String> newOrder) {
        if (newOrder.size() != columns.size()) {
            // TODO: error
            return null;
        }

        // TODO: maybe use hashmaps for columns
        List<Attribute> newAtts = new ArrayList<>();
        columns.forEach(col -> newAtts.add(null));
        List<Column> newColumns = new ArrayList<>();
        columns.forEach(col -> newColumns.add(null));
        List<List<DataCell>> newRows = new ArrayList<>();
        rows.forEach(row -> {
            List<DataCell> newRow = new ArrayList<>();
            columns.forEach(col -> newRow.add(null));
            newRows.add(newRow);
        });

        for (int i = 0; i < columns.size(); i++) {
            int oldIdx = i;
            Column oldColumn = columns.get(oldIdx);

            // Find new placement of the old attribute
            int newIdx = newOrder.indexOf(oldColumn.getLabel());

            // Place into the new Index
            if (attributeOrder != null) {
                newAtts.set(newIdx, attributeOrder.get(oldIdx));
            }
            newColumns.set(newIdx, oldColumn);
            IntStream.range(0, rows.size()).forEach(rowIdx -> {
                DataCell oldCell = rows.get(rowIdx).get(oldIdx);
                newRows.get(rowIdx).set(newIdx, oldCell);
            });
        }

        if (newAtts.isEmpty()) {
            return new DataTable(newColumns, newRows);
        }
        return new DataTable(newAtts, newColumns, newRows);
    }

    public DataTable getDataForPlan(GraphPlan plan) {
        // To use, attributeOrder cannot be null
        if (attributeOrder == null) {
            return null;
        }

        // Reorder via attributes
        List<GraphAttribute> gAtts = plan.getAllOrderedAttributes();
        List<Attribute> newOrder = new ArrayList<>(gAtts.stream().map(GraphAttribute::attribute).toList());

        List<Attribute> nullList = new ArrayList<>();
        nullList.add(null);
        newOrder.removeAll(nullList);

        return reOrderColumns(newOrder);
    }

    public List<String> convertToHexColour(Attribute attribute, Color startColour, Color endColour) {
        if (!attributeOrder.contains(attribute)) {
            return null;
        }

        int colourIdx = attributeOrder.indexOf(attribute);

        attributeOrder.remove(colourIdx);
        columns.remove(colourIdx);
        List<String> dataVals = new ArrayList<>();
        rows.forEach(row -> {
            dataVals.add(row.get(colourIdx).getValue());
            row.remove(colourIdx);
        });

        List<String> hexColours = null;
        switch (attribute.getDBType()) {
            case BOOLEAN -> {
                String trueColour = "#" + colorToString(startColour);
                String falseColour = "#" + colorToString(endColour);
                hexColours = dataVals.stream()
                        .map(Boolean::valueOf)
                        .map(bool -> {
                            if (bool) {
                                return trueColour;
                            }
                            return falseColour;
                        }).toList();
            }
            case STRING, DATE, TIME, TIMESTAMP ->
                // Assign value based on sorted order
                    hexColours = convertIntegersToHexColors(getStringValues(dataVals), startColour, endColour);
            case INT -> {
                List<Float> floatList = dataVals.stream().map(intStr -> Integer.valueOf(intStr).floatValue()).toList();
                hexColours = convertFloatsToHexColors(floatList, startColour, endColour);
            }
            case DOUBLE, FLOAT -> {
                List<Float> floatList = dataVals.stream().map(Float::valueOf).toList();
                hexColours = convertFloatsToHexColors(floatList, startColour, endColour);
            }
            default -> System.out.println(
                    "DataTable.convertToHexColour: Tried to convert unsupported type " + attribute.getDBType() + " to hex colour");
        }

        return hexColours;
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
