package edu.illinois.greengps.db;

public enum TableConstants { // this matches the constants file that the server uses, add new fields to the end to preserve ordering
	
	// GPS table fields: mapping 0-5 to column names
	GPSALTITUDE ( "GPSAltitude" ), 							// 0
	GPSBEARING ( "GPSBearing" ), 
	GPSLATITUDE ( "GPSLatitude" ),
	GPSLONGITUDE ( "GPSLongitude" ),
	GPSSPEED ( "GPSSpeed" ),
	GPSTIME ( "GPSTime" ),									// 5
		
	// OBD table fields: mapping 6-24 to column names
	OBDAIRINTAKETEMP ( "OBDAirIntakeTemp" ),				// 6
	OBDAMBIENTAIRTEMP ( "OBDAmbientAirTemp" ),
	OBDBAROMETRICPRESS ( "OBDBarometricPress" ),
	OBDCOMMANDEQRATIO ( "OBDCommandEqRatio" ),
	OBDCOOLANTTEMP ( "OBDCoolantTemp" ),					// 10
	OBDENGINELOAD ( "OBDEngineLoad" ),
	OBDENGINERPM ( "OBDEngineRPM" ),
	OBDENGINERUNTIME ( "OBDEngineRuntime" ),
	OBDFUELECONOMY ( "OBDFuelEconomy" ),					// deprecated
	OBDFUELECONOMYCMDMAP ( "OBDFuelEconomyCmdMAP" ),		// 15 deprecated
	OBDFUELECONOMYMAP ("OBDFuelEconomyMAP" ),				// deprecated
	OBDFUELPRESS ( "OBDFuelPress" ),
	OBDLONGTERMFUELTRIM ( "OBDLongTermFuelTrim" ),
	OBDMASSAIRFLOW ( "OBDMassAirFlow" ),
	OBDSHORTTERMFUELTRIM ( "OBDShortTermFuelTrim" ),		// 20
	OBDSPEED ( "OBDSpeed" ),
	OBDTHROTTLEPOSITION ( "OBDThrottlePosition" ), 
	OBDTIMINGADVANCE ( "OBDTimingAdvance" ),
	OBDINTAKEMANIFOLDPRESS ( "OBDIntakeManifoldPress" ),	// 24
	OBDFUELRATE ( "OBDFuelRate" ),
	
	// All other fields, 25+
	TAG ( "Tag" ),											// 26
	GPSSAMPLEID ( "SampleID" ),
	OBDSAMPLEID ( "SampleID" ),
	NODEID ( "NodeID" ),									// 29
	
	SAMPLETYPE ( "SampleType" ),							// 30	
	ENGINESTATUSCHANGE ( "EngineStatusChange" ),			// 31
	
	/***************** Add new fields after this line to keep ENUM ordering **************/
	
	TRIPID ( "TripID");
	
	

	private final String desc;
	
	TableConstants(String s) {
		desc = s;
	}
	
	public String toString() {
		return desc;
	}
	
	public String ordinalToString() {
		return Integer.toString(ordinal());
	}
	
	public static TableConstants convert(int value)
	{
		try {
			return TableConstants.class.getEnumConstants()[value];
		} catch (Exception e) {
			return null;
		}
	}
	
	public static TableConstants convert(String ordinal) {
		try {
			int value = Integer.parseInt(ordinal);
			return TableConstants.class.getEnumConstants()[value];
		} catch (Exception e) {
			return null;
		}
	}
	
	public static TableConstants fromString(String desc) {
		if (desc != null) {
			for (TableConstants b : TableConstants.values()) {
				if (desc.equals(b.desc)) 
					return b;
			}
		}
	    
	    throw new IllegalArgumentException ("No enum with description: " + desc);
	}
}
