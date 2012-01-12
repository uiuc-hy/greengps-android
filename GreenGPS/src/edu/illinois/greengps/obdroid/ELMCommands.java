package edu.illinois.greengps.obdroid;

import java.util.Collection;
import java.util.HashMap;
import edu.illinois.greengps.db.TableConstants;

public class ELMCommands {

	private HashMap<Integer, ELMParam> params = new HashMap<Integer, ELMParam>();
	
	public ELMCommands() throws Exception {
		
		/*
		 *  Set all commands we are interested in, the params each override
		 *  parseMsg() because the values are calculated differently.
		 *  
		 *  formulas from http://en.wikipedia.org/wiki/OBD-II_PIDs#Standard_PIDs
		 *  
		 *  msg[0] is mode
		 *  msg[1] is pid
		 *  msg[2] is A
		 *  msg[3] is B ...
		 */
		
		params.put(new Integer(0x10),
				new ELMParam(TableConstants.OBDMASSAIRFLOW, "g/s", 0x01, 0x10) {
					@Override
					public void parseMsg(int msg[], int msgLen) {
						this.lastVal = (float)((msg[2] << 8 | msg[3]) / 100.0);
					}
				}
			);
		params.put(new Integer(0x0d),
				new ELMParam(TableConstants.OBDSPEED, "km/h", 0x01, 0x0d) {
					@Override
					public void parseMsg(int msg[], int msgLen) {
						this.lastVal = (float) msg[2];
					}
				}
			);
		params.put(new Integer(0x44),
				new ELMParam(TableConstants.OBDCOMMANDEQRATIO, "%", 0x01, 0x44) {
					@Override
					public void parseMsg(int msg[], int msgLen) {
						this.lastVal = (float)(msg[2] << 8 | msg[3]) / 32768;
					}
				}
			);
		params.put(new Integer(0x0c),
				new ELMParam(TableConstants.OBDENGINERPM, "rpm", 0x01, 0x0c) {
					@Override
					public void parseMsg(int msg[], int msgLen) {
						this.lastVal = (float)((msg[2] << 8 | msg[3]) / 4.0);
					}
				}
			);
		params.put(new Integer(0x11),
				new ELMParam(TableConstants.OBDTHROTTLEPOSITION, "%", 0x01, 0x11) {
					@Override
					public void parseMsg(int msg[], int msgLen) {
						this.lastVal = (float)(msg[2] * 100 / 255.0);
					}
				}
			);
		
		/*
		params.put(new Integer(0x04),
			new ELMParam(TableConstants.OBDENGINELOAD, "%", 0x01, 0x04) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)msg[2] * 100 / 255;
				}
			}
		);
		params.put(new Integer(0x05),
			new ELMParam(TableConstants.OBDCOOLANTTEMP, "C", 0x01, 0x05) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2] - 40);
				}
			}
		);
		params.put(new Integer(0x06),
			new ELMParam(TableConstants.OBDSHORTTERMFUELTRIM, "%", 0x01, 0x06) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2]-128) * 100/128;
				}
			}
		);
		params.put(new Integer(0x07),
			new ELMParam(TableConstants.OBDLONGTERMFUELTRIM, "%", 0x01, 0x07) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2]-128) * 100/128;
				}
			}
		);
		params.put(new Integer(0x0a),
			new ELMParam(TableConstants.OBDFUELPRESS, "kPa", 0x01, 0x0a) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float) msg[2];
				}
			}
		);
		params.put(new Integer(0x0b),
			new ELMParam(TableConstants.OBDINTAKEMANIFOLDPRESS, "kPa", 0x01, 0x0b) {
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float) msg[2];
				}
			}
		);


		params.put(new Integer(0x0e),
			new ELMParam(TableConstants.OBDTIMINGADVANCE, "g/s", 0x01, 0x0e) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = ((float)msg[2] / (float)2.0) - 64;
				}
			}
		);
		params.put(new Integer(0x0f),
			new ELMParam(TableConstants.OBDAIRINTAKETEMP, "C", 0x01, 0x0f) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2] - 40);
				}
			}
		);


		params.put(new Integer(0x1f),
			new ELMParam(TableConstants.OBDENGINERUNTIME, "s", 0x01, 0x1f) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2] << 8 | msg[3]);
				}
			}
		);
		params.put(new Integer(0x33),
			new ELMParam(TableConstants.OBDBAROMETRICPRESS, "kPa", 0x01, 0x33) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2]);
				}
			}
		);

		params.put(new Integer(0x46),
			new ELMParam(TableConstants.OBDAMBIENTAIRTEMP, "C", 0x01, 0x46) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)(msg[2] - 40);
				}
			}
		);
		params.put(new Integer(0x5e),
			new ELMParam(TableConstants.OBDFUELRATE, "L/h", 0x01, 0x5e) {
				@Override
				public void parseMsg(int msg[], int msgLen) {
					this.lastVal = (float)((msg[2] << 8 | msg[3]) * 0.05);
				}
			}
		);
		*/
	}
	
	public Collection<ELMParam> findSupportedCommands(String bits) {
		/*
		 * TODO: implement bitwise matching from one or more ECU's to determine
		 * which commands are supported
		 */
		return params.values();
	}
	
	public Collection<ELMParam> getSupportedCommands() {
		return params.values();
	}
}

