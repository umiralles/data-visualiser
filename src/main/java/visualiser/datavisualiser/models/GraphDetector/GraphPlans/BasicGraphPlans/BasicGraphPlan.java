package visualiser.datavisualiser.models.GraphDetector.GraphPlans.BasicGraphPlans;

import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.*;

public abstract class BasicGraphPlan extends GraphPlan {

    private final Attribute kAtt;
    private final List<Attribute> orderedAtts;

    BasicGraphPlan(Attribute kAtt, List<Attribute> orderedAtts) {
        this.kAtt = kAtt;
        this.orderedAtts = orderedAtts;
    }

    @Override
    public String getOrderedAttributesRepresentation() {
        StringBuilder sb = new StringBuilder(kAtt.getTable());
        sb.append(": ").append(kAtt.getColumn());

        for (Attribute att : orderedAtts) {
            sb.append(" -> ").append(att.getColumn());
        }

        return sb.toString();
    }

    @Override
    public List<String> getOrderedColumnNames() {
        List<String> columns = new ArrayList<>();
        columns.add(kAtt.getColumn());
        orderedAtts.forEach(att -> columns.add(att.getColumn()));

        return columns;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static BasicGraphPlan getDummyInstance() {
        return null;
    }

    public abstract BasicGraphPlan getInstance(Attribute kAtt, List<Attribute> orderedAtts);

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
        return Objects.hash(kAtt, orderedAtts);
    }
}
