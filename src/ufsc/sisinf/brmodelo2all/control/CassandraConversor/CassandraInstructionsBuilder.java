package ufsc.sisinf.brmodelo2all.control.CassandraConversor;


import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;

import java.util.List;

public class CassandraInstructionsBuilder {
    private NosqlConfigurationData configData;

    /* Main Cassandra keyword*/
    static final String DROP = "DROP";
    static final String CREATE = "CREATE";
    static final String KEYSPACE = "KEYSPACE";
    static final String WITH = "WITH";
    static final String REPLICATION = "replication";
    static final String CLASS = "class";
    static final String REPLICATIONFACTOR = "replication_factor";
    static final String USE = "use";
    static final String TABLE = "TABLE";
    static final String TYPE = "TYPE";
    static final String PRIMARY = "PRIMARY";
    static final String KEY = "KEY";
    static final String LIST = "list";

    /* Language Tokens*/
    static final String COMMA = ", ";
    static final String SEMICOLON = ";";
    static final String COLON = ":";
    static final String BREAKLINE = "\n";
    static final String OPENBRACES = "{";
    static final String CLOSEBRACES = "}";
    static final String OPENPARENTHESES = "(";
    static final String CLOSEPARENTHESES = ")";
    static final String OPENBRACKETS = "[";
    static final String CLOSEBRACKTS = "]";
    static final String OPENCHEVRON = "<";
    static final String CLOSECHEVRON = ">";
    static final String SPACE = " ";
    static final String EQUAL = "=";
    static final String ONEQUOTE = "'";
    static final String TAB = "  ";


    /* ConfigVariables */
    private String dbName;
    private String cassandraClass;
    private String cassandraReplicationFactor;

    public CassandraInstructionsBuilder() {
        this.configData = NosqlConfigurationData.getInstance();
        dbName = configData.getDbName();
        cassandraClass = configData.getCassandraClass();
        cassandraReplicationFactor = configData.getCassandraReplicationFactor();
    }

    private String surroundWithQuotes(String word) {
        return ONEQUOTE + word + ONEQUOTE;
    }
    private String surroundWithChevron(String word) {
        return OPENCHEVRON + word + CLOSECHEVRON;
    }

    public String genInitialDBInstructions () {
        return DROP + SPACE + KEYSPACE + SPACE + dbName + SEMICOLON + BREAKLINE
                + CREATE + SPACE + KEYSPACE + SPACE + dbName + SPACE + WITH + SPACE + REPLICATION + SPACE +  EQUAL + SPACE
                + OPENBRACES
                    + surroundWithQuotes(CLASS) + COLON + SPACE + surroundWithQuotes(cassandraClass) + COMMA
                    + surroundWithQuotes(REPLICATIONFACTOR) + COLON + surroundWithQuotes(cassandraReplicationFactor)
                + CLOSEBRACES + SEMICOLON + BREAKLINE
                + USE + SPACE + dbName + SEMICOLON + BREAKLINE + BREAKLINE;
    }

    public String getAttributeType (CassandraAttribute attribute) {
        if (attribute.getType().equals(CassandraAttribute.CassandraTypes.NEWTYPE)) {
            return attribute.getName().toUpperCase();
        }

        return attribute.getType().toString();
    }

    public String genAttributesInstructions (List<CassandraAttribute> attributes) {
        String instructions = "";

        for (CassandraAttribute attribute : attributes) {
            if (attribute.isMultipleAttributes()) {
                instructions += TAB + attribute.getName() + SPACE + LIST + surroundWithChevron(getAttributeType(attribute)) + COMMA + BREAKLINE;
            } else {
                instructions += TAB + attribute.getName() + SPACE + getAttributeType(attribute) + COMMA + BREAKLINE;
            }
        }

        return instructions;
    }

    public String genPrimaryKey(String key) {
        return key != null ? PRIMARY + SPACE + KEY + SPACE + OPENPARENTHESES + key + CLOSEPARENTHESES : "";
    }

    public String genTablesInstructions (CassandraObjectData data) {
        return CREATE + SPACE + TABLE + SPACE + data.getObjectName() + SPACE + OPENPARENTHESES
                + BREAKLINE + genAttributesInstructions(data.getAttributes())
                + TAB + genPrimaryKey(data.getPrimaryKey())
                + BREAKLINE + CLOSEPARENTHESES + SEMICOLON + BREAKLINE;
    }

    public String genTypeInstructions (CassandraObjectData data) {
        return CREATE + SPACE + TYPE + SPACE + data.getObjectName().toUpperCase() + SPACE + OPENPARENTHESES
                + BREAKLINE + genAttributesInstructions(data.getAttributes())
                + BREAKLINE + CLOSEPARENTHESES + SEMICOLON + BREAKLINE;
    }
}
