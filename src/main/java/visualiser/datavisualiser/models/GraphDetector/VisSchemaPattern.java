package visualiser.datavisualiser.models.GraphDetector;

public enum VisSchemaPattern {
    BASIC_ENTITY("Basic Entity"),
    WEAK_ENTITY("Weak Entity"),
    ONE_MANY_REL("One-Many Relationship"),
    MANY_MANY_REL("Many-Many Relationship"),
    REFLEXIVE("Reflexive Relationship");

    private final String name;

    VisSchemaPattern(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static VisSchemaPattern getVisSchemaPattern(String name) {
        for (VisSchemaPattern pattern : VisSchemaPattern.values()) {
            if (pattern.name.equals(name)) {
                return pattern;
            }
        }

        return null;
    }
}
