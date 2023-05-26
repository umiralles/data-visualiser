package visualiser.datavisualiser.models.GoogleChart;

// 'name' is used for the google.visualisations type name
public enum ChartType {
    BAR_CHART("BarChart"),
    PIE_CHART("PieChart"),
    BUBBLE_CHART("BubbleChart"),
    CALENDAR("Calendar"),
    CHOROPLETH_MAP("GeoChart"),
    SCATTER_DIAGRAM("ScatterChart"),
//    WORD_CLOUD(),
    NOT_SUPPORTED(null);

    private final String name;

    ChartType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
