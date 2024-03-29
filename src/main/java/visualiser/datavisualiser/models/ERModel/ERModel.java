package visualiser.datavisualiser.models.ERModel;

import visualiser.datavisualiser.models.DataTable.Column;
import visualiser.datavisualiser.models.DataTable.DataCell;
import visualiser.datavisualiser.models.DataTable.DataTable;
import visualiser.datavisualiser.models.DataTable.DataType;
import visualiser.datavisualiser.models.ERModel.Entities.EntityType;
import visualiser.datavisualiser.models.ERModel.Entities.StrongEntityType;
import visualiser.datavisualiser.models.ERModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.ERModel.Keys.Attribute;
import visualiser.datavisualiser.models.ERModel.Keys.ForeignAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.ERModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.ERModel.Relations.Relation;
import visualiser.datavisualiser.models.ERModel.Relations.RelationType;
import visualiser.datavisualiser.models.ERModel.Relationships.*;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ERModel {
    private final String catalog;
    private final String schemaPattern;
    private final HashMap<String, EntityType> entities;
    private final HashMap<String, Relationship> relationships;
    private final HashMap<String, GeneralizationHierarchy> hierarchies;

    // Private use variables
    private final String username;
    private final String password;
    private final String url;
    private final Connection conn;
    private final DatabaseMetaData md;
    private final ArrayList<String> tableNames;
    private final HashMap<String, Relation> relations;
    private final HashMap<String, InclusionDependency> ids;

    public ERModel(String username, String password, String url, String schemaPattern,
                   ArrayList<String> entityRelations) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.catalog = url.substring(url.lastIndexOf('/') + 1);
        this.schemaPattern = schemaPattern;

        try {
            Properties props = new Properties();
            props.setProperty("user", this.username);
            props.setProperty("password", this.password);

            this.conn = DriverManager.getConnection(this.url, props);
            this.md = conn.getMetaData();

            this.tableNames = generateTableNames();

            this.relations = collectUntypedRelations();
            if (entityRelations != null) {
                setRelationsTypes(this.relations, entityRelations);
            } else {
                setRelationsTypes(this.relations);
            }

            this.ids = collectInclusionDependencies(this.relations);
            removeInvalidInclusionDependencies(this.ids);
            removeRedundantInclusionDependencies(this.ids);

            this.entities = collectEntities(this.relations, this.ids);
            this.relationships = collectRelationships(this.relations, this.ids);
            this.hierarchies = collectGeneralizationHierarchies(this.relationships);

        } catch (SQLException e) {
            System.out.println("SQL Exception in ERModel: " + e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    public ERModel(String username, String password, String url, String schemaPattern, String[] entityRelations) {
        this(username, password, url, schemaPattern, Arrays.stream(entityRelations).collect(Collectors.toCollection(ArrayList::new)));
    }

    public ERModel(String username, String password, String url, String schemaPattern) {
        this(username, password, url, schemaPattern, (ArrayList<String>) null);
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public HashMap<String, EntityType> getEntities() {
        return entities;
    }

    public EntityType getEntity(String name) {
        return entities.get(name);
    }

    public HashMap<String, Relationship> getRelationships() {
        return relationships;
    }

    public InclusionRelationship getInclusionRelationship(Relation a, Relation b) {
        Relationship rel = relationships.get(InclusionRelationship.generateName(a, b));

        if (rel instanceof InclusionRelationship incRel) {
            return incRel;
        }

        return null;
    }

    // Maps the related relation (that encompasses 'relation') to the inclusion relationship that relates it
    public Map<Relation, InclusionRelationship> getRelatedInclusionRelationships(Relation relation) {
        Map<Relation, InclusionRelationship> incRels = new HashMap<>();

        for (Relation otherRelation : relations.values()) {
            InclusionRelationship incRel = getInclusionRelationship(relation, otherRelation);
            if (incRel == null) {
                incRel = getInclusionRelationship(otherRelation, relation);
                if (incRel != null && incRel.isA()) {
                    incRel = null;
                }
            }

            if (incRel != null) {
                incRels.put(otherRelation, incRel);
                incRels.putAll(getRelatedInclusionRelationships(otherRelation));
            }
        }

        if (incRels.containsKey(relation)) {
            InclusionRelationship rel = incRels.get(relation);
            incRels.remove(relation);
            Relation mapRel = rel.getB();
            if (mapRel.equals(relation)) {
                mapRel = rel.getA();
            }
            incRels.put(mapRel, rel);
        }

        return incRels;
    }

    public BinaryRelationship getBinaryRelationship(Relation a, Relation b) {
        Relationship rel = relationships.get(BinaryRelationship.generateName(a, b));

        if (rel instanceof BinaryRelationship binRel) {
            return binRel;
        }

        return null;
    }

    public NAryRelationship getNAryRelationship(Relation a, Relation b) {
        Relationship rel = relationships.get(NAryRelationship.generateName(a, b));

        if (rel instanceof NAryRelationship nAryRel) {
            return nAryRel;
        }

        return null;
    }

    public HashMap<String, GeneralizationHierarchy> getHierarchies() {
        return hierarchies;
    }

    public HashMap<String, Relation> getRelations() {
        return relations;
    }

    public Relation getRelation(String name) {
        return relations.get(name);
    }

    public void closeConnection() throws SQLException {
        conn.close();
    }

    public DataTable getDataTableWithAttributes(EntityType entity, Relationship rel, VisSchemaPattern pattern,
                                                Set<PrimaryKey> pks, Set<Attribute> atts) throws SQLException {
        List<PrimaryKey> pksList = new ArrayList<>(pks);
        List<List<PrimaryAttribute>> pkAtts = new ArrayList<>();
        pksList.forEach(pk -> pkAtts.add(new ArrayList<>(pk.getPAttributes())));

        // if there is a binary relationship, check if one primary key contains the other. If so, remove the
        //  similar attributes from the larger key
        if (rel instanceof BinaryRelationship && !rel.getA().equals(rel.getB()) && pattern == VisSchemaPattern.WEAK_ENTITY) {
            // There should be two primary keys
            List<List<PrimaryAttribute>> sharedAtts = pksList.get(0).sharedAttributes(pksList.get(1));
            if (pkAtts.get(0).size() > pkAtts.get(1).size()) {
                List<PrimaryAttribute> zeroSharedAtts = sharedAtts.get(0);
                pkAtts.get(0).removeAll(zeroSharedAtts);
            } else if (pkAtts.get(1).size() > pkAtts.get(0).size()) {
                List<PrimaryAttribute> oneSharedAtts = sharedAtts.get(1);
                pkAtts.get(1).removeAll(oneSharedAtts);
            }
        }

        ArrayList<Column> columns = new ArrayList<>();
        for (int i = 0; i < pksList.size(); i++) {
            PrimaryKey key = pksList.get(i);
            List<PrimaryAttribute> keyAtts = pkAtts.get(i);

            if (keyAtts.size() == 1) {
                DataType keyType = keyAtts.get(0).getDBType().getDataType();
                columns.add(new Column(keyType, key.toString(), keyAtts.get(0).toString()));
            } else {
                columns.add(new Column(DataType.STRING, key.toString(), key.getTable()));
            }
        }

        for (Attribute att : atts) {
            columns.add(new Column(att.getDBType().getDataType(), att.toString(), att.getColumn()));
        }

        Set<Attribute> queryAtts = new HashSet<>(atts);
        pkAtts.forEach(queryAtts::addAll);

        List<List<DataCell>> rows = getRowsFromQueryAndAtts(generateQuery(entity, rel, queryAtts), pkAtts, new ArrayList<>(atts));

        return new DataTable(columns, rows);
    }

    private String generateQuery(EntityType entity, Relationship rel, Set<Attribute> atts) {
        List<Attribute> attsList = new ArrayList<>(atts);

        StringBuilder q = new StringBuilder("SELECT ");
        q.append(attsList.get(0).getTable()).append('.').append(attsList.get(0).getColumn())
                .append(" AS ").append(getQueryName(attsList.get(0)));
        for (int i = 1; i < attsList.size(); i++) {
            q.append(", ").append(attsList.get(i).getTable()).append('.').append(attsList.get(i).getColumn())
                    .append(" AS ").append(getQueryName(attsList.get(i)));
        }

        q.append(" FROM ");
        Set<String> tables = attsList.stream().map(Attribute::getTable).collect(Collectors.toSet());

        if (rel == null) {
            // Basic entity query needed, so no initial joins
            q.append(schemaPattern).append('.').append(entity.getName());
            tables.remove(entity.getName());

            // Check for inclusion relationships
            if (tables.size() > 0) {
                q.append('\n');
                q.append(getInclusionRelationshipJoinQuery(getRelation(entity.getName()),
                        new HashSet<>(List.of(entity.getName())), tables));
            }

        } else if (rel instanceof BinaryRelationship binRel) {
            q.append(schemaPattern).append('.').append(binRel.getA().getName()).append('\n');
            List<List<Attribute>> bsToAs = binRel.getB().findAttsExportedTo(binRel.getA());

            q.append(getInnerJoinQuery(binRel.getB().getName(), bsToAs.get(1), bsToAs.get(0)));
            tables.remove(binRel.getA().getName());
            tables.remove(binRel.getB().getName());

            // Check for inclusion relationships
            if (tables.size() > 0) {
                q.append('\n');
                q.append(getInclusionRelationshipJoinQuery(binRel.getA(),
                        new HashSet<>(List.of(binRel.getA().getName(), binRel.getB().getName())), tables));
            }

        } else if (rel instanceof NAryRelationship nAryRel) {
            q.append(schemaPattern).append('.').append(nAryRel.getA().getName()).append('\n');
            Relation relationshipRel = nAryRel.getRelationshipRelation();
            List<List<Attribute>> aToRels = nAryRel.getA().findAttsExportedTo(relationshipRel);
            List<List<Attribute>> bToRels = nAryRel.getB().findAttsExportedTo(relationshipRel);

            q.append(getInnerJoinQuery(nAryRel.getRelationshipRelation().getName(), aToRels.get(0), aToRels.get(1))).append('\n');
            q.append(getInnerJoinQuery(nAryRel.getB().getName(), bToRels.get(1), bToRels.get(0)));
            tables.remove(nAryRel.getA().getName());
            tables.remove(nAryRel.getRelationshipRelation().getName());
            tables.remove(nAryRel.getB().getName());
        }

        q.append('\n').append("WHERE ");
        q.append(attsList.get(0).getTable()).append('.').append(attsList.get(0).getColumn()).append(" IS NOT NULL");
        for (int i = 1; i < attsList.size(); i++) {
            q.append(" AND ").append(attsList.get(i).getTable()).append('.').append(attsList.get(i).getColumn())
                    .append(" IS NOT NULL");
        }

        return q.toString();
    }

    private String getInclusionRelationshipJoinQuery(Relation relatedTo, Set<String> joinedTables, Set<String> tables) {
        StringBuilder q = new StringBuilder();

        Map<Relation, InclusionRelationship> relatedIncs = getRelatedInclusionRelationships(relatedTo);

        Set<String> extraTables = new HashSet<>(tables);
        int loopCount = 0;
        do {
            tables = new HashSet<>(extraTables);
            for (String table : tables) {
                Relation tableRelation = getRelation(table);
                InclusionRelationship incRel = relatedIncs.get(tableRelation);
                if (incRel == null) {
                    throw new IllegalArgumentException("ERModel.getInclusionRelationshipQuery: no InclusionRelationship " +
                            "found for table " + table);
                }

                Relation otherRelation = incRel.getB();
                if (otherRelation.equals(tableRelation)) {
                    otherRelation = incRel.getA();
                }

                if (!joinedTables.contains(otherRelation.getName())) {
                    // this table does not connect directly, so add a connecting table
                    extraTables.add(otherRelation.getName());
                    loopCount++;
                    continue;
                }

                extraTables.remove(table);
                joinedTables.add(table);
                // Inclusion relationships must have the same primary key
                List<List<PrimaryAttribute>> bsToAs = incRel.getB().findSharedPrimaryAttributes(incRel.getA());
                q.append(getInnerJoinQuery(incRel.getB().getName(), bsToAs.get(1), bsToAs.get(0))).append('\n');
            }
        } while (extraTables.size() > 0 && loopCount < 10000);

        if (loopCount == 10000) {
            StringBuilder message = new StringBuilder("ERModel.getInclusionRelationshipQuery: tables");
            extraTables.forEach(table -> message.append(" ").append(table));
            message.append(" could not be found related to ").append(relatedTo.getName());
            throw new IllegalArgumentException(message.toString());
        }

        return q.toString();
    }

    //  joinTable:          table to inner join on
    //  viaTableAttributes: original table attributes
    //  viaJoinAttributes:  joinTable attributes corresponding to the viaTableAttributes
    private String getInnerJoinQuery(String joinTable, List<? extends Attribute> viaTableAttributes,
                                     List<? extends Attribute> viaJoinAttributes) {
        StringBuilder q = new StringBuilder("INNER JOIN ");
        q.append(schemaPattern).append(".").append(joinTable).append('\n').append("ON ");
        q.append(viaTableAttributes.get(0)).append(" = ").append(viaJoinAttributes.get(0));

        for (int i = 1; i < viaJoinAttributes.size(); i++) {
            q.append(" AND ").append(viaTableAttributes.get(i)).append(" = ").append(viaJoinAttributes.get(i));
        }

        return q.toString();
    }

    private static String getQueryName(Attribute attribute) {
        return attribute.getTable() + attribute.getColumn();
    }

    private List<List<DataCell>> getRowsFromQueryAndAtts(String query, List<List<PrimaryAttribute>> pks, List<Attribute> atts) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        List<List<DataCell>> rows = new ArrayList<>();

        while(rs.next()){
            ArrayList<DataCell> row = new ArrayList<>();

            for (List<PrimaryAttribute> pAtts : pks) {
                String firstKeyVal = String.valueOf(rs.getObject(getQueryName(pAtts.get(0))));
                StringBuilder s = new StringBuilder(firstKeyVal);

                for (int i = 1; i < pAtts.size(); i++) {
                    String keyVal = String.valueOf(rs.getObject(getQueryName(pAtts.get(i))));
                    s.append(", ").append(keyVal);
                }

                if (pAtts.size() == 1) {
                    row.add(new DataCell(s.toString(), pAtts.get(0).getDBType().getDataType()));
                } else {
                    row.add(new DataCell(s.toString(), DataType.STRING));
                }
            }

            for (Attribute att : atts) {
                row.add(new DataCell(String.valueOf(rs.getObject(getQueryName(att))), att.getDBType().getDataType()));
            }

            rows.add(row);
        }

        rs.close();
        stmt.close();

        return rows;
    }

    private ArrayList<String> generateTableNames() throws SQLException {
        ResultSet tables = md.getTables(getCatalog(), getSchemaPattern(), "", new String[]{"TABLE"});
        ArrayList<String> tableNames = new ArrayList<>();

        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        tables.close();

        return tableNames;
    }

    private HashMap<String, Relation>  collectUntypedRelations() throws SQLException {
        ArrayList<HashSet<PrimaryAttribute>> pks = new ArrayList<>();
        ArrayList<HashSet<ForeignAttribute>> fks = new ArrayList<>();
        ArrayList<HashSet<Attribute>> otherColss = new ArrayList<>();

        for (String table : tableNames) {
            HashSet<Attribute> currOtherCols = new HashSet<>();
            ResultSet resCols = md.getColumns(getCatalog(), getSchemaPattern(), table, null);
            while (resCols.next()) {
                String column = resCols.getString("COLUMN_NAME");
                String type = resCols.getString("TYPE_NAME");

                DBType dbType = DBType.findType(type);
                if (dbType == null) {
                    throw new IllegalStateException("Representation " + type + " is not included in " + DBType.class);
                }

                currOtherCols.add(new Attribute(table, column, dbType));
            }
            resCols.close();

            // Collect Primary Keys for each table
            HashSet<PrimaryAttribute> currPks = new HashSet<>();
            ResultSet resPks = md.getPrimaryKeys(getCatalog(), getSchemaPattern(), table);
            while (resPks.next()) {
                String column = resPks.getString("column_name");
                String name = resPks.getString("pk_name");
                // Since all attributes of this table are in currOtherCols, this should exist if everything is correct
                //      so does not need to be checked
                Attribute baseAttribute = currOtherCols.stream().dropWhile((attribute) -> !attribute.isAttribute(table, column)).findFirst().get();
                currOtherCols.remove(baseAttribute);

                currPks.add(new PrimaryAttribute(name, baseAttribute));
            }
            resPks.close();

            // Collect where the keys were exported to
            ResultSet resEks = md.getExportedKeys(getCatalog(), getSchemaPattern(), table);
            while (resEks.next()) {
                String column = resEks.getString("pkcolumn_name");
//                    String name = resEks.getString("pk_name");

                // fk Schema (fktable_schem) as well? fk Category (fktable_cat)??
                String fkName = resEks.getString("fk_name");
                String fkTable = resEks.getString("fktable_name");
                String fkColumn = resEks.getString("fkcolumn_name");

                // Find the primary key with the same column
                for (PrimaryAttribute currAttribute : currPks) {
                    if (currAttribute.isAttribute(table, column)) {
                        // Add this as a foreign key equivalent to the primary key
                        currAttribute.addInstance(fkName, fkTable, fkColumn, currAttribute.getDBType());
                        break;
                    }
                }
            }
            resEks.close();

            HashSet<ForeignAttribute> currFks = new HashSet<>();
            ResultSet resFks = md.getImportedKeys(getCatalog(), getSchemaPattern(), table);
            while (resFks.next()) {
                String column = resFks.getString("fkcolumn_name");
                String name = resFks.getString("fk_name");
                String pkName = resFks.getString("pk_name");
                String pkTable = resFks.getString("pktable_name");
                String pkColumn = resFks.getString("pkcolumn_name");

                Optional<Attribute> baseAttribute = currOtherCols.stream().dropWhile((attribute) -> !attribute.isAttribute(table, column)).findFirst();
                if (baseAttribute.isPresent()) {
                    currFks.add(new ForeignAttribute(name, pkName, pkTable, pkColumn, baseAttribute.get()));
                    continue;
                }

                Optional<PrimaryAttribute> basePKAttribute = currPks.stream().dropWhile((attribute) -> !attribute.isAttribute(table, column)).findFirst();
                if (basePKAttribute.isPresent()) {
                    currPks.remove(basePKAttribute.get());
                    currPks.add(new PrimaryAttribute(basePKAttribute.get(), name, pkTable, pkColumn));
                }
            }
            resFks.close();

            pks.add(currPks);
            fks.add(currFks);
            otherColss.add(currOtherCols);
        }

        // Create Relation objects
        HashMap<String, Relation>  relations = new HashMap<>();
        for (int i = 0; i < tableNames.size(); i++) {
            String table = tableNames.get(i);
            HashSet<PrimaryAttribute> pk = pks.get(i);
            Optional<PrimaryAttribute> opPKAtt = pk.stream().findFirst();
            if (opPKAtt.isEmpty()) {
                // Something is wrong with the database
                throw new SQLException("ERModel.collectUntypedRelations: Relation " + table + " has no primary key");
            }

            String pkName = opPKAtt.get().getPKName();
            HashSet<ForeignAttribute> fkAtts = fks.get(i);
            HashSet<Attribute> otherCols = otherColss.get(i);
            Relation relation = new Relation(table, new PrimaryKey(pkName, pk), otherCols);

            // Set foreign and non keys
            if (fkAtts.size() > 0) {
                HashSet<Attribute> nonKeys = otherCols.stream().filter((att) -> !fkAtts.contains(att))
                        .collect(Collectors.toCollection(HashSet::new));

                relation.setForeignKeys(fkAtts);
                relation.setNonKeys(nonKeys);
            } else {
                relation.setNonKeys(otherCols);
            }

            relations.put(relation.getName(), relation);
        }

        return relations;
    }

    // DOES modify relations
    private void setRelationsTypes(HashMap<String, Relation> relations) {
        setRelationsTypes(relations, null);
    }

    // DOES modify relations
    private void setRelationsTypes(Map<String, Relation> relations, List<String> entityRelations) {
        // Detect strong entity relations
        for (Relation relation : relations.values()) {
            // If there is only one primary key attribute, it must be a strong relation
            if (relation.getPrimaryKey().size() == 1) {
                relation.setType(RelationType.STRONG);
                relation.setPrimaryKeyAtts(relation.getPrimaryKeySet());
                continue;
            }

            boolean strongRelation = true;
            for (Relation otherRelation : relations.values()) {
                if (!relation.equals(otherRelation) && relation.sharesPrimaryAttribute(otherRelation)) {
                    strongRelation = false;
                    break;
                }
            }

            if (strongRelation) {
                relation.setType(RelationType.STRONG);
                relation.setPrimaryKeyAtts(relation.getPrimaryKeySet());
            }
        }

        // Regular relationship first pass (contains only strong entity primaries)
        HashSet<Relation> strongRelations = relations.values().stream()
                .filter((relation) -> relation.getType() == RelationType.STRONG)
                .collect(Collectors.toCollection(HashSet::new));

        ArrayList<Relation> unclassifiedEntityRelations = relations.values().stream()
                .filter((relation) -> relation.getType() == null)
                .collect(Collectors.toCollection(ArrayList::new));

        for (Relation relation : unclassifiedEntityRelations) {
            // Check that each primary key attribute is a part of a strong relation
            HashSet<PrimaryAttribute> pk = relation.getPrimaryKeySet();
            boolean regRelation = true;
            for (PrimaryAttribute attribute : pk) {
                boolean currKeyIsStrongPk = false;
                for (Relation strongRelation : strongRelations) {
                    if (strongRelation.exportsPrimaryAttributeTo(attribute)) {
                        currKeyIsStrongPk = true;
                        break;
                    }
                }

                regRelation = regRelation && currKeyIsStrongPk;
            }

            if (regRelation) {
                relation.setType(RelationType.REGULAR);
                relation.setPrimaryKeyAtts(relation.getPrimaryKeySet());
            }
        }

        // Uses 'entityRelations' to identify weak vs specific relations
        if (entityRelations != null) {
            unclassifiedEntityRelations = unclassifiedEntityRelations.stream()
                    // Take both unclassified and relations that are entities
                    .filter((relation) -> relation.getType() == null && entityRelations.contains(relation.getName()))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            unclassifiedEntityRelations = unclassifiedEntityRelations.stream()
                    // Take unclassified relations
                    .filter((relation) -> relation.getType() == null)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Weak classification
        int numProspWeak;
        do {
            numProspWeak = unclassifiedEntityRelations.size();

            for (Relation relation : unclassifiedEntityRelations) {
                // k1: subset of the primary key that contains keys of entity relations
                // k2: remaining subset of the primary key that does not contain a key of any other relation
                // A weak entity must also be an entity (as confirmed by a user)
                HashSet<PrimaryAttribute> k1 = new HashSet<>();
                HashSet<PrimaryAttribute> k2 = new HashSet<>();

                for (Relation otherRelation : relations.values()) {
                    if (otherRelation.equals(relation)) {
                        continue;
                    }

                    if (otherRelation.isEntityRelation()) {
                        List<List<PrimaryAttribute>> sharedPrimaries = relation.findSharedPrimaryAttributes(otherRelation);

                        // Add attributes from relation (not otherRelation)
                        k1.addAll(sharedPrimaries.get(0));
                    }
                }

                if (k1.size() == 0) {
                    continue;
                }

                // Collect k2 (primary attributes that aren't in k1)
                for (PrimaryAttribute pka : relation.getPrimaryKeySet()) {
                    if (!k1.contains(pka)) {
                        k2.add(pka);
                    }
                }

                // the primary key only contains primary attributes of weak or strong relations, so is regular
                if (k2.size() == 0) {
                    relation.setType(RelationType.REGULAR);
                    relation.setPrimaryKeyAtts(relation.getPrimaryKeySet());
                    break;
                }

                // Check that k2 does not contain the key of any other relation
                boolean doesNotContainK2 = true;
                for (Relation otherRelation : relations.values()) {
                    if (otherRelation.equals(relation)) {
                        continue;
                    }

                    List<List<Attribute>> exportedAttributes = otherRelation.findAttsExportedTo(k2);
                    if (exportedAttributes.get(0).size() > 0) {
                        // a k2 attribute is contained in the key of 'otherRelation'
                        doesNotContainK2 = false;
                        break;
                    }
                }

                if (doesNotContainK2) {
                    relation.setType(RelationType.WEAK);
                    relation.setPrimaryKeyAtts(k1);
                    relation.setDanglingKeys(k2);
                }
            }

            unclassifiedEntityRelations = unclassifiedEntityRelations.stream()
                    .filter((relation) -> relation.getType() == null)
                    .collect(Collectors.toCollection(ArrayList::new));
        } while (unclassifiedEntityRelations.size() < numProspWeak);

        unclassifiedEntityRelations = relations.values().stream()
                .filter((relation) -> relation.getType() == null)
                .collect(Collectors.toCollection(ArrayList::new));

        // the rest of the unclassified relations are either regular or specific
        for (Relation relation : unclassifiedEntityRelations) {
            // k1: subset of the primary key that contains keys of entity relations
            // k2: remaining subset of the primary key that does not contain a key of any other relation
            // A weak entity must also be an entity (as confirmed by a user)
            HashSet<PrimaryAttribute> k1 = new HashSet<>();
            HashSet<PrimaryAttribute> k2 = new HashSet<>();

            for (Relation otherRelation : relations.values()) {
                if (otherRelation.equals(relation)) {
                    continue;
                }

                if (otherRelation.isEntityRelation()) {
                    List<List<PrimaryAttribute>> sharedPrimaries = relation.findSharedPrimaryAttributes(otherRelation);

                    // Add attributes from relation (not otherRelation)
                    k1.addAll(sharedPrimaries.get(0));
                }
            }

            // Regular Relation
            if (k1.size() == relation.getPrimaryKey().size()) {
                relation.setType(RelationType.REGULAR);
                relation.setPrimaryKeyAtts(relation.getPrimaryKeySet());
                continue;
            }

            // Specific Relation
            for (PrimaryAttribute pka : relation.getPrimaryKeySet()) {
                if (!k1.contains(pka)) {
                    k2.add(pka);
                }
            }

            relation.setType(RelationType.SPECIFIC);
            relation.setPrimaryKeyAtts(k1);
            relation.setGeneralKeys(k2);
        }
    }

    private HashMap<String, InclusionDependency> collectInclusionDependencies(HashMap<String, Relation> relations) {
        HashMap<String, InclusionDependency> inclusionDependencies = new HashMap<>();

        // Inclusion dependencies
        for (Relation relation : relations.values()) {
            for (Relation otherRelation : relations.values()) {
                if (relation.equals(otherRelation)) {
                    continue;
                }

                // Two STRONG relations have the same key, could be < or >
                if (relation.getType() == RelationType.STRONG && otherRelation.getType() == RelationType.STRONG) {
                    if (inclusionDependencies.values().stream().anyMatch(
                            (id) -> (id.getA().equals(relation) && id.getB().equals(otherRelation))
                                    || (id.getA().equals(otherRelation) && id.getB().equals(relation)))) {
                        // this pair has been analysed before
                        continue;
                    }

                    List<List<Attribute>> sharedAttributes = otherRelation.findSharedPrimaryAttributes(relation.getPrimaryKeyAtts());

                    List<Attribute> otherRelationAtts = sharedAttributes.get(0);
                    List<Attribute> relationAtts = sharedAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute pka = relationAtts.get(i);
                        Attribute otherPka = otherRelationAtts.get(i);

                        inclusionDependencies.put(InclusionDependency.generateName(pka, otherPka),
                                new InclusionDependency(relation, pka, otherRelation, otherPka));
                        inclusionDependencies.put(InclusionDependency.generateName(otherPka, pka),
                                new InclusionDependency(otherRelation, otherPka, relation, pka));
                    }
                }

                // The key of an entity relation (strong or weak) appears as a foreign key of another relation then:
                //      otherRelation.att < relation.att is possible
                if (relation.isEntityRelation()) {
                    List<List<Attribute>> expAttributes = relation.findAttsExportedTo(otherRelation);
                    List<Attribute> relationAtts = expAttributes.get(0);
                    List<Attribute> otherRelationAtts = expAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute relationAtt = relationAtts.get(i);
                        Attribute otherRelationAtt = otherRelationAtts.get(i);
                        inclusionDependencies.put(InclusionDependency.generateName(otherRelationAtt, relationAtt),
                                new InclusionDependency(otherRelation, otherRelationAtt, relation, relationAtt));
                    }
                }

                // The primary key attributes of a relationship relation (reg or specific) or a weak entity relation
                //      appear as the key of an entity relation then:
                //      relation.att < otherRelation.att is possible
                if ((relation.getType() == RelationType.REGULAR || relation.getType() == RelationType.SPECIFIC
                        || relation.getType() == RelationType.WEAK)
                        && otherRelation.isEntityRelation()) {

                    List<List<Attribute>> sharedAttributes = otherRelation.findAttsExportedTo(relation.getPrimaryKeyAtts());
                    List<Attribute> otherRelationAtts = sharedAttributes.get(0);
                    List<Attribute> relationAtts = sharedAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute pKAtt = relationAtts.get(i);
                        Attribute key = otherRelationAtts.get(i);

                        inclusionDependencies.put(InclusionDependency.generateName(pKAtt, key),
                                new InclusionDependency(relation, pKAtt, otherRelation, key));
                    }
                }
            }
        }

        return inclusionDependencies;
    }

    public String generateQueryAll(String table, String column) {
        return "SELECT " + column + " FROM " + getSchemaPattern() + "." + table + "\n";
    }

    public ArrayList<Object> getAllValues(Attribute a) throws SQLException {
        Statement stmt = conn.createStatement();

        ArrayList<Object> vals = new ArrayList<>();
        ResultSet rs = stmt.executeQuery(generateQueryAll(a.getTable(), a.getColumn()));

        int cols = rs.getMetaData().getColumnCount();
        while(rs.next()) {
            for (int i = 1; i <= cols; i++) {
                Object cell = DBType.getValue(rs, i, a.getDBType());
                vals.add(cell);
            }
        }
        rs.close();

        return vals;
    }

    // DOES modify ids
    private void removeInvalidInclusionDependencies(HashMap<String, InclusionDependency> ids) throws SQLException {
        for (InclusionDependency id : new ArrayList<>(ids.values())) {
            HashSet<Object> x1Vals = new HashSet<>(getAllValues(id.getX1()));
            x1Vals.remove(null);
            HashSet<Object> x2Vals = new HashSet<>(getAllValues(id.getX2()));
            x2Vals.remove(null);

            boolean x1sContainedInX2s = x2Vals.containsAll(x1Vals);

            if (/*x1Vals.size() == 0 || */ !x1sContainedInX2s) {
                ids.remove(id.getName());
                continue;
            }

            boolean x2sContainedInX1s = x1Vals.containsAll(x2Vals);

            if (x2Vals.size() == x1Vals.size() && x2sContainedInX1s) {
                id.setCovered();
            }
        }
    }

    // DOES modify ids
    private void removeRedundantInclusionDependencies(HashMap<String, InclusionDependency> ids) throws SQLException {
        // Make a copy of ids since some values will be removed
        List<InclusionDependency> idsCopy = new ArrayList<>(ids.values());

        for (InclusionDependency idA : idsCopy) {
            for (InclusionDependency idB : idsCopy) {
                // If idA is A.X < B.X and idB is B.Y < C.Y and Y is a subset of X then:
                //      A.Y < C.Y is redundant
                if (!ids.containsKey(InclusionDependency.generateName(idA.getX1(), idB.getX2())) ||
                        idA.equals(idB) || !idA.getB().equals(idB.getA())) {
                    continue;
                }

                HashSet<Object> xVals = new HashSet<>(getAllValues(idA.getX2()));
                xVals.remove(null);
                HashSet<Object> yVals = new HashSet<>(getAllValues(idB.getX1()));
                yVals.remove(null);

                if (xVals.containsAll(yVals)) {
                    ids.remove(InclusionDependency.generateName(idA.getX1(), idB.getX2()));
                }
            }
        }

    }

    // Does NOT modify relations or ids
    private HashMap<String, EntityType> collectEntities(HashMap<String, Relation>  relations, HashMap<String, InclusionDependency> ids) {
        HashMap<String, EntityType> entities = new HashMap<>();

        for (Relation relation : relations.values()) {
            if (relation.getType() == RelationType.STRONG) {
                entities.put(relation.getName(), new StrongEntityType(relation));
                continue;
            }

            if (relation.getType() == RelationType.WEAK) {
                // Check if the primary key attributes X of 'relation' W appear as the key of an entity relation
                //      (strong or weak) A such that W.X < A.X
                HashSet<InclusionDependency> weakIds = ids.values().stream().filter((id) -> id.getA().equals(relation))
                        .collect(Collectors.toCollection(HashSet::new));
                HashSet<Relation> possibleOwners = new HashSet<>();
                for (InclusionDependency id : weakIds) {
                    Relation otherRelation = id.getB();
                    if (!otherRelation.isEntityRelation()) {
                        continue;
                    }

                    // Check if all primary keys are contained in the X2 relation of this dependency
                    if (id.getB().exportedToAllFromPrimaryKey(relation.getPrimaryKeyAtts())) {
                        possibleOwners.add(id.getB());
                    }
                }

                if (possibleOwners.size() > 0) {
                    entities.put(relation.getName(), new WeakEntityType(relation, possibleOwners.stream().findFirst().get(), possibleOwners));
                } else {
                    // Case for no possible owners
                }
            }
        }
        // relation type is not weak or strong

        return entities;
    }

    // Does NOT modify relations or ids
    public HashMap<String, Relationship> collectRelationships(HashMap<String, Relation> relations, HashMap<String, InclusionDependency> ids) {
        HashMap<String, Relationship> relationships = new HashMap<>();
        for (Relation relation : relations.values()) {
            /* INCLUSION RELATIONSHIPS */
            // Two strong entity relations have the same key X with an inclusion dependency relation.X < otherRelation.X
            for (Relation otherRelation : relations.values()) {
                if (relation.equals(otherRelation) ||
                    relation.getType() != RelationType.STRONG || otherRelation.getType() != RelationType.STRONG ||
                    !relation.matchesPrimaryKeyAtts(otherRelation) ||
                    !InclusionDependency.idsExistBetweenAllPrimaryKeyAtts(relation, otherRelation, ids)) {
                    continue;
                }

                // Check for otherRelation.X < relation.X
                if (InclusionDependency.idsExistBetweenAllPrimaryKeyAtts(otherRelation, relation, ids)) {
                    // Check that a relationship with otherRelation and relation hasn't been found in the opposite direction
                    Optional<Relationship> otherToRelation = relationships.values().stream()
                            .dropWhile(ir -> !(ir.getA().equals(otherRelation) && ir.getB().equals(relation))).findFirst();

                    if (otherToRelation.isEmpty()) {
                        InclusionRelationship newIr = new InclusionRelationship(relation, otherRelation, InclusionRelationship.UNKNOWN_TYPE);

                        relationships.put(newIr.getName(), newIr);
                    }
                    continue;
                }

                InclusionRelationship newIr = new InclusionRelationship(relation, otherRelation, InclusionRelationship.IS_A_TYPE);

                relationships.put(newIr.getName(), newIr);
            }

            /* BINARY RELATIONSHIPS 1 */
            // Two entity relations (strong or weak) where relation has a foreign key appearing as a key of otherRelation
            //  where there is an inclusion dependency relation.x < otherRelation.X
            ArrayList<InclusionDependency> potentialBinRelations = new ArrayList<>();
            HashSet<String> foreignRelationNames = relation.getAllForeignAttributes().stream()
                    .map(ForeignAttribute::getRelationImportedFrom)
                    .collect(Collectors.toCollection(HashSet::new));

            for (Relation otherRelation : relations.values()) {
                if (relation.equals(otherRelation) ||
                        // Check that it's not an inclusion relationship already
                        relationships.containsKey(InclusionRelationship.generateName(relation, otherRelation)) ||
                        !(relation.isEntityRelation() && otherRelation.isEntityRelation()) ||
                        !foreignRelationNames.contains(otherRelation.getName())) {
                    continue;
                }

                List<List<Attribute>> exportedAtts = otherRelation.findAttsExportedTo(relation);
                List<Attribute> otherRelationAtts = exportedAtts.get(0);
                List<Attribute> relationAtts = exportedAtts.get(1);

                for (int i = 0; i < otherRelationAtts.size(); i++) {
                    Attribute otherAtt = otherRelationAtts.get(i);
                    Attribute relationAtt = relationAtts.get(i);

                    if (ids.get(InclusionDependency.generateName(relationAtt, otherAtt)) != null) {
                        potentialBinRelations.add(ids.get(InclusionDependency.generateName(relationAtt, otherAtt)));
                    }
                }
            }

            if (potentialBinRelations.size() == 0) {
                continue;
            }

            for (InclusionDependency id : potentialBinRelations) {
                String relName = BinaryRelationship.generateName(id.getA(), id.getB());

                if (relationships.containsKey(relName)) {
                    BinaryRelationship newRel = new BinaryRelationship((BinaryRelationship) relationships.get(relName), id);
                    relationships.put(relName, newRel);
                } else {
                    relationships.put(relName, new BinaryRelationship(List.of(id)));
                }
            }
        }

        /* BINARY RELATIONSHIPS 2 */
        // An inclusion dependency exists between two non-key attributes A.x < B.y
        for (InclusionDependency id : ids.values()) {
            if ((id.getA().hasNonKey(id.getX1()) || id.getA().hasNonPKForeignKey(id.getX1()))
                    && (id.getB().hasNonKey(id.getX2()) || id.getA().hasNonPKForeignKey(id.getX2()))) {
                String relName = BinaryRelationship.generateName(id.getA(), id.getB());

                if (relationships.containsKey(relName)) {
                    BinaryRelationship newRel = new BinaryRelationship((BinaryRelationship) relationships.get(relName), id);
                    relationships.put(relName, newRel);
                } else {
                    relationships.put(relName, new BinaryRelationship(List.of(id)));
                }
            }
        }

        /* NARY RELATIONSHIPS (REGULAR) */
        // Conditions to be a part of a nary relationship:
        //  There is a subset X of the primary attributes of a regular relationship relation, R, which
        //  appears as a key of an entity relation (strong or weak), A,
        //  and there is an inclusion dependency R.X < A.X
        for (Relation rel : relations.values()) {
            if (rel.getType() != RelationType.REGULAR) {
                continue;
            }

            // Find possible participating inclusion dependencies/entity types
            HashSet<EntityType> relParts = new HashSet<>();
            for (InclusionDependency id : ids.values()) {
                Relation relPart = id.getB();
                if (!id.getA().equals(rel) || !relPart.isEntityRelation() ||
                    // Check that A's primary attributes are a subset of rel's
                    !rel.containsAllInPrimaryKeyAtts(relPart.getPrimaryKeyAtts())) {
                    continue;
                }

                boolean allAPksHaveIds = true;
                for (PrimaryAttribute x2Pka : relPart.getPrimaryKeySet()) {
                    if (ids.get(InclusionDependency.generateName(rel.findInPrimaryKey(x2Pka), x2Pka)) == null) {
                        allAPksHaveIds = false;
                        break;
                    }
                }

                if (allAPksHaveIds) {
                    EntityType entPart = entities.get(relPart.getName());
                    // entPart should exist
                    //  > Also all entParts should be unique
                    relParts.add(entPart);
                }
            }

            List<EntityType> relPartsArr = new ArrayList<>(relParts);

            if (relPartsArr.size() == 1) {
                // the relationship is to itself
                String name = relPartsArr.get(0).getName();

                NAryRelationship newNAry = new NAryRelationship(rel, relations.get(name), relations.get(name));
                relationships.put(newNAry.getName(), newNAry);
            } else if (relPartsArr.size() > 1) {
                String name1 = relPartsArr.get(0).getName();
                String name2 = relPartsArr.get(1).getName();

                NAryRelationship newNAry = new NAryRelationship(rel, relations.get(name1), relations.get(name2));
                relationships.put(newNAry.getName(), newNAry);
            }
        }

        return relationships;
    }

    // Does NOT modify rels
    private HashMap<String, GeneralizationHierarchy> collectGeneralizationHierarchies(HashMap<String, Relationship> rels) throws SQLException {
        HashMap<String, GeneralizationHierarchy> hierarchies = new HashMap<>();

        HashSet<InclusionRelationship> is_as = rels.values().stream()
                .filter(rel -> (rel instanceof InclusionRelationship) && ((InclusionRelationship) rel).isA())
                .map(ir -> (InclusionRelationship) ir).collect(Collectors.toCollection(HashSet::new));

        // Collect generics from is_a relationships (for A.X IS_A B.X, B is a generic type)
        HashSet<Relation> genericTypes = is_as.stream().map(Relationship::getB)
                .collect(Collectors.toCollection(HashSet::new));

        // Check if any generic types are a generalisation hierarchy
        for (Relation generic : genericTypes) {
            // Collect specifics for the generic type
            ArrayList<Relation> specifics = is_as.stream().filter(ir -> ir.getB().equals(generic))
                    .map(Relationship::getA).collect(Collectors.toCollection(ArrayList::new));
            if (specifics.size() <= 1) {
                // there is only one specific, so this isn't a generalization hierarchy
                continue;
            }

            // Collect all values for each attribute which is a part of the relations' primary keys
            //  They should be the same size, due to the nature of an is_a relationship (pkas must match for inclusion dependencies)
            int numAttributes = generic.getPrimaryKeyAtts().size();
            ArrayList<HashSet<Object>> genValss = new ArrayList<>(numAttributes);
            ArrayList<PrimaryAttribute> gPkas = generic.getPrimaryKeyAtts().stream()
                    .sorted(Comparator.comparing(PrimaryAttribute::getPKName)).collect(Collectors.toCollection(ArrayList::new));
            for (PrimaryAttribute pka : gPkas) {
                genValss.add(new HashSet<>(getAllValues(pka)));
            }

            // A list for each specific of each primary attribute's values -> essentially a genValss for each specific
            ArrayList<ArrayList<HashSet<Object>>> specsValss = new ArrayList<>(numAttributes);
            for (PrimaryAttribute gPka : gPkas) {
                ArrayList<HashSet<Object>> specsVals = new ArrayList<>(specifics.size());
                for (Relation specific : specifics) {
                    specsVals.add(new HashSet<>(getAllValues(specific.findInPrimaryKey(gPka))));
                }

                specsValss.add(specsVals);
            }

            boolean isGenHierarchy = true;
            // Check for overlap
            for (int pkaIdx = 0; pkaIdx < numAttributes; pkaIdx++) {
                HashSet<Object> genVals = genValss.get(pkaIdx);
                ArrayList<HashSet<Object>> specsVals = specsValss.get(pkaIdx);

                // Check for overlap
                HashSet<Object> mergedSet = new HashSet<>();
                for (HashSet<Object> specVals : specsVals) {
                    for (Object obj : specVals) {
                        if (!mergedSet.add(obj)) {
                            // There is an overlapping value
                            isGenHierarchy = false;
                            break;
                        }
                    }

                    if (!isGenHierarchy) {
                        break;
                    }
                }

                if (!isGenHierarchy) {
                    break;
                }

                // Check for containment
                if (!genVals.containsAll(mergedSet)) {
                    isGenHierarchy = false;
                    break;
                }
            }

            if (isGenHierarchy) {
                GeneralizationHierarchy newGh = new GeneralizationHierarchy(generic, new HashSet<>(specifics));

                hierarchies.put(newGh.getName(), newGh);
            }
        }

        return hierarchies;
    }

    // DOES modify entities
    // Does NOT modify relations, relationships
    private void setEntityTypeAttributes(HashMap<String, EntityType> entities, HashMap<String, Relation> relations, HashSet<Relationship> relationships) {

        for (Relation rel : relations.values()) {
            // Get all foreign attributes
            HashSet<ForeignAttribute> fAtts = rel.getAllForeignAttributes();

            // ENTITY RELATIONS
            if (rel.isEntityRelation()) {
                EntityType entityRel = entities.get(rel.getName());

                // Does not contain foreign key attributes
                if (fAtts.isEmpty()) {
                    // Assign all non-key attributes to the entity type
                    entityRel.setDescriptiveAttributes(rel.getNonKeys());
                    continue;
                }

                // Has foreign key attributes
                continue;
            }

            // This should exist is everything is correct
            Optional<Relationship> opRelationshipRel = relationships.stream()
                    .filter(r -> r instanceof NAryRelationship && r.getName().equals(rel.getName()))
                    .findFirst();

            if (opRelationshipRel.isEmpty()) {
                continue;
            }

            NAryRelationship relationshipRel = (NAryRelationship) opRelationshipRel.get();

            // RELATIONSHIP RELATIONS
            // Does not contain foreign key attributes
            if (fAtts.isEmpty()) {
                relationshipRel.setDescriptiveAttributes(rel.getNonKeys());
            }

            // Has foreign key attributes
        }
    }
}
