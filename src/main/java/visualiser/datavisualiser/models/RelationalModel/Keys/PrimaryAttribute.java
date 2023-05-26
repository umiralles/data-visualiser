package visualiser.datavisualiser.models.RelationalModel.Keys;

import visualiser.datavisualiser.models.RelationalModel.DBType;

import java.util.HashSet;

public class PrimaryAttribute extends Attribute {
//
//    private final KeyType type;

    private final String pKName;

    // Name imported from. If null, then not a foreign key
    private final String impKeyName;
    private final Attribute impAttribute;

    // Foreign key attributes
    //  names, tables and columns should always be the same length
    private final HashSet<ForeignAttribute> fAttributes;

    // TODO: Make it so that fAttributes are added to constructor if possible (i.e. remove addInstance)
    public PrimaryAttribute(String name, String table, String column, DBType type, HashSet<ForeignAttribute> fAttributes,
                            String impKeyName, Attribute impAttribute) {
//        this.type = type;
        super(table, column, type);
        this.pKName = name;
        this.fAttributes = fAttributes;
        this.impKeyName = impKeyName;
        this.impAttribute = impAttribute;
    }

    public PrimaryAttribute(String name, Attribute baseAttribute) {
        this(name, baseAttribute.getTable(), baseAttribute.getColumn(), baseAttribute.getDBType(), new HashSet<>(), null, null);
    }

    public PrimaryAttribute(PrimaryAttribute baseAttribute, String impKeyName, String impTable, String impColumn) {
        this(baseAttribute.getPKName(), baseAttribute.getTable(), baseAttribute.getColumn(), baseAttribute.getDBType(),
                baseAttribute.fAttributes, impKeyName, new Attribute(impTable, impColumn, baseAttribute.getDBType()));
    }

    public String getPKName() {
        return pKName;
    }

    public String getImpKeyName() { return impKeyName; }

    public Attribute getImpAttribute() { return impAttribute; }

    public boolean isForeign() {
        return impKeyName != null;
    }

    public void addInstance(String name, String table, String column, DBType type) {
        fAttributes.add(new ForeignAttribute(name, pKName, this, table, column, type));
    }

    public boolean isKey(String keyName) {
        return keyName.equals(this.getPKName());
    }

    public boolean exportedTo(String table, String column) {
        for (ForeignAttribute fAttribute : fAttributes) {
            if (fAttribute.isAttribute(table, column)) {
                return true;
            }
        }

        return false;
    }

    public boolean exportedTo(Attribute attribute) {
        return exportedTo(attribute.getTable(), attribute.getColumn());
    }

    public boolean isEquivalent(String table, String column) {
        return exportedTo(table, column);
    }

    public boolean isEquivalent(Attribute eqAtt) {
        if (exportedTo(eqAtt)) {
            return true;
        }

        if (eqAtt instanceof PrimaryAttribute eqKey) {
            return eqKey.exportedTo(this);
        }

        return false;
    }

    public static PrimaryAttribute findEquivalentAttIn(HashSet<PrimaryAttribute> pAtts, Attribute findAtt) {
        for (PrimaryAttribute pAtt : pAtts) {
            if (pAtt.isEquivalent(findAtt)) {
                return pAtt;
            }
        }

        return null;
    }


//    public enum KeyType {
//        PRIMARY,
//        FOREIGN,
//        GENERAL,
//        DANGLING,
//        NON,
//    }
}
