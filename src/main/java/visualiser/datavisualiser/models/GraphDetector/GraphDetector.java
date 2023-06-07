package visualiser.datavisualiser.models.GraphDetector;

import org.reflections.Reflections;
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
    private final Relationship relationship;
    private final List<PrimaryKey> primaryKeys;
    private final Set<Attribute> attributes;
    // map from graph type name -> possible graph plans
    private final Map<String, Set<GraphPlan>> plans;

    // Should always be either null or all data for all possible attributes
    private DataTable data = null;
    // Limits K1
    private int lim1 = -1;
    // Limits K2, Not used for basic entity plans
    private int lim2 = -1;

    private GraphDetector(Relationship relationship, List<PrimaryKey> primaryKeys, Set<Attribute> attributes,
                          Map<String, Set<GraphPlan>> plans) {
        this.relationship = relationship;
        this.primaryKeys = primaryKeys;
        this.attributes = attributes;
        this.plans = plans;
    }

    public void setLim1(int lim1) {
        this.lim1 = lim1;
    }

    public void setLim2(int lim2) {
        this.lim2 = lim2;
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

        return new GraphDetector(null, List.of(eRel.getPrimaryKey()), new HashSet<>(as), plans);
    }

    public static GraphDetector generateWeakPlans(BinaryRelationship rel, List<Attribute> as) {
        /* Checks for illegal arguments */
        if (as.isEmpty()) {
            throw new IllegalArgumentException("GraphDetector.generateWeakPlans: not enough attributes specified for weak relationship " + rel.getName());
        }

        Reflections reflections = new Reflections(WeakGraphPlan.class.getPackageName());
        Set<Class<? extends WeakGraphPlan>> subClasses = reflections.getSubTypesOf(WeakGraphPlan.class);

        Map<String, Set<GraphPlan>> plans = new HashMap<>();
        for (Class<? extends WeakGraphPlan> subClass : subClasses) {
            Set<GraphPlan> typePlans;
            String planName;
            try {
                WeakGraphPlan dummyPlan = (WeakGraphPlan) subClass.getMethod("getDummyInstance").invoke(null);

                if (dummyPlan == null) {
                    throw new RuntimeException("GraphDetector.generateWeakPlans: subclass " +
                            subClass.getName() + "has not overwritten getDummyInstance method.");
                }

                if ((dummyPlan.isCompleteRelationship() != rel.isComplete()) ||
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

        return new GraphDetector(rel, List.of(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey()),
                new HashSet<>(as), plans);
    }

    public static GraphDetector generateOneManyPlans(BinaryRelationship rel, List<Attribute> as) {
        /* Checks for illegal arguments */
        if (as.isEmpty()) {
            throw new IllegalArgumentException("GraphDetector.generateOneManyPlans: not enough attributes specified for weak relationship " + rel.getName());
        }

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

                if (!dummyPlan.fitKTypes(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey())) {
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

        return new GraphDetector(rel, List.of(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey()),
                new HashSet<>(as), plans);
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

        return new GraphDetector(rel, List.of(rel.getA().getPrimaryKey(), rel.getB().getPrimaryKey()),
                new HashSet<>(as), plans);
    }

    public Map<String, Set<GraphPlan>> getPlans() {
        return plans;
    }

    public DataTable getData(ERModel rm) throws SQLException {
        if (data == null) {
            if (attributes.isEmpty() || primaryKeys.isEmpty()) {
                // TODO: error
                return null;
            }

            // make a datatable based on the attributes
            this.data = rm.getDataTableWithAttributes(relationship, new HashSet<>(primaryKeys), attributes);
        }

        if (primaryKeys.size() == 1) {
            return DataTable.getWithLimit(data, rm, relationship, primaryKeys.get(0).toString(), lim1, null, -1);
        }

        return DataTable.getWithLimit(data, rm, relationship, primaryKeys.get(0).toString(), lim1,
                                        primaryKeys.get(1).toString(), lim2);
    }
}
