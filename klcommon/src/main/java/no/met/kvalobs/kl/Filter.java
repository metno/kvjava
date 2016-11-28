/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Filter.java,v 1.1.2.9 2007/09/27 09:02:19 paule Exp $                                                       

  Copyright (C) 2007 met.no

  Contact information:
  Norwegian Meteorological Institute
  Box 43 Blindern
  0313 OSLO
  NORWAY
  email: kvalobs-dev@met.no

  This file is part of KVALOBS

  KVALOBS is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 
  of the License, or (at your option) any later version.
  
  KVALOBS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License along 
  with KVALOBS; if not, write to the Free Software Foundation Inc., 
  51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package no.met.kvalobs.kl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;

import no.met.kvutil.dbutil.*;

import java.lang.Math;
import no.met.kvutil.StringHolder;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import no.met.kvutil.LongHolder;
import org.apache.log4j.Logger;

public class Filter {

	DbConnection con=null;
//	Kv2KlimaFilter dbKv2KlimaFilterElem=null;
//	ParamFilter    paramFilter=null;
	boolean       filterEnabled;
	static FilterCache cache=new FilterCache(2000, getMatricsPath());
	
	static  Logger logger=Logger.getLogger(Filter.class);
	
	protected void addToString(StringHolder sh, String s){
		if(s==null || sh==null)
			return;
		
		if(sh.getValue()==null){
			sh.setValue(s);
		}else{
			sh.setValue(sh.getValue()+" "+s);
		}
	}

	static Path getMatricsPath() {
		String appName = KlApp.getAppName();
		Path path =  Paths.get(KlApp.getKvpath());
		return path.resolve("var/log/kvalobs/"+appName+".matrics");
	}



	protected boolean paramFilter(long stationID, long typeID, int paramID, int level,
			                      int sensor, boolean useLevelAndSensor, 
							      Timestamp obstime,
                                  StringHolder msg){
		ParamFilter    paramFilter=cache.getParamFilter(stationID);

		if(!paramFilter.filter(paramID, typeID, 
				               level, sensor, useLevelAndSensor,
				               obstime, con) ) {
			if( useLevelAndSensor )
				addToString(msg, "[paramFilter] Blocked param: "+paramID+
						         " sensor: "+sensor+
					             " level: "+level);
			else
				addToString(msg, "[paramFilter] Blocked param: "+paramID + 
						         ", No sensor and level." );
			return false;
		}

		return true;
	}

	public boolean inDateInterval(Timestamp fromDate, Timestamp toDate, 
								    Timestamp obstime){
		boolean ret=false;
		
		logger.debug("DEBUG: Filter.inDateInterval: obstime: "+obstime);
		logger.debug("DEBUG: Filter.inDateInterval: "+fromDate +" - " + toDate);
		
		if(fromDate==null){
			if(toDate==null)
				ret=true;
			else if(obstime.compareTo(toDate)<=0)
				ret=true;
		}else if(obstime.compareTo(fromDate)>=0){
			if(toDate==null)
				ret=true;
			else if(obstime.compareTo(toDate)<=0)
				ret=true;
		}
		
		logger.debug("DEBUG: Filter.inDateInterval: obstime in dateinterval: " + ret);
		return ret;
	}
	
	protected boolean doStatusS(Kv2KlimaFilter dbelem, 
            					StringHolder   msg){
		if(dbelem==null)
			return true;
		
		if(dbelem.getTypeid()!=1)
			return true;

		String status=dbelem.getStatus();

		if(status==null)
			return true;

		if(status.length()>0){
			char chStatus=status.charAt(0);

			if(chStatus=='S' || chStatus=='s')
				return true;
		}

		addToString(msg, 
				   "[doStatusS] blocked SYNOP data!");
		return false;
	}

	protected boolean doStatus(Kv2KlimaFilter dbelem, 
            					 StringHolder msg){
		if(dbelem==null)
			return true;

		if(dbelem.getStnr()==0){
			addToString(msg,
			           "[doStatus.MISSING] blocked, not in table T_KV2KLIMA_FILTER or no valid time interval!");
			return false;
		}
			
		
		String status=dbelem.getStatus();

		if(status==null){
			addToString(msg,
	           "[doStatus.MISSING] blocked, not in table T_KV2KLIMA_FILTER or no valid time interval!");
			return false;
		}

		if(status.length()==0)
			return true;
		
		char chStatus=status.charAt(0);

		if(chStatus=='D' || chStatus=='d')
			return true;
			
		addToString(msg,
					"[doStatus.TEST] status=" + chStatus + ", blocked!");

		return false;
	}

	
	
