package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.InputAttribute;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Entities.EntityType;
import visualiser.datavisualiser.models.RelationalModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;
import visualiser.datavisualiser.models.RelationalModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.RelationalModel.Relationships.NAryRelationship;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.*;

public class DataDatabaseController implements Initializable {

    @FXML
    private Text k1WarningText;

    @FXML
    private TextField k1TableField;
    @FXML
    private TextField k1AttField;
    @FXML
    private TextField k2TableField;
    @FXML
    private TextField k2AttField;
    @FXML
    private TextField att1TableField;
    @FXML
    private TextField att1NameField;
    @FXML
    private TextField att2TableField;
    @FXML
    private TextField att2NameField;
    @FXML
    private TextField att3TableField;
    @FXML
    private TextField att3NameField;
    @FXML
    private TextField att4TableField;
    @FXML
    private TextField att4NameField;

    private List<TextField> attTableFields;
    private List<TextField> attNameFields;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.attTableFields = List.of(att1TableField, att2TableField, att3TableField, att4TableField);
        this.attNameFields = List.of(att1NameField, att2NameField, att3NameField, att4NameField);
    }

    @FXML
    protected void onBackButtonClick() {
        ViewUtils.switchTo(View.DATA_SELECT);
    }

    @FXML
    protected void onGenerateButtonClick() {
        // TODO: Remove this (for testing basic entity)
        if (k1TableField.getText().isBlank()) {
            k1TableField.setText("city");
            k1AttField.setText("name");
            att1TableField.setText("city");
            att1NameField.setText("elevation");
            att2TableField.setText("city");
            att2NameField.setText("longitude");
        }

        String k1Table = k1TableField.getText();
        String k1Att = k1AttField.getText();
        if (k1Table.isBlank() || k1Att.isBlank()) {
            k1WarningText.setText("These fields are required");
            return;
        }

        InputAttribute k1 = new InputAttribute(k1Table, k1Att);

        String k2Table = k2TableField.getText();
        String k2Att = k2AttField.getText();
        InputAttribute k2 = null;
        if (!k2Table.isBlank() && !k2Att.isBlank()) {
            k2 = new InputAttribute(k2Table, k2Att);
        }

        Set<String> attTablesSet = new HashSet<>();
        List<InputAttribute> attributes = new ArrayList<>();
        for (int i = 0; i < attTableFields.size(); i++) {
            String attTable = attTableFields.get(i).getText();
            String attName = attNameFields.get(i).getText();

            if (attTable.isBlank() || attName.isBlank()) {
                continue;
            }

            attTablesSet.add(attTable);
            attributes.add(new InputAttribute(attTable, attName));
        }

        if (attributes.size() > 0 && attTablesSet.size() != 1) {
            k1WarningText.setText("Attributes must be of the same table");
            return;
        }

        // Set k1 to the key attribute if the attributes all belong to k2
        String attTable = null;
        if (attributes.size() > 0 && k2 != null) {
            attTable = attTablesSet.stream().findFirst().get();

            if (attTable.equals(k2.table())) {
                InputAttribute temp = k1;
                k1 = k2;
                k2 = temp;
            }
        }

        User user = ViewUtils.receiveData();
        ERModel rm = user.getRelationalModel();

        /* BASIC ENTITY GRAPH */
        if (k2 == null || (attributes.size() > 0 && k1.table().equals(attTable) && k2.table().equals(attTable))) {
            if (k2 != null) {
                attributes.add(k2);
            }

            GraphDetector gd = GraphDetector.generateBasicPlans(rm, k1, attributes);

            user.setGraphDetector(gd);
            ViewUtils.sendData(user);
            ViewUtils.switchTo(View.DATA_VIS);
            return;
        }

        EntityType k1Ent = rm.getEntity(k1.table());
        EntityType k2Ent = rm.getEntity(k2.table());

        // TODO: idk man this is weird. if k1Ent and k2Ent aren't null these shouldn't be either
        Relation k1Rel = rm.getRelation(k1.table());
        Relation k2Rel = rm.getRelation(k2.table());

        if (k1Ent == null || k2Ent == null || k1Rel == null || k2Rel == null) {
            // TODO: error (can only be weak, one many or many many relationship from here)
            return;
        }

        /* WEAK ENTITY GRAPH */
        if (k1Ent instanceof WeakEntityType && ((WeakEntityType) k1Ent).getOwnerName().equals(k2Ent.getName())) {
            GraphDetector gd = GraphDetector.generateWeakPlans(rm, k1, k2, attributes);
            user.setGraphDetector(gd);

        } else if (k2Ent instanceof WeakEntityType && ((WeakEntityType) k2Ent).getOwnerName().equals(k1Ent.getName())) {
            GraphDetector gd = GraphDetector.generateWeakPlans(rm, k2, k1, attributes);
            user.setGraphDetector(gd);
        }

        /* ONE MANY GRAPH */
        BinaryRelationship br = rm.getBinaryRelationship(k2, k1);
        if (br != null) {
            GraphDetector gd = GraphDetector.generateOneManyPlans(rm, k1, k2, attributes);
            // TODO: could also be a weak entity relationship gd?
            user.setGraphDetector(gd);
        }

        /* MANY MANY GRAPH */
        Relation attRel = rm.getRelation(attTable);
        if (attRel != null) {
            // TODO: this is bad
            NAryRelationship nAry = (NAryRelationship) rm.getRelationships().values().stream()
                    .filter(rel -> rel instanceof NAryRelationship && ((NAryRelationship) rel).getRelationshipName().equals(attRel.getName()))
                    .findFirst().orElse(null);
            if (nAry != null) {
                boolean reflexive = nAry.getA().equals(nAry.getB());
                GraphDetector gd = GraphDetector.generateManyManyPlans(rm, reflexive, k1, k2, attributes);
                user.setGraphDetector(gd);
            }
        }

        GraphDetector gd = user.getGraphDetector();
        if (gd != null) {
            ViewUtils.sendData(user);
            ViewUtils.switchTo(View.GRAPH_SELECT);
        }
    }
}
