package visualiser.datavisualiser.models.RelationalModel.Keys;

import java.util.*;
import java.util.stream.Collectors;

public class PrimaryKey {

    private final String pkName;
    private final HashSet<PrimaryAttribute> pAttributes;

    public PrimaryKey(String name, HashSet<PrimaryAttribute> pAttributes) {
        this.pkName = name;
        this.pAttributes = pAttributes;
    }

    public String getPkName() {
        return pkName;
    }

    public HashSet<PrimaryAttribute> getPAttributes() {
        return pAttributes;
    }

    public int size() {
        return pAttributes.size();
    }

    // Returns the foreign attribute version of primary attributes that are also foreign keys
    public HashSet<ForeignAttribute> getForeignAttributes() {
        return pAttributes.stream().filter(PrimaryAttribute::isForeign)
                .map(pAtt -> new ForeignAttribute(pAtt.getPKName(), pAtt.getImpKeyName(), pAtt.getImpAttribute(), pAtt))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public PrimaryAttribute findInPAttributes(String name) {
        Optional<PrimaryAttribute> opAtt = pAttributes.stream().dropWhile(att -> !att.getColumn().equals(name)).findFirst();

        return opAtt.orElse(null);
    }

    // gets the equivalent attribute in this primary key corresponding to the given attribute
    public PrimaryAttribute findInPAttributes(Attribute expAtt) {
        for (PrimaryAttribute pAtt : pAttributes) {
            if (pAtt.isEquivalent(expAtt)) {
                return pAtt;
            }
        }

        return null;
    }

    // gets the equivalent attribute in this primary key corresponding to the given attribute
    //  if it is the attribute or has been exported to the attribute
    public PrimaryAttribute getPAttributeExportedTo(Attribute expAtt) {
        for (PrimaryAttribute pAtt : pAttributes) {
            if (pAtt.isAttribute(expAtt) || pAtt.exportedTo(expAtt)) {
                return pAtt;
            }
        }

        return null;
    }

    // Has same functionality as sharedAttributes below, but preserves PrimaryAttribute class
    // sharedAttribute[0] = this attributes
    // sharedAttribute[1] = otherKey attributes
    public List<ArrayList<PrimaryAttribute>> sharedAttributes(PrimaryKey otherKey) {
        ArrayList<PrimaryAttribute> thisAttributes = new ArrayList<>();
        ArrayList<PrimaryAttribute> otherKeyAttributes = new ArrayList<>();

        for (PrimaryAttribute otherAtt : otherKey.pAttributes) {
            PrimaryAttribute thisAtt = findInPAttributes(otherAtt);

            if (thisAtt != null) {
                thisAttributes.add(thisAtt);
                otherKeyAttributes.add(otherAtt);
            }
        }

        return Arrays.asList(thisAttributes, otherKeyAttributes);
    }

    // sharedAttribute[0] = this attributes
    // sharedAttribute[1] = otherKey attributes
    public List<ArrayList<Attribute>> sharedAttributes(HashSet<? extends Attribute> otherAttributes) {
        ArrayList<Attribute> thisAttributes = new ArrayList<>();
        ArrayList<Attribute> otherKeyAttributes = new ArrayList<>();

        for (Attribute otherAtt : otherAttributes) {
            PrimaryAttribute thisAtt = findInPAttributes(otherAtt);

            if (thisAtt != null) {
                thisAttributes.add(thisAtt);
                otherKeyAttributes.add(otherAtt);
            }
        }

        return Arrays.asList(thisAttributes, otherKeyAttributes);
    }

    // attExportedTo[0] = this attributes
    // attExportedTo[1] = expRelation attributes
    public List<ArrayList<Attribute>> attsExportedTo(PrimaryKey expToKey) {
        return attsExportedTo(expToKey.pAttributes);
    }

    // attExportedTo[0] = this attributes (PrimaryAttribute)
    // attExportedTo[1] = expRelation attributes (Attribute)
    public List<ArrayList<Attribute>> attsExportedTo(HashSet<? extends Attribute> expToAttributes) {
        ArrayList<Attribute> thisAttributes = new ArrayList<>();
        ArrayList<Attribute> expToKeyAttributes = new ArrayList<>();

        for (PrimaryAttribute thisAtt : pAttributes) {
            for (Attribute otherAtt : expToAttributes) {
                if (thisAtt.exportedTo(otherAtt)) {
                    thisAttributes.add(thisAtt);
                    expToKeyAttributes.add(otherAtt);
                }
            }
        }

        return Arrays.asList(thisAttributes, expToKeyAttributes);
    }
}
