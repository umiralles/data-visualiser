package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.ResourceBundle;

public class DataSelectController implements Initializable {

    @FXML
    private Label titleLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();

        titleLabel.setText("Connected to " + user.getRelationalModel().getCatalog() + " and processed it's data");
    }

    @FXML
    protected void onHomeButtonClick() {
        ViewUtils.switchTo(View.HOME);
    }

    @FXML
    protected void onDatabaseButtonClick() {
        ViewUtils.switchTo(View.DATA_CHOOSE_DATABASE);
    }

    @FXML
    protected void onModelButtonClick() {
        ViewUtils.switchTo(View.DATA_CHOOSE_MODEL);
    }
}
