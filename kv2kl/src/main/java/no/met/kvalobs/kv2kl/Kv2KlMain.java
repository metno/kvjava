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
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import no.met.kvclient.KvDataSubscribeInfo;
import no.met.kvclient.service.ParamList;
import no.met.kvclient.service.StatusId;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.WhichData;
import no.met.kvclient.service.WhichDataList;
import no.met.kvutil.GetOpt;
import no.met.kvutil.ProcUtil;
import no.met.kvutil.FileUtil;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import static java.lang.System.exit;
import static java.lang.System.setProperty;

public class Kv2KlMain {
    static Logger logger = Logger.getLogger(Kv2KlMain.class);

    public static void use(int exitcode) {
        System.out.println("Usage: kv2kl [-h] [-c configFile] -d [-p directory]");
        System.out.println("  OPTIONS");
        System.out.println("    -d disable the data filter.");
        System.out.println("    -c configfile Use configfile.");
        System.out.println("    -p do not create a pid file.");
        System.out.println("    -n do not save data to the database.");
        System.out.println("    -l hours Connect to kvalobs at startup and load hours times");
        System.err.println("       of data to the database. Default 0.");
        System.err.println("    -k Print out the configuration and exit.");
        System.out.println("    -h This help message.");
        exit(exitcode);
    }

    static public void checkDirs(KvConfig conf, boolean createIfNotExist) {
        try {
            FileUtil.checkDir(conf.rundir, true);
            FileUtil.checkDir(conf.logdir, true);
        } catch (IOException ex) {
            System.err.println("checkDirs: " + ex.getMessage());
            exit(1);
        }
    }

    public static void main(String[] args) {
        //Set the default timezone to GMT.
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        ParamList param;
        Kv2KlApp app;
        WhichDataList whichData = new WhichDataList();
        KvDataSubscribeInfo dataSubscribeInfo;
        KlDataReceiver dataReceiver;
        KvHintListener hint;
        KvConfig conf = KvConfig.config("kv2kl");
        KvState kvState=KvState.loadState(conf);

        System.out.println(conf);

        long pid = ProcUtil.getPid();

        System.out.println("pidfile: '" + conf.getPidPath() + "'");

        SubscribeId subscriberid;
        SubscribeId hintid;
        Instant now = Instant.now();
        GetOpt go = new GetOpt("c:hkdpnl:");
        String kvserver = null;
        String kvname = null;
        int hoursBack = 0;
        boolean enableFilter = true;
        boolean createPidFile = true;
        boolean saveDataToDb = true;
        boolean printConfigAndExit=false;

        char c;

        while ((c = go.getopt(args)) != GetOpt.DONE) {
            switch (c) {
                case 'h':
                    use(0);
                    break;
                case 'c':
                    conf.configfile = go.optarg();
                    break;
                case 'k':
                    printConfigAndExit=true;
                    break;
                case 'd':
                    enableFilter = false;
                    break;
                case 'p':
                    createPidFile = false;
                    break;
                case 'n':
                    saveDataToDb = false;
                    break;
                case 'l':
                    try {
                        hoursBack = Integer.parseInt(go.optarg());
                        if( hoursBack < 0)
                            hoursBack*=-1;
                    }
                    catch(NumberFormatException ex ) {
                        System.err.println("The argument to -l must be a number.");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unknown option character " + c);
                    logger.fatal("Unknown option character " + c);
                    use(1);
            }
        }

        if( printConfigAndExit  ) {
            System.err.println("Config\n\n" + conf.toString());
            System.exit(0);
        }

        checkDirs(conf, true);

        System.out.println("Configfile (in): " + conf.configfile);

//    	 int i=configfile.lastIndexOf(".conf");
//         	
//    	 if(i<=0 || i != configfile.length()-5 || configfile.charAt(i-1) == '/') {
//    		 System.out.println("FATAL: the configuration file <" + configfile + "> is not named like 'name.conf'");
//    		 System.exit(1);
//    	 }
//        
//    	 kvname=configfile.substring(0, i);
//    	 i=kvname.lastIndexOf('/');
//    	 
//    	 if(i>-1){
//    		 if(i<kvname.length()){
//    			 kvname = kvname.substring(i+1);
//    		 }else{
//    			 System.out.println("FATAL: the name of the configuration file must be on the form 'name.conf' <" + configfile +">");
//        		 System.exit(1);
//    		 }
//    	 }
//    	 
//    	 String logfile=kvname+"_log.conf";
//    	 
//    	//    	Konfigurer loggesystemet, log4j.
//     	System.out.println("log4j conf: "+kvpath+"/etc/"+logfile);
        PropertyConfigurator.configure(InitLogger.getLogProperties(conf));

        go = null; //We dont need it anymore.

        app = new Kv2KlApp(args, conf, kvserver, false);
        dataSubscribeInfo = new KvDataSubscribeInfo();
        dataReceiver = new KlDataReceiver(app, kvname + ".dat", enableFilter, saveDataToDb);
        hint = new KvHintListener(app);

        logger.info("Starting: " + now);

        now = now.minus(1, ChronoUnit.HOURS);

        whichData.add(new WhichData(0, StatusId.All,
                Instant.now().minus(Duration.ofHours(hoursBack)),
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

        subscriberid = app.subscribeData(dataSubscribeInfo,
                dataReceiver);


        if (subscriberid == null) {
            System.err.println("FATAL: Cant subscribe on <KvData>! Exiting !!!!");
            logger.fatal("Cant subscribe on <KvData>! Exiting !!!!");
            try {
                app.shutdown();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            exit(1);
        }

        hintid = app.subscribeKvHint(hint);

        if (hintid == null) {
            logger.warn("Cant subscribe on <KvHint>! Exiting !!!!");
        }

        app.setKvIsUp(true);

        if (createPidFile)
            app.createPidFile(conf.getPidPath().toString());

        logger.info("Subscribe on <KvData>, subscriberid <"
                + subscriberid + "> Ctrl+c for aa avslutte!");
        try {
            System.out.println("Subscribe on <KvData>, subscriberid <"
                    + subscriberid + "> Ctrl+c for aa avslutte!");
            app.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        app.removePidFile();
        kvState.saveState();
        logger.info("Prorgram terminate!");
    }
}


	
