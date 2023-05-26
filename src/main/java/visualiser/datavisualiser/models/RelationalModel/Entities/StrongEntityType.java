package visualiser.datavisualiser.models.RelationalModel.Entities;

import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

import java.util.HashSet;

public class StrongEntityType extends EntityType {
    private StrongEntityType(String name, HashSet<PrimaryAttribute> primaryAttributes) {
        super(name, primaryAttributes);
    }

    public StrongEntityType(Relation strongRelation) {
        this(strongRelation.getName(), strongRelation.getPrimaryKeySet());
    }
}
