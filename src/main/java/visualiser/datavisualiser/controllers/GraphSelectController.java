package visualiser.datavisualiser.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.InputAttribute;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.*;

public class GraphSelectController implements Initializable {

    @FXML
    private ChoiceBox<String> graphChoice;

    @FXML
    public VBox attributesVBox;

    /* Templates for attributes choices in the attributesVBox */
    @FXML
    public HBox attributeHBoxTemplate;

    @FXML
    public Label typeLabelTemplate;

    @FXML
    public ChoiceBox<String> typeChoiceBoxTemplate;

    private GraphDetector gd = null;

    private List<GraphPlan> chosenPlans = null;

    private GraphPlan chosenPlan = null;

    @FXML
    public void onHomeButtonClick() {
        ViewUtils.switchTo(View.HOME);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();

        ERModel rm = user.getRelationalModel();
        Relationship r = user.getRelationship();
//        List<Attribute> atts = user.getAttributes();

        /* TODO: FOR TESTING */
        Relation city = rm.getRelation("city");
        Attribute elevation = city.findAttribute("elevation");
        Attribute population = city.findAttribute("population");
        List<Attribute> atts = List.of(elevation, population);

        List<InputAttribute> inpAtts = atts.stream().map(InputAttribute::new).toList();

        switch (user.getVisSchemaPattern()) {
            case BASIC_ENTITY -> {
//                gd = GraphDetector.generateBasicPlans(rm, new InputAttribute(k1), inpAtts);
            }
            case WEAK_ENTITY -> {
                // Possibly not the right way round?
//                gd = GraphDetector.generateWeakPlans(rm, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case ONE_MANY_REL -> {
//                gd = GraphDetector.generateOneManyPlans(rm, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case MANY_MANY_REL -> {
//                gd = GraphDetector.generateManyManyPlans(rm, false, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case REFLEXIVE -> {
//                gd = GraphDetector.generateManyManyPlans(rm, true, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
        }

        if (gd == null) {
            // TODO: no graphs
            return;
        }

        Map<String, Set<GraphPlan>> plans = gd.getPlans();
        for (String planType : plans.keySet()) {
            if (plans.get(planType) == null  || plans.get(planType).isEmpty()) {
                continue;
            }

            graphChoice.getItems().add(planType);
        }

        graphChoice.setOnAction(event -> {
            chosenPlans = new ArrayList<>(plans.get(graphChoice.getValue()));
            chosenPlan = chosenPlans.get(0);

            // TODO:
//            List<AttributeType> orderedTypes = chosenPlan.getOrderedAttributeTypes();
//            List<Attribute> orderedAtts = chosenPlan.getOrderedAttributes();
            List<AttributeType> orderedTypes = List.of();
            List<Attribute> orderedAtts = List.of();

            for (int i = 0; i < orderedTypes.size(); i++) {
                AttributeType type = orderedTypes.get(i);
                Attribute attribute = orderedAtts.get(i);

                HBox typeBox = new HBox();
                typeBox.setAlignment(attributeHBoxTemplate.getAlignment());
                typeBox.setSpacing(attributeHBoxTemplate.getSpacing());

                // Copy the children from the original HBox
                ObservableList<Node> children = attributeHBoxTemplate.getChildren();
                for (Node child : children) {
                    if (child instanceof Label template) {
                        // Label case:
                        Label dupLabel = new Label();
                        dupLabel.setText(type.name() + ":");

                        dupLabel.getStyleClass().clear();
                        dupLabel.getStyleClass().add(template.getStyleClass().get(0));

                        HBox.setMargin(dupLabel, HBox.getMargin(template));

                        typeBox.getChildren().add(0, dupLabel);

                    } else if (child instanceof ChoiceBox<?> template) {
                        // ChoiceBox case:
                        ChoiceBox<String> dupChoice = new ChoiceBox<>();

                        HBox.setMargin(dupChoice, HBox.getMargin(template));

                        typeBox.getChildren().add(1, dupChoice);
                    }
                }


                attributesVBox.getChildren().add(typeBox);
            }
//
//            for (AttributeType mand : chosenPlan.)
        });
    }
}
