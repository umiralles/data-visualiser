package visualiser.datavisualiser.models.RelationalModel;

import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.List;
import java.util.stream.IntStream;

public enum AttributeType {
    /* DEFINITIVE TYPES */
    TEMPORAL, // Is scalar
    GEOGRAPHICAL, // Is lexical and discrete

    /* UNSURE TYPES */
    LEXICAL, // Could be geographical, is discrete
    SCALAR, // Could be a colour and could be discrete
    DISCRETE, // Could be a colour
    COLOUR,
    ANY,
    // A type that cannot be put into a graph (e.g. Bytes, Arrays)
    INVALID;

    // returns true if encasing type is an encasing type or the same as this type
    public boolean isType(AttributeType encasingType) {
        if (equals(encasingType)) {
            return true;
        }

        return switch (this) {
            case TEMPORAL -> encasingType == AttributeType.SCALAR || encasingType == AttributeType.COLOUR
                    || encasingType == AttributeType.ANY;
            case GEOGRAPHICAL -> encasingType == AttributeType.LEXICAL || encasingType == AttributeType.DISCRETE
                    || encasingType == AttributeType.COLOUR || encasingType == AttributeType.ANY;
            case LEXICAL -> encasingType == AttributeType.GEOGRAPHICAL || encasingType == AttributeType.DISCRETE
                    || encasingType == AttributeType.COLOUR || encasingType == AttributeType.ANY;
            case SCALAR -> encasingType == AttributeType.DISCRETE
                    || encasingType == AttributeType.COLOUR || encasingType == AttributeType.ANY;
            case DISCRETE -> encasingType == AttributeType.COLOUR || encasingType == AttributeType.ANY;
            case COLOUR -> encasingType == AttributeType.ANY;
            case ANY, INVALID -> false;
        };
    }

    public static List<Integer> findMatchingIndices(AttributeType encasingType, List<Attribute> atts) {
        return IntStream.range(0, atts.size())
                .filter(idx -> atts.get(idx).getDBType().getAttType().isType(encasingType))
                .boxed().toList();
    }

    public static List<Attribute> findMatchingAttributes(AttributeType encasingType, List<Attribute> atts) {
        return atts.stream().filter(att -> att.getDBType().getAttType().isType(encasingType)).toList();
    }
}
