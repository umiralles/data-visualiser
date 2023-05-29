package visualiser.datavisualiser.models.ERModel.Relations;

import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.ForeignAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;

import java.util.*;
import java.util.stream.Collectors;

public class Relation {

    private final String name;
    private final PrimaryKey primaryKey;
    // Attributes not part of the primary key
    private final HashSet<Attribute> otherAttributes;
    private HashSet<ForeignAttribute> foreignKeys;
    private HashSet<Attribute> nonKeys;
    private HashSet<PrimaryAttribute> primaryKeyAtts;
    private HashSet<PrimaryAttribute> generalKeys;
    private HashSet<PrimaryAttribute> danglingKeys;

    private RelationType type = null;

    private Relation(String name, PrimaryKey primaryKeys, HashSet<Attribute> otherAttributes,
                     HashSet<ForeignAttribute> foreignKeys, HashSet<Attribute> nonKeys, HashSet<PrimaryAttribute> primaryKeyAtts,
                     HashSet<PrimaryAttribute> generalKeys, HashSet<PrimaryAttribute> danglingKeys) {
        this.name = name;
        this.primaryKey = primaryKeys;
        this.otherAttributes = otherAttributes;
        this.foreignKeys = foreignKeys;
        this.nonKeys = nonKeys;
        this.primaryKeyAtts = primaryKeyAtts;
        this.generalKeys = generalKeys;
        this.danglingKeys = danglingKeys;
    }

    public Relation(String name, PrimaryKey primaryKeys, HashSet<Attribute> otherAttributes) {
        this(name, primaryKeys, otherAttributes, null, null, null, null, null);
    }

