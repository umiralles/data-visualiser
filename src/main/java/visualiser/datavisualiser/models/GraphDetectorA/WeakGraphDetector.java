package visualiser.datavisualiser.models.GraphDetectorA;

import visualiser.datavisualiser.models.GraphDetectorA.GraphPlans.WeakGraphPlan;
import visualiser.datavisualiser.models.GraphDetectorA.GraphPlans.WeakGraphType;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.*;

public class WeakGraphDetector extends GraphDetector {
    private final Map<WeakGraphType, Set<WeakGraphPlan>> graphPlans;

    private WeakGraphDetector(String query, Map<WeakGraphType, Set<WeakGraphPlan>> graphPlans) {
        super(query);
        this.graphPlans = graphPlans;
    }

    public Map<WeakGraphType, Set<WeakGraphPlan>> getGraphPlans() {
        return graphPlans;
    }

    /* FOR A WEAK ENTITY */
    public static WeakGraphDetector getWeakEntityQueryAndGraphTypes(ERModel rm,
                                                                    String attachedEntityName, String key1AttName,
                                                                    String weakEntityName, String key2AttName,
                                                                    List<String> attributes) throws IllegalArgumentException {
        // k1:      key attribute of the relation attached to the weak entity
        // k2:      key attribute of the weak relation
        // atts:    attributes of the weak relation
        // TODO: put checks into function in WeakGraphPlans?
        if (attributes.size() < 1) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: no attributes specified for entity " + weakEntityName);
        }

        Relation attachedRel = rm.getRelation(attachedEntityName);
        if (attachedRel == null || !attachedRel.isEntityRelation()) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: entity " + attachedEntityName + " not found");
        }
        Attribute k1 = attachedRel.findAttribute(key1AttName);
        if (k1 == null) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: attribute " + key1AttName
                    + " is not an attribute of " + attachedEntityName);
        }

        Relation weakRel = rm.getRelation(weakEntityName);
        if (weakRel == null || !attachedRel.isEntityRelation()) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: entity " + weakEntityName + " not found");
        }
        Attribute k2 = weakRel.findInPrimaryKey(key2AttName);
        if (k2 == null) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: attribute " + key2AttName
                    + " is not an attribute of " + weakEntityName);
        }

        ArrayList<Attribute> atts = weakRel.findAttributes(attributes);
        if (atts == null) {
            throw new IllegalArgumentException("GraphDetector.getWeakEntityQueryAndGraphTypes: attributes for "
                    + weakEntityName + " were not found in the relation " + weakRel.getName());
        }

        // Match attTypes with BasicGraphType enums
        Map<WeakGraphType, Set<WeakGraphPlan>> graphPlans = WeakGraphPlan.matchToGraphs(k1, k2, atts);

        /* GENERATE QUERY */
        // Detect join columns



        Set<String> allWeakAttsNames = new HashSet<>(attributes);
        allWeakAttsNames.add(key2AttName);

        String query = generateWeakEntityQuery(rm, attachedEntityName, key1AttName, weakEntityName, allWeakAttsNames, Set.of("bruh"));

        return new WeakGraphDetector(query, graphPlans);
    }

    // TODO: Does not work rn
    private static String generateWeakEntityQuery(ERModel rm, String attachedTable, String attachedColumn,
                                                  String weakTable, Set<String> weakColumns, Set<String> joinColumns) {
        // TODO: throw exception
        assert weakColumns.size() > 0;

        ArrayList<String> wColsArr = new ArrayList<>(weakColumns);
        ArrayList<String> jColsArr = new ArrayList<>(joinColumns);

        StringBuilder q = new StringBuilder("SELECT ");
        q.append(wColsArr.get(0));

        for (int i = 1; i < wColsArr.size(); i++) {
            q.append(", ");
            q.append(wColsArr.get(i));
        }

        q.append(" FROM ").append(rm.getSchemaPattern()).append(".").append(weakTable);

        return q.toString();
    }

    // TODO: Restrictions
    private String generateWeakEntityQuery(ERModel rm, String attachedTable, String attachedColumn,
                                           String weakTable, Set<String> weakColumns, Set<String> joinColumns,
                                           String restrictions) {
        return generateWeakEntityQuery(rm, attachedTable, attachedColumn, weakTable, weakColumns, joinColumns) + " " + restrictions;
    }
}
