package visualiser.datavisualiser.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.Charts.Chart;
import visualiser.datavisualiser.models.Charts.GoogleCharts.GoogleTreeMap;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans.BasicGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans.ManyManyGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans.OneManyGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.WeakGraphPlans.WeakGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;
import visualiser.datavisualiser.models.User;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class GraphSelectController implements Initializable {

    private static final String NOT_SELECTED = "N/A";
    private static final String KEY_ONE = "Key 1";
    private static final String KEY_TWO = "Key 2";
    private static final String ASCENDING = "Ascending";
    private static final String DESCENDING = "Descending";

    @FXML
    private ChoiceBox<String> graphChoice;

    @FXML
    private WebView graphWebView;

    @FXML
    public Text k1AttsText;

    @FXML
    public HBox k2AttsHBox;

    @FXML
    public Text k2AttsText;

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
    public ChoiceBox<String> limitAttChoice;

    @FXML
    public ChoiceBox<String> limitOrderChoice;

    @FXML
    public TextField limitSet1TextField;

    @FXML
    public TextField limitSet2TextField;

    @FXML
    public Text graphErrorText;

    private final Map<String, Attribute> attributeMap = new HashMap<>();

    // Plans that are of the graph type selected
    private List<GraphPlan> chosenPlans = null;

    // Plan being displayed
    private GraphPlan chosenPlan = null;

    private ChangeListener<? super Worker.State> chartListener = null;

    @FXML
    public void onHomeButtonClick() {
        /* Reset user */
        ViewUtils.sendData(new User());
        ViewUtils.switchTo(View.HOME);
    }

    @FXML
    public void onModelSelectButtonClick() {
        User user = ViewUtils.receiveData();
        user.setGraphDetector(null);
        user.setVisSchemaPattern(null);
        user.setRelationship(null);
        ViewUtils.sendData(user);
        ViewUtils.switchTo(View.DATA_CHOOSE_MODEL);

//        Set<GraphPlan> pls = user.getGraphDetector().getPlans().get("Bubble Chart");
//
//        updateShownGraph(user.getERModel(), user.getGraphDetector(), pls.stream().findFirst().get());
    }

    @FXML
    public void onLimitSetButtonClick() {
        User user = ViewUtils.receiveData();
        reDisplayChosenPlan(user.getERModel(), user.getGraphDetector());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = ViewUtils.receiveData();

        GraphDetector gd = user.getGraphDetector();
        if (gd == null) {
            // TODO: error
            return;
        }

        /* LIMITS */
        // Set limit attribute choice boxes
        for (Attribute additionalAtt : gd.getAttributes()) {
            limitAttChoice.getItems().add(additionalAtt.toString());
        }

        limitAttChoice.getItems().add(KEY_ONE);
        switch (user.getVisSchemaPattern()) {
            case WEAK_ENTITY, ONE_MANY_REL, MANY_MANY_REL, REFLEXIVE -> limitAttChoice.getItems().add(KEY_TWO);
        }

        limitAttChoice.setValue(limitAttChoice.getItems().get(0));

        // Set limit order choice boxes
        limitOrderChoice.getItems().add(ASCENDING);
        limitOrderChoice.getItems().add(DESCENDING);
        limitOrderChoice.setValue(limitOrderChoice.getItems().get(0));

        /* KEYS */
        // Set key text boxes
        switch (user.getVisSchemaPattern()) {
            case BASIC_ENTITY -> {
                k2AttsHBox.setVisible(false);
                limitSet2TextField.setVisible(false);
                List<PrimaryAttribute> keyAtts = new ArrayList<>(user.getERModel()
                        .getRelation(gd.getEntity().getName()).getPrimaryKey().getPAttributes());
                k1AttsText.setText(attsToString(keyAtts));
            }
            case WEAK_ENTITY, ONE_MANY_REL, MANY_MANY_REL, REFLEXIVE -> {
                List<Attribute> k1Atts = new ArrayList<>(gd.getRelationship().getB().getPrimaryKeySet());
                List<Attribute> k2Atts = new ArrayList<>(gd.getRelationship().getA().getPrimaryKeySet());

                if (user.getVisSchemaPattern() == VisSchemaPattern.WEAK_ENTITY) {
                    List<List<Attribute>> sharedPAtts = gd.getRelationship().getB().getPrimaryKey().sharedAttributes(new HashSet<>(k2Atts));
                    k2Atts.removeAll(sharedPAtts.get(1));
                }

                k1AttsText.setText(attsToString(k1Atts));
                k2AttsText.setText(attsToString(k2Atts));
            }
        }

        /* PLAN TYPES */
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
            graphErrorText.setText("");

            chosenPlans = new ArrayList<>(plans.get(graphChoice.getValue()));

            /* Set new graph */
            this.chosenPlan = chosenPlans.get(0);
            updateGraphPlanType(rm, gd);

            // Add each type to side menu
            List<GraphAttribute> orderedAtts = this.chosenPlan.getAllOrderedAttributes();
            for (int i = 0; i < orderedAtts.size(); i++) {
                addAttTypeToVBox(orderedAtts.get(i), i);
            }
            attTypesVBox.getChildren().remove(attributeHBoxTemplate);
        });

        graphChoice.setValue(graphChoice.getItems().get(0));
    }

    private String attsToString(List<? extends Attribute> pAtts) {
        StringBuilder keyStr = new StringBuilder(pAtts.get(0).toString());
        for (int i = 1; i < pAtts.size(); i++) {
            keyStr.append(" + ").append(pAtts.get(i).getColumn());
        }

        return keyStr.toString();
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
                dupLabel.setAlignment(template.getAlignment());

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

    private void updateGraphPlanType(ERModel rm, GraphDetector gd) {
        // Set limit defaults
        if (this.chosenPlan instanceof BasicGraphPlan basicPlan) {
            limitSet1TextField.setText(String.valueOf(basicPlan.getKUpperLim()));
            limitSet2TextField.setText(String.valueOf(-1));
        } else if (this.chosenPlan instanceof WeakGraphPlan weakPlan) {
            limitSet1TextField.setText(String.valueOf(weakPlan.getK1UpperLim()));
            limitSet2TextField.setText(String.valueOf(weakPlan.getK2UpperLim()));
        } else if (this.chosenPlan instanceof OneManyGraphPlan oneManyPlan) {
            limitSet1TextField.setText(String.valueOf(oneManyPlan.getK1UpperLim()));
            limitSet2TextField.setText(String.valueOf(oneManyPlan.getK2PerK1UpperLim()));
        } else if (this.chosenPlan instanceof ManyManyGraphPlan manyManyPlan) {
            limitSet1TextField.setText(String.valueOf(manyManyPlan.getK1UpperLim()));
            limitSet2TextField.setText(String.valueOf(manyManyPlan.getK2UpperLim()));
        }

        reDisplayChosenPlan(rm, gd);
    }

    private void reDisplayChosenPlan(ERModel rm, GraphDetector gd) {
        Comparator<? super DataCell> limitComparator = null;
        switch (limitOrderChoice.getValue()) {
            case ASCENDING -> limitComparator = Comparator.naturalOrder();
            case DESCENDING -> limitComparator = Comparator.reverseOrder();
        }

        try {
            String compareAttId = limitAttChoice.getValue();
            if (compareAttId.equals(KEY_ONE)) {
                if (chosenPlan instanceof BasicGraphPlan basicPlan) {
                    compareAttId = basicPlan.getK1().toString();
                } else if (chosenPlan instanceof WeakGraphPlan weakPlan) {
                    compareAttId = weakPlan.getOwnerKey().toString();
                } else if (chosenPlan instanceof OneManyGraphPlan oneManyPlan) {
                    compareAttId = oneManyPlan.getParentKey().toString();
                } else if (chosenPlan instanceof ManyManyGraphPlan manyManyPlan) {
                    compareAttId = manyManyPlan.getK1().toString();
                }
            } else if (compareAttId.equals(KEY_TWO)) {
                if (chosenPlan instanceof WeakGraphPlan weakPlan) {
                    compareAttId = weakPlan.getWeakKey().toString();
                } else if (chosenPlan instanceof OneManyGraphPlan oneManyPlan) {
                    compareAttId = oneManyPlan.getChildKey().toString();
                } else if (chosenPlan instanceof ManyManyGraphPlan manyManyPlan) {
                    compareAttId = manyManyPlan.getK2().toString();
                }
            }

            Chart chart = chosenPlan.getChart(gd.getData(rm, Integer.parseInt(limitSet1TextField.getText()),
                    Integer.parseInt(limitSet2TextField.getText()), compareAttId, limitComparator));
            if (chart == null) {
                graphErrorText.setText("This graph plan has no supported graph visualiser.");
                return;
            }

            chart.setSize(690, 400);
//            chart.testChart();
            this.chartListener = chart.showChart(graphWebView, chartListener);

            if (chart instanceof GoogleTreeMap) {
                graphErrorText.setText("(use right click to go up in depth)");
            }

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
            this.chosenPlan = optPlan.get();
            updateGraphPlanType(user.getERModel(), user.getGraphDetector());
        }
    }
}
