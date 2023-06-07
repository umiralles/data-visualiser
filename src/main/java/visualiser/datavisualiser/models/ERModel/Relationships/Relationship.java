package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.Relations.Relation;

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

    public abstract String getName();

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
