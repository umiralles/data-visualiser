package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.ERModel.InclusionDependency;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.List;
import java.util.Objects;

public class BinaryRelationship extends Relationship {

    private final List<Attribute> x1s;
    private final List<Attribute> x2s;

    private final boolean isComplete;

    // represents a binary relationship where a is 0:1 and b is 0:N
    public BinaryRelationship(Relation a, List<Attribute> x1s, Relation b, List<Attribute> x2s, boolean isComplete) {
        super(a, b);

        this.x1s = x1s;
        this.x2s = x2s;
        this.isComplete = isComplete;
    }

    public BinaryRelationship(List<InclusionDependency> ids) {
        this(ids.get(0).getA(), ids.stream().map(InclusionDependency::getX1).toList(),
                ids.get(0).getB(), ids.stream().map(InclusionDependency::getX2).toList(),
                ids.stream().allMatch(InclusionDependency::isCovered));
        // TODO: All ids should be for the same relations, ids must contain at least one item
    }

    public boolean isWeakRelationship(ERModel rm) {
        EntityType e1 = rm.getEntity(getA().getName());
        EntityType e2 = rm.getEntity(getB().getName());

        return e1 instanceof WeakEntityType && ((WeakEntityType) e1).getOwnerName().equals(e2.getName());
    }

    public List<Attribute> getX1s() {
        return x1s;
    }

    public List<Attribute> getX2s() {
        return x2s;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public static String generateName(Relation a, Relation b) {
        return a.getName() + " << " + b.getName();
    }

    @Override
    public String getName() {
        return generateName(getA(), getB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getA(), getB(), x1s, x2s);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BinaryRelationship eq)) {
            return false;
        }

        return getA().equals(eq.getA()) && getB().equals(eq.getB()) && x1s.equals(eq.x1s) && x2s.equals(eq.x2s);
    }
}
