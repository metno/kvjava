/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Kv2KlApp.java,v 1.1.2.11 2007/09/27 09:02:19 paule Exp $                                                       

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
package no.met.kvalobs.kv2kl;

import no.met.kvalobs.kl.KlApp;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.FileUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public class Kv2KlApp extends KlApp
{
	static Logger logger=Logger.getLogger(Kv2KlApp.class);

    Timer        dbCleanupTimer=null;
    Timer        isUpTimer=null;
    boolean     kvIsUp;
    Instant    kvLastContact=null;
    String       kvIsUpLogFile=null;
    boolean     isUpTaskIsRunning=false;
        
    class DbCleanup extends TimerTask{
    	Kv2KlApp app;
	
    	public DbCleanup(Kv2KlApp app_){
    		app=app_;
    	}

    	public void run(){
    		int n;
            NDC.push("DbCleanup");
    		logger.debug("DbCleanup: running db connection cleanup!");
    		n=app.checkForConnectionsToClose();
            
            if(n>0)
                logger.info("Idle connections closed: "+n);
            
            NDC.pop();
    	}
    }

    class IsUp extends TimerTask{
    	Kv2KlApp app;
    	String   filename;
	
    	public IsUp(Kv2KlApp app, String filename){
    		this.app=app;
    		this.filename=filename;
    	}

    	public void run(){
            NDC.push("IsUp");
    		logger.debug("Updating the file '"+filename+"'!");
    		Instant now=Instant.now();
    		Instant lastContact=app.getLastKvContact();
    		boolean isUp=app.getKvIsUp();
    		
    		String buf=null;
    
    		app.setIsUpTimeIsRunning(true);	
			
    		
    		if(!isUp){
    			
    			if(lastContact==null) //In startup.
    				buf="Startup: "+now;
    			else
    				buf="Down: "+lastContact;
    			
    			logger.debug(buf);
    		}else{
    			Duration s=Duration.between(lastContact, now).abs();
    			
    			if(s.getSeconds()<3600){
    				buf="Up: "+lastContact;
    				logger.debug(buf);
    			}else{
    				logger.warn("Last contact from kvalobs: "+s+ " s since!");
    			}
    		}
    		
    		if(buf!=null){
    			if(!FileUtil.writeStr2File(filename, buf)){
    				logger.warn("Cant update the file '"+filename+"'!");
    			}
    		}
    		
    		app.setIsUpTimeIsRunning(false);
    		
    		NDC.pop();
    	}
    }



	public Kv2KlApp(String[] args, KvConfig conf, boolean usingSwing){
		super(args, conf.conf, usingSwing);

		PropertiesHelper prop=getConf();
		String admin=prop.getProperty("admin");
		setDbIdleTime(300); //5 minutter
		setDbTimeToLive(3600); //1 time

		if(admin!=null)
			admin=admin.trim();

		//Run the timer as a deamon, ie terminate the timer
		//when the application is about to terminate.
		dbCleanupTimer=new Timer(true);
		dbCleanupTimer.schedule(new DbCleanup(this), 60000, 60000);
	}


    synchronized void setIsUpTimeIsRunning(boolean f){
    	isUpTaskIsRunning=f;
    }
    
    synchronized boolean getIsUpTimeIsRunning(){
    	return isUpTaskIsRunning;
    }
    
    synchronized public void setKvIsUp(boolean isUp){
    	kvLastContact=Instant.now();
    	kvIsUp=isUp;
    }
    
    synchronized public boolean getKvIsUp(){
    	return kvIsUp;
    }
    
    synchronized public void updateLastKvContact(){
    	kvIsUp=true;
    	kvLastContact=Instant.now();
    }
    
    synchronized public Instant getLastKvContact(){
    	return kvLastContact;
    }
    
    protected void onExit(){
    	if(isUpTimer!=null){
    		isUpTimer.cancel();
    	
    		while(getIsUpTimeIsRunning());
    	
    		String buf="Stopped: "+ Instant.now();
    	
//    		if(!FileUtil.writeStr2File(kvIsUpLogFile, buf)){
//    			logger.warn("Cant update the file '"+kvIsUpLogFile+"'!");
//    		}
    	}
    	removePidFile();
    	logger.info("Prorgram terminate!");
    }
}

