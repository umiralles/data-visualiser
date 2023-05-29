package visualiser.datavisualiser.controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.GoogleChart.DataTable;
import visualiser.datavisualiser.models.GoogleChart.GoogleChart;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DataVisController implements Initializable {
    @FXML
    private WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();
        GraphDetector gd = user.getGraphDetector();
        GraphPlan plan = user.getPlan();
        ERModel rm = user.getRelationalModel();

        try {
            DataTable unorderedData = gd.getData(rm);
            List<String> orderedColumnNames = List.of("not working");
//            List<String> orderedColumnNames = plan.getOrderedAttributes().stream().map(Attribute::getColumn).toList();
//            List<String> orderedColumnNames = plan.getOrderedAttributes().stream()
//                    .map(att -> att.getTable() + "." + att.getColumn()).toList();
            DataTable orderedData = unorderedData.reOrderColumns(orderedColumnNames);

            GoogleChart chart = new GoogleChart("planChart", orderedData, gd.getGoogleChartOptions(), plan.getGoogleChartType());
//            chart.writeJson("html/chart.json");

            WebEngine engine = webView.getEngine();

            engine.load(getClass().getResource("html/google_chart.html").toExternalForm());

            // Inject the JSON data into the WebView after the page finishes loading
            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    String jsonString = chart.getJson().toString().replace("'", "\\'");

//                    engine.executeScript("var globalJson = " + jsonString + ";");
                    engine.executeScript("drawChart('" + jsonString + "');");
                }
            });
        } catch (SQLException e) {
            System.out.println("DataVisController.initialize: " + e.getMessage());
        }

    }

    @FXML
    protected void onHomeButtonClick() {
        ViewUtils.switchTo(View.HOME);
    }

}
