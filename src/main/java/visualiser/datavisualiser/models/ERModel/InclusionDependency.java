package visualiser.datavisualiser.models.ERModel;

import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class InclusionDependency {
    // Represents a possible inclusion dependency A.x1 < B.x2
    private final Relation a;
    private final Attribute x1;
    private final Relation b;
    private final Attribute x2;

    // There is a full covering of all values
    private boolean isCovered = false;

    public InclusionDependency(Relation a, Attribute x1, Relation b, Attribute x2) {
        this.a = a;
        this.x1 = x1;
        this.b = b;
        this.x2 = x2;
    }

    public Relation getA() {
        return a;
    }

    public Attribute getX1() {
        return x1;
    }

    public Relation getB() {
        return b;
    }

    public Attribute getX2() {
        return x2;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public void setCovered() {
        this.isCovered = true;
    }

    public String getName() {
        return generateName(x1, x2);
    }

    public static String generateName(Attribute x1, Attribute x2) {
        return x1.getTable() + "." + x1.getColumn() + " < " + x2.getTable() + "." + x2.getColumn();
    }

    public static boolean idsExistBetweenAllPrimaryKeyAtts(Relation x1, Relation x2, HashMap<String, InclusionDependency> ids) {
        HashSet<PrimaryAttribute> as = x1.getPrimaryKeyAtts();
        HashSet<PrimaryAttribute> bs = x2.getPrimaryKeyAtts();

        for (PrimaryAttribute a : as) {
            boolean aHasId = bs.stream().dropWhile(b -> ids.get(InclusionDependency.generateName(a, b)) == null)
                    .findFirst().isPresent();

            if (!aHasId) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, x1, b, x2);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InclusionDependency eq)) {
            return false;
        }

        return a.equals(eq.a) && x1.equals(eq.x1) && b.equals(eq.b) && x2.equals(eq.x2);
    }
}
