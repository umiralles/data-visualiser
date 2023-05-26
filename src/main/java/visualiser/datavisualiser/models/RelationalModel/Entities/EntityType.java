package visualiser.datavisualiser.models.RelationalModel.Entities;

import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;

import java.util.HashSet;
import java.util.Objects;

public abstract class EntityType {
    private final String name;
    private final HashSet<PrimaryAttribute> primaryAttributes;
    private HashSet<Attribute> descriptiveAttributes;

    EntityType(String name, HashSet<PrimaryAttribute> primaryAttributes) {
        this.name = name;
        this.primaryAttributes = primaryAttributes;
    }

    public String getName() {
        return name;
    }

    public HashSet<PrimaryAttribute> getPrimaryAttributes() {
        return primaryAttributes;
    }

    public HashSet<Attribute> getDescriptiveAttributes() {
        return descriptiveAttributes;
    }

    public void setDescriptiveAttributes(HashSet<Attribute> descriptiveAttributes) {
        this.descriptiveAttributes = descriptiveAttributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EntityType eq)) {
            return false;
        }

        return name.equals(eq.name);
    }
}
