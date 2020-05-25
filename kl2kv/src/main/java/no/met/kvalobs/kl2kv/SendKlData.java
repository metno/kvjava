/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: SendData.java,v 1.1.2.6 2007/09/27 09:02:19 paule Exp $                                                       

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
package no.met.kvalobs.kl2kv;

import java.util.*;
import org.apache.log4j.Logger;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvalobs.kl.*;
import no.met.kvclient.service.SendDataToKv.Result;



public class SendKlData implements DataToKv, SendDataToKv {
    
    private Kl2KvApp     app;
    private DataToKv dataToKv;
    private int      msgCount;
    private int      obsCount;

    static Logger logger = Logger.getLogger(SendKlData.class);

	private class MyDataToKv implements no.met.kvalobs.kl2kv.DataToKv{
		Boolean dryRun;
		public MyDataToKv(Boolean dryRun){
			this.dryRun=dryRun;
		}

		@Override
		public boolean sendData(String data, long stationid, long typeid)  {
			Result res;
			String decoder ="kldata/stationid="+stationid+"/type="+typeid;

			if( dryRun ) {
				System.out.println(decoder+"\n"+data+"\n");
				return true;
			}

			try {
				res = app.sendData(data, decoder);
			}
			catch( Exception e) {
				System.err.println("Failed to send data to kvalobs: " + e.getMessage());
				return false;
			}

			if(!res.isOk() ){
				System.out.println("Failed: stationid : " +stationid +(typeid>0?" typeid: " + typeid:"")
						+" - " + res);
				logger.warn("Failed: stationid : " +stationid +"typeid: " + typeid
						+ " - " + res);
				return false;
			}

			return true;
		}
	}




	/**
     * This contructor is primary used for testing. It takes an
     * DataToKv interface.
     * @param app The application class for this program
     * @param dataToKv an DataToKv interface. If it is null
     *         an default is created that sends data to kvalobs.
     */
    public SendKlData(Kl2KvApp app, DataToKv dataToKv, Boolean dryRun){
    	obsCount=0;
    	msgCount=0;

    	if(dataToKv==null)
    		this.dataToKv=new MyDataToKv(dryRun);
    	else
    		this.dataToKv=dataToKv;
    	
    	this.app=app;
    }


    public SendKlData(Kl2KvApp app, Boolean dryRun){
    	this(app, null, dryRun);
    }

	public boolean sendData(String data, long stationid, long typeid){

		return dataToKv.sendData(data, stationid, typeid);
	}


	public boolean sendDataToKv(String sTypeid, Station[] stations, List<TimeRange> obstimes,
								boolean disableQC1 ){

		KlDataHelper dh;
		DbConnection con=null;

		con=app.newKlDbConnection();

		if(con==null){
			System.out.println("Cant create a database connection!");
			logger.error("Cant create a database connection!");
			return false;
		}

		dh=new KlDataHelper(con,this,sTypeid, obstimes, disableQC1, app.getTablename());

		System.out.println("Running for typeid: " + dh.getTypeid());
		System.out.println("Stations: " + Station.toString(stations));
		System.out.println("Table: "+dh.getTable());

		logger.info("Running for typeid: " + (dh.getTypeid()==null?"all":dh.getTypeid()) ) ;
		logger.info("Stations: " + Station.toString(stations));
		logger.info("Table: "+dh.getTable());

		for(Station st : stations){
			if(dh.sendDataToKv(st)){
				System.out.println("Data sendt for station(s): " + st);
				logger.info("Data sendt for station(s): " + st);
			}else{
				msgCount+=dh.getMsgCount();
				obsCount+=dh.getObsCount();
				System.out.println("Failed to send data for station(s): " +st);
				logger.error("Failed to send data for station(s): " +st);
				return false;
			}
		}

		msgCount+=dh.getMsgCount();
		obsCount+=dh.getObsCount();

		app.releaseKlDbConnection(con);
		return true;
	}


    public int getMsgCount(){ return msgCount;}
    public int getObsCount(){ return obsCount;}
    
}

