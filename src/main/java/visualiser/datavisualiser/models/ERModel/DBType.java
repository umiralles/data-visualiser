package visualiser.datavisualiser.models.ERModel;

import visualiser.datavisualiser.models.DataTable.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

public enum DBType {
    // Types as available for a ResultSet object
    //  To use for other sql databases add to the list of representations for each object
    BOOLEAN(AttributeType.DISCRETE, DataType.BOOLEAN, Boolean.class),
    STRING(AttributeType.LEXICAL, DataType.STRING, String.class, "varchar"),
    DATE(AttributeType.TEMPORAL, DataType.DATE, Date.class, "date"),
    TIME(AttributeType.TEMPORAL, DataType.TIMEOFDAY, Timestamp.class),
    TIMESTAMP(AttributeType.TEMPORAL, DataType.DATETIME, Timestamp.class),
    INT(AttributeType.SCALAR, DataType.INT, Integer.class, "int4", "int2"),
    DOUBLE(AttributeType.SCALAR, DataType.DOUBLE, Double.class),
    FLOAT(AttributeType.SCALAR, DataType.FLOAT, Float.class, "numeric"),
    LONG(AttributeType.SCALAR, null, Long.class),
    BYTE(AttributeType.INVALID, null, Byte.class),
    ARRAY(AttributeType.INVALID, null, Arrays.class);

    private final AttributeType attType;
    private final DataType dataType;
    private final Class<?> javaClass;
    private final HashSet<String> representations;

    DBType(AttributeType attType, DataType dataType, Class<?> javaClass, String... representations) {
        this.attType = attType;
        this.dataType = dataType;
        this.javaClass = javaClass;
        this.representations = Arrays.stream(representations).collect(Collectors.toCollection(HashSet::new));
    }

    public AttributeType getAttType() {
        return attType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public static DBType findType(String typeString) {
        for (DBType type : DBType.values()) {
            if (type.representations.contains(typeString)) {
                return type;
            }
        }

        return null;
    }

    // Returns a 'type' object
    //      use type.cast(object) to retrieve
    public static Object getValue(ResultSet rs, String columnLabel, DBType type) throws SQLException {
        switch (type) {
            case BOOLEAN -> {
                return rs.getBoolean(columnLabel);
            }
            case STRING -> {
                return rs.getString(columnLabel);
            }
            case DATE -> {
                return rs.getDate(columnLabel);
            }
            case TIME -> {
                return rs.getTime(columnLabel);
            }
            case TIMESTAMP -> {
                return rs.getTimestamp(columnLabel);
            }
            case INT -> {
                return rs.getInt(columnLabel);
            }
            case DOUBLE -> {
                return rs.getDouble(columnLabel);
            }
            case FLOAT -> {
                return rs.getFloat(columnLabel);
            }
            case LONG -> {
                return rs.getLong(columnLabel);
            }
            case BYTE -> {
                return rs.getByte(columnLabel);
            }
            case ARRAY -> {
                return rs.getArray(columnLabel);
            }
        }

        // all cases should return, so this should not be possible
        return rs.getObject(columnLabel);
    }

    // Returns a 'type' object
    //      use type.cast(object) to retrieve
    public static Object getValue(ResultSet rs, int columnIndex, DBType type) throws SQLException {
        switch (type) {
            case BOOLEAN -> {
                return rs.getBoolean(columnIndex);
            }
            case STRING -> {
                return rs.getString(columnIndex);
            }
            case DATE -> {
                return rs.getDate(columnIndex);
            }
            case TIME -> {
                return rs.getTime(columnIndex);
            }
            case TIMESTAMP -> {
                return rs.getTimestamp(columnIndex);
            }
            case INT -> {
                return rs.getInt(columnIndex);
            }
            case DOUBLE -> {
                return rs.getDouble(columnIndex);
            }
            case FLOAT -> {
                return rs.getFloat(columnIndex);
            }
            case LONG -> {
                return rs.getLong(columnIndex);
            }
            case BYTE -> {
                return rs.getByte(columnIndex);
            }
            case ARRAY -> {
                return rs.getArray(columnIndex);
            }
        }

        // all cases should return, so this should not be possible
        return rs.getObject(columnIndex);
    }
}
