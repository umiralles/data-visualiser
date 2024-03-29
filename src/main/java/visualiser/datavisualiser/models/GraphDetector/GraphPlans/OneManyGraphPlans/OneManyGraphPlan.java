package visualiser.datavisualiser.models.GraphDetector.GraphPlans.OneManyGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.*;

public abstract class OneManyGraphPlan extends GraphPlan {

    private final PrimaryKey parentKey;
    private final PrimaryKey childKey;
    private final List<GraphAttribute> orderedMandatoryAtts;
    private final List<GraphAttribute> orderedOptionalAtts;

    OneManyGraphPlan(PrimaryKey parentKey, PrimaryKey childKey, List<GraphAttribute> orderedMandatoryAtts, List<GraphAttribute> orderedOptionalAtts) {
        this.parentKey = parentKey;
        this.childKey = childKey;
        this.orderedMandatoryAtts = orderedMandatoryAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    @Override
    public String getName() {
        return parentKey.getTable() + " - " + childKey.getTable();
    }

    public PrimaryKey getParentKey() {
        return parentKey;
    }

    public PrimaryKey getChildKey() {
        return childKey;
    }

    protected List<GraphAttribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    protected List<GraphAttribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static OneManyGraphPlan getDummyInstance() {
        return null;
    }

    public abstract OneManyGraphPlan getInstance(PrimaryKey k1, PrimaryKey k2,
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

    public boolean fitKTypes(PrimaryKey parentKey, PrimaryKey childKey) {
        Set<PrimaryAttribute> k1Atts = parentKey.getPAttributes();
        Set<PrimaryAttribute> k2Atts = childKey.getPAttributes();

        boolean k1Correct;
        if (k1Atts.size() == 1) {
            // If there is one primary attribute then use that to check the type
            k1Correct = k1Atts.stream().findFirst().get().getDBType().getAttType().isType(getK1Type());
        } else {
            // If there are more than one primary attributes, the AttributeType is equivalent to LEXICAL
            k1Correct = AttributeType.LEXICAL.isType(getK1Type());
        }

        if (k1Correct) {
            if (k2Atts.size() == 1) {
                // If there is one primary attribute then use that to check the type
                return k2Atts.stream().findFirst().get().getDBType().getAttType().isType(getK2Type());
            }

            // If there are more than one primary attributes, the AttributeType is equivalent to LEXICAL
            return AttributeType.LEXICAL.isType(getK2Type());
        }

        return false;
    }

    public Set<GraphPlan> fitAttributesToPlan(PrimaryKey parentKey, PrimaryKey childKey, List<Attribute> unorderedAtts) {
        int attSize = unorderedAtts.size();
        int smallestAttsSize = getMandatories().size();
        int largestAttsSize = smallestAttsSize + getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize) {
            return Collections.emptySet();
        }

        Set<GraphPlan> plans = new HashSet<>();
        List<List<GraphAttribute>> possibleOrders = findMandatoryAndOptionalAttsOrder(unorderedAtts, getMandatories(), getOptionals());

        int numMandatories = getMandatories().size();
        for (List<GraphAttribute> possibleOrder : possibleOrders) {
            List<GraphAttribute> possibleMandOrder = possibleOrder.subList(0, numMandatories);
            List<GraphAttribute> possibleOptOrder = possibleOrder.subList(numMandatories, possibleOrder.size());

            plans.add(getInstance(parentKey, childKey, possibleMandOrder, possibleOptOrder));
        }

        return plans;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentKey, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
