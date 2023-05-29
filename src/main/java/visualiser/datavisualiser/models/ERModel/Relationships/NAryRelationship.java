package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.HashSet;
import java.util.Objects;

public class NAryRelationship extends Relationship {

    private final String relationshipName;
    private final Relation relationship;
    private HashSet<Attribute> descriptiveAttributes;

    public NAryRelationship(Relation relationship, Relation a, Relation b) {
        super(a, b);
        this.relationshipName = relationship.getName();
        this.relationship = relationship;
    }

    @Override
    public String getName() {
        return generateName(getA(), getB());
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setDescriptiveAttributes(HashSet<Attribute> descriptiveAttributes) {
        this.descriptiveAttributes = descriptiveAttributes;
    }

    public boolean isReflexive() {
        return getA().equals(getB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationship);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NAryRelationship eq)) {
            return false;
        }

        return relationship.equals(eq.relationship);
    }
}
