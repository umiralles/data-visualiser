package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;

public class HomeController {

    @FXML
    protected void onDataConnectButtonClick() {
        ViewUtils.switchTo(View.DATA_CONNECT);
    }
}