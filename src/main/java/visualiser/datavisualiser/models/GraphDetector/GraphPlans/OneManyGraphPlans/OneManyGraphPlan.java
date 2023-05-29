package visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans;

import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;

import java.util.*;

public abstract class OneManyGraphPlan extends GraphPlan {

    private final PrimaryKey k1;
    private final PrimaryKey k2;
    private final List<Attribute> orderedMandAtts;
    private final List<Attribute> orderedOptionalAtts;

    OneManyGraphPlan(PrimaryKey k1, PrimaryKey k2, List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts) {
        this.k1 = k1;
        this.k2 = k2;
        this.orderedMandAtts = orderedMandAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    public List<Attribute> getOrderedMandatories() {
        return orderedMandAtts;
    }

    public List<Attribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static OneManyGraphPlan getDummyInstance() {
        return null;
    }

    public abstract OneManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
                                                 List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts);

    public abstract String getPlanName();
    public abstract int getK1LowerLim();
    public abstract int getK1UpperLim();
    public abstract AttributeType getK1Type();
    public abstract int getK2PerK1LowerLim();
    public abstract int getK2PerK1UpperLim();
    public abstract AttributeType getK2Type();
    public abstract List<AttributeType> getMandatories();
    public abstract List<AttributeType> getOptionals();

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

    private List<List<Attribute>> orderAttributesByType(Attribute k1Att, Attribute k2Att, List<Attribute> atts) {
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
        return Objects.hash(k1, orderedMandAtts, orderedOptionalAtts);
    }
}
