package edu.illinois.greengps.obdroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import edu.illinois.greengps.activity.FusionSuiteSingleton;

import android.util.Log;

public class ELMInterface {
	private static final String TAG = "ELMInterface";
	private static final Integer ELMMAXBUF = 512;
	private InputStream _in;
	private OutputStream _out;
	private Integer _lock = new Integer(0);
	public String elmid = "Unknown";
	public String vid = "Unknown";
	
	private Collection<ELMParam> params = new Vector<ELMParam>();
	
	public ELMInterface(InputStream in, OutputStream out) {
		_in = in;
		_out = out;
		
		try {
			ELMCommands ec = new ELMCommands();
			params = ec.findSupportedCommands(null);
		} catch (Exception e) {
			FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
			singleton.display(TAG, "Error initializing ELMCommands");
		}
	}
		
	private void flush() throws Exception  {
		_out.flush();
		while (_in.available() > 0) {
			_in.read();
		}
	}

	public static String toHexString(String s)
	{
		return toHexString(s.toCharArray());
	}
	
	public static String toHexString(char[]bytes)
	{
	    StringBuilder sb = new StringBuilder(bytes.length*2);
	    for(char b: bytes) {
	    	if (Character.isLetter(b) || Character.isDigit(b)) {
	    		sb.append(b);
	    	} else if (b == '\r') {
	    		sb.append("\\r");
	    	} else if (b == '\n') {
	    		sb.append("\\n");
	    	} else if (b == ' ') {
	    		sb.append(' ');
	    	} else {	    		
	    	    sb.append("\\" + Integer.toHexString(b+0x800).substring(1));
	    	}
	    }
	    return sb.toString();
	}
	
	private String readResponse() throws Exception	{
		byte[] buf = new byte[ELMMAXBUF];
		String response = "";

		while (true) {
			int bytesRead = this._in.read(buf);
			String strBuf = new String(buf, 0, bytesRead);
			response += strBuf;
			
			if (strBuf.startsWith("-1")) {
				throw new ELMException(
					"Timed out. Received [" + toHexString(response) + "]");
			}
			
			/*
			 * On initial connection, we can get off by one with our responses.
			 * Ignore any response with "ELM"
			 */
			if (strBuf.contains(">")) {
				break;
			}
			//Log.i(TAG, "Read so far: " + toHexString(response));
		}
		
		FusionSuiteSingleton.getInstance().logMsg(TAG, "Received: " + toHexString(response));
		response = response.replace("SEARCHING...", "");
		response = response.trim();

		if (response.contains("?")) {
			throw new ELMException("Did not understand");
		}

		if (response.contains("BUS BUSY")) {
			throw new ELMException("Bus busy");
		}

		if (response.contains("FB ERROR")) {
			throw new ELMException("Feedback error");
		}

		if (response.contains("DATA ERROR")) {
			throw new ELMException("Data error");
		}

		if (response.contains("NO DATA")) {
			throw new ELMException("No data");
		}

		if (response.contains("UNABLE TO CONNECT")) {
			throw new ELMException("Unable to connect to ECU");
		}
		
		return response;		
	}
	
	private void sendCmd(String cmd) throws Exception  {
		String s = cmd.trim() + "\r";
		this._out.write(s.getBytes());
	}
	
	public String doRawCmd(String cmd) throws Exception, ELMException {
		FusionSuiteSingleton.getInstance().logMsg(TAG, "doing " + cmd);
		synchronized(this._lock) {
			sendCmd(cmd);
			try {
				return readResponse().replace(">", "");
			} catch (Exception e) {
				String cause = e.getMessage();
				if (cause == null) cause = "Unknown problem";
				throw new ELMException(cause + " while executing [" + cmd + "]");
			}
		}
	}
	
	public String tryRawCmd(String cmd) throws Exception {
		try {
			String res = doRawCmd(cmd);
			return res;
		} catch (ELMException e) {
			// ignore ELMException, most likely command timeout
			return null;
		}
	}
	
	public String doCmd(String cmd, String desc) throws Exception  {
		FusionSuiteSingleton.getInstance().logMsg(TAG, "doing " + cmd);
		synchronized(this._lock) {
			sendCmd(cmd);
			String response;
			try {
				response = readResponse();
				if (response.contains("ELM")) {
					Log.i(TAG, "Early response, trying again");
					sendCmd(cmd);
					response = readResponse();
				}
			} catch (Exception e) {
				String cause = e.getMessage();
				if (cause == null) cause = "Unknown problem";
				throw new ELMException(cause + "while executing [" + cmd + "]");
			}
			if (!response.contains("OK")) {
				throw new ELMException("Unable to " + desc + " [" + toHexString(response) + "]");
			}
			return response.replaceAll("[\r\n>]", " ");
		}
	}
	
