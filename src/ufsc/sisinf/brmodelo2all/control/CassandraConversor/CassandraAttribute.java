package ufsc.sisinf.brmodelo2all.control.CassandraConversor;

public class CassandraAttribute {

    private String name;
    private CassandraTypes  type;
    private boolean multipleAttributes;
    public enum CassandraTypes {UUID, TIMESTAMP, TEXT, INT, NEWTYPE}

    public CassandraAttribute (String name, CassandraTypes type, Boolean multipleAttributes) {
        this.name = name;
        this.type = type;
        this.multipleAttributes = multipleAttributes;
    }

    public static CassandraTypes typeConverter (String type) {
        switch (type.toLowerCase()) {
            case "objectid":
            case "id":
                return CassandraTypes.UUID;
            case "int":
            case "double":
            case "integer":
                return CassandraTypes.INT;
            case "date":
                return CassandraTypes.TIMESTAMP;
            case "string":
                return CassandraTypes.TEXT;

        }

        return CassandraTypes.TEXT;
    }

    public String getName() {
        return name;
    }

    public CassandraTypes getType() {
        return type;
    }

    public boolean isMultipleAttributes() {
        return multipleAttributes;
    }

}
