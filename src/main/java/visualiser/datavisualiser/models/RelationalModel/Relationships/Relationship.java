package visualiser.datavisualiser.models.RelationalModel.Relationships;

import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.Objects;

public abstract class Relationship {

    private final Relation a;
    private final Relation b;

    protected Relationship(Relation a, Relation b) {
        this.a = a;
        this.b = b;
    }

    public Relation getA() {
        return a;
    }

    public Relation getB() {
        return b;
    }

    public String getName() {
        return generateName(a, b);
    }

    public static String generateName(Relation a, Relation b) {
        return a.getName() + "<" + b.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Relationship eq)) {
            return false;
        }

        return a.equals(eq.a) && b.equals(eq.b);
    }
}
