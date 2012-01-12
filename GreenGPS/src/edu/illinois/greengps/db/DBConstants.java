package edu.illinois.greengps.db;

public interface DBConstants {

	public static final String DATABASE_NAME = "greengps.db";
	public static final int DATABASE_VERSION = 1; // increment version to clear an old database
	
	public static final String FS_TABLE = "fusionsuite";
	public static final String TABLE_CONFIG_MAP = "configMap";
	
	public static final String FS_ID = "SID";
	public static final String FS_BID = "BSONID";
	public static final String FS_DATA = "DATA"; // giant data blob of any string format
	
	public static final String STATE_TABLE = "fsState";
	public static final String STATE_ID = "stateID";
	public static final String STATE_NAME = "stateName";
	public static final String STATE_STATUS = "stateStatus";
	
}