	protected boolean doStatusT(Kv2KlimaFilter dbelem, 
			                         StringHolder msg){
		if(dbelem==null)
			return true;
			
		String status=dbelem.getStatus();
		
		if(status==null)
			return true;
		
		if(status.length()>0){
			char chStatus=status.charAt(0);
				
			if(chStatus=='T' || chStatus=='t'){
				addToString(msg,
						    "[doStatusT] blocked teststation.");
				return false;
			}
		}
			
		return true;
	}
	
	protected void doNyStnr(LongHolder stationID, long typeID_, 
							Kv2KlimaFilter dbElem,
						    StringHolder   msg){
		if(dbElem.getNytt_stnr()!=0){
			if(dbElem.getTypeid()==0 || 
			   dbElem.getTypeid()==Math.abs(typeID_)){
				addToString(msg, 
							"[doNyStnr] Changed stnr to "+
							dbElem.getNytt_stnr()+".");

				stationID.setValue( dbElem.getNytt_stnr() );
			}
		}
	}
	
	
	public Filter(DbConnection con_){
		filterEnabled=true;
		con=con_;
	}
	
	public void setFilterEnabled(boolean enabled){
		filterEnabled=enabled;
	}
	
	public boolean getFilterEnabled(){
		return filterEnabled;
	}
	
	public Kv2KlimaFilter loadFromDb( long stationID, long typeID_, 
									  Timestamp obstime){
		long typeid=Math.abs(typeID_);
		return cache.getKv2KlimaFilter(stationID,typeid, con);
	}
	

	
	public boolean filter(LongHolder stationID, long typeID_, int paramID, 
			              int level, int sensor, boolean useLevelAndSensor,
			              Instant obstime_, StringHolder msg){
		Kv2KlimaFilter dbElem;

		if(!filterEnabled)
			return true;
		
		logger.debug(" -- Filter:  sid: "+stationID.getValue() + " tid: "+typeID_
		             + " paramID: "+paramID + " obstime: " + obstime_);
		
		Timestamp obstime = Timestamp.from(obstime_);
		logger.debug(" -- Filter:  Incomming obstime decoded to: " + obstime_ + " (GMT) -> Timestamp: " + obstime );
		
		dbElem=loadFromDb( stationID.getValue(), typeID_, obstime);
	
		if( ! dbElem.isOk() ) {
		//There was a problem with loading from the database.
			logger.error(Instant.now() 
		     			+" ERROR: filter: DB error, blocking the observation!");
			addToString(msg, "BLOCKED: DB error, blocking the observation. obstime: "+ obstime +" stationid: " + stationID.getValue() + " typeid: " +  typeID_ );
			return false;
		}

		
		
		if( ! dbElem.hasFilterElements() ){
			logger.info("BLOCKED: No filter data for station: " + stationID.getValue() + " typeid: " +  typeID_ );
			addToString(msg, "BLOCKED: No filter data for stationid: " + stationID.getValue() + " typeid: " +  typeID_ );
			return false;
		}
		
		if( ! dbElem.setCurrentFilterElem(obstime) ) {
			logger.info("BLOCKED: No filter element for station that match obstime.");
			addToString(msg, "BLOCKED: No filter data for station at obstime : " + obstime + " stationid: " + stationID.getValue() + " typeid: " +  typeID_ );
			return false;
		}

		if(!doStatus(dbElem, msg))
			return false;
		
		doNyStnr(stationID, typeID_, dbElem, msg);
		
		if(!paramFilter(stationID.getValue(), typeID_, paramID, 
			            level, sensor, useLevelAndSensor,
			            obstime, msg))
			return false;
		
		return true;
	}
	
	
	public boolean filter(TextDataElem data, 
            StringHolder msg){
		LongHolder stationID = new LongHolder(data.stationID);

		if( filter(stationID, data.typeID, data.paramID, 0, 0,
				false, data.obstime, msg ) ){
			//The stationid may have changed.
			data.stationID = stationID.getValue();
			return true;
		}

		return false;
	}

	
	
	/** Shall we use this observation element. 
	 * 
	 * @param data an observation element.
	 * 
	 * @return true if we shall use this observation element and false otherwise.
	 */
	public boolean filter(DataElem data, 
			              StringHolder msg){
		LongHolder stationID = new LongHolder(data.stationID);
		
		if( filter(stationID, data.typeID, data.paramID, data.level, data.sensor, 
				   true, data.obstime, msg ) ){
			//The stationid may have changed.
			data.stationID = stationID.getValue();
			return true;
		}
		
		return false;
		
	}
}
