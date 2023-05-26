package visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans;

import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.RelationalModel.AttributeType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.*;

public abstract class ManyManyGraphPlan extends GraphPlan {

    private final Attribute k1Att;
    private final Attribute k2Att;
    private final List<Attribute> orderedAtts;

    ManyManyGraphPlan(Attribute k1Att, Attribute k2Att, List<Attribute> orderedAtts) {
        this.k1Att = k1Att;
        this.k2Att = k2Att;
        this.orderedAtts = orderedAtts;
    }

    @Override
    public String getOrderedAttributesRepresentation() {
        StringBuilder sb = new StringBuilder(k1Att.getTable());
        sb.append(".").append(k1Att.getColumn())
                .append(" + ").append(k2Att.getTable())
                .append(".").append(k2Att.getColumn());

        for (Attribute att : orderedAtts) {
            sb.append(" -> ").append(att.getColumn());
        }

        return sb.toString();
    }

    @Override
    public List<String> getOrderedColumnNames() {
        List<String> columns = new ArrayList<>();
        columns.add(k1Att.getColumn());
        columns.add(k2Att.getColumn());
        orderedAtts.forEach(att -> columns.add(att.getColumn()));

        return columns;
    }

    /* MUST BE OVERWRITTEN */
    // Return a dummy instance that can be used to access the subclass' functions
    public static ManyManyGraphPlan getDummyInstance() {
        return null;
    }

    public abstract ManyManyGraphPlan getInstance(Attribute k1Att, Attribute k2Att, List<Attribute> orderedAtts);

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
    public Set<GraphPlan> fitAttributesToPlan(Attribute k1Att, Attribute k2Att, List<Attribute> unorderedAtts) {
        Set<GraphPlan> plans = new HashSet<>();
        List<List<Attribute>> possibleOrders = orderAttributesByType(k1Att, k2Att, unorderedAtts);

        for (List<Attribute> possibleOrder : possibleOrders) {
            plans.add(getInstance(k1Att, k2Att, possibleOrder));
        }

        return plans;
    }

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
        return Objects.hash(k1Att, orderedAtts);
    }
}
