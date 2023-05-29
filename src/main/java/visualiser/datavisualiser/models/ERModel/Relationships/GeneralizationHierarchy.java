package visualiser.datavisualiser.models.ERModel.Relationships;

import visualiser.datavisualiser.models.ERModel.Relations.Relation;

import java.util.HashSet;

public class GeneralizationHierarchy {

    // for A is_a B, A is specific and B is generic
    private final Relation generic;
    private final HashSet<Relation> specifics;

    public GeneralizationHierarchy(Relation generic, HashSet<Relation> specifics) {
        this.generic = generic;
        this.specifics = specifics;
    }

    public String getName() {
        return generic.getName();
    }
}
