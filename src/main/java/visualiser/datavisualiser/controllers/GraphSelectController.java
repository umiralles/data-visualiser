package visualiser.datavisualiser.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class GraphSelectController implements Initializable {

    private static final String NOT_SELECTED = "N/A";

    @FXML
    private ChoiceBox<String> graphChoice;

    @FXML
    private WebView graphWebView;

    @FXML
    public VBox attTypesVBox;

    /* Templates for attributes choices in the attributesVBox */
    @FXML
    public HBox attributeHBoxTemplate;

    @FXML
    public Label typeLabelTemplate;

    @FXML
    public ChoiceBox<String> typeChoiceBoxTemplate;

    @FXML
    public Text graphErrorText;

    // Plans that are of the graph type selected
    private List<GraphPlan> chosenPlans = null;

    // Plan being displayed
    private GraphPlan chosenPlan = null;

    private final Map<String, Attribute> attributeMap = new HashMap<>();

    @FXML
    public void onHomeButtonClick() {
        /* Reset user */
        ViewUtils.sendData(new User());
        ViewUtils.switchTo(View.HOME);
    }

    @FXML
    public void onModelSelectButtonClick() {
        User user = ViewUtils.receiveData();
//        user.setGraphDetector(null);
//        user.setVisSchemaPattern(null);
//        user.setRelationship(null);
//        ViewUtils.sendData(user);
//        ViewUtils.switchTo(View.DATA_CHOOSE_MODEL);

        Set<GraphPlan> pls = user.getGraphDetector().getPlans().get("Bubble Chart");

        updateShownGraph(user.getERModel(), user.getGraphDetector(), pls.stream().findFirst().get());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();

        GraphDetector gd = user.getGraphDetector();
        if (gd == null) {
            // TODO: error
            return;
        }

        Map<String, Set<GraphPlan>> plans = gd.getPlans();
        for (String planType : plans.keySet()) {
            if (plans.get(planType) == null || plans.get(planType).isEmpty()) {
                continue;
            }

            graphChoice.getItems().add(planType);
        }

        /* Load first plan */
        ERModel rm = user.getERModel();
        graphChoice.setOnAction(event -> {
            /* Reset */
            clearAttTypesVBox();

            chosenPlans = new ArrayList<>(plans.get(graphChoice.getValue()));

            /* Set new graph */
            updateShownGraph(rm, gd, chosenPlans.get(0));

            // Add each type to side menu
            List<GraphAttribute> orderedAtts = chosenPlans.get(0).getAllOrderedAttributes();
            for (int i = 0; i < orderedAtts.size(); i++) {
                addAttTypeToVBox(orderedAtts.get(i), i);
            }
            attTypesVBox.getChildren().remove(attributeHBoxTemplate);
        });

        graphChoice.setValue(graphChoice.getItems().get(0));
    }

    private List<Attribute> getSelectedAttributes() {
        List<Attribute> chosenAtts = new ArrayList<>();
        for (Node typeNode : attTypesVBox.getChildren()) {
            if (!(typeNode instanceof HBox typeHBox)) {
                continue;
            }

            for (Node child : typeHBox.getChildren()) {
                if (!(child instanceof ChoiceBox<?> choiceBox)) {
                    continue;
                }

                String attStr = (String) choiceBox.getValue();
                if (attStr.equals(NOT_SELECTED)) {
                    chosenAtts.add(null);
                    continue;
                }

                Attribute chosenAtt = attributeMap.get(attStr);
                chosenAtts.add(chosenAtt);
            }
        }

        return chosenAtts;
    }

    private void clearAttTypesVBox() {
        attTypesVBox.getChildren().clear();
        attTypesVBox.getChildren().add(attributeHBoxTemplate);
    }

    private void addAttTypeToVBox(GraphAttribute gAtt, Integer typeIdx) {
        HBox typeBox = new HBox();
        typeBox.setAlignment(attributeHBoxTemplate.getAlignment());
        typeBox.setSpacing(attributeHBoxTemplate.getSpacing());
        typeBox.setPadding(attributeHBoxTemplate.getPadding());
        VBox.setMargin(typeBox, VBox.getMargin(attributeHBoxTemplate));

        // Copy the children from the original HBox
        ObservableList<Node> children = attributeHBoxTemplate.getChildren();
        for (Node child : children) {
            if (child instanceof Label template) {
                // Label case:
                Label dupLabel = new Label();
                dupLabel.setText(gAtt.typeInGraph().name().toLowerCase() + ":");

                dupLabel.getStyleClass().clear();
                dupLabel.getStyleClass().add(template.getStyleClass().get(0));

                dupLabel.setMinWidth(template.getMinWidth());
                dupLabel.setMaxWidth(template.getMaxWidth());
                HBox.setMargin(dupLabel, HBox.getMargin(template));

                typeBox.getChildren().add(0, dupLabel);

            } else if (child instanceof ChoiceBox<?> template) {
                // ChoiceBox case:
                ChoiceBox<String> dupChoice = new ChoiceBox<>();
                HBox.setMargin(dupChoice, HBox.getMargin(template));

                Set<Attribute> possAtts = new HashSet<>();
                for (GraphPlan plan : chosenPlans) {
                    possAtts.add(plan.getAllOrderedAttributes().get(typeIdx).attribute());
                }

                if (gAtt.optional()) {
                    dupChoice.getItems().add(NOT_SELECTED);
                    possAtts.remove(null);
                }

                // Add to choices and to attribute map
                possAtts.forEach(att -> {
                    attributeMap.put(att.toString(), att);
                    dupChoice.getItems().add(att.toString());
                });

                if (gAtt.attribute() == null) {
                    dupChoice.setValue(NOT_SELECTED);
                } else {
                    dupChoice.setValue(gAtt.attribute().toString());
                }

                dupChoice.setOnAction(this::updateGraphFromChoice);

                typeBox.getChildren().add(1, dupChoice);

            } else if (child instanceof Text asterisk && asterisk.getText().equals("*") && !gAtt.optional()) {
                Text dupAsterisk = new Text("*");
                HBox.setMargin(dupAsterisk, HBox.getMargin(asterisk));

                dupAsterisk.getStyleClass().clear();
                dupAsterisk.getStyleClass().add(asterisk.getStyleClass().get(0));

                typeBox.getChildren().add(2, dupAsterisk);
            }
        }

        attTypesVBox.getChildren().add(typeBox);
    }

    private void updateShownGraph(ERModel rm, GraphDetector gd, GraphPlan chosenPlan) {
        this.chosenPlan = chosenPlan;

        try {
            Chart chart = chosenPlan.getChart(gd.getData(rm));
            if (chart == null) {
                graphErrorText.setText("This graph plan has no supported graph.");
                return;
            }

            chart.setSize(690, 415);
//            chart.testChart();
            chart.showChart(graphWebView);

        } catch (SQLException e) {
            System.out.println("GraphSelectController.updateShownGraph: " + e.getMessage());
        }
    }

    private void updateGraphFromChoice(ActionEvent actionEvent) {
        /* Find a plan matching the chosen options */
        List<Attribute> chosenAtts = getSelectedAttributes();
        List<GraphPlan> possiblePlans = new ArrayList<>(chosenPlans);
        for (int i = 0; i < chosenAtts.size(); i++) {
            int typeIdx = i;
            Attribute att = chosenAtts.get(i);

            if (att == null) {
                possiblePlans = possiblePlans.stream()
                        .filter(plan -> plan.getAllOrderedAttributes().get(typeIdx).optional())
                        .toList();
                continue;
            }

            possiblePlans = possiblePlans.stream()
                    .filter(plan -> att.equals(plan.getAllOrderedAttributes().get(typeIdx).attribute()))
                    .toList();
        }

        Optional<GraphPlan> optPlan = possiblePlans.stream().findFirst();
        if (optPlan.isEmpty()) {
            // TODO: load ... page
        } else {
            User user = ViewUtils.receiveData();
            updateShownGraph(user.getERModel(), user.getGraphDetector(), optPlan.get());
        }
    }
}
