package visualiser.datavisualiser.models.Charts;

import visualiser.datavisualiser.models.DataTable.DataType;

import java.util.Set;

public enum GoogleDataType {
    BOOLEAN("boolean", DataType.BOOLEAN),
    NUMBER("number", DataType.DOUBLE, DataType.INT, DataType.FLOAT),
    STRING("string", DataType.STRING),
    DATE("date", DataType.DATE),
    DATETIME("datetime", DataType.DATETIME),
    TIMEOFDAY("timeofday", DataType.TIMEOFDAY);

    // name:    from Google Charts cols properties
    //          https://developers.google.com/chart/interactive/docs/reference#dataparam
    private final String name;

    private final Set<DataType> possibleTypes;

    GoogleDataType(String name, DataType... possibleTypes) {
        this.name = name;
        this.possibleTypes = Set.of(possibleTypes);
    }

    public String getName() {
        return name;
    }

    // Only one GoogleDataType should apply to a DataType, so only the first found is returned //
    public static String getNameFromDataType(DataType type) {
        for (GoogleDataType googleType : values()) {
            if (googleType.possibleTypes.contains(type)) {
                return googleType.getName();
            }
        }

        return null;
    }
}
