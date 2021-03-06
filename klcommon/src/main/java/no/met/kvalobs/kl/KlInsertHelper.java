/*
  Kvalobs - Free Quality Control Software for Meteorological Observations

  $Id$

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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import no.met.kvclient.service.DataIdElement;
import no.met.kvutil.DateTimeUtil;
import no.met.kvutil.FileUtil;
import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.StringHolder;
import no.met.kvclient.service.ObsData;
import no.met.kvclient.service.ObsDataList;
import no.met.kvutil.LongHolder;
import no.met.kvutil.dbutil.IExec;
import org.apache.log4j.Logger;

public class KlInsertHelper {
    static Logger logger = Logger.getLogger(KlInsertHelper.class);
    static Logger filterlog = Logger.getLogger("filter");
    IDbStatus status;
    PrintWriter fout;
    DbConnectionMgr conMgr = null;
    String dataTableName = null;
    String textDataTableName = null;
    String foreignDataTableName = null;
    String foreignTextDataTableName = null;
    TypeRouter typeRouter=null;
    boolean enableFilter;

    class DummyStatus implements IDbStatus {

        public DummyStatus() {
        }

        @Override
        public void updateLastDbTime() {
        }

        @Override
        public void setDbError(String message) {
        }
    }

    public KlInsertHelper(DbConnectionMgr conMgr, IDbStatus status, String backupfile, boolean enableFilter) {
        if( status==null )
            this.status=new DummyStatus();
        else
            this.status=status;

        fout = null;
        this.conMgr = conMgr;
        this.enableFilter = enableFilter;
        this.typeRouter = new TypeRouter();
        typeRouter.setEnableFilter(enableFilter, false);

        if (backupfile != null) {
            try {
                fout = new PrintWriter(new FileWriter(backupfile, true), true);
                fout.println("Starter her:");
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public KlInsertHelper(DbConnectionMgr conMgr, IDbStatus status, String backupfile, TypeRouter typeRouter_) {
        if( status==null )
            this.status=new DummyStatus();
        else
            this.status=status;

        fout = null;
        this.conMgr = conMgr;
        this.typeRouter=typeRouter_;
        this.enableFilter = typeRouter.getEnableFilter();
        setDataTableName(typeRouter.getDefaultTable());
        setTextDataTableName(typeRouter.getDefaultTextTable());
        setForeignDataTable(typeRouter.getForeignTable());
        setForeignTextDataTable(typeRouter.getForeignTextTable());

        if (backupfile != null) {
            try {
                fout = new PrintWriter(new FileWriter(backupfile, true), true);
                fout.println("Starter her:");
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }


    public KlInsertHelper(DbConnectionMgr conMgr, String backupfile, TypeRouter typeRouter) {
        this(conMgr, null, backupfile,typeRouter);
    }


    public KlInsertHelper(DbConnectionMgr conMgr, String backupfile) {
        this(conMgr, backupfile, new TypeRouter());
    }

    public KlInsertHelper(DbConnectionMgr conMgr) {
        this(conMgr, null, new TypeRouter());
    }

    public DbConnection newDbConnection() {
        try {
            return conMgr.newDbConnection();
        } catch (SQLException e) {
            logger.warn("Cant create a new database connection: " +
                    e.getMessage());
            return null;
        }
    }


    public void releaseDbConnection(DbConnection con) {
        String msg;

        try {
            conMgr.releaseDbConnection(con);
            return;
        } catch (IllegalArgumentException e) {
            msg = e.getMessage();
        } catch (IllegalStateException e) {
            msg = e.getMessage();
        } catch (SQLException e) {
            msg = e.getMessage();
        }

        logger.warn("Cant release the database connection: " + msg);
    }

    public void setDataTableName(String dataTable) {
        dataTableName = dataTable;
    }

    public void setTextDataTableName(String textDataTable) {
        textDataTableName = textDataTable;
    }

    public void setForeignDataTable(String foreignData) {
        foreignDataTableName = foreignData;
    }

    public void setForeignTextDataTable(String foreignTextData) {
        foreignDataTableName = foreignTextData;
    }

    protected boolean usetypeid(long typeid, LinkedList<Long> typelist) {
        if (typelist == null || typelist.isEmpty())
            return true;

        ListIterator<Long> it = typelist.listIterator();

        while (it.hasNext()) {
            Long n = it.next();

            if (typeid == n || n==0) {
                return true;
            }
        }

        return false;
    }


    protected boolean doInsertData(DbConnection dbcon, KlDataHelper dh) throws NoData{
        IExec query = dh.createInsertQuery();

        if (query == null) {
            logger.info("SKIPPING: stationid: " + dh.getStationID() +
                    " typid: " + dh.getTypeID() );

            return true;
        }

        logger.debug(query);

        try {
            dbcon.exec(query);
            return true;
        } catch (SQLException SQLe) {
            String sqlState = SQLe.getSQLState();

            if (sqlState != null &&
                    sqlState.startsWith("23")) {
                logger.warn(Instant.now() + ": " + SQLe);
                query = dh.createUpdateQuery();
                return updateData(dbcon, query);
            } else {
                logger.error(Instant.now() + ": " + SQLe);
            }
        } catch (Exception e) {
            logger.error(Instant.now() + ": " + e);
        }

        if (query != null && fout != null)
            fout.println(query);

        return false;
    }

    protected boolean updateData(DbConnection dbcon, IExec updateQuery) {
        IExec query = updateQuery;

        if (query == null)
            return true;

        try {
            dbcon.exec(query);
            return true;
        } catch (SQLException SQLe) {
            logger.error(Instant.now() + ": " + SQLe + "\n" + query);
        } catch (Exception e) {
            logger.error(Instant.now() + ": " + e);
        }

        if (query != null && fout != null)
            fout.println(query);

        return false;
    }


    public boolean insertData(ObsDataList obsData, LinkedList<Long> typelist) {
        DbConnection dbconn = newDbConnection();
        Filter filter = new Filter(dbconn);
        boolean loggedFilter;
        boolean loggedNewObs;
        long typeid = 0;
        TypeRouter.RouterElement routerElement=null;
        StringHolder msg = new StringHolder();
        boolean filterRet;
        boolean ret = true;

        filter.setFilterEnabled(enableFilter);

        logger.debug("InsertData (Enter): " + Instant.now());

        if (dbconn == null) {
            logger.error("No Db connection!");
            return false;
        }

        try {
            if (obsData == null) {
                logger.warn("Opppsss: NO DATA!");
                return true;
            }

            //Debug
            /*for(DataIdElement e : obsData.keySet()) {
                if( e.stationID==12550 && e.typeID==502) {
                    String out="------- stationid: " + e.stationID + " typeid: " + e.typeID + " received: " + DateTimeUtil.nowToString() + "---\n";
                    out += obsData + "------------- END -----------------\n";
                    FileUtil.appendStr2File("level_insert_debug.txt", out);
                }
            }*/


            KlDataHelper dh = new KlDataHelper(dbconn.getDbdriver());

            Iterator<ObsData> itObsData = obsData.iterator();
            while (itObsData.hasNext()) {
                ObsData data = itObsData.next();
                msg.setValue(null);
                loggedFilter = false;
                loggedNewObs = false;

                //DataElem[] elem=obsData[i].dataList;
                dh.init(data.dataList, data.textDataList);
                LongHolder stationID = new LongHolder();

                while (dh.next()) {
                    if (typeid != dh.getTypeID()) {
                        typeid = dh.getTypeID();
                        routerElement = typeRouter.getTable(typeid);
                        dh.setDataTables(routerElement);
                        filter.setFilterEnabled(routerElement.enableFilter(true));
                        loggedNewObs = false;
                    }

                    if (!usetypeid(dh.getTypeID(), typelist)) {
                        continue;
                    }

                    if (!loggedNewObs) {
                        loggedNewObs = true;
                        logger.info("New obs: stationid: " + dh.getStationID() +
                                " typid: " + dh.getTypeID() +
                                " obstime: " + dh.getObstime());
                    }

                    stationID.setValue(dh.getStationID());

                    filterRet = filter.filter(stationID, dh.getTypeID(), dh.getParamID(),
                            dh.getLevel(), dh.getSensor(), dh.useLevelAndSensor(),
                            dh.getObstime(), msg);

                    //The stationid my have changed
                    dh.setStationID(stationID.getValue());

                    if (!loggedFilter && msg.getValue() != null) {
                        loggedFilter = true;
                        filterlog.info(dh.getStationID() +
                                " " +
                                dh.getTypeID() +
                                " " +
                                dh.getObstime() + ": " +
                                msg.getValue());
                    }

                    if (!filterRet)
                        continue;

                    if (!doInsertData(dbconn, dh)) {
                        logger.error("DBERROR: stationid: " + dh.getStationID() +
                                " typid: " + dh.getTypeID() +
                                " obstime: " + dh.getObstime());
                        ret = false;
                    }
                }
            }
            status.updateLastDbTime();
        } catch (Exception e) {
            status.setDbError(e.getMessage());
            logger.error(Instant.now() + ": " + e);
            e.printStackTrace();
        }

        releaseDbConnection(dbconn);

        logger.debug("DataReceiver (Return): " + Instant.now());
        return ret;
    }
}
