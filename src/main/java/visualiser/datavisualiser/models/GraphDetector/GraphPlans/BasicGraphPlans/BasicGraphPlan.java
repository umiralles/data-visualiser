package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;

import java.util.*;

public abstract class BasicGraphPlan extends GraphPlan {

    private final PrimaryKey k1;
    private final List<Attribute> orderedMandatoryAtts;
    private final List<Attribute> orderedOptionalAtts;

    BasicGraphPlan(PrimaryKey k1, List<Attribute> orderedMandatoryAtts, List<Attribute> orderedOptionalAtts) {
        this.k1 = k1;
        this.orderedMandatoryAtts = orderedMandatoryAtts;
        this.orderedOptionalAtts = orderedOptionalAtts;
    }

    public List<Attribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    public List<Attribute> getOrderedOptionals() {
        return orderedOptionalAtts;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static BasicGraphPlan getDummyInstance() {
        return null;
    }

    public abstract BasicGraphPlan getInstance(PrimaryKey k1,
                                               List<Attribute> orderedMandAtts, List<Attribute> orderedOptionalAtts);

    public abstract String getPlanName();
    public abstract int getKLowerLim();
    public abstract int getKUpperLim();
    public abstract AttributeType getKType();
    public abstract List<AttributeType> getMandatories();
    public abstract List<AttributeType> getOptionals();

    public Set<GraphPlan> fitAttributesToPlan(PrimaryKey k1, List<Attribute> unorderedAtts) {
        int attSize = unorderedAtts.size();
        int smallestAttsSize = getMandatories().size();
        int largestAttsSize = smallestAttsSize + getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize) {
            return Collections.emptySet();
        }

        // TODO: Check that all atts in k1 are correct

//        List<List<Attribute>> possibleMandatories = findMandatoryAttsOrders(unorderedAtts, getMandatories());
//        if (possibleMandatories.isEmpty()) {
//            return Collections.emptySet();
//        }
//
//        List<List<Attribute>> possibleOptionals = findOptionalAttsOrders(unorderedAtts, possibleMandatories, getOptionals());

        Set<GraphPlan> plans = new HashSet<>();

        // TODO: bruh
        List<List<Attribute>> possibleOrders = List.of();

        for (List<Attribute> possibleOrder : possibleOrders) {
            plans.add(getInstance(k1, new ArrayList<>(), possibleOrder));
        }

        return plans;
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
