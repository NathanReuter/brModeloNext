package ufsc.sisinf.brmodelo2all.util;

public class AppConstants {

	public static final int TEXT_FIELD = 0;

	public static final int CHECK_BOX = 1;

	public static final int COMBO_BOX = 2;

	public static final String dbName = "NovoDB";

	public static final String MONGO_DEFAULT_VALIDATION_LEVEL = "MODERATE";

	public static final String MONGO_DEFAULT_ACTION_LEVEL = "ERROR";

	public static final String CASSANDRA_DEFAULT_CLASS = "SimpleStrategy";

	public static final String CASSANDRA_DEFAULT_REPLICATION_FACTOR = "3";

	public static enum JSON_SCHEMA_TYPES {STRING, INTEGER, NUMBER, BOOLEAN}

	public static final String MONGO_HELP_INSTRUCTIONS = "/*This is an automatic generated schema code built to run in Mongo Shell. \n" +
			"\t* To run it properly: \n" +
			"\t* 1: Save file like 'file.js'\n" +
			"\t* 2: Run the command in your terminal:\n" +
			"\t* \t$ mongo < 'file/location/path/file.js'*/";
	public static final String CASSANDRA_HELP_INSTRUCTIONS =
			"# This is an automatic generated schema code built to run in Cassandra Shell. \n" +
					"# To run it properly: \n" +
					"# 1: Save the file like 'file.cql'\n" +
					"# 2: Run the command in your terminal: \n" +
					"# 	$ cqlsh -f 'file/location/path/file.cql'";
	public static final String Redis_HELP_INSTRUCTIONS =
			"-- This is an automatic generated schema code built to be interpred by Redis Enviroment. \n" +
					"-- To run it properly: \n" +
					"--   1: Save file like 'file.lua'\n" +
					"--   2: Run the command in your terminal:\n" +
					"--   $ redis-cli --eval 'file/location/path/file.lua' collectionToTest:id attribute1 attribute2...atributeN";


}