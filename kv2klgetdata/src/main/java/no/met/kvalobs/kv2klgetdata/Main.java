/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Main.java,v 1.1.2.10 2007/09/27 09:02:19 paule Exp $                                                       

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
package no.met.kvalobs.kv2klgetdata;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import no.met.kvalobs.kl.KlApp;
import no.met.kvalobs.kl.KlInsertHelper;
import no.met.kvalobs.kl.Range;
import no.met.kvalobs.kl.TypeRouter;
import no.met.kvalobs.kl.TypeRouterParser;
import no.met.kvclient.KvBaseConfig;
import no.met.kvclient.service.*;
import no.met.kvutil.*;
import no.met.kvutil.MiGMTTime;
import no.met.kvutil.dbutil.DbConnectionMgr;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {
    //static java.util.logging.Logger logger = Logger.getLogger(Main.class);
    static org.apache.log4j.Logger logger = Logger.getLogger(Main.class);
    static void use(int no) {
        System.out.println("\n\n  kv2klgetdata -- -f fromdate [-s kvserver] [-c conffile] [-t todate] \n");
        System.out.println("         [-i typeidlist]  [-dd] [-nf] [-e,--termin obstime] stnr1 stnr2 .. stnrN");
        System.out.println("\n   kv2klgetdata henter data fra kvalobs og fyller opp i klimadatabasen.");
        System.out.println("   Dataene filtreres med filterdata fra T_KV2KLIMA_FILTER,   t_kv2klima_param_filter");
        System.out.println("   og t_kv2klima_typeid_param_filter .");
        System.out.println("");
        System.out.println("   -nf Deaktiver filteret, dvs. at filteret ikke skal brukes!");
        System.out.println("   -dd Data skal ikke lagres til databasen!");
        System.out.println("   -c conffile Angi en alternativ konfigurationfil");
        System.out.println("               Default konfigurasjonsfil er: $KVALOBS/etc/kv2kl.conf");
        System.out.println("               Dvs. samme file som kv2kl bruker!");
        System.out.println("   -e termin Hent data kun for denne observasjonsterminen.");
        System.out.println("   --termin termin Hent data kun for denne observasjonsterminen.");
        System.out.println("   -f fromdate Hent data fra og med denne observasjonsterminen.");
        System.out.println("   -t todate hent data til og med denne observasjonsterminen.");
        System.out.println("             Hvis -t ikke angis hentes data frem til nåtid.");
        System.out.println("             For å hente data fra bare en termin set todate til det samme som fromdate eller bruk -e termin .");
        System.out.println("   -i typeid En liste av typeid som skal hentes.");
        System.out.println("             Hvis -i ikke angis lagres data for alle typeid.");
        System.out.println("   -v verbose.");
        System.out.println("   stnr1 stnr2 .. stnrN er stasjonene man ønsker å hente data");
        System.out.println("   for. Hvis man ikke angir noen stasjoner hentes data for alle");
        System.out.println("   stasjonene. Stasjonsnummer kan også være et interval. Da angis");
        System.out.println("   det med stnrLav-stnrHøy. Det må ikke være mellomrom mellom - og");
        System.out.println("   stasjonsnumrene. Stasjonsnummrene må være større enn 0.");
        System.out.println("\n  Datoformatet til fromdate, todate og termin er:");
        System.out.println("  'YYYY-MM-DD hh:mm:ss'.");
        System.out.println("  Fnuttene rundt må være med!");
        System.out.println("\n  Foramatet til typeid er: ");
        System.out.println("  'typeid1 typeid2 .. typeidN'");
        System.out.println("  Fnuttene må være med hvis det angis mer enn en typeid");
        System.out.println("\n\n ");

        System.exit(no);

    }

    static OffsetDateTime datecheck(String date, OffsetDateTime ifNull) {
        if (date == null)
            return ifNull;
        try {
            return DateTimeUtil.parse(date);
        }
        catch(DateTimeParseException ex) {
            System.err.println("Invalid time format: '"+date+"'. " + ex.getMessage());
        }

        return null;
    }

    static LinkedList<Long> createtTypelist(String typelist) {
        LinkedList<Long> ll = new LinkedList<Long>();
        if (typelist == null) {
            ll.add(new Long(0));
            return ll;
        }

        String[] res = typelist.split(" |,");

        for (int i = 0; i < res.length; i++) {
            try {
                ll.add(new Long(Long.parseLong(res[i])));
            } catch (NumberFormatException e) {
                //Just ignore exceptions
                e.printStackTrace();
            }
        }

        return ll;
    }


    static TypeRouter getTypeRouter(App app, KvBaseConfig conf) {
        LinkedList<String> paths = new LinkedList<String>();
        paths.addLast(System.getProperty("user.dir"));
        paths.addLast(System.getProperty("user.home"));
        paths.addLast(conf.etcdir);
        
        
        String file = conf.configTypeRouter;
        TypeRouter router = new TypeRouter();

        if( file.isEmpty() ) {
            System.err.println("No type router file is defined. Using default.");
            //logger.warning("No type router file is defined. Using default.");
            router.setDefaultTable(app.getDataTableName(), false);
            router.setDefaultTextTable(app.getTextDataTableName(), false);
            return router;
        }

        Path typeToTableFile = FileUtil.searchFile(file, paths);
        System.err.println("Using type route file: '" + conf.configTypeRouter+"'.");
        logger.info("Using type route file: '" + conf.configTypeRouter+"'.");
            
        if (typeToTableFile != null) {
            try {
                System.err.println("Parsing type route file: '" + conf.configTypeRouter+"'.");
                TypeRouterParser parser = new TypeRouterParser();
                router = parser.parseConf(typeToTableFile.toString());
            } catch (Exception e) {
              //  logger.fatal("Failed to read (typeToTable) file '" + typeToTableFile + "'. " + e.getMessage());
                System.out.println("Failed to read (typeToTable) file '" + typeToTableFile + "'. " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.err.println("Type route file: '" + file+"' do not exict.");
        }

        router.setDefaultTable(app.getDataTableName(), true);
        router.setDefaultTextTable(app.getTextDataTableName(), true);
        router.setForeignTable(app.getForeignDataTableName(), true);
        router.setForeignTextTable(app.getForeignTextDataTableName(), true);

        router.setDefaultTextTable("T_TEXT_DATA", true);
        router.setDefaultTable("kv2klima", true);
        router.setEnableFilter(app.getEnableFilter(), true);
        System.err.println("TypeRouter routes\n" + router.toString());
        //logger.info("TypeRouter routes\n" + router.toString());
        return router;
    }


    public static void main(String[] args) {
        //Set the default timezone to GMT.
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String conffile = null;
        String kvpath = KlApp.getKvpath();

        if (kvpath == null) {
            System.out.println("FATAL: Propertie KVALOBS must be set!");
            System.exit(1);
        }

        Param[] param;
        KlInsertHelper insertstmt;
        App app;

        GetOptDesc myopt[] = {
                new GetOptDesc('h', null, false),
                new GetOptDesc('t', null, true),
                new GetOptDesc('f', null, true),
                new GetOptDesc('i', null, true),
                new GetOptDesc('c', null, true),
                new GetOptDesc('n', "nf", false),
                new GetOptDesc('d', "dd", false),
                new GetOptDesc('e', "termin", true),
                new GetOptDesc('v', null, false)
        };

        GetOpt opt = new GetOpt(myopt);
        char c;
        boolean disableFilter = false;
        boolean saveData = true;
        boolean verbose = false;
        String sToDate = null;
        String sFromDate = null;
        OffsetDateTime dtToDate;
        OffsetDateTime dtFromDate;
        String typeid = null;
        List<String> list = null;
        LinkedList<Long> typelist = null;
        WhichDataList whichData = new WhichDataList();
        int nObservations = 0;
        int nObsMsg = 0;
        int nIterations = 0;

        while ((c = opt.getopt(args)) != GetOpt.DONE) {
            switch (c) {
                case 'h':
                    use(0);
                    break;
                case 't':
                    sToDate = opt.optarg();
                    break;
                case 'f':
                    sFromDate = opt.optarg();
                    break;
                case 'e':
                    sFromDate = opt.optarg();
                    sToDate = sFromDate;
                    break;
                case 'i':
                    typeid = opt.optarg();
                    break;
                case 'c':
                    conffile = opt.optarg();
                    break;
                case 'n':
                    disableFilter = true;
                    break;
                case 'd':
                    saveData = false;
                    break;
                case 'v':
                    verbose = true;
                    break;
                default:
                    logger.error("Unknown option character " + c);
                    use(1);
            }
        }

        KvBaseConfig myConf = KvBaseConfig.config("kv2kl", conffile);
        PropertyConfigurator.configure(InitLogger.getLogProperties(myConf));


        list = (List<String>) opt.getFilenameList();

        app = new App(args, myConf.conf);
      

        System.err.println(myConf);

        dtToDate = datecheck(sToDate, OffsetDateTime.now());
        dtFromDate = datecheck(sFromDate, OffsetDateTime.MAX);

        if( dtToDate == null ){
            logger.error("ERROR: Ugyldig todate '"+sToDate+"'.\n");
            use(1);
        }

        //To implement inclusive todate.
        //Microsecond is the time resolution to timestamp in the database.
        OffsetDateTime dtToDate_ = DateTimeUtil.plusMicosecond(dtToDate, 1);

        if (dtFromDate == null) {
            logger.error("ERROR: Ugyldig fromdate '"+sFromDate+"'.\n");
            use(1);
        }

        if (dtFromDate.isEqual(OffsetDateTime.MAX)) {
            logger.error("ERROR: fromdate må angis!\n");
            use(1);
        }

        Range stationList[] = Range.ranges(list);

        if (stationList == null) {
            System.out.println("ERROR: stasjons listen er ugyldig.");
            logger.error("Stasjons listen er ugyldig.");
            use(1);
        }

        typelist = createtTypelist(typeid);

        for (Range station : stationList) {
            if (!station.ok())
                continue;

            long stationid2 = station.getFirst();

            if (station.isInterval())
                stationid2 = station.getLast();

            for (Long tid : typelist) {
                WhichData wd = new WhichData(station.getFirst(), tid, StatusId.All,
                        dtFromDate, dtToDate_);
                wd.stationid2 = stationid2;
                whichData.add(wd);
            }
        }

        //TypeRouter router = new TypeRouter();
        TypeRouter router = getTypeRouter(app, myConf);
        //router.setDefaultTable(app.getDataTableName(),false);
        //router.setDefaultTextTable(app.getTextDataTableName(), false);

        if( disableFilter)
            router.setEnableFilter(false, false);
        else
            router.setEnableFilter(true, false);

        //System.err.println("TypeRouter definition: \n"+router.toString());

        insertstmt = new KlInsertHelper(app.getKlConnectionMgr(), null, router);
        MiGMTTime start = new MiGMTTime();

        logger.info("Options: ");
        logger.info("fromDate: " + DateTimeUtil.toString(dtFromDate, DateTimeUtil.FMT_PARSE));
        logger.info("toDate: " + DateTimeUtil.toString(dtToDate, DateTimeUtil.FMT_PARSE));
        logger.info("Stations: " + Range.toString(stationList));
        logger.info("typeid:   " + typelist);
        logger.info("kvserver: " + myConf.conf.getProperty("kv.dbconnect", ""));
        logger.info("disable filter: " + disableFilter);
        logger.info("verbose: " + verbose);

        for (WhichData wd : whichData) {
            logger.info("WhichData: " + wd);
        }



        DbConnectionMgr kvDbMgr = KlApp.getKvConnectionMgr();

        if (kvDbMgr == null)
            logger.info("Connected to kvalobs database: (UNKNOWN)");
        else
            logger.info("Connected to kvalobs database: " + kvDbMgr.connectInfo());

        try {
            DataIterator it = app.getData(whichData);

            if (it == null) {
                logger.fatal("Cant connect to the kvalobs database.");
                System.exit(1);
            }

            ObsDataList datalist = it.next();

            while (datalist != null) {
                nIterations++;
                nObsMsg += datalist.size();

                for (ObsData od : datalist)
                    nObservations += od.size();

                if (saveData)
                    insertstmt.insertData(datalist, typelist);

                if (verbose && datalist != null)
                    System.err.println(datalist.toString());

                datalist = it.next();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            app.shutdown();
        } catch (InterruptedException ex) {
        }

        MiGMTTime stop = new MiGMTTime();

        long days, hours, min, secs;

        secs = start.secsTo(stop);
        logger.info("secs: " + secs);
        days = secs / 86400;
        hours = (secs - days * 86400) / 3600;
        min = (secs - days * 86400 - hours * 3600) / 60;
        secs = secs - days * 86400 - hours * 3600 - min * 60;

        //logger.info("kvserver: " + app.getKvServer());
        logger.info("Program started at:     " + start);
        logger.info("Prorgram terminated at: " + stop);
        logger.info("Elapsed time:           " +
                (days > 0 ? days + " day(s) " : "") + hours + "h " + min + "m " + secs + "s");
        logger.info("# iterations:   " + nIterations);
        logger.info("# obsMsg:       " + nObsMsg);
        logger.info("# observations: " + nObservations);
    }
}
