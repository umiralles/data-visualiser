package visualiser.datavisualiser.models.GraphDetector;

import org.reflections.Reflections;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.GoogleChart.Column;
import visualiser.datavisualiser.models.GoogleChart.DataCell;
import visualiser.datavisualiser.models.GoogleChart.DataTable;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans.BasicGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class GraphDetector {

    // used for generating the datatable
    private final VisSchemaPattern vPattern;
    private final Set<Attribute> attributes;
    // map from graph type name -> possible graph plans
    private final Map<String, Set<GraphPlan>> plans;

    private DataTable data = null;

    private GraphDetector(VisSchemaPattern vPattern, Set<Attribute> attributes, Map<String, Set<GraphPlan>> plans) {
        this.vPattern = vPattern;
        this.attributes = attributes;
        this.plans = plans;
    }


    // kInput: must be a key attribute of the entity
    // aInputs: all other attributes to be included. Must be part of the entity
    public static GraphDetector generateBasicPlans(ERModel rm, InputAttribute kInput, List<InputAttribute> aInputs) {
        String entityName = kInput.table();

        Relation entityRelation = rm.getRelation(entityName);
        if (entityRelation == null || !entityRelation.isEntityRelation()) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: entity " + entityName + " not found");
        }

        Attribute k = entityRelation.findInPrimaryKey(kInput.column());
        if (k == null) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: attribute " + kInput.column()
                    + " is not a primary attribute of " + entityName);
        }

        List<String> aNames = aInputs.stream().map(InputAttribute::column).toList();
        List<Attribute> as = entityRelation.findAttributes(aNames);
        if (as == null) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: attributes for "
                    + entityName + " were not found in the relation " + entityRelation.getName());
        }

        return generateBasicPlans(rm, rm.getEntity(entityName), as);
    }

    public static GraphDetector generateBasicPlans(ERModel rm, EntityType entity, List<Attribute> as) {
        /* Checks for illegal arguments */
        if (as.isEmpty()) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: not enough attributes specified for entity " + entity.getName());
        }

        Reflections reflections = new Reflections(BasicGraphPlan.class.getPackageName());
        Set<Class<? extends BasicGraphPlan>> subClasses = reflections.getSubTypesOf(BasicGraphPlan.class);

        Relation eRel = rm.getRelation(entity.getName());
        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        for (Class<? extends BasicGraphPlan> subClass : subClasses) {
            Set<GraphPlan> typePlans;
            String planName;
            try {
                BasicGraphPlan dummyPlan = (BasicGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateBasicPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }

                if (!dummyPlan.fitKType(eRel.getPrimaryKey())) {
                    continue;
                }

                planName = dummyPlan.getPlanName();
                typePlans = dummyPlan.fitAttributesToPlan(eRel.getPrimaryKey(), as);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (typePlans != null && !typePlans.isEmpty()) {
                plans.put(planName, typePlans);
            }
        }

        if (plans.isEmpty()) {
            return null;
        }

        return new GraphDetector(VisSchemaPattern.BASIC_ENTITY, new HashSet<>(as), plans);
    }

    public static GraphDetector generateWeakPlans(ERModel rm, InputAttribute k1, InputAttribute k2,
                                                  List<InputAttribute> attributes) {

        return null;
    }

    public static GraphDetector generateOneManyPlans(ERModel rm, InputAttribute k1, InputAttribute k2,
                                                     List<InputAttribute> attributes) {

        return null;
    }

    public static GraphDetector generateManyManyPlans(ERModel rm, boolean reflexive, InputAttribute k1, InputAttribute k2,
                                                      List<InputAttribute> attributes) {

        return null;
    }

    public Map<String, Set<GraphPlan>> getPlans() {
        return plans;
    }

    public DataTable getData(ERModel rm) throws SQLException {
        if (data != null) {
            return data;
        }

        if (attributes.isEmpty()) {
            // TODO: error
            return null;
        }

        // make a datatable based on the attributes
        switch (vPattern) {
            case BASIC_ENTITY -> {
                String table = attributes.stream().findFirst().get().getTable();
                List<Attribute> attsList = attributes.stream().toList();
                Set<String> attsStr = attsList.stream().map(Attribute::getColumn).collect(Collectors.toSet());

                ArrayList<Column> columns = new ArrayList<>();
                for (Attribute att : attsList) {
                    columns.add(new Column(att.getDBType().getDataType(), att.getColumn()));
                }

                List<List<DataCell>> rows = rm.getRowsFromQueryAndAtts(generateBasicEntityQuery(rm, table, attsStr), attsList);

                data = new DataTable(attsList, columns, rows);
            }

            case WEAK_ENTITY -> {
            }
            case ONE_MANY_REL -> {
            }
            case MANY_MANY_REL -> {
            }
            case REFLEXIVE -> {
            }
        }

        return data;
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
}
