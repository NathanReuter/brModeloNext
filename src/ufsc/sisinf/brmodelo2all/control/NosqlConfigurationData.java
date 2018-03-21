package ufsc.sisinf.brmodelo2all.control;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class NosqlConfigurationData {
    private static NosqlConfigurationData ourInstance = new NosqlConfigurationData();
    public static NosqlConfigurationData getInstance() {
        return ourInstance;
    }

    private String dbName;
    private enum mongoValidationLevels {MODERATE, STRICT};
    private enum  mongoValidationActions {WARNING, ERROR};
    private mongoValidationLevels  mongoLevel;
    private mongoValidationActions  mongoAction;

    private NosqlConfigurationData() {
        dbName = AppConstants.dbName;
        mongoLevel = mongoValidationLevels.valueOf(AppConstants.MONGO_DEFAULT_VALIDATION_LEVEL);
        mongoAction = mongoValidationActions.valueOf(AppConstants.MONGO_DEFAULT_ACTION_LEVEL);
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
}
