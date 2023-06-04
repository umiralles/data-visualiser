package visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ManyManyGraphPlan extends GraphPlan {

    private final PrimaryKey k1;
    private final PrimaryKey k2;
    private final List<GraphAttribute> orderedMandatoryAtts;
    private final List<GraphAttribute> orderedOptionalAtts;

    ManyManyGraphPlan(PrimaryKey k1, PrimaryKey k2, List<GraphAttribute> orderedMandatoryAtts, List<GraphAttribute> orderedOptionalAtts) {
        this.k1 = k1;
        this.k2 = k2;
        this.orderedMandatoryAtts = orderedMandatoryAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    @Override
    public String getName() {
        return k1.getTable() + " - " + k2.getTable();
    }

    public List<GraphAttribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    public List<GraphAttribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static ManyManyGraphPlan getDummyInstance() {
        return null;
    }

    public abstract ManyManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                                  List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts);

    @Override
    public List<GraphAttribute> getAllOrderedAttributes() {
        List<GraphAttribute> allAtts = new ArrayList<>(orderedMandatoryAtts);
        allAtts.addAll(orderedOptionalAtts);
        return allAtts;
    }

    public abstract String getPlanName();

    public abstract int getK1LowerLim();

    public abstract int getK1UpperLim();

    public abstract AttributeType getK1Type();

    public abstract int getK2LowerLim();

    public abstract int getK2UpperLim();

    public abstract AttributeType getK2Type();

    public abstract boolean isReflexiveRelationship();

    public abstract List<AttributeType> getMandatories();
    public abstract List<AttributeType> getOptionals();

    // TODO: add reflexive stuff
//    public Set<GraphPlan> fitAttributesToPlan(Attribute k1Att, Attribute k2Att, List<Attribute> unorderedAtts) {
//        Set<GraphPlan> plans = new HashSet<>();
//        List<List<Attribute>> possibleOrders = orderAttributesByType(k1Att, k2Att, unorderedAtts);
//
//        for (List<Attribute> possibleOrder : possibleOrders) {
//            plans.add(getInstance(k1Att, k2Att, possibleOrder));
//        }
//
//        return plans;
//    }

    private List<List<GraphAttribute>> orderAttributesByType(Attribute k1Att, Attribute k2Att, List<Attribute> atts) {
        int attSize = atts.size();
        int smallestAttsSize = getMandatories().size();
        int largestAttsSize = smallestAttsSize + getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize
                || !k1Att.getDBType().getAttType().isType(getK1Type())
                || !k2Att.getDBType().getAttType().isType(getK2Type())) {
            return Collections.emptyList();
        }

        return findMandatoryAndOptionalAttsOrder(atts, getMandatories(), getOptionals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, k2, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
