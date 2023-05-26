package visualiser.datavisualiser.models.RelationalModel.Entities;

import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.HashSet;

public class WeakEntityType extends EntityType {

    private final String ownerName;

    private final HashSet<Relation> possibleOwners;

    private WeakEntityType(String name, HashSet<PrimaryAttribute> danglingAttributes, String ownerName,
                           HashSet<Relation> possibleOwners) {
        super(name, danglingAttributes);
        this.ownerName = ownerName;
        this.possibleOwners = possibleOwners;
    }

    public WeakEntityType(Relation weakRelation, Relation weakOwner, HashSet<Relation> possibleOwners) {
        this(weakRelation.getName(), weakRelation.getDanglingKeys(), weakOwner.getName(), possibleOwners);
    }

    public String getOwnerName() {
        return ownerName;
    }
}
