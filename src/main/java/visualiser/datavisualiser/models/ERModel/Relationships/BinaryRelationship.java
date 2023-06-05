package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.InclusionDependency;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BinaryRelationship extends Relationship {

    private final List<Attribute> x1s;
    private final List<Attribute> x2s;

    // represents a binary relationship where a is 0:1 and b is 0:N
    public BinaryRelationship(Relation a, List<Attribute> x1s, Relation b, List<Attribute> x2s) {
        super(a, b);

        this.x1s = x1s;
        this.x2s = x2s;
    }

    public BinaryRelationship(List<InclusionDependency> ids) {
        this(ids.get(0).getA(), ids.stream().map(InclusionDependency::getX1).toList(),
                ids.get(0).getB(), ids.stream().map(InclusionDependency::getX2).toList());
        // TODO: All ids should be for the same relations, ids must contain at least one item
    }

    public List<Attribute> getX1s() {
        return x1s;
    }

    public List<Attribute> getX2s() {
        return x2s;
    }

    public static String generateName(String table1, String table2) {
        return table1 + " << " + table2;
    }

    @Override
    public String getName() {
        return generateName(getA().getName(), getB().getName());
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
