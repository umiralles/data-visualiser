package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.json.JSONObject;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.ResourceBundle;

public class DataVisController implements Initializable {
    @FXML
    private WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();
        GraphDetector gd = user.getGraphDetector();
        GraphPlan plan = user.getPlan();
        ERModel rm = user.getERModel();

//        try {
//            DataTable unorderedData = gd.getData(rm);
//            List<String> orderedColumnNames = List.of("not working");
////            List<String> orderedColumnNames = plan.getOrderedAttributes().stream().map(Attribute::getColumn).toList();
////            List<String> orderedColumnNames = plan.getOrderedAttributes().stream()
////                    .map(att -> att.getTable() + "." + att.getColumn()).toList();
//            DataTable orderedData = unorderedData.reOrderColumnsViaLabels(orderedColumnNames);
//
//            GoogleChart chart = new GoogleChart("planChart", orderedData, getGoogleChartOptions(), plan.getGoogleChartType());
//            chart.writeJson("html/chart.json");
//
//        } catch (SQLException e) {
//            System.out.println("DataVisController.initialize: " + e.getMessage());
//        }

    }

    @FXML
    protected void onHomeButtonClick() {
        ViewUtils.switchTo(View.HOME);
    }

    // For WebView of width 775, height 500: width 765, height 480
    public JSONObject getGoogleChartOptions() {
        return new JSONObject()
                .put("title", "New Chart")
                .put("width", 765)
                .put("height", 480);
    }
}
