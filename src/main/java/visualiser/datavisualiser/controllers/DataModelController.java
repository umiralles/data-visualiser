package visualiser.datavisualiser.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.ERModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.NAryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DataModelController implements Initializable {

    private static final String NOT_SELECTED = "N/A";
    private static final String DEFAULT_LABEL = "...";
    private static final String VIS_SCHEMA_SEPARATOR = ": ";

    @FXML
    private ChoiceBox<String> entity1Choice;

    @FXML
    private Rectangle entity1BasicBox;

    @FXML
    private Rectangle entity1WeakBox;

    @FXML
    private Label entity1Label;
    @FXML
    public Text k1Text;

    @FXML
    private ChoiceBox<String> entity2Choice;

    @FXML
    private Rectangle entity2BasicBox;

    @FXML
    private Rectangle entity2WeakBox;

    @FXML
    private Label entity2Label;

    @FXML
    public Text k2Text;

    @FXML
    public ChoiceBox<String> visSchemaChoice;

    @FXML
    private VBox addAttributesVBox;

    @FXML
    public HBox attributeHBoxTemplate;

    @FXML
    private Button genGraphsButton;

    @FXML
    public Text generateGraphErrorText;

    private EntityType selectedE1 = null;
    private EntityType selectedE2 = null;
    private VisSchemaPattern currVisPattern = null;
    private Relationship currRelationship = null;
    private Map<String, Attribute> currAtts = new HashMap<>();

    @FXML
    private void onGenGraphsButtonClick() {
        generateGraphErrorText.setText("");

        User user = ViewUtils.receiveData();
        ERModel rm = user.getERModel();


        GraphDetector gd = null;
        switch (currVisPattern) {
            case BASIC_ENTITY -> gd = GraphDetector.generateBasicPlans(rm, selectedE1, getCheckedAttributes());
            case WEAK_ENTITY -> gd = GraphDetector.generateWeakPlans((BinaryRelationship) currRelationship, getCheckedAttributes());
            case ONE_MANY_REL -> gd = GraphDetector.generateOneManyPlans((BinaryRelationship) currRelationship, getCheckedAttributes());
            case MANY_MANY_REL, REFLEXIVE -> gd = GraphDetector.generateManyManyPlans((NAryRelationship) currRelationship, getCheckedAttributes());
        }

        if (gd == null) {
            generateGraphErrorText.setText("No plots found for this selection.");
            return;
        }

        user.setGraphDetector(gd);
        ViewUtils.sendData(user);
        ViewUtils.switchTo(View.GRAPH_SELECT);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();
        ERModel rm = user.getERModel();

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
        }

        entity1BasicBox.setVisible(false);
        entity1WeakBox.setVisible(false);
        entity1Label.setVisible(false);

        entity2BasicBox.setVisible(false);
        entity2WeakBox.setVisible(false);
        entity2Label.setVisible(false);

        // Set Entity 1 Choice action
        entity1Choice.setOnAction(event -> {
            /* Disable */
            entity2Choice.getItems().clear();
            entity2Choice.setValue(null);
            entity2Choice.setDisable(true);
            entity2BasicBox.setVisible(false);
            entity2WeakBox.setVisible(false);
            entity2Label.setVisible(false);
            k2Text.setText(DEFAULT_LABEL);

            visSchemaChoice.getItems().clear();
            visSchemaChoice.setValue(null);
            visSchemaChoice.setDisable(true);
            clearAttributesVBox();
            genGraphsButton.setDisable(true);
            generateGraphErrorText.setText("");

            selectedE1 = null;
            selectedE2 = null;
            currRelationship = null;
            currVisPattern = null;
            k1Text.setText(DEFAULT_LABEL);

            /* New E1 selected */
            selectedE1 = rm.getEntity(entity1Choice.getValue());

            // Show entity image representation
            entity1BasicBox.setVisible(true);
            entity1WeakBox.setVisible(selectedE1 instanceof WeakEntityType);
            entity1Label.setText(selectedE1.getName());
            entity1Label.setVisible(true);

            // Show primary key
            Relation e1Rel = rm.getRelation(selectedE1.getName());
            k1Text.setText(e1Rel.getPrimaryKey().getAttributesRepresentation());

            /* Add Vis Schema Basic Entity Choice */
            visSchemaChoice.getItems().add(getVisSchemaChoiceName(VisSchemaPattern.BASIC_ENTITY, selectedE1.getName()));
            visSchemaChoice.setValue(visSchemaChoice.getItems().get(0));

            /* Add E2 choices */
            for (EntityType e2 : entities) {
                // TODO: could time save
                if (!checkForRelationships(rm, selectedE1, e2).isEmpty()) {
                    entity2Choice.getItems().add(e2.getName());
                }
            }

            // Add null choice for entity 2 (since it's not required)
            entity2Choice.getItems().add(NOT_SELECTED);
            entity2Choice.setValue(NOT_SELECTED);
            entity2Choice.setDisable(false);

            /* Enable */
            visSchemaChoice.setDisable(false);
        });

        // Set Entity 2 Choice action
        entity2Choice.setOnAction(event -> {
            String e2Value = entity2Choice.getValue();
            if (e2Value == null) {
                return;
            }

            /* Reset */
            selectedE2 = null;
            entity2BasicBox.setVisible(false);
            entity2WeakBox.setVisible(false);
            entity2Label.setVisible(false);
            k2Text.setText(DEFAULT_LABEL);

            genGraphsButton.setDisable(true);
            generateGraphErrorText.setText("");
            visSchemaChoice.getItems().clear();
            visSchemaChoice.setValue(null);
            visSchemaChoice.setDisable(true);
            clearAttributesVBox();

            currRelationship = null;
            currVisPattern = null;

            if (e2Value.equals(NOT_SELECTED)) {
                // Offer a basic entity relationship for e1
                visSchemaChoice.getItems().add(getVisSchemaChoiceName(VisSchemaPattern.BASIC_ENTITY, selectedE1.getName()));
                visSchemaChoice.setValue(visSchemaChoice.getItems().get(0));
                return;
            }

            /* New E1 selected */
            selectedE2 = rm.getEntity(entity2Choice.getValue());

            // Show entity image representation
            entity2BasicBox.setVisible(true);
            entity2WeakBox.setVisible(selectedE2 instanceof WeakEntityType);
            entity2Label.setText(selectedE2.getName());
            entity2Label.setVisible(true);

            // Show primary key
            Relation e2Rel = rm.getRelation(selectedE2.getName());
            k2Text.setText(e2Rel.getPrimaryKey().getAttributesRepresentation());

            /* Add Vis Schema Choices */
            if (selectedE1 != null) {
                setVisSchemaChoiceForTwoEntities(rm, selectedE1, selectedE2);
            }

            /* Enable */
            visSchemaChoice.setDisable(false);
        });

        visSchemaChoice.setOnAction(event -> {
            if (visSchemaChoice.getValue() == null) {
                return;
            }

            /* Reset */
            clearAttributesVBox();
            generateGraphErrorText.setText("");

            /* Get the chosen relationship and type */
            String[] visSchemaVals = visSchemaChoice.getValue().split(VIS_SCHEMA_SEPARATOR);
            if (visSchemaVals.length != 2) {
                // Something went wrong
                return;
            }

            currVisPattern = VisSchemaPattern.getVisSchemaPattern(visSchemaVals[0]);
            if (currVisPattern == null) {
                // Something went wrong
                return;
            }

            if (currVisPattern != VisSchemaPattern.BASIC_ENTITY) {
                currRelationship = rm.getRelationships().get(visSchemaVals[1]);
                if (currRelationship == null) {
                    // Something went wrong
                    return;
                }
            }

            /* Add appropriate attributes to addAttributes ChoiceBox */
            // TODO: also add inclusion relationship attributes
            addRelationToAttributesVBox(rm.getRelation(selectedE1.getName()));

            if (currVisPattern != VisSchemaPattern.BASIC_ENTITY) {
                addRelationToAttributesVBox(rm.getRelation(selectedE2.getName()));
            }

            addAttributesVBox.getChildren().remove(attributeHBoxTemplate);

            /* Enable */
            genGraphsButton.setDisable(false);
        });
    }

    private List<Attribute> getCheckedAttributes() {
        List<Attribute> selectedAtts = new ArrayList<>();
        for (Node child : addAttributesVBox.getChildren()) {
            if (!(child instanceof HBox attributeHBox) || child.equals(attributeHBoxTemplate)) {
                continue;
            }

            // Collect attribute info from HBox
            boolean checked = false;
            String attName = null;
            for (Node attChild : attributeHBox.getChildren()) {
                if (attChild instanceof Label attLabel) {
                    attName = attLabel.getText();
                } else if (attChild instanceof CheckBox attCheckBox) {
                    checked = attCheckBox.isSelected();
                }
            }

            if (checked) {
                selectedAtts.add(currAtts.get(attName));
            }
        }

        return selectedAtts;
    }

    private void clearAttributesVBox() {
        currAtts = new HashMap<>();
        addAttributesVBox.getChildren().clear();
        addAttributesVBox.getChildren().add(attributeHBoxTemplate);
    }

    private String getAttributeChoiceName(Attribute att) {
        return att.toString() + " (" + att.getDBType().name() + ")";
    }

    private void addRelationToAttributesVBox(Relation rel) {
//        rel.getPrimaryKeySet().forEach(att -> {
//            addAttributeToVBox(att);
//            currAtts.put(getAttributeChoiceName(att), att);
//        });
        rel.getOtherAttributes().forEach(att -> {
            addAttributeToVBox(att);
            currAtts.put(getAttributeChoiceName(att), att);
        });
    }

    private void addAttributeToVBox(Attribute att) {
        HBox newAttHBox = new HBox();
        newAttHBox.setAlignment(attributeHBoxTemplate.getAlignment());
        newAttHBox.setSpacing(attributeHBoxTemplate.getSpacing());
        newAttHBox.setPadding(attributeHBoxTemplate.getPadding());

        // Copy the children of the template
        ObservableList<Node> children = attributeHBoxTemplate.getChildren();
        for (Node child : children) {
            if (child instanceof Label template) {
                // Label case:
                Label dupLabel = new Label();
                dupLabel.setText(getAttributeChoiceName(att));

                dupLabel.getStyleClass().clear();
                dupLabel.getStyleClass().add(0, template.getStyleClass().get(0));

                newAttHBox.getChildren().add(dupLabel);
            } else if (child instanceof CheckBox) {
                // CheckBox case:
                newAttHBox.getChildren().add(2, new CheckBox());

            } else if (child instanceof Region template) {
                // Middle Region case:
                Region dupRegion = new Region();
                HBox.setHgrow(dupRegion, HBox.getHgrow(template));
                newAttHBox.getChildren().add(1, dupRegion);
            }
        }

        addAttributesVBox.getChildren().add(newAttHBox);
    }

    private String getVisSchemaChoiceName(VisSchemaPattern pattern, String name) {
        return pattern.getName() + VIS_SCHEMA_SEPARATOR + name;
    }

    private void setVisSchemaChoiceForTwoEntities(ERModel rm, EntityType e1, EntityType e2) {
        List<Relationship> relationships = checkForRelationships(rm, e1, e2);

        for (Relationship rel : relationships) {
            if (rel instanceof BinaryRelationship) {
                // Check for weak relationship
                if (((BinaryRelationship) rel).isWeakRelationship(rm)) {
                    visSchemaChoice.getItems()
                            .add(getVisSchemaChoiceName(VisSchemaPattern.WEAK_ENTITY, rel.getName()));

                } else {
                    visSchemaChoice.getItems()
                            .add(getVisSchemaChoiceName(VisSchemaPattern.ONE_MANY_REL, rel.getName()));
                }
            } else if (rel instanceof NAryRelationship) {
                // Check for reflexive relationship
                if (((NAryRelationship) rel).isReflexive()) {
                    visSchemaChoice.getItems().add(getVisSchemaChoiceName(VisSchemaPattern.REFLEXIVE, rel.getName()));
                } else {
                    visSchemaChoice.getItems().add(getVisSchemaChoiceName(VisSchemaPattern.MANY_MANY_REL, rel.getName()));
                }
            }

            // Nothing here for Inclusion Relationships

            if (visSchemaChoice.getItems().isEmpty()) {
                visSchemaChoice.setDisable(true);
            } else {
                visSchemaChoice.setValue(visSchemaChoice.getItems().get(0));
            }
        }
    }

    private List<Relationship> checkForRelationships(ERModel rm, EntityType e1, EntityType e2) {
        Relation e1Rel = rm.getRelation(e1.getName());
        Relation e2Rel = rm.getRelation(e2.getName());

        // TODO: also check for relationships which have an inclusion 1-1 relationship
        List<Relationship> relationships = new ArrayList<>();

        // Check weak or binary relationship
        BinaryRelationship br1 = rm.getBinaryRelationship(e1Rel, e2Rel);
        if (br1 != null) {
            relationships.add(br1);
        }

        BinaryRelationship br2 = rm.getBinaryRelationship(e2Rel, e1Rel);
        if (br2 != null) {
            relationships.add(br2);
        }

        // Check for n-ary relationship
        NAryRelationship nAry = rm.getNAryRelationship(e1Rel, e2Rel);
        if (nAry == null) {
            nAry = rm.getNAryRelationship(e2Rel, e1Rel);
        }

        if (nAry != null) {
            relationships.add(nAry);
        }

        return relationships;
    }
}
