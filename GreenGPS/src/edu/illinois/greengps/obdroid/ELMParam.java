package edu.illinois.greengps.obdroid;

import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.db.TableConstants;

abstract public class ELMParam {

	public int mode;
	public int pid;
	public TableConstants name;
	public String unit;
	public boolean hasCmd;
	public int errors;
	
	public float lastVal = Float.NaN;
	
	public ELMParam(TableConstants name, String unit, int mode, int pid) {
		this.mode = mode;
		this.pid = pid;
		this.name = name;
		this.unit = unit;
		this.hasCmd = true;
		this.errors = 0;
	}
	
	public void setHasCmd(boolean hasCmd) {
		this.hasCmd = hasCmd;
	}
	
	public void parseMsg(int msg[], int msgLen) {}
	
	/**
	 * Set this value to Float.NaN and add an error to the counter
	 * @return whether the param has exceed the error limit
	 */
	public boolean addError() {
		this.lastVal = Float.NaN;
		if (errors++ > FusionSuiteSingleton.obd_max_errors) {
			// Too many timeouts, assume that this command is not supported and ignore entirely
			hasCmd = false;
			FusionSuiteSingleton.getInstance().logMsg("ELMParam", "Too many errors. Disabling " + name);
		}
		
		return hasCmd;
	}
	
	public void resetError() {
		errors = 0;
	}
	
	public float getVal() {
		return lastVal;
	}
	
	public String getCmd() {
		if (hasCmd) {
			return String.format("%02x%02x", this.mode, this.pid);
		} else {
			return null;
		}
	}
}

