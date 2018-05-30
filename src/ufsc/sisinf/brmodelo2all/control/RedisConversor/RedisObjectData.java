package ufsc.sisinf.brmodelo2all.control.RedisConversor;

import ufsc.sisinf.brmodelo2all.control.CassandraConversor.CassandraAttribute;

import java.util.ArrayList;
import java.util.List;

public class RedisObjectData {

    private String objectName;
    private List<String> requiredAttributes = new ArrayList<String>();

    public RedisObjectData() { }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<String> getAttributes() {
        return requiredAttributes;
    }

    public void addAttributes(String attribute) {
        requiredAttributes.add(attribute);
    }
}
