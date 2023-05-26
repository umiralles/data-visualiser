package visualiser.datavisualiser.models.RelationalModel.Keys;

import visualiser.datavisualiser.models.RelationalModel.DBType;

public class ForeignAttribute extends Attribute {

    private final String fKName;
    private final String pkName;
    private final Attribute pkAttribute;

    public ForeignAttribute(String name, String pkName, Attribute pkAttribute, String table, String column, DBType type) {
        super(table, column, type);
        this.fKName = name;
        this.pkName = pkName;
        this.pkAttribute = pkAttribute;
    }

    public ForeignAttribute(String fkName, String pkName, Attribute pkAttribute, Attribute baseAttribute) {
        this(fkName, pkName, pkAttribute, baseAttribute.getTable(), baseAttribute.getColumn(), baseAttribute.getDBType());
    }

    public ForeignAttribute(String fkName, String pkName, String pkTable, String pkColumn, Attribute baseAttribute) {
        this(fkName, pkName, new Attribute(pkTable, pkColumn, baseAttribute.getDBType()), baseAttribute);
    }

    public String getFKName() {
        return fKName;
    }

    public String getPKName() {
        return pkName;
    }

    public String getRelationImportedFrom() {
        return pkAttribute.getTable();
    }
}
