package ufsc.sisinf.brmodelo2all.control.CassandraConversor;

import java.util.ArrayList;
import java.util.List;

public class CassandraObjectData {

    private String objectName;
    private String primaryKey;
    private List<CassandraAttribute> attributes = new ArrayList<CassandraAttribute>();

    public CassandraObjectData() { }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<CassandraAttribute> getAttributes() {
        return attributes;
    }

    public void addAttributes(CassandraAttribute attribute) {
        attributes.add(attribute);
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

}
