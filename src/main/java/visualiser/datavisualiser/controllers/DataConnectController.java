package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.User;

public class DataConnectController {

    @FXML
    private TextField urlField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField schemaField;

    @FXML
    private Text connectingText;

    @FXML
    protected void onConnectButtonClick() {
        // TODO: could be with https://stackoverflow.com/questions/14154474/javafx-change-text-before-and-after-run-task
        connectingText.setText("Connecting and processing...");

        try {
            ERModel rm = processDatabase(urlField.getText(), usernameField.getText(), passwordField.getText(), schemaField.getText());
            User user = new User();
            user.setERModel(rm);
            ViewUtils.sendData(user);

            ViewUtils.switchTo(View.DATA_SELECT);
        } catch (IllegalArgumentException e) {
            connectingText.setText("Database not found.");
        }
    }

    private ERModel processDatabase(String url, String username, String password, String schema) {
        return new ERModel(username, password, url, schema);
    }
}
