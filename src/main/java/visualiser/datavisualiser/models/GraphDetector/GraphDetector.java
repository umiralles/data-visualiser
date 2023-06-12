package visualiser.datavisualiser.models.GraphDetector;

import org.reflections.Reflections;
import visualiser.datavisualiser.models.DataTable.Column;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.ERModel.Relationships.BinaryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.NAryRelationship;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans.BasicGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans.ManyManyGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans.OneManyGraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.WeakGraphPlans.WeakGraphPlan;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

public class GraphDetector {

    // used for generating the datatable
    private final EntityType entity;
    private final Relationship relationship;
    private final Set<Attribute> attributes;
    // map from graph type name -> possible graph plans
    private final Map<String, Set<GraphPlan>> plans;

    // Should always be either null or all data for all possible attributes
    private DataTable data;

    private GraphDetector(EntityType entity, Relationship relationship, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans, DataTable data) {
        this.entity = entity;
        this.relationship = relationship;
        this.attributes = attributes;
        this.plans = plans;
        this.data = data;
    }

    private GraphDetector(Relationship relationship, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans, DataTable data) {
        this(null, relationship, attributes, plans, data);
    }

    private GraphDetector(Relationship relationship, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans) {
        this(relationship, attributes, plans, null);
    }

    private GraphDetector(EntityType entity, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans, DataTable data) {
        this(entity, null, attributes, plans, data);
    }

    private GraphDetector(EntityType entity, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans) {
        this(entity, attributes, plans, null);
    }

