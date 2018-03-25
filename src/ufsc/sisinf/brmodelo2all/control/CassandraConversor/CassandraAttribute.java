package ufsc.sisinf.brmodelo2all.control.CassandraConversor;

public class CassandraAttribute {

    private String name;
    private CassandraTypes  type;
    public enum CassandraTypes {UUID, TIMESTAMP, TEXT, INT}

    public CassandraAttribute (String name, CassandraTypes type) {
        this.name = name;
        this.type = type;
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

}