	public String tryCmd(String cmd, String desc) throws Exception {
		try {
			String res = doCmd(cmd, desc);
			return res;
		} catch (ELMException e) {
			// ignore ELMException, most likely command timeout
			return null;
		}
	}
	
	public void getSupportedPids() throws ELMException, Exception {
		// TODO: test for supported pids
		FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
		String s = tryRawCmd("0100");
		flush();
		singleton.logMsg(TAG, "Command 0100:\n" + s);
		s = tryRawCmd("0120");
		flush();
		singleton.logMsg(TAG, "Command 0120:\n" + s);
		s = tryRawCmd("0140");
		flush();
		singleton.logMsg(TAG, "Command 0140:\n" + s);
	}
			
	public void reset() throws Exception{
//		exitPowerSave(); // no OK
		
		this.elmid = tryRawCmd("atz");
		Thread.sleep(200);
		flush();
		
		this.vid = tryRawCmd("0902");
		flush();
		
		doCmd("ate0", "disable echo");
		doCmd("atl0", "disable linefeed");
		// TODO: if ath0 not supported, then parsing result will certainly fail
		doCmd("ath0", "disable headers");
		tryCmd("ats0", "disable spaces");  // may not be supported
		tryCmd("atbrd 45", "try higher baud rate"); // may not be supported
		
		getSupportedPids();
		
		//Determine the fastest working timeout value
		String[] timeouts = {"0A", "14", "1E", "28", "32"};
		for (String timeout: timeouts) {
			try {
				doCmd("atst" + timeout, "set timeout");
				
				for (int i = 0; i < 3; i++) {
					doRawCmd("0100");
				}
				
				Log.i(TAG, "timeout set to " + timeout);
				return;
				
			} catch (Exception e) { 
				Log.i(TAG, "timeout " + timeout + "failed, trying next");
			}
		}
		throw new ELMException("Unable to set any timeout");
	}
	
	public ELMParam getParam(int mode, int pid) {
		for (Iterator<ELMParam> i = this.params.iterator(); i.hasNext();) {
			ELMParam p = i.next();
			if (p.mode == mode && p.pid == pid) {
				return p;
			}
		}
		return null;
	}
	
	private void parseRawResults(String results) throws Exception {
		int[]msg = new int[5];
		String lines[] = results.split("[\\r\\n]+");
		for (String line: lines) {
			line = line.replace(" ", "");
			int numBytes = 0;
			for (int i = 0; i < line.length(); i+=2) {
				numBytes++;
				try {
					msg[i / 2] = Integer.parseInt(line.substring(i, i + 2), 16) & 0xff;
				} catch (Exception e) {
					msg[i / 2] = 0;
				}
			}
			if ((msg[0] & 0x40) != 0) {
				int mode = msg[0] & ~0x40;
				int pid = msg[1];
			    ELMParam p = getParam(mode, pid);
			    p.parseMsg(msg, numBytes);
			}
		}
	}
	
	public Collection<ELMParam> getParams() throws Exception, IOException {

		int num_results = 0; // number of usable query outcomes

		for (Iterator<ELMParam> i = this.params.iterator(); i.hasNext();) {
			ELMParam p = i.next();
			try {
				if (p.hasCmd) {
					String results = doRawCmd(p.getCmd());
					if (results != null) {
						Log.i(TAG, String.format("%s: %s [%s]",
								p.name, p.getCmd(), toHexString(results)));
						//String results = "41 0C 1A F8\r\r41 0C 1A F8\r\r";
						parseRawResults(results);
						num_results += 1;
					} 
				}
			} catch (Exception e) {
				p.lastVal = Float.NaN;
				FusionSuiteSingleton.getInstance().logMsg(TAG, e.toString());
				if (e.getClass().equals(IOException.class)) {
					throw e;
				}
			}
		}
		
		if (num_results > 0)
			return this.params;
		else 
			// probably an engine off, but the socket has not closed
			throw new IOException("All NULL, No response from engine");	
	}
	
	public void enterPowerSave() throws Exception {
		tryCmd("atlp", "low power mode");
	}
	
	public void exitPowerSave() throws ELMException, Exception {
		tryRawCmd("@");
	}
}

