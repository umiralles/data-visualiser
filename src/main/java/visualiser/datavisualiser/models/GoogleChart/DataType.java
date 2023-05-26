package visualiser.datavisualiser.models.GoogleChart;

public enum DataType {
    BOOLEAN("boolean"),
    INT("number"),
    FLOAT("number"),
    DOUBLE("number"),
    STRING("string"),
    DATE("date"),
    DATETIME("datetime"),
    TIMEOFDAY("timeofday");

    // name:    from Google Charts cols properties
    //          https://developers.google.com/chart/interactive/docs/reference#dataparam
    private final String name;

    DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
