package visualiser.datavisualiser.models.GraphDetectorA;

import visualiser.datavisualiser.models.GraphDetectorA.GraphPlans.BasicGraphPlan;
import visualiser.datavisualiser.models.GraphDetectorA.GraphPlans.BasicGraphType;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.*;

public class BasicGraphDetector extends GraphDetector {

    private final Map<BasicGraphType, Set<BasicGraphPlan>> graphPlans;

    private BasicGraphDetector(String query, Map<BasicGraphType, Set<BasicGraphPlan>> graphPlans) {
        super(query);
        this.graphPlans = graphPlans;
    }

    public Map<BasicGraphType, Set<BasicGraphPlan>> getGraphPlans() {
        return graphPlans;
    }

    /* FOR A BASIC ENTITY */
    public static BasicGraphDetector getBasicEntityQueryAndGraphTypes(ERModel rm,
                                                                      String entityName, String keyAttName,
                                                                      List<String> attributes) throws IllegalArgumentException {
        // k:       key attribute of the entity relation
        // atts:    attributes of the entity relation
        if (attributes.size() < 1) {
            throw new IllegalArgumentException("GraphDetector.getBasicEntityQueryAndGraphTypes: no attributes specified for entity " + entityName);
        }

        Relation rel = rm.getRelation(entityName);
        if (rel == null || !rel.isEntityRelation()) {
            throw new IllegalArgumentException("GraphDetector.getBasicEntityQueryAndGraphTypes: entity " + entityName + " not found");
        }

        Attribute k = rel.findInPrimaryKey(keyAttName);
        if (k == null) {
            throw new IllegalArgumentException("GraphDetector.getBasicEntityQueryAndGraphTypes: attribute " + keyAttName
                    + " is not a primary attribute of " + entityName);
        }

        ArrayList<Attribute> atts = rel.findAttributes(attributes);
        if (atts == null) {
            throw new IllegalArgumentException("GraphDetector.getBasicEntityQueryAndGraphTypes: attributes for "
                    + entityName + " were not found in the relation " + rel.getName());
        }

        // Match attTypes with BasicGraphType enums
        Map<BasicGraphType, Set<BasicGraphPlan>> graphPlans = BasicGraphPlan.matchToGraphs(k, atts);

        /* GENERATE QUERY */
        Set<String> allEntityAttsNames = new HashSet<>(attributes);
        allEntityAttsNames.add(keyAttName);
        String query = generateBasicEntityQuery(rm, entityName, allEntityAttsNames);

        return new BasicGraphDetector(query, graphPlans);
    }

    private static String generateBasicEntityQuery(ERModel rm, String table, Set<String> columns) {
        assert columns.size() > 0;

        ArrayList<String> colsArr = new ArrayList<>(columns);

        StringBuilder q = new StringBuilder("SELECT ");
        q.append(colsArr.get(0));

        for (int i = 1; i < colsArr.size(); i++) {
            q.append(", ");
            q.append(colsArr.get(i));
        }

        q.append(" FROM ").append(rm.getSchemaPattern()).append(".").append(table);

        return q.toString();
    }

    // TODO: Restrictions
    private String generateBasicEntityQuery(ERModel rm, String table, Set<String> columns, String restrictions) {
        return generateBasicEntityQuery(rm, table, columns) + " " + restrictions;
    }
}
