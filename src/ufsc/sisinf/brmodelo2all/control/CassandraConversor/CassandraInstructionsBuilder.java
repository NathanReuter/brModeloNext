package ufsc.sisinf.brmodelo2all.control.CassandraConversor;


import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

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
    static final String FROZEN = "frozen";
    static final String IFNOTEXISTS = "IF NOT EXISTS";

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
    static final String DOUBLEQUOTE = "\"";
    static final String TAB = "  ";


    /* ConfigVariables */
    private String dbName;
    private String cassandraClass;
    private String cassandraReplicationFactor;

    public CassandraInstructionsBuilder() {
        this.configData = NosqlConfigurationData.getInstance();
        dbName = configData.getDbName();
        cassandraClass = configData.getCassandraStrategy();
        cassandraReplicationFactor = configData.getCassandraReplicationFactor();
    }

    private String surroundWithQuotes(String word) {
        return ONEQUOTE + word + ONEQUOTE;
    }
    private String surroundWithChevron(String word) {
        return OPENCHEVRON + word + CLOSECHEVRON;
    }

    public String genInitialDBInstructions () {
        return AppConstants.CASSANDRA_HELP_INSTRUCTIONS + BREAKLINE
                + BREAKLINE + CREATE + SPACE + KEYSPACE + SPACE + IFNOTEXISTS + SPACE + dbName + SPACE + WITH + SPACE + REPLICATION + SPACE +  EQUAL + SPACE
                + OPENBRACES + BREAKLINE
                    + TAB + surroundWithQuotes(CLASS) + COLON + SPACE + surroundWithQuotes(cassandraClass) + COMMA
                    + BREAKLINE + TAB + surroundWithQuotes(REPLICATIONFACTOR) + COLON + surroundWithQuotes(cassandraReplicationFactor)
                + BREAKLINE + CLOSEBRACES + SEMICOLON + BREAKLINE
                + USE + SPACE + dbName + SEMICOLON + BREAKLINE + BREAKLINE;
    }

    public String getAttributeType (CassandraAttribute attribute, boolean isList) {
        if (attribute.getType().equals(CassandraAttribute.CassandraTypes.NEWTYPE)) {
            return isList ? addFrozenToken(attribute.getName().toUpperCase()): attribute.getName().toUpperCase();
        }

        return attribute.getType().toString();
    }

    public String addFrozenToken (String word) {
        return FROZEN + SPACE + surroundWithChevron(word);
    }

    public String genAttributesInstructions (List<CassandraAttribute> attributes) {
        String instructions = "";

        for (CassandraAttribute attribute : attributes) {
            if (attribute.isMultipleAttributes()) {
                instructions += TAB + attribute.getName() + SPACE + LIST
                        + surroundWithChevron(getAttributeType(attribute, true)) + COMMA + BREAKLINE;
            } else {
                instructions += TAB + attribute.getName() + SPACE + getAttributeType(attribute, false) + COMMA + BREAKLINE;
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
