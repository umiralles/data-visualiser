package visualiser.datavisualiser.models.GraphDetectorA;

import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Entities.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GraphDetector {

    private final String query;

    protected GraphDetector(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    // TODO (in progress): have a static method to detect the right graph detector to go to
    public static GraphDetector generateGraphDetector(ERModel rm, List<InputAttribute> inputAttributes)
            throws IllegalArgumentException {
        assert inputAttributes.size() > 0;

        Set<String> inputTables = new HashSet<>();

        inputAttributes.forEach(in -> inputTables.add(in.table()));

        if (inputTables.size() == 1) {
            String entityName = inputTables.stream().findFirst().get();
            EntityType entity = rm.getEntity(entityName);
            if (entity == null) {
                throw new IllegalArgumentException("GraphDetector.generateGraphDetector: the entity " + entityName +
                        " could not be found in " + rm.getCatalog() + "." + rm.getSchemaPattern());
            }

//            return BasicGraphDetector.generateGraphDetector(rm, entity, inputAttributes);
            return null;
        }

        if (inputTables.size() == 2) {

        }

        return null;
    }
}
