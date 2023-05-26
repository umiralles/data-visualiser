package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.GraphDetector.InputAttribute;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Entities.EntityType;
import visualiser.datavisualiser.models.RelationalModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;
import visualiser.datavisualiser.models.RelationalModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.RelationalModel.Relationships.NAryRelationship;
import visualiser.datavisualiser.models.RelationalModel.Relationships.Relationship;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DataModelController implements Initializable {

    private static final String NOT_SELECTED = "N/A";

    private static final String NO_RELATIONSHIP = "No relationship found.";
    private static final String BASIC_RELATIONSHIP = "Basic Relationship";
    private static final String WEAK_RELATIONSHIP = "Weak Relationship";
    private static final String BINARY_RELATIONSHIP = "Binary Relationship";
    private static final String NARY_RELATIONSHIP = "N-Ary Relationship";
    private static final String REFLEXIVE_RELATIONSHIP = "Reflexive Relationship";

    @FXML
    private ChoiceBox<String> entity1Choice;

    @FXML
    private Rectangle entity1BasicBox;

    @FXML
    private Rectangle entity1WeakBox;

    @FXML
    private Label entity1Label;

    @FXML
    private ChoiceBox<String> k1Choice;

    @FXML
    private ChoiceBox<String> entity2Choice;

    @FXML
    private Rectangle entity2BasicBox;

    @FXML
    private Rectangle entity2WeakBox;

    @FXML
    private Label entity2Label;

    @FXML
    private ChoiceBox<String> k2Choice;

    @FXML
    private ChoiceBox<String> addAttributesChoice;

    @FXML
    private VBox addAttributesBox;

    @FXML
    private Label relationshipLabel;

    @FXML
    private Button genGraphsButton;

    private EntityType selectedE1 = null;
    private PrimaryAttribute selectedK1 = null;
    private EntityType selectedE2 = null;
    private PrimaryAttribute selectedK2 = null;
    private VisSchemaPattern currVisPattern = null;
    private Relationship currRelationship = null;
    private List<Attribute> selectedAtts = null;

    @FXML
    private void onGenGraphsButtonClick() {
        User user = ViewUtils.receiveData();

        user.setVisSchemaPattern(currVisPattern);
        user.setRelationship(currRelationship);
        user.setK1(selectedK1);
        user.setK2(selectedK2);
        user.setAttributes(selectedAtts);

        ViewUtils.sendData(user);
        ViewUtils.switchTo(View.GRAPH_SELECT);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();
        ERModel rm = user.getRelationalModel();

        /* CHECK FOR RELATIONSHIPS FUNCTIONALITY */
//        EntityType city = rm.getEntity("city");
//        PrimaryAttribute cPk = country.getPrimaryAttributes().stream().findFirst().get();
//        EntityType waterbody = rm.getEntity("waterbody");
//        PrimaryAttribute pPk = province.getPrimaryAttributes().stream().findFirst().get();
//
//        ArrayList<BinaryRelationship> binRelationships = rm.getRelationships().values().stream().filter(ent -> ent instanceof BinaryRelationship)
//                .map(ent -> (BinaryRelationship) ent)
//                .sorted(Comparator.comparing((Relationship rel) -> rel.getA().getName())).collect(Collectors.toCollection(ArrayList::new));
//
//        ArrayList<String> binRelStr = binRelationships.stream()
//                .map(br -> "(1/0:1) " + br.getA().getName() + "." + br.getX1().getColumn() + " < " + br.getB().getName() + "." + br.getX2().getColumn() + " (0:N)")
//                .collect(Collectors.toCollection(ArrayList::new));

//        ArrayList<NAryRelationship> nAryRelationships = rm.getRelationships().values().stream().filter(ent -> ent instanceof NAryRelationship)
//                .map(ent -> (NAryRelationship) ent)
//                .sorted(Comparator.comparing((Relationship rel) -> rel.getB().getName())).collect(Collectors.toCollection(ArrayList::new));
//
//        ArrayList<String> nAryRelStr = nAryRelationships.stream()
//                .map(nar -> nar.getA().getName() + " " + nar.getName().toUpperCase() + " " + nar.getB().getName())
//                .collect(Collectors.toCollection(ArrayList::new));
//
//        Relationship r = checkForRelationship(rm, city, waterbody);

        ArrayList<EntityType> entities = rm.getEntities().values().stream()
                .sorted(Comparator.comparing(EntityType::getName)).collect(Collectors.toCollection(ArrayList::new));
        for (EntityType entity : entities) {
            entity1Choice.getItems().add(entity.getName());
            entity2Choice.getItems().add(entity.getName());
        }

        // Add null choice for entity 2 (not required)
        entity2Choice.getItems().add(NOT_SELECTED);
        entity2Choice.setValue(NOT_SELECTED);

        entity1BasicBox.setVisible(false);
        entity1WeakBox.setVisible(false);
        entity1Label.setVisible(false);

        entity2BasicBox.setVisible(false);
        entity2WeakBox.setVisible(false);
        entity2Label.setVisible(false);

        // Set Entity 1 Choice action
        entity1Choice.setOnAction(event -> {
            selectedK1 = null;
            genGraphsButton.setDisable(true);
            relationshipLabel.setText("...");

            selectedE1 = rm.getEntity(entity1Choice.getValue());

            // Show entity image representation
            entity1BasicBox.setVisible(true);
            entity1WeakBox.setVisible(selectedE1 instanceof WeakEntityType);
            entity1Label.setText(selectedE1.getName());
            entity1Label.setVisible(true);

            // Add choices to key 1 choice box
            k1Choice.getItems().clear();
            ArrayList<PrimaryAttribute> keyAtts = selectedE1.getPrimaryAttributes().stream()
                    .sorted(Comparator.comparing(Attribute::getColumn)).collect(Collectors.toCollection(ArrayList::new));
            for (PrimaryAttribute keyAtt : keyAtts) {
                k1Choice.getItems().add(keyAtt.getColumn());
            }

            k1Choice.setDisable(false);
        });

        // Set Entity 2 Choice action
        entity2Choice.setOnAction(event -> {
            selectedK2 = null;
            genGraphsButton.setDisable(true);
            relationshipLabel.setText("...");

            // null choice selected
            if (entity2Choice.getValue().equals(NOT_SELECTED)) {
                entity2BasicBox.setVisible(false);
                entity2WeakBox.setVisible(false);
                entity2Label.setVisible(false);
                k2Choice.setDisable(true);
                selectedE2 = null;
                return;
            }

            selectedE2 = rm.getEntity(entity2Choice.getValue());

            // Set entity image representation
            entity2BasicBox.setVisible(true);
            entity2WeakBox.setVisible(selectedE2 instanceof WeakEntityType);
            entity2Label.setText(selectedE2.getName());
            entity2Label.setVisible(true);

            // Add choices to key 2 choice box
            k2Choice.getItems().clear();
            ArrayList<PrimaryAttribute> keyAtts = selectedE2.getPrimaryAttributes().stream()
                    .sorted(Comparator.comparing(Attribute::getColumn)).collect(Collectors.toCollection(ArrayList::new));
            for (PrimaryAttribute keyAtt : keyAtts) {
                k2Choice.getItems().add(keyAtt.getColumn());
            }

            k2Choice.setDisable(false);
        });

        k1Choice.setOnAction(event -> {
            Relation e1 = rm.getRelation(selectedE1.getName());
            selectedK1 = e1.findInPrimaryKey(k1Choice.getValue());

            // Check if other choice is made
            if (selectedE2 == null) {
                relationshipLabel.setText(BASIC_RELATIONSHIP);
                currVisPattern = VisSchemaPattern.BASIC_ENTITY;
                genGraphsButton.setDisable(false);
                return;
            }

            if (selectedK2 == null) {
                relationshipLabel.setText("...");
                return;
            }

            // Set relationship type to found relationship or to null
            Relationship relationship = checkForRelationship(rm, selectedE1, selectedE2);

            if (relationship == null) {
                relationshipLabel.setText(NO_RELATIONSHIP);
                currRelationship = null;
                currVisPattern = null;
                genGraphsButton.setDisable(true);
                return;
            }

            currRelationship = relationship;
            genGraphsButton.setDisable(false);

            if (relationship instanceof BinaryRelationship) {
                // check for weak relationship
                EntityType entA = rm.getEntity(relationship.getA().getName());

                if (entA instanceof WeakEntityType
                        && ((WeakEntityType) entA).getOwnerName().equals(relationship.getB().getName())) {
                    relationshipLabel.setText(WEAK_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.WEAK_ENTITY;
                    return;
                }

                relationshipLabel.setText(BINARY_RELATIONSHIP);
                currVisPattern = VisSchemaPattern.ONE_MANY_REL;
                return;
            }

            if (relationship instanceof NAryRelationship) {
                if (relationship.getA().equals(relationship.getB())) {
                    relationshipLabel.setText(REFLEXIVE_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.REFLEXIVE;
                    return;
                } else {
                    relationshipLabel.setText(NARY_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.MANY_MANY_REL;
                    return;
                }
            }

            relationshipLabel.setText("Unknown relationship");
            currRelationship = null;
            currVisPattern = null;
            genGraphsButton.setDisable(true);
        });

        k2Choice.setOnAction(event -> {
            Relation e2 = rm.getRelation(selectedE2.getName());
            selectedK2 = e2.findInPrimaryKey(k2Choice.getValue());

            // Check if other choice is made
            if (selectedE1 == null || selectedK1 == null) {
                relationshipLabel.setText("...");
                return;
            }

            // Set relationship type to found relationship or to null
            // TODO: repeated code
            Relationship relationship = checkForRelationship(rm, selectedE1, selectedE2);

            if (relationship == null) {
                relationshipLabel.setText(NO_RELATIONSHIP);
                currRelationship = null;
                currVisPattern = null;
                genGraphsButton.setDisable(true);
                return;
            }

            currRelationship = relationship;
            genGraphsButton.setDisable(false);

            if (relationship instanceof BinaryRelationship) {
                // check for weak relationship
                EntityType entA = rm.getEntity(relationship.getA().getName());

                if (entA instanceof WeakEntityType
                        && ((WeakEntityType) entA).getOwnerName().equals(relationship.getB().getName())) {
                    relationshipLabel.setText(WEAK_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.WEAK_ENTITY;
                    return;
                }

                relationshipLabel.setText(BINARY_RELATIONSHIP);
                currVisPattern = VisSchemaPattern.ONE_MANY_REL;
                return;
            }

            if (relationship instanceof NAryRelationship) {
                if (relationship.getA().equals(relationship.getB())) {
                    relationshipLabel.setText(REFLEXIVE_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.REFLEXIVE;
                    return;
                } else {
                    relationshipLabel.setText(NARY_RELATIONSHIP);
                    currVisPattern = VisSchemaPattern.MANY_MANY_REL;
                    return;
                }
            }

            relationshipLabel.setText("Unknown relationship");
            currRelationship = null;
            currVisPattern = null;
            genGraphsButton.setDisable(true);
        });
    }

    private Relationship checkForRelationship(ERModel rm, EntityType e1, EntityType e2) {
        Relation e1Rel = rm.getRelation(e1.getName());
        Relation e2Rel = rm.getRelation(e2.getName());

        List<ArrayList<PrimaryAttribute>> sharedAtts = e1Rel.findSharedPrimaryAttributes(e2Rel);
        ArrayList<PrimaryAttribute> e1SharedAtts = sharedAtts.get(0);
        ArrayList<PrimaryAttribute> e2SharedAtts = sharedAtts.get(1);

        // Check weak relationship
        // TODO: it would be better to have one binary relation for a pair of entities with a list of attributes contained in it
        for (int i = 0; i < e1SharedAtts.size(); i++) {
            PrimaryAttribute k1 = e1SharedAtts.get(i);
            PrimaryAttribute k2 = e2SharedAtts.get(i);

            BinaryRelationship br1 = rm.getBinaryRelationship(new InputAttribute(k1), new InputAttribute(k2));

            if (br1 != null && e1 instanceof WeakEntityType && ((WeakEntityType) e1).getOwnerName().equals(e2.getName())) {
                return br1;
            }

            BinaryRelationship br2 = rm.getBinaryRelationship(new InputAttribute(k2), new InputAttribute(k1));

            if (br2 != null && e2 instanceof WeakEntityType && ((WeakEntityType) e2).getOwnerName().equals(e1.getName())) {
                return br2;
            }

            // Check for binary relationship
            if (br1 != null) {
                return br1;
            } else if (br2 != null) {
                return br2;
            }
        }

        // Check for n-ary relationship
        NAryRelationship nAry = rm.getNAryRelationship(e1Rel, e2Rel);
        if (nAry == null) {
            nAry = rm.getNAryRelationship(e2Rel, e1Rel);
        }

        return nAry;
    }
}
