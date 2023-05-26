package visualiser.datavisualiser.models.RelationalModel.Relationships;

import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;

public class InclusionRelationship extends Relationship {
    // This is a binary relationship type in the eer diagram

    // Inclusion relationships always:
    //  - are two strong entities
    //  - match primary key attributes
    //  - have an inclusion dependency a.X < b.X
    //  - if there is NO inclusion dependency b.X < a.X then it is an is_a relationship

    // CONSTANTS
    public static boolean IS_A_TYPE = true;
    public static boolean UNKNOWN_TYPE = false;

    // If the relationship has been identified as an is-a relationship
    //  false could mean a is-a b, a is-a-kind-of b, a is-part-of b, a has b etc.
    private final boolean is_a;

    // Identifies a relationship a < b (eg. a is-a b)
    public InclusionRelationship(Relation a, Relation b, boolean is_a) {
        super(a, b);
        this.is_a = is_a;
    }

    public boolean isA() {
        return is_a;
    }

    public String printIsA() {
        if (is_a) {
            return "IS_A";
        }

        return "UNKNOWN";
    }
}
