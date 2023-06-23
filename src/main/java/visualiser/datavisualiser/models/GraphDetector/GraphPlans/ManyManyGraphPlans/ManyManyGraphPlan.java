package visualiser.datavisualiser.models.GraphDetector.GraphPlans.ManyManyGraphPlans;

import visualiser.datavisualiser.models.ERModel.AttributeType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphAttribute;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;

import java.util.*;

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

    public PrimaryKey getK1() {
        return k1;
    }

    public PrimaryKey getK2() {
        return k2;
    }

    protected List<GraphAttribute> getOrderedMandatories() {
        return orderedMandatoryAtts;
    }

    protected List<GraphAttribute> getOrderedOptionals() {
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

    public boolean fitKTypes(PrimaryKey k1, PrimaryKey k2) {
        Set<PrimaryAttribute> k1Atts = k1.getPAttributes();
        Set<PrimaryAttribute> k2Atts = k2.getPAttributes();

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

    public Set<GraphPlan> fitAttributesToPlan(PrimaryKey k1, PrimaryKey k2, List<Attribute> unorderedAtts) {
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

            plans.add(getInstance(k1, k2, possibleMandOrder, possibleOptOrder));
        }

        return plans;
    }

    @Override
    public int hashCode() {
        return Objects.hash(k1, k2, orderedMandatoryAtts, orderedOptionalAtts);
    }
}
