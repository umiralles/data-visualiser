package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.*;

public abstract class BasicGraphPlan extends GraphPlan {

    private final PrimaryKey k1;
    private final List<GraphAttribute> orderedMandatoryAtts;
    private final List<GraphAttribute> orderedOptionalAtts;

    BasicGraphPlan(PrimaryKey k1, List<GraphAttribute> orderedMandatoryAtts, List<GraphAttribute> orderedOptionalAtts) {
        this.k1 = k1;
        this.orderedMandatoryAtts = orderedMandatoryAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    @Override
    public String getName() {
        return k1.getTable();
    }

    public List<GraphAttribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    public List<GraphAttribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static BasicGraphPlan getDummyInstance() {
        return null;
    }

    public abstract BasicGraphPlan getInstance(PrimaryKey k1,
                                               List<GraphAttribute> orderedMandAtts, List<GraphAttribute> orderedOptionalAtts);

    public abstract String getPlanName();

    public abstract int getKLowerLim();

    public abstract int getKUpperLim();

    public abstract AttributeType getKType();

    public abstract List<AttributeType> getMandatories();

    public abstract List<AttributeType> getOptionals();

    public PrimaryKey getK1() {
        return k1;
    }

    public List<GraphAttribute> getOrderedMandatoryAtts() {
        return orderedMandatoryAtts;
    }

    public List<GraphAttribute> getOrderedOptionalAtts() {
        return orderedOptionalAtts;
    }

    @Override
    public List<GraphAttribute> getAllOrderedAttributes() {
        List<GraphAttribute> allAtts = new ArrayList<>(orderedMandatoryAtts);
        allAtts.addAll(orderedOptionalAtts);
        return allAtts;
    }

    public boolean fitKType(PrimaryKey k1) {
        Set<PrimaryAttribute> k1Atts = k1.getPAttributes();

        // If there is one primary attribute then use that to check the type
        if (k1Atts.size() == 1) {
            return k1Atts.stream().findFirst().get().getDBType().getAttType().isType(getKType());
        }

        // If there are more than one primary attributes, the AttributeType is equivalent to LEXICAL
        return AttributeType.LEXICAL.isType(getKType());
    }

    public Set<GraphPlan> fitAttributesToPlan(PrimaryKey k1, List<Attribute> unorderedAtts) {
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

            plans.add(getInstance(k1, possibleMandOrder, possibleOptOrder));
        }

        return plans;
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
