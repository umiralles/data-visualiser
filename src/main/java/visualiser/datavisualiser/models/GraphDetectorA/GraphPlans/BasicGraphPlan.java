package visualiser.datavisualiser.models.GraphDetectorA.GraphPlans;

import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.*;

public class BasicGraphPlan extends GraphPlan {

    private final BasicGraphType type;
    private final Attribute kAtt;
    private final List<Attribute> orderedAtts;

    private BasicGraphPlan(BasicGraphType type, Attribute kAtt, List<Attribute> orderedAtts) {
        this.kAtt = kAtt;
        this.orderedAtts = orderedAtts;
        this.type = type;
    }

    @Override
    public void generateGraph() {
        // TODO: implement
    }

    // Returns a map of a graph type to the possible graph plans
    public static Map<BasicGraphType, Set<BasicGraphPlan>> matchToGraphs(Attribute kAtt, List<Attribute> atts) {
        Map<BasicGraphType, Set<BasicGraphPlan>> plans = new HashMap<>();
        for (BasicGraphType type : BasicGraphType.values()) {
            Set<BasicGraphPlan> typePlans = fitTypeToPlan(type, kAtt, atts);
            if (!typePlans.isEmpty()) {
                plans.put(type, typePlans);
            }
        }

        return plans;
    }

    // Returns null if type does not fit attributes
    public static Set<BasicGraphPlan> fitTypeToPlan(BasicGraphType type, Attribute kAtt, List<Attribute> atts) {
        Set<BasicGraphPlan> plans = new HashSet<>();
        List<List<Attribute>> possibleOrders = orderAttributesByType(type, kAtt, atts);

        for (List<Attribute> possibleOrder : possibleOrders) {
            plans.add(new BasicGraphPlan(type, kAtt, possibleOrder));
        }

        return plans;
    }

    // not nullable
    private static List<List<Attribute>> orderAttributesByType(BasicGraphType type, Attribute kAtt, List<Attribute> atts) {
        int attSize = atts.size();
        int smallestAttsSize = type.getMandatories().size();
        int largestAttsSize = smallestAttsSize + type.getOptionals().size();

        if (attSize < smallestAttsSize || attSize > largestAttsSize
            || !kAtt.getDBType().getAttType().isType(type.getkType())) {
            return Collections.emptyList();
        }

        return findMandatoryAndOptionalAttsOrder(atts, type.getMandatories(), type.getOptionals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kAtt, orderedAtts, type);
    }
}
