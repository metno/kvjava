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

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.IOException;

import no.met.kvalobs.kl.TypeRouterParser;
import no.met.kvclient.KvDataSubscribeInfo;
import no.met.kvclient.service.*;
import no.met.kvutil.*;
import no.met.kvalobs.kl.TypeRouter;


import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.setOut;

public class Kv2KlMain {
    static Logger logger = Logger.getLogger(Kv2KlMain.class);

    public static void use(int exitcode) {
        System.out.println("Usage: kv2kl OPTIONS");
        System.out.println("  OPTIONS");
        System.out.println("    -d|--disable-filter Disable the data filter.");
        System.out.println("    -e|--enable-filter Enable the data filter.");
        System.out.println("    -c|--conf configfile Use configfile.");
        System.out.println("    -p|--no-pid-file Do not create a pid file.");
        System.out.println("    -n|--no-db Do not save data to the database.");
        System.out.println("    -l|--load-hours hours Connect to kvalobs at startup and load hours times");
        System.err.println("       of data to the database. Default 0.");
        System.err.println("    -k|list-conf Print out the configuration and exit.");
        System.out.println("    -h|--help This help message.");
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

    static class SaveKvState extends TimerTask {
        KvState kvState;

        public SaveKvState(KvState kvState) {
            this.kvState = kvState;
        }

        public void run() {
            NDC.push("SaveKvState");
            kvState.saveState();
            NDC.pop();
        }
    }

    static GetOptDesc[] getOptDescription() {
        return new GetOptDesc[]{
                new GetOptDesc('c', "conf", true),
                new GetOptDesc('h', "help", false),
                new GetOptDesc('k', "list-conf", false),
                new GetOptDesc('d', "disable-filter", false),
                new GetOptDesc('e', "enable-filter", false),
                new GetOptDesc('p', "no-pid-file", false),
                new GetOptDesc('n', "no-db", false),
                new GetOptDesc('l', "load-hours", true)
        };

    }

    static TypeRouter getTypeRouter(Kv2KlApp app, KvConfig conf) {
        LinkedList<String> paths = new LinkedList<String>();
        paths.addLast(System.getProperty("user.dir"));
        paths.addLast(System.getProperty("user.home"));
        paths.addLast(conf.etcdir);
        String file = (conf.appName==null || conf.appName.isEmpty()?"kv2kl":conf.appName) + "_type_to_table.json";

        Path typeToTableFile = FileUtil.searchFile(file, paths);
        TypeRouter router = new TypeRouter();
        if (typeToTableFile != null) {
            try {
                TypeRouterParser parser = new TypeRouterParser();
                router = parser.parseConf(typeToTableFile.toString());
            } catch (Exception e) {
                logger.fatal("Failed to read (typeToTable) file '" + typeToTableFile + "'. " + e.getMessage());
                System.out.println("Failed to read (typeToTable) file '" + typeToTableFile + "'. " + e.getMessage());
                System.exit(1);
            }
        }
        router.setDefaultTable(app.getDataTableName(), true);
        router.setDefaultTextTable(app.getTextDataTableName(), true);
        router.setForeignTable(app.getForeignDataTableName(), true);
        router.setForeignTextTable(app.getForeignTextDataTableName(), true);

        router.setDefaultTextTable("T_TEXT_DATA", true);
        router.setDefaultTable("kv2klima", true);
        router.setEnableFilter(app.getEnableFilter(), true);
        System.out.println("TypeRouter routes\n" + router.toString());
        logger.info("TypeRouter routes\n" + router.toString());
        return router;
    }

    public static void main(String[] args) {
        //Set the default timezone to GMT.

/*
        TypeRouterParser parser = new TypeRouterParser("kv2klima");
        try {
            TypeRouter router=parser.parseConf("kv2kl-testdb_type_to_table.json");
            System.out.println("......completed\n"+router);
        } catch (Exception e) {
            System.out.println("IllException: " + e.getMessage());
        }
*/



        System.out.println("CWD: '" + System.getProperty("user.dir") + "'");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        ParamList param;
        Kv2KlApp app;
        WhichDataList whichData = new WhichDataList();
        KvDataSubscribeInfo dataSubscribeInfo;
        KlDataReceiver dataReceiver;
        KvHintListener hint;
        Timer saveStateTimer = new Timer(true);
        KvConfig conf = KvConfig.config("kv2kl");
        String appName=(conf.appName==null || conf.appName.isEmpty()?"kv2kl":conf.appName);
        KvState kvState = KvState.loadState(conf);

        System.out.println(conf);

        long pid = ProcUtil.getPid();

        System.out.println("pidfile: '" + conf.getPidPath() + "'");

        SubscribeId subscriberid;
        SubscribeId hintid;
        Instant now = Instant.now();
        GetOpt go = new GetOpt(getOptDescription());
//        String kvserver = null;
//        String kvname = null;
        int hoursBack = 0;
        boolean createPidFile = true;
        boolean saveDataToDb = true;
        boolean printConfigAndExit = false;

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
                    printConfigAndExit = true;
                    break;
                case 'd':
                    conf.conf.setProperty("filter.enable", "false");
                    break;
                case 'e':
                    conf.conf.setProperty("filter.enable", "true");
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
                        if (hoursBack < 0)
                            hoursBack *= -1;
                    } catch (NumberFormatException ex) {
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

        if (printConfigAndExit) {
            System.err.println("Config\n\n" + conf.toString());
            System.exit(0);
        }


        checkDirs(conf, true);


        System.out.println("Configfile (in): " + conf.configfile);

        FileUtil.writeStr2File(conf.logdir + "/" + appName + "_conf.properties", conf.toString());


        Properties logProperties = InitLogger.getLogProperties(conf);
        PropertiesHelper.saveToFile(logProperties, conf.logdir + "/" + appName + "_log.properties");

        PropertyConfigurator.configure(logProperties);


        go = null; //We dont need it anymore.

        app = new Kv2KlApp(args, conf, false);

        TypeRouter typeRoutes = getTypeRouter(app, conf);
        dataSubscribeInfo = new KvDataSubscribeInfo();
        dataReceiver = new KlDataReceiver(app, kvState, appName + ".dat", typeRoutes, saveDataToDb);
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

            //Save to the state file every 5 minute.
            //The statefile has some monitoring information and the kafka group var.
            saveStateTimer.schedule(new SaveKvState(kvState), 60000, 60000);

            app.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        app.removePidFile();
        kvState.saveState();
        logger.info("Prorgram terminate!");
    }
}


	
