package visualiser.datavisualiser.models.ERModel.Entities;

import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.HashSet;

public class StrongEntityType extends EntityType {
    private StrongEntityType(String name, HashSet<PrimaryAttribute> primaryAttributes) {
        super(name, primaryAttributes);
    }

    public StrongEntityType(Relation strongRelation) {
        this(strongRelation.getName(), strongRelation.getPrimaryKeySet());
    }
}