    public EntityType getEntity() {
        return entity;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    public Map<String, Set<GraphPlan>> getPlans() {
        return plans;
    }

    public DataTable getData(ERModel rm, int lim1, int lim2, String compareAttId,
                             Comparator<? super DataCell> comparator) throws SQLException {
        List<PrimaryKey> primaryKeys = new ArrayList<>();
        if (entity != null) {
            primaryKeys.add(rm.getRelation(entity.getName()).getPrimaryKey());
        } else if (relationship != null) {
            primaryKeys.add(relationship.getB().getPrimaryKey());
            primaryKeys.add(relationship.getA().getPrimaryKey());
        }

        if (data == null) {
            if (primaryKeys.isEmpty()) {
                // TODO: error
                return null;
            }

            // make a datatable based on the attributes
            this.data = loadData(rm, relationship, new HashSet<>(primaryKeys), attributes);
        }

        if (primaryKeys.size() == 1) {
            return DataTable.getWithLimit(data, rm, relationship, primaryKeys.get(0).toString(), lim1, compareAttId, comparator);
        }

        return DataTable.getWithLimit(data, rm, relationship, primaryKeys.get(0).toString(), lim1,
                primaryKeys.get(1).toString(), lim2, compareAttId, comparator);
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

        return new GraphDetector(entity, new HashSet<>(as), plans);
    }

    public static GraphDetector generateWeakPlans(ERModel rm, BinaryRelationship rel, List<Attribute> as) {
        Reflections reflections = new Reflections(WeakGraphPlan.class.getPackageName());
        Set<Class<? extends WeakGraphPlan>> subClasses = reflections.getSubTypesOf(WeakGraphPlan.class);

        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        Boolean isComplete = null;
        DataTable data = null;
        for (Class<? extends WeakGraphPlan> subClass : subClasses) {
            Set<GraphPlan> typePlans;
            String planName;
            try {
                WeakGraphPlan dummyPlan = (WeakGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateWeakPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }


                if (!dummyPlan.fitKTypes(rel.getB().getPrimaryKey(), rel.getA().getPrimaryKey())) {
                    continue;
                }

                if (dummyPlan.mustBeComplete()) {
                    // Check for completeness
                    if (isComplete == null) {
                        data = loadData(rm, null, rel, new HashSet<>(as));
                        isComplete = GraphDetector.checkForCompleteness(data, rel);
                    }

                    if (!isComplete) {
                        continue;
                    }
                }

                planName = dummyPlan.getPlanName();
                typePlans = dummyPlan.fitAttributesToPlan(rel.getB().getPrimaryKey(), rel.getA().getPrimaryKey(), as);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SQLException e) {
                throw new RuntimeException(e);
            }

            if (typePlans != null && !typePlans.isEmpty()) {
                plans.put(planName, typePlans);
            }
        }

        if (plans.isEmpty()) {
            return null;
        }

        return new GraphDetector(rel, new HashSet<>(as), plans, data);
    }

    public static GraphDetector generateOneManyPlans(BinaryRelationship rel, List<Attribute> as) {
        Reflections reflections = new Reflections(OneManyGraphPlan.class.getPackageName());
        Set<Class<? extends OneManyGraphPlan>> subClasses = reflections.getSubTypesOf(OneManyGraphPlan.class);

        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        for (Class<? extends OneManyGraphPlan> subClass : subClasses) {
            Set<GraphPlan> typePlans;
            String planName;
            try {
                OneManyGraphPlan dummyPlan = (OneManyGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateOneManyPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }

                if (!dummyPlan.fitKTypes(rel.getB().getPrimaryKey(), rel.getA().getPrimaryKey())) {
                    continue;
                }

                planName = dummyPlan.getPlanName();
                typePlans = dummyPlan.fitAttributesToPlan(rel.getB().getPrimaryKey(), rel.getA().getPrimaryKey(), as);
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

        return new GraphDetector(rel, new HashSet<>(as), plans);
    }

    public static GraphDetector generateManyManyPlans(NAryRelationship rel, List<Attribute> as) {
        /* Checks for illegal arguments */
        if (as.isEmpty()) {
            throw new IllegalArgumentException("GraphDetector.generateManyManyPlans: not enough attributes specified for weak relationship " + rel.getName());
        }

        Reflections reflections = new Reflections(ManyManyGraphPlan.class.getPackageName());
        Set<Class<? extends ManyManyGraphPlan>> subClasses = reflections.getSubTypesOf(ManyManyGraphPlan.class);

        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        for (Class<? extends ManyManyGraphPlan> subClass : subClasses) {
            Set<GraphPlan> typePlans;
            String planName;
            try {
                ManyManyGraphPlan dummyPlan = (ManyManyGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateManyManyPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }

                if ((dummyPlan.isReflexiveRelationship() != rel.isReflexive()) ||
                        !dummyPlan.fitKTypes(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey())) {
                    continue;
                }

                planName = dummyPlan.getPlanName();
                typePlans = dummyPlan.fitAttributesToPlan(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey(), as);
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

        return new GraphDetector(rel, new HashSet<>(as), plans);
    }

    // Data is complete if the same set of values appear for k2 for each instance of k1
    private static boolean checkForCompleteness(DataTable data, BinaryRelationship rel) {
        String k1Id = rel.getB().getPrimaryKey().toString();
        String k2Id = rel.getA().getPrimaryKey().toString();

        int k1Idx = -1;
        int k2Idx = -1;
        for (int i = 0; i < data.columns().size(); i++) {
            Column col = data.columns().get(i);
            if (col.id().equals(k1Id)) {
                k1Idx = i;
            } else if (col.id().equals(k2Id)) {
                k2Idx = i;
            }
        }

        if (k1Idx == -1 || k2Idx == -1) {
            throw new IllegalArgumentException("GraphDetector.checkForCompleteness: the wrong DataTable was used with relationship " + rel.getName());
        }

        HashMap<String, Set<String>> k1ToK2s = new HashMap<>();
        for (List<DataCell> row : data.rows()) {
            String k1Val = row.get(k1Idx).value();
            String k2Val = row.get(k2Idx).value();

            if (k1ToK2s.containsKey(k1Val)) {
                k1ToK2s.get(k1Val).add(k2Val);
            } else {
                k1ToK2s.put(k1Val, new HashSet<>(Collections.singleton(k2Val)));
            }
        }

        if (k1ToK2s.isEmpty()) {
            return true;
        }

        // Check that every set for each k1 have the same vals
        Set<String> sampleSet = k1ToK2s.values().stream().findAny().get();
        for (String k1 : k1ToK2s.keySet()) {
            Set<String> testSet = k1ToK2s.get(k1);
            if (!sampleSet.containsAll(testSet) || !testSet.containsAll(sampleSet)) {
                return false;
            }
        }

        return true;
    }

    public static DataTable loadData(ERModel rm, Relationship relationship,
                                     Set<PrimaryKey> primaryKeys, Set<Attribute> attributes) throws SQLException {
        return rm.getDataTableWithAttributes(relationship, primaryKeys, attributes);
    }

    public static DataTable loadData(ERModel rm, EntityType entity, Relationship relationship,
                                     Set<Attribute> attributes) throws SQLException {
        Set<PrimaryKey> primaryKeys = new HashSet<>();
        if (entity != null) {
            primaryKeys.add(rm.getRelation(entity.getName()).getPrimaryKey());
        } else if (relationship != null) {
            primaryKeys.add(relationship.getA().getPrimaryKey());
            primaryKeys.add(relationship.getB().getPrimaryKey());
        }

        return loadData(rm, relationship, primaryKeys, attributes);
    }
}
