package visualiser.datavisualiser.models.RelationalModel.Relationships;

import visualiser.datavisualiser.models.RelationalModel.InclusionDependency;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.Objects;

public class BinaryRelationship extends Relationship {

    private final Attribute x1;
    private final Attribute x2;

    // represents a binary relationship where a is 0:1 and b is 0:N
    public BinaryRelationship(Relation a, Attribute x1, Relation b, Attribute x2) {
        super(a, b);

        this.x1 = x1;
        this.x2 = x2;
    }

    public BinaryRelationship(InclusionDependency id) {
        this(id.getA(), id.getX1(), id.getB(), id.getX2());
    }

    public Attribute getX1() {
        return x1;
    }

    public Attribute getX2() {
        return x2;
    }

    public static String generateName(String table1, String column1, String table2, String column2) {
        return table1 + "." + column1 + " < " + table2 + "." + column2;
    }

    @Override
    public String getName() {
        return generateName(x1.getTable(), x1.getColumn(), x2.getTable(), x2.getColumn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getA(), getB(), x1, x2);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BinaryRelationship eq)) {
            return false;
        }

        return getA().equals(eq.getA()) && getB().equals(eq.getB()) && x1.equals(eq.x1) && x2.equals(eq.x2);
    }
}
