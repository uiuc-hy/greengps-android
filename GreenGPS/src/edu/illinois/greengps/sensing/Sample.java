package edu.illinois.greengps.sensing;

import java.util.Iterator;
import java.util.Map;

import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.db.TableConstants;

public class Sample 
{
  public long id;
  public String tag;
  public TableConstants type;
  public Map<TableConstants, String> attribute_values;

  public Sample(long id, String tag, Map<TableConstants, String> attribute_values, TableConstants sampleType) {
    this.id = id;
    this.tag = tag;
    this.type = sampleType;
    this.attribute_values = attribute_values;
  }

  public synchronized String toString() {
	
	String str = TableConstants.SAMPLETYPE.ordinalToString() + ":" + type.ordinalToString() + " "
				+ type.ordinalToString() + ":" + id + " "
				+ TableConstants.TAG.ordinalToString() + ":"  + tag; 

	
    Iterator<Map.Entry<TableConstants, String>> it =
      attribute_values.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry<TableConstants, String> pairs = it.next();
        str = str + " " + pairs.getKey().ordinalToString() + ":" + pairs.getValue();
    }
    if (FusionSuiteSingleton.debug)
    	return str + calculated();
    else 
    	return str;
  }
  
  public synchronized String toReadableString() {
	  String str = TableConstants.SAMPLETYPE + ":" + type.ordinalToString() + " "
		+ type + ":" + id + " "
		+ TableConstants.TAG + ":"  + tag; 

	  Iterator<Map.Entry<TableConstants, String>> it =
		  attribute_values.entrySet().iterator();
	  while (it.hasNext()) {
		  Map.Entry<TableConstants, String> pairs = it.next();
		  str = str + " " + pairs.getKey().toString() + ":" + pairs.getValue();
	  }
	  
	  if (FusionSuiteSingleton.debug)
		  return str + calculated();
	  else 
		  return str;
  }
  
  public String calculated() { // fuel rate and rpm in string form
	  String str = " ";
	  
	  double maf, eqv=1, fuelrate;
	  try{
		  maf = Double.parseDouble(attribute_values.get(TableConstants.OBDMASSAIRFLOW));
	  } catch (Exception e) {
		  return str;
	  }

	  try {
		  eqv = Double.parseDouble(attribute_values.get(TableConstants.OBDCOMMANDEQRATIO));
		  // 14.7 * 6.17 * 454 = 41177.346
		  fuelrate = maf / (eqv * 41177.346);
	  } catch (Exception e) {
		  fuelrate = maf / (41177.346);
	  }
	  
	  str = str + " FuelRate(gmin):" + fuelrate*60;
	  str = str + " FuelRate(ghour):" + fuelrate*3600;  

	  try { 
		  double vss = Double.parseDouble(attribute_values.get(TableConstants.OBDSPEED));
		  double mpg = 7.107 * vss * eqv / maf;
		  str = str + " MPG(mpg):" + mpg;
	  } catch (Exception e) {
		  return str;
	  }
	  
	  return str;
  }
}


