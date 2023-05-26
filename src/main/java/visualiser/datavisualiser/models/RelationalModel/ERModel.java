package visualiser.datavisualiser.models.RelationalModel;

import visualiser.datavisualiser.models.GoogleChart.DataCell;
import visualiser.datavisualiser.models.GraphDetector.InputAttribute;
import visualiser.datavisualiser.models.RelationalModel.Entities.EntityType;
import visualiser.datavisualiser.models.RelationalModel.Entities.StrongEntityType;
import visualiser.datavisualiser.models.RelationalModel.Entities.WeakEntityType;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.ForeignAttribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryKey;
import visualiser.datavisualiser.models.RelationalModel.Relations.Relation;
import visualiser.datavisualiser.models.RelationalModel.Relations.RelationType;
import visualiser.datavisualiser.models.RelationalModel.Relationships.*;

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
    private final ArrayList<String> entityRelations;
    private final ArrayList<String> tableNames;
    private final HashMap<String, Relation> relations;
    private final HashSet<InclusionDependency> ids;

    // TODO: remove
    public HashSet<InclusionDependency> getIds() {
        return ids;
    }

    public ERModel(String username, String password, String url, String schemaPattern, ArrayList<String> entityRelations) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.catalog = url.substring(url.lastIndexOf('/') + 1);
        this.schemaPattern = schemaPattern;
        this.entityRelations = entityRelations;

        try {
//            String url = "jdbc:postgresql://localhost/" + catalog;
            Properties props = new Properties();
            props.setProperty("user", this.username);
            props.setProperty("password", this.password);
//            props.setProperty("user", "postgres");
//            props.setProperty("password", "post");
//            props.setProperty("ssl", "true");

            this.conn = DriverManager.getConnection(this.url, props);
            this.md = conn.getMetaData();

            this.tableNames = generateTableNames();

            this.relations = collectUntypedRelations();
            if (entityRelations != null) {
                setRelationsTypes(this.relations, this.entityRelations);
            } else {
                setRelationsTypes(this.relations);
            }

            this.ids = collectInclusionDependencies(this.relations);
            removeInvalidInclusionDependencies(this.ids);
            removeRedundantInclusionDependencies(this.ids);

            this.entities = collectEntities(this.relations, this.ids);
            this.relationships = collectRelationships(this.relations, this.ids);
            this.hierarchies = collectGeneralizationHierarchies(this.relationships);

//            setEntityTypeAttributes(this.entities, this.relations, this.relationships);

        } catch (SQLException e) {
            System.out.println("SQL Exception in RelationalDatabase: " + e.getMessage());
            throw new RuntimeException(e);
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

    public BinaryRelationship getBinaryRelationship(InputAttribute k1, InputAttribute k2) {
        return (BinaryRelationship) relationships.get(BinaryRelationship.generateName(k1.table(), k1.column(), k2.table(), k2.column()));
    }

    public NAryRelationship getNAryRelationship(Relation a, Relation b) {
        return (NAryRelationship) relationships.get(NAryRelationship.generateName(a, b));
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

    public List<List<DataCell>> getRowsFromQueryAndAtts(String query, List<Attribute> atts) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        List<List<DataCell>> rows = new ArrayList<>();

        while(rs.next()){
            ArrayList<DataCell> row = new ArrayList<>();

            for (Attribute att : atts) {
                row.add(new DataCell(String.valueOf(rs.getObject(att.getColumn())), att.getDBType().getDataType()));
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

                currOtherCols.add(new Attribute(table, column, DBType.findType(type)));
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
                // TODO: Something is wrong with the database
                throw new SQLException("RelationalDatabase.collectUntypedRelations: Relation " + table + " has no primary key");
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
    private void setRelationsTypes(HashMap<String, Relation>  relations, ArrayList<String> entityRelations) {
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
                        List<ArrayList<PrimaryAttribute>> sharedPrimaries = relation.findSharedPrimaryAttributes(otherRelation);

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

                    List<ArrayList<Attribute>> exportedAttributes = otherRelation.findAttsExportedTo(k2);
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
                    List<ArrayList<PrimaryAttribute>> sharedPrimaries = relation.findSharedPrimaryAttributes(otherRelation);

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

        // TODO: Could use a condition checking if entityRelations are all classified
    }

    private HashSet<InclusionDependency> collectInclusionDependencies(HashMap<String, Relation> relations) {
        HashSet<InclusionDependency> inclusionDependencies = new HashSet<>();

        // Inclusion dependencies
        for (Relation relation : relations.values()) {
            for (Relation otherRelation : relations.values()) {
                if (relation.equals(otherRelation)) {
                    continue;
                }

                // Two STRONG relations have the same key, could be < or >
                if (relation.getType() == RelationType.STRONG && otherRelation.getType() == RelationType.STRONG) {
                    if (inclusionDependencies.stream().anyMatch(
                            (id) -> (id.getA().equals(relation) && id.getB().equals(otherRelation))
                                    || (id.getA().equals(otherRelation) && id.getB().equals(relation)))) {
                        // this pair has been analysed before
                        continue;
                    }

                    List<ArrayList<Attribute>> sharedAttributes = otherRelation.findSharedPrimaryAttributes(relation.getPrimaryKeyAtts());

                    ArrayList<Attribute> otherRelationAtts = sharedAttributes.get(0);
                    ArrayList<Attribute> relationAtts = sharedAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute pka = relationAtts.get(i);
                        Attribute otherPka = otherRelationAtts.get(i);

                        inclusionDependencies.add(new InclusionDependency(relation, pka, otherRelation, otherPka));
                        inclusionDependencies.add(new InclusionDependency(otherRelation, otherPka, relation, pka));
                    }
                }

                // The key of an entity relation (strong or weak) appears as a foreign key of another relation then:
                //      otherRelation.att < relation.att is possible
                if (relation.isEntityRelation()) {
                    List<ArrayList<Attribute>> expAttributes = relation.findAttsExportedTo(otherRelation);
                    ArrayList<Attribute> relationAtts = expAttributes.get(0);
                    ArrayList<Attribute> otherRelationAtts = expAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute relationAtt = relationAtts.get(i);
                        Attribute otherRelationAtt = otherRelationAtts.get(i);
                        inclusionDependencies.add(new InclusionDependency(otherRelation, otherRelationAtt, relation, relationAtt));
                    }
                }

                // The primary key attributes of a relationship relation (reg or specific) or a weak entity relation
                //      appear as the key of an entity relation then:
                //      relation.att < otherRelation.att is possible
                if ((relation.getType() == RelationType.REGULAR || relation.getType() == RelationType.SPECIFIC
                        || relation.getType() == RelationType.WEAK)
                        && otherRelation.isEntityRelation()) {

                    List<ArrayList<Attribute>> sharedAttributes = otherRelation.findSharedPrimaryAttributes(relation.getPrimaryKeyAtts());
                    ArrayList<Attribute> relationAtts = sharedAttributes.get(0);
                    ArrayList<Attribute> otherRelationAtts = sharedAttributes.get(1);

                    for (int i = 0; i < relationAtts.size(); i++) {
                        Attribute pKAtt = relationAtts.get(i);
                        Attribute key = otherRelationAtts.get(i);

                        inclusionDependencies.add(new InclusionDependency(relation, pKAtt, otherRelation, key));
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
        // TODO: Could save this in attribute/inclusion dependency?
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
    private void removeInvalidInclusionDependencies(HashSet<InclusionDependency> ids) throws SQLException {
        for (InclusionDependency id : (HashSet<InclusionDependency>) ids.clone()) {
            HashSet<Object> x1Vals = new HashSet<>(getAllValues(id.getX1()));
            x1Vals.remove(null);
            HashSet<Object> x2Vals = new HashSet<>(getAllValues(id.getX2()));
            x2Vals.remove(null);

            if (/*x1Vals.size() == 0 || */ !x2Vals.containsAll(x1Vals)) {
                ids.remove(id);
            }

            if (x2Vals.size() == x1Vals.size()) {
                id.setCovered();
            }
        }
    }

    // DOES modify ids
    private void removeRedundantInclusionDependencies(HashSet<InclusionDependency> ids) throws SQLException {
        // Make a copy of ids since some values will be removed
        HashSet<InclusionDependency> idsCopy = (HashSet<InclusionDependency>) ids.clone();

        for (InclusionDependency idA : idsCopy) {
            for (InclusionDependency idB : idsCopy) {
                // If idA is A.X < B.X and idB is B.Y < C.Y and Y is a subset of X then:
                //      A.Y < C.Y is redundant
                if (idA.equals(idB) || !idA.getB().equals(idB.getA())) {
                    continue;
                }

                HashSet<Object> xVals = new HashSet<>(getAllValues(idA.getX2()));
                xVals.remove(null);
                HashSet<Object> yVals = new HashSet<>(getAllValues(idB.getX1()));
                yVals.remove(null);

                if (xVals.containsAll(yVals)) {
                    ids.remove(new InclusionDependency(idA.getA(), idA.getX1(), idB.getB(), idB.getX2()));
                }
            }
        }

    }

    // Does NOT modify relations or ids
    private HashMap<String, EntityType> collectEntities(HashMap<String, Relation>  relations, HashSet<InclusionDependency> ids) {
        HashMap<String, EntityType> entities = new HashMap<>();

        for (Relation relation : relations.values()) {
            if (relation.getType() == RelationType.STRONG) {
                entities.put(relation.getName(), new StrongEntityType(relation));
                continue;
            }

            if (relation.getType() == RelationType.WEAK) {
                // Check if the primary key attributes X of 'relation' W appear as the key of an entity relation
                //      (strong or weak) A such that W.X < A.X
                HashSet<InclusionDependency> idsW = ids.stream().filter((id) -> id.getA().equals(relation))
                        .collect(Collectors.toCollection(HashSet::new));
                HashSet<Relation> possibleOwners = new HashSet<>();
                for (InclusionDependency id : idsW) {
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
                    // TODO: if more than one possible, ask user
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
    public HashMap<String, Relationship> collectRelationships(HashMap<String, Relation> relations, HashSet<InclusionDependency> ids) {
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
                    !(relation.isEntityRelation() && otherRelation.isEntityRelation()) ||
                    !foreignRelationNames.contains(otherRelation.getName())) {
                    continue;
                }

                List<ArrayList<Attribute>> exportedAtts = otherRelation.findAttsExportedTo(relation);
                ArrayList<Attribute> otherRelationAtts = exportedAtts.get(0);
                ArrayList<Attribute> relationAtts = exportedAtts.get(1);

                for (int i = 0; i < otherRelationAtts.size(); i++) {
                    Attribute otherAtt = otherRelationAtts.get(i);
                    Attribute relationAtt = relationAtts.get(i);

                    InclusionDependency potentialBinRelation = new InclusionDependency(relation, relationAtt, otherRelation, otherAtt);
                    if (ids.contains(potentialBinRelation)) {
                        potentialBinRelations.add(potentialBinRelation);
                    }
                }
            }

            // TODO: if more than one possible, ask user?? but why
            // TODO: eg. airport has two potentials: island and city which are both binary relationships??? How to do this?
            if (potentialBinRelations.size() > 0) {
                for (InclusionDependency id : potentialBinRelations) {
                    BinaryRelationship newBr = new BinaryRelationship(id);

                    relationships.put(newBr.getName(), newBr);
                }
            }
        }

        /* BINARY RELATIONSHIPS 2 */
        // An inclusion dependency exists between two non-key attributes A.x < B.y
        for (InclusionDependency id : ids) {
            // TODO: should this be for both non keys and foreign non keys or just non keys? I think both
            if ((id.getA().hasNonKey(id.getX1()) || id.getA().hasNonPKForeignKey(id.getX1()))
                    && (id.getB().hasNonKey(id.getX2()) || id.getA().hasNonPKForeignKey(id.getX2()))) {
                BinaryRelationship newBr = new BinaryRelationship(id);

                relationships.put(newBr.getName(), newBr);
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
            for (InclusionDependency id : ids) {
                Relation relPart = id.getB();
                if (!id.getA().equals(rel) || !relPart.isEntityRelation() ||
                    // Check that A's primary attributes are a subset of rel's
                    !rel.containsAllInPrimaryKeyAtts(relPart.getPrimaryKeyAtts())) {
                    continue;
                }

                boolean allAPksHaveIds = true;
                for (PrimaryAttribute x2Pka : relPart.getPrimaryKeySet()) {
                    if (!ids.contains(new InclusionDependency(rel, rel.findInPrimaryKey(x2Pka), relPart, x2Pka))) {
                        allAPksHaveIds = false;
                        break;
                    }
                }

                if (allAPksHaveIds) {
                    EntityType entPart = entities.get(relPart.getName());
                    // TODO: entPart should exist but add a check maybe?
                    //      > Also all entParts should be unique
                    relParts.add(entPart);
                }
            }

            List<EntityType> relPartsArr = relParts.stream().toList();

            if (relPartsArr.size() == 1) {
                // the relationship is to itself
                // TODO: make relationships for two entity types
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

            // This should exist is everything is correct TODO: check this
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