    public String getName() {
        return name;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public HashSet<PrimaryAttribute> getPrimaryKeySet() {
        return primaryKey.getPAttributes();
    }

    public HashSet<Attribute> getOtherAttributes() {
        return otherAttributes;
    }

    public HashSet<PrimaryAttribute> getGeneralKeys() {
        return generalKeys;
    }

    public HashSet<PrimaryAttribute> getDanglingKeys() {
        return danglingKeys;
    }

    public HashSet<Attribute> getNonKeys() {
        return nonKeys;
    }

    public RelationType getType() {
        return type;
    }

    public void setType(RelationType type) {
        this.type = type;
    }

    public void setForeignKeys(HashSet<ForeignAttribute> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public void setNonKeys(HashSet<Attribute> nonKeys) {
        this.nonKeys = nonKeys;
    }

    public HashSet<PrimaryAttribute> getPrimaryKeyAtts() {
        return primaryKeyAtts;
    }

    public void setPrimaryKeyAtts(HashSet<PrimaryAttribute> primaryKeyAtts) {
        this.primaryKeyAtts = primaryKeyAtts;
    }

    public void setGeneralKeys(HashSet<PrimaryAttribute> generalKeys) {
        this.generalKeys = generalKeys;
    }

    public void setDanglingKeys(HashSet<PrimaryAttribute> danglingKeys) {
        this.danglingKeys = danglingKeys;
    }

    public HashSet<ForeignAttribute> getAllForeignAttributes() {
        // Get foreign attributes from the primary key
        HashSet<ForeignAttribute> foreignAttributes = new HashSet<>(primaryKey.getForeignAttributes());

        if (foreignKeys != null) {
            foreignAttributes.addAll(foreignKeys);
        }

        return foreignAttributes;
    }

    public boolean isEntityRelation() {
        return type == RelationType.STRONG || type == RelationType.WEAK;
    }

    public boolean hasNonPKForeignKey(Attribute attribute) {
        if (foreignKeys == null) {
            return false;
        }

        return foreignKeys.stream().map(fk -> (Attribute) fk).collect(Collectors.toCollection(HashSet::new)).contains(attribute);
    }

    public boolean hasNonKey(Attribute attribute) {
        return nonKeys.contains(attribute);
    }

    // returns the equivalent primary attribute to 'attribute'
    //  if none match, then return null
    public PrimaryAttribute findInPrimaryKey(Attribute attribute) {
        return primaryKey.findInPAttributes(attribute);
    }

    public PrimaryAttribute findInPrimaryKey(String column) {
        return primaryKey.findInPAttributes(column);
    }

    // returns the equivalent primary attribute that has been exported to 'attribute'
    //  if none match, then return null
    public PrimaryAttribute findExportedInPrimaryKey(Attribute attribute) {
        return primaryKey.getPAttributeExportedTo(attribute);
    }

    // returns the equivalent primary key attribute to 'attribute'
    //  if none match, then return null
    public PrimaryAttribute findInPrimaryKeyAtts(Attribute attribute) {
        for (PrimaryAttribute primaryAttribute : primaryKeyAtts) {
            if (primaryAttribute.isEquivalent(attribute)) {
                return primaryAttribute;
            }
        }

        return null;
    }

    // Checks if the KeyAttribute 'attribute' is contained in this relation's key
    public boolean exportsPrimaryAttributeTo(Attribute attribute) {
        return findPrimaryAttributeExportedTo(attribute) != null;
    }

    // gets the equivalent attribute in this primary key corresponding to the given attribute
    //  if it is the attribute or has been exported to the attribute
    public PrimaryAttribute findPrimaryAttributeExportedTo(Attribute attribute) {
        return primaryKey.getPAttributeExportedTo(attribute);
    }

    // Gets attributes that were exported to Attributes in 'attributes'
    // attExportedTo[0] = this attributes
    // attExportedTo[1] = 'attributes' attributes
    public List<ArrayList<Attribute>> findAttsExportedTo(HashSet<? extends Attribute> attributes) {
        return primaryKey.attsExportedTo(attributes);
    }

    // Gets attributes that were exported to Attributes in 'expRelation'
    // attExportedTo[0] = this attributes
    // attExportedTo[1] = 'expRelation' attributes
    public List<ArrayList<Attribute>> findAttsExportedTo(Relation expRelation) {
        HashSet<Attribute> allExpRelationAttributes = new HashSet<>(expRelation.getPrimaryKeySet());
        allExpRelationAttributes.addAll(expRelation.otherAttributes);

        return findAttsExportedTo(allExpRelationAttributes);
    }

    public boolean exportedToAllFromPrimaryKey(HashSet<PrimaryAttribute> attributes) {
        for (PrimaryAttribute attribute : attributes) {
            if (findExportedInPrimaryKey(attribute) == null) {
                return false;
            }
        }

        return true;
    }

    public boolean containsAllInPrimaryKeyAtts(HashSet<PrimaryAttribute> attributes) {
        for (PrimaryAttribute attribute : attributes) {
            if (findInPrimaryKeyAtts(attribute) == null) {
                return false;
            }
        }

        return true;
    }

    public boolean matchesPrimaryKeyAtts(Relation otherRelation) {
        HashSet<PrimaryAttribute> otherPKAtts = (HashSet<PrimaryAttribute>) otherRelation.getPrimaryKeyAtts().clone();
        for (PrimaryAttribute thisPKAtt : primaryKeyAtts) {
            PrimaryAttribute otherPKAtt = PrimaryAttribute.findEquivalentAttIn(otherPKAtts, thisPKAtt);

            if (otherPKAtt == null) {
                return false;
            }

            otherPKAtts.remove(otherPKAtt);
        }

        return otherPKAtts.size() == 0;
    }

    // Checks if any of the primary attributed of 'relation' are exported from this relation
    public boolean sharesPrimaryAttribute(Relation relation) {
        return findSharedPrimaryAttributes(relation).get(0).size() > 0;
    }

    // getSharedPrimaryAttributes[0] = this attributes
    // getSharedPrimaryAttributes[1] = relation attributes
    public List<List<PrimaryAttribute>> findSharedPrimaryAttributes(Relation relation) {
        return primaryKey.sharedAttributes(relation.primaryKey);
    }

    // getSharedPrimaryAttributes[0] = this attributes
    // getSharedPrimaryAttributes[1] = relation attributes
    public List<List<Attribute>> findSharedPrimaryAttributes(HashSet<? extends Attribute> attributes) {
        return primaryKey.sharedAttributes(attributes);
    }

    public Attribute findAttribute(String name) {
        Attribute att = primaryKey.findInPAttributes(name);
        if (att != null) {
            return att;
        }

        Optional<Attribute> opAtt = otherAttributes.stream()
                .dropWhile(otherAtt -> !otherAtt.getColumn().equals(name)).findFirst();
        return opAtt.orElse(null);
    }

    // If one of the attributes isn't found, return null
    public ArrayList<Attribute> findAttributes(List<String> attNames) {
        ArrayList<Attribute> atts = new ArrayList<>();
        for (String name : attNames) {
            Attribute att = findAttribute(name);

            if (att != null) {
                atts.add(att);
                continue;
            }

            return null;
        }

        return atts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Relation eq)) {
            return false;
        }

        return name.equals(eq.name);
    }
}