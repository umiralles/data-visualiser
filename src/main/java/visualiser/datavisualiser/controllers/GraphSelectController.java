package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.InputAttribute;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;
import visualiser.datavisualiser.models.RelationalModel.Relationships.Relationship;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.util.*;

public class GraphSelectController implements Initializable {
    @FXML
    private ChoiceBox<String> graphChoice;

    @FXML
    private ChoiceBox<String> implementationChoice;

    @FXML
    private Button generateButton;

    GraphDetector gd = null;

    Map<String, GraphPlan> chosenPlans = null;

    private GraphPlan chosenPlan = null;

    @FXML
    public void onGenerateButtonClick() {
        User user = ViewUtils.receiveData();
        user.setGraphDetector(gd);
        user.setPlan(chosenPlan);
        ViewUtils.sendData(user);
        ViewUtils.switchTo(View.DATA_VIS);
    }

    @FXML
    public void onHomeButtonClick() {
        ViewUtils.switchTo(View.HOME);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();

        ERModel rm = user.getRelationalModel();
        Relationship r = user.getRelationship();
        PrimaryAttribute k1 = user.getK1();
        PrimaryAttribute k2 = user.getK2();
//        List<Attribute> atts = user.getAttributes();

        /* TODO: FOR TESTING */
        Relation city = rm.getRelation("city");
        Attribute elevation = city.findAttribute("elevation");
        Attribute population = city.findAttribute("population");
        List<Attribute> atts = List.of(elevation, population);

        List<InputAttribute> inpAtts = atts.stream().map(InputAttribute::new).toList();

        switch (user.getVisSchemaPattern()) {
            case BASIC_ENTITY -> {
                gd = GraphDetector.generateBasicPlans(rm, new InputAttribute(k1), inpAtts);
            }
            case WEAK_ENTITY -> {
                // Possibly not the right way round?
                gd = GraphDetector.generateWeakPlans(rm, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case ONE_MANY_REL -> {
                gd = GraphDetector.generateOneManyPlans(rm, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case MANY_MANY_REL -> {
                gd = GraphDetector.generateManyManyPlans(rm, false, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
            }
            case REFLEXIVE -> {
                gd = GraphDetector.generateManyManyPlans(rm, true, new InputAttribute(k1), new InputAttribute(k2), inpAtts);
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
            chosenPlans = new HashMap<>();
            implementationChoice.getItems().clear();

            for (GraphPlan plan : plans.get(graphChoice.getValue())) {
                chosenPlans.put(plan.getOrderedAttributesRepresentation(), plan);
                implementationChoice.getItems().add(plan.getOrderedAttributesRepresentation());
            }

            implementationChoice.setDisable(false);
        });

        implementationChoice.setOnAction(event -> {
            chosenPlan = chosenPlans.get(implementationChoice.getValue());
            generateButton.setDisable(false);
        });
    }
}
