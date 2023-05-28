package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans.ManyManyGraphPlan;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryKey;

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

    public Set<GraphPlan> fitAttributesToPlan(Attribute kAtt, List<Attribute> unorderedAtts) {
        Set<GraphPlan> plans = new HashSet<>();
        List<List<Attribute>> possibleOrders = orderAttributesByType(kAtt, unorderedAtts);

        for (List<Attribute> possibleOrder : possibleOrders) {
            plans.add(getInstance(kAtt, possibleOrder));
        }

        return plans;
    }

    private List<List<Attribute>> orderAttributesByType(Attribute kAtt, List<Attribute> atts) {
        int attSize = atts.size();
        int smallestAttsSize = getMandatories().size();
        int largestAttsSize = smallestAttsSize + getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize
                || !kAtt.getDBType().getAttType().isType(getKType())) {
            return Collections.emptyList();
        }

        return findMandatoryAndOptionalAttsOrder(atts, getMandatories(), getOptionals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
