package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.InclusionDependency;
import visualiser.datavisualiser.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

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
        // TODO: fix this https://stackoverflow.com/questions/14154474/javafx-change-text-before-and-after-run-task
        connectingText.setText("Connecting and processing...");

        ERModel rm = processDatabase(urlField.getText(), usernameField.getText(), passwordField.getText(), schemaField.getText());


        HashSet<InclusionDependency> ids = rm.getIds();
        HashSet<InclusionDependency> coveredIds = rm.getIds().stream().filter(InclusionDependency::isCovered).collect(Collectors.toCollection(HashSet::new));
        ArrayList<String> coveredIdsStr = coveredIds.stream()
                .map(id -> id.getA().getName() + "." + id.getX1().getColumn() + " < " + id.getB().getName() + "." + id.getX2().getColumn())
                .collect(Collectors.toCollection(ArrayList::new));

        User user = new User();
        user.setERModel(rm);
        ViewUtils.sendData(user);

        ViewUtils.switchTo(View.DATA_SELECT);
    }

    private ERModel processDatabase(String url, String username, String password, String schema) {
        // TODO: remove this
        if (url.isBlank()) {
            url = "jdbc:postgresql://localhost/mondial_plus_extra";
            username = "postgres";
            password = "post";
            schema = "public";
        }

        return new ERModel(username, password, url, schema);
    }
}
