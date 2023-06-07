package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.HashSet;
import java.util.Objects;

public class NAryRelationship extends Relationship {

    private final String relationshipName;
    private final Relation relationshipRelation;
    private HashSet<Attribute> descriptiveAttributes;

    public NAryRelationship(Relation relationshipRelation, Relation a, Relation b) {
        super(a, b);
        this.relationshipName = relationshipRelation.getName();
        this.relationshipRelation = relationshipRelation;
    }

    @Override
    public String getName() {
        return generateName(getA(), getB());
    }

    public static String generateName(Relation a, Relation b) {
        return a.getName() + " ~ " + b.getName();
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public Relation getRelationshipRelation() {
        return relationshipRelation;
    }

    public void setDescriptiveAttributes(HashSet<Attribute> descriptiveAttributes) {
        this.descriptiveAttributes = descriptiveAttributes;
    }

    public boolean isReflexive() {
        return getA().equals(getB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationshipRelation);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NAryRelationship eq)) {
            return false;
        }

        return relationshipRelation.equals(eq.relationshipRelation);
    }
}
