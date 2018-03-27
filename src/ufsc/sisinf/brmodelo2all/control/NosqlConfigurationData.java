package ufsc.sisinf.brmodelo2all.control;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class NosqlConfigurationData {
    private static NosqlConfigurationData ourInstance = new NosqlConfigurationData();

    public static NosqlConfigurationData getInstance() {
        return ourInstance;
    }
    private String dbName;
    public enum mongoValidationLevels {MODERATE, STRICT};
    public enum  mongoValidationActions {WARNING, ERROR};
    public enum  cassandraStrategies {SimpleStrategy, NetworkTopologyStrategy};
    private mongoValidationLevels  mongoLevel;
    private mongoValidationActions  mongoAction;
    private cassandraStrategies cassandraStrategy;
    private String cassandraReplicationFactor;

    private NosqlConfigurationData() {
        dbName = AppConstants.dbName;
        mongoLevel = mongoValidationLevels.valueOf(AppConstants.MONGO_DEFAULT_VALIDATION_LEVEL);
        mongoAction = mongoValidationActions.valueOf(AppConstants.MONGO_DEFAULT_ACTION_LEVEL);
        cassandraStrategy = cassandraStrategies.valueOf(AppConstants.CASSANDRA_DEFAULT_CLASS);
        cassandraReplicationFactor = AppConstants.CASSANDRA_DEFAULT_REPLICATION_FACTOR;
    }

    public void setDbName (String name) {
        dbName = name;
    }

    public String getDbName() {
        return dbName;
    }

    public void setMongoValidationLevel(String level) {
        mongoLevel = mongoValidationLevels.valueOf(level);
    }

    public String getMongoValidationLevel() {
        return mongoLevel.toString();
    }

    public void setMongoValidationActions(String action) {
        mongoAction = mongoValidationActions.valueOf(action);
    }

    public String getMongoValidationActions() {
        return mongoAction.toString();
    }

    public String getCassandraStrategy() {
        return cassandraStrategy.toString();
    }

    public void setCassandraStrategy(String strategy) {
        cassandraStrategy = cassandraStrategies.valueOf(strategy);
    }

    public String getCassandraReplicationFactor() {
        return cassandraReplicationFactor;
    }

    public void setCassandraReplicationFactor(String cassandraReplicationFactor) {
        this.cassandraReplicationFactor = cassandraReplicationFactor;
    }
}
