package visualiser.datavisualiser;

public enum View {
    HOME("fxml/home-view.fxml"),
    DATA_CONNECT("fxml/data-connect-view.fxml"),
    DATA_SELECT("fxml/data-select-view.fxml"),
    DATA_CHOOSE_DATABASE("fxml/data-database-view.fxml"),
    DATA_CHOOSE_MODEL("fxml/data-model-view.fxml"),
    DATA_VIS("fxml/data-vis-view.fxml"),
    GRAPH_SELECT("fxml/graph-select-view.fxml");

    private final String fileName;

    View(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
