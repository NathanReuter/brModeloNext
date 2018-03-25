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

    public String genInitialDBInstructions () {
        return DROP + SPACE + KEYSPACE + SPACE + dbName + SEMICOLON + BREAKLINE
                + CREATE + SPACE + KEYSPACE + SPACE + dbName + SPACE + WITH + SPACE + REPLICATION + SPACE +  EQUAL + SPACE
                + OPENBRACES
                    + surroundWithQuotes(CLASS) + COLON + SPACE + surroundWithQuotes(cassandraClass) + COMMA
                    + surroundWithQuotes(REPLICATIONFACTOR) + COLON + surroundWithQuotes(cassandraReplicationFactor)
                + CLOSEBRACES + SEMICOLON + BREAKLINE
                + USE + SPACE + dbName + SEMICOLON + BREAKLINE + BREAKLINE;
    }

    public String genAttributesInstructions (List<CassandraAttribute> attributes) {
        String instructions = "";

        for (CassandraAttribute attribute : attributes) {
            instructions += TAB + attribute.getName() + SPACE + attribute.getType() + COMMA + BREAKLINE;
        }

        return instructions;
    }

    public String genTablesInstructions (CassandraObjectData data) {
        return CREATE + SPACE + TABLE + SPACE + data.getObjectName() + SPACE + OPENPARENTHESES
                + BREAKLINE + genAttributesInstructions(data.getAttributes())
                + BREAKLINE + CLOSEPARENTHESES + SEMICOLON + BREAKLINE;
    }

    public String genTypeInstructions () {
        return CREATE + SPACE + TYPE + SPACE + "tablename" + SPACE + OPENPARENTHESES
                + BREAKLINE + CLOSEPARENTHESES + SEMICOLON + BREAKLINE;
    }
}
