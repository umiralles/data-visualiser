package visualiser.datavisualiser.models.GraphDetectorA.GraphPlans;

import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.*;

public class OneManyGraphPlan extends GraphPlan {

    private final OneManyGraphType type;
    private final Attribute k1Att;
    private final Attribute k2Att;
    private final List<Attribute> orderedAtts;

    public OneManyGraphPlan(OneManyGraphType type, Attribute k1Att, Attribute k2Att, List<Attribute> orderedAtts) {
        this.type = type;
        this.k1Att = k1Att;
        this.k2Att = k2Att;
        this.orderedAtts = orderedAtts;
    }

    @Override
    public void generateGraph() {
        // TODO: Implement this
    }

    // TODO: Copied from BasicGraphPlan
    // Returns a map of a graph type to the possible graph plans
    public static Map<OneManyGraphType, Set<OneManyGraphPlan>> matchToGraphs(Attribute k1Att, Attribute k2Att, List<Attribute> atts) {
        Map<OneManyGraphType, Set<OneManyGraphPlan>> plans = new HashMap<>();
        for (OneManyGraphType type : OneManyGraphType.values()) {
            Set<OneManyGraphPlan> typePlans = fitTypeToPlan(type, k1Att, k2Att, atts);
            if (!typePlans.isEmpty()) {
                plans.put(type, typePlans);
            }
        }

        return plans;
    }

    // TODO: Copied from BasicGraphPlan
    // Returns null if type does not fit attributes
    public static Set<OneManyGraphPlan> fitTypeToPlan(OneManyGraphType type, Attribute k1Att, Attribute k2Att, List<Attribute> atts) {
        Set<OneManyGraphPlan> plans = new HashSet<>();
        List<List<Attribute>> possibleOrders = orderAttributesByType(type, k1Att, k2Att, atts);

        for (List<Attribute> possibleOrder : possibleOrders) {
            plans.add(new OneManyGraphPlan(type, k1Att, k2Att, possibleOrder));
        }

        return plans;
    }

    // TODO: Copied from BasicGraphPlan
    private static List<List<Attribute>> orderAttributesByType(OneManyGraphType type, Attribute k1Att, Attribute k2Att, List<Attribute> atts) {
        int attSize = atts.size();
        int smallestAttsSize = type.getMandatories().size();
        int largestAttsSize = smallestAttsSize + type.getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize
                || !k1Att.getDBType().getAttType().isType(type.getK1Type())
                || !k2Att.getDBType().getAttType().isType(type.getK2Type())) {
            return Collections.emptyList();
        }

        return findMandatoryAndOptionalAttsOrder(atts, type.getMandatories(), type.getOptionals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1Att, k2Att, orderedAtts, type);
    }
}
