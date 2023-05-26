package visualiser.datavisualiser.models.RelationalModel;

import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;

import java.util.ArrayList;
import java.util.List;

public enum AttributeType {
    /* DEFINITIVE TYPES */
    TEMPORAL, // Is scalar
    GEOGRAPHICAL, // Is lexical and discrete

    /* UNSURE TYPES */
    LEXICAL, // Could be geographical, is discrete
    SCALAR, // Could be a colour
    DISCRETE, // Could be a colour
    COLOUR,
    ANY,
    // A type that cannot be put into a graph (e.g. Bytes, Arrays)
    INVALID;

    AttributeType() {}

//    public boolean isScalar() {
//        return switch (this) {
//            case SCALAR, TEMPORAL -> true;
//            case DISCRETE, GEOGRAPHICAL, LEXICAL, INVALID, COLOUR, ANY -> false;
//        };
//    }
//
//    public boolean isDiscrete() {
//        return switch (this) {
//            case DISCRETE, GEOGRAPHICAL, LEXICAL -> true;
//            case SCALAR, TEMPORAL, INVALID, COLOUR, ANY -> false;
//        };
//    }
//
//    public boolean canBeColour() {
//        return switch (this) {
//            case SCALAR, DISCRETE, TEMPORAL, GEOGRAPHICAL, LEXICAL, COLOUR -> true;
//            case INVALID, ANY -> false;
//        };
//    }

    // returns true if encasing type is an encasing type of this
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
            case SCALAR, DISCRETE -> encasingType == AttributeType.COLOUR || encasingType == AttributeType.ANY;
            case COLOUR -> encasingType == AttributeType.ANY;
            case ANY, INVALID -> false;
        };
    }

//    // returns true if type2 IS DEFINITELY type1 (NOT type1 is type2)
//    public static boolean matches(AttributeType type1, AttributeType type2) {
//        if (type1.equals(type2)) {
//            return true;
//        }
//
//        if (type2 == AttributeType.INVALID) {
//            return false;
//        }
//
//        return switch (type1) {
//            case ANY -> true;
//            case SCALAR -> type2.isScalar();
//            case DISCRETE -> type2.isDiscrete();
//            case COLOUR -> type2.canBeColour();
//            case GEOGRAPHICAL, LEXICAL -> type2 == AttributeType.GEO_OR_LEXICAL;
//            case GEO_OR_LEXICAL -> type2 == AttributeType.LEXICAL || type2 == AttributeType.GEOGRAPHICAL;
//            case INVALID, TEMPORAL -> false; // the 'smallest' case, type2 must be temporal if type1 is temporal
//        };
//    }

    // return -1 if no index is found
    public static ArrayList<Integer> findMatchingIndices(AttributeType type, List<Attribute> atts) {
        ArrayList<Integer> possibleIdxs = new ArrayList<>();
        for (int i = 0; i < atts.size(); i++) {
            if (atts.get(i).getDBType().getAttType().isType(type)) {
                possibleIdxs.add(i);
            }
        }

        return possibleIdxs;
    }
}
