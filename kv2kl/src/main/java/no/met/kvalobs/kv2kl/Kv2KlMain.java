/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Kv2KlMain.java,v 1.1.2.10 2007/09/27 09:02:19 paule Exp $                                                       

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

import java.util.*;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import no.met.kvalobs.kl.KlApp;
import no.met.kvclient.KvDataSubscribeInfo;
import no.met.kvclient.service.ParamList;
import no.met.kvclient.service.StatusId;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.WhichData;
import no.met.kvclient.service.WhichDataList;
import no.met.kvutil.GetOpt;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Kv2KlMain {
	static Logger logger = Logger.getLogger(Kv2KlMain.class);
	
	public static void use(int exitcode){
		System.out.println("Usage: kv2kl [-s kvserver] [-h] [-c configFile] -d ");
		System.out.println("  OPTIONS");
		System.out.println("    -d disable the data filter.");
		System.out.println("    -s kvserver Override the kvserver in the config file.");
		System.out.println("    -c configfile Use configfile.");
		System.out.println("    -h This help message.");
		System.exit(exitcode);
    }

	
    public static void main(String[] args)
    {
    	//Set the default timezone to GMT.
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

    	String kvpath=KlApp.getKvpath();
    	String configfile="kv2kl.conf";
    	ParamList param;
    	Kv2KlApp app;
    	WhichDataList whichData=new WhichDataList();
    	KvDataSubscribeInfo dataSubscribeInfo;
    	KlDataReceiver dataReceiver;
    	KvHintListener hint;
    	
    	if(kvpath==null){
    		System.out.println("FATAL: Propertie KVALOBS must be set!");
    		System.exit(1);
    	}
    	    	
    	SubscribeId subscriberid;
    	SubscribeId hintid;
    	Instant now=Instant.now();
    	GetOpt go = new GetOpt("c:hs:d");
    	String kvserver=null;
    	String kvname=null;
    	boolean enableFilter=true;

    	char c;
         
    	while ((c = go.getopt(args)) != GetOpt.DONE) {
             switch(c) {
             case 'h':
                 use(0);
                 break;
             case 'c':
                 configfile= go.optarg();
                 break;
             case 's':
            	 kvserver=go.optarg();
            	 break;
             case 'd':
            	 enableFilter = false;
            	 break;
             default:
                 System.err.println("Unknown option character " + c);
             	 logger.fatal("Unknown option character " + c);
                 use(1);
             }
         }

    	System.out.println("Configfile (in): " + configfile );
    	 
    	 int i=configfile.lastIndexOf(".conf");
         	
    	 if(i<=0 || i != configfile.length()-5 || configfile.charAt(i-1) == '/') {
    		 System.out.println("FATAL: the configuration file <" + configfile + "> is not named like 'name.conf'");
    		 System.exit(1);
    	 }
        
    	 kvname=configfile.substring(0, i);
    	 i=kvname.lastIndexOf('/');
    	 
    	 if(i>-1){
    		 if(i<kvname.length()){
    			 kvname = kvname.substring(i+1);
    		 }else{
    			 System.out.println("FATAL: the name of the configuration file must on the form 'name.conf' <" + configfile +">");
        		 System.exit(1);
    		 }
    	 }
    	 
    	 String logfile=kvname+"_log.conf";
    	 
    	//    	Konfigurer loggesystemet, log4j.
     	System.out.println("log4j conf: "+kvpath+"/etc/"+logfile);
     	PropertyConfigurator.configure(InitLogger.getLogProperties(configfile, FileSystems.getDefault().getPath(".")));
    	
     	go=null; //We dont need it anymore.

     	app=new Kv2KlApp(args, configfile, kvserver, false);
     	dataSubscribeInfo=new KvDataSubscribeInfo();
     	dataReceiver=new KlDataReceiver( app, kvname+".dat", enableFilter ); 
     	hint=new KvHintListener(app);
         
    	logger.info("Starting: " +now);
	
   		now=now.minus(1, ChronoUnit.HOURS);
	    
   		whichData.add(	new WhichData(0, StatusId.All,
   				                   Instant.now().minus(Duration.ofHours(3)),
   				                   Instant.now()));	
	    
//   		if(!app.getKvData(whichData, dataReceiver)){
//   			logger.fatal("getKvData: failed. Exiting !!!!");
//   			try {
//				app.shutdown();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//   			return;
//   		}
//	
	  
    	logger.info("getKvData: a background thread is started!");

    	subscriberid=app.subscribeData(dataSubscribeInfo,
    										     dataReceiver);
    	
	
    	if(subscriberid==null){
    		logger.fatal("Cant subscribe on <KvData>! Exiting !!!!");
    		try {
				app.shutdown();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		System.exit(1);
    	}
    	
    	hintid=app.subscribeKvHint(hint);
    	
    	if(hintid==null){
    		logger.warn("Cant subscribe on <KvHint>! Exiting !!!!");
    	}
    	
    	app.setKvIsUp(true);
    	app.createPidFile();
    	
    	logger.info("Subscribe on <KvData>, subscriberid <" 
    			           + subscriberid+"> Ctrl+c for aa avslutte!");
    	try {
			app.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	

    	logger.info("Prorgram terminate!");
    }
}


	
