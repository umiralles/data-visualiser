package visualiser.datavisualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import visualiser.datavisualiser.View;
import visualiser.datavisualiser.ViewUtils;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Entities.StrongEntityType;
import visualiser.datavisualiser.models.ERModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.ERModel.Relations.RelationType;
import visualiser.datavisualiser.models.ERModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.InclusionRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.NAryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;
import visualiser.datavisualiser.models.User;

import java.util.ArrayList;
import java.util.Comparator;
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


//        // TODO: testing
//        HashSet<InclusionDependency> ids = rm.getIds();
//        HashSet<InclusionDependency> coveredIds = rm.getIds().stream().filter(InclusionDependency::isCovered).collect(Collectors.toCollection(HashSet::new));
//        ArrayList<String> coveredIdsStr = coveredIds.stream()
//                .map(id -> id.getA().getName() + "." + id.getX1().getColumn() + " < " + id.getB().getName() + "." + id.getX2().getColumn())
//                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Relation> strong = rm.getRelations().values().stream().filter(rel -> rel.getType() == RelationType.STRONG)
                .sorted(Comparator.comparing(Relation::getName)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Relation> weak = rm.getRelations().values().stream().filter(rel -> rel.getType() == RelationType.WEAK)
                .sorted(Comparator.comparing(Relation::getName)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Relation> specific = rm.getRelations().values().stream().filter(rel -> rel.getType() == RelationType.SPECIFIC)
                .sorted(Comparator.comparing(Relation::getName)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Relation> regular = rm.getRelations().values().stream().filter(rel -> rel.getType() == RelationType.REGULAR)
                .sorted(Comparator.comparing(Relation::getName)).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<EntityType> strEntities = rm.getEntities().values().stream().filter(ent -> ent instanceof StrongEntityType)
                .sorted(Comparator.comparing(EntityType::getName)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<EntityType> weEntities = rm.getEntities().values().stream().filter(ent -> ent instanceof WeakEntityType)
                .sorted(Comparator.comparing(EntityType::getName)).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<InclusionRelationship> incRelationships = rm.getRelationships().values().stream().filter(ent -> ent instanceof InclusionRelationship)
                .map(ent -> (InclusionRelationship) ent)
                .sorted(Comparator.comparing(InclusionRelationship::printIsA)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<BinaryRelationship> binRelationships = rm.getRelationships().values().stream().filter(ent -> ent instanceof BinaryRelationship)
                .map(ent -> (BinaryRelationship) ent)
                .sorted(Comparator.comparing((Relationship rel) -> rel.getA().getName())).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<NAryRelationship> nAryRelationships = rm.getRelationships().values().stream().filter(ent -> ent instanceof NAryRelationship)
                .map(ent -> (NAryRelationship) ent)
                .sorted(Comparator.comparing((Relationship rel) -> rel.getB().getName())).collect(Collectors.toCollection(ArrayList::new));

        // String representations
        ArrayList<String> strongStr = strong.stream().map(Relation::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> weakStr = weak.stream().map(Relation::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> specificStr = specific.stream().map(Relation::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> regularStr = regular.stream().map(Relation::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> strongEntStr = strEntities.stream().map(EntityType::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> weakEntStr = weEntities.stream().map(EntityType::getName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> incRelStr = incRelationships.stream()
                .map(ir -> ir.getA().getName() + " " + ir.printIsA() + " " + ir.getB().getName())
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> binRelStr = binRelationships.stream()
                .map(br -> "(1/0:1) " + br.getA().getName() + " < " + br.getB().getName() + " (0:N)")
                .collect(Collectors.toCollection(ArrayList::new));
//            ArrayList<String> nAryRelStr = nAryRelationships.stream().map(ir -> ir.getA().getName() + " ~ " + ir.getB().getName())
//                    .sorted().collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> nAryRelStr = nAryRelationships.stream()
                .map(nar -> nar.getA().getName() + " " + nar.getName().toUpperCase() + " " + nar.getB().getName())
                .collect(Collectors.toCollection(ArrayList::new));

        User user = new User();
        user.setERModel(rm);
        ViewUtils.sendData(user);

        ViewUtils.switchTo(View.DATA_SELECT);
    }

    private ERModel processDatabase(String url, String username, String password, String schema) {
        // TODO: remove this
        if (url.isBlank()) {
            url = "jdbc:postgresql://localhost/mondial_plus_extra_2";
            username = "postgres";
            password = "post";
            schema = "public";
        }

        return new ERModel(username, password, url, schema);
    }
}
