package visualiser.datavisualiser.models.GraphDetector;

import org.json.JSONObject;
import visualiser.datavisualiser.models.GoogleChart.Column;
import visualiser.datavisualiser.models.GoogleChart.DataCell;
import visualiser.datavisualiser.models.GoogleChart.DataTable;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans.*;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

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
        /* Checks for illegal arguments */
        String entityName = kInput.table();
        if (aInputs.isEmpty()) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: not enough attributes specified for entity " + entityName);
        }

        List<String> aNames = new ArrayList<>();
        Set<String> aTables = new HashSet<>();
        for (InputAttribute aInput : aInputs) {
            aNames.add(aInput.column());
            aTables.add(aInput.table());
        }

        // Check that all attributes are for the same entity
        if (aTables.size() != 1 || !aTables.stream().findFirst().get().equals(entityName)) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: not all attributes are within entity " + entityName);
        }

        Relation entityRelation = rm.getRelation(entityName);
        if (entityRelation == null || !entityRelation.isEntityRelation()) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: entity " + entityName + " not found");
        }

        Attribute k = entityRelation.findInPrimaryKey(kInput.column());
        if (k == null) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: attribute " + kInput.column()
                    + " is not a primary attribute of " + entityName);
        }

        List<Attribute> as = entityRelation.findAttributes(aNames);
        if (as == null) {
            throw new IllegalArgumentException("GraphDetector.generateBasicPlans: attributes for "
                    + entityName + " were not found in the relation " + entityRelation.getName());
        }

        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        // TODO: reflections
        Set<Class<? extends BasicGraphPlan>> subClasses = Set.of(BarChartPlan.class, BubbleChartPlan.class, CalendarPlan.class,
                ChoroplethMapPlan.class, ScatterDiagramPlan.class, WordCloudPlan.class);
        for (Class<? extends BasicGraphPlan> subClass : subClasses) {

            Set<GraphPlan> typePlans;
            String planName;
            try {
                BasicGraphPlan dummyPlan = (BasicGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateBasicPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }

                planName = dummyPlan.getPlanName();
                typePlans = dummyPlan.fitAttributesToPlan(k, as);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (!typePlans.isEmpty()) {
                plans.put(planName, typePlans);
            }
        }

        as.add(k);
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

    // For WebView of width 775, height 500: width 765, height 480
    public JSONObject getGoogleChartOptions() {
        return new JSONObject()
                .put("title", "New Chart")
                .put("width", 765)
                .put("height", 480);
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

                data = new DataTable(columns, rows);
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
