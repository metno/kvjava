package no.met.kvalobs.kl;

import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import no.met.kvutil.dbutil.IExec;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

public class OracleKlSql extends KlTblNames implements IKlSql {
    static Logger logger=Logger.getLogger(DefaultKlSql.class);


    public OracleKlSql(){
        super();
    }

    public OracleKlSql(String dataTableName, String textDataTableName ){
        super(dataTableName, textDataTableName);
    }

    public IExec createDataUpdateQuery(DataElem elem ){
        logger.debug("Update data in a Oracle database ("+getDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final private DataElem d=elem;

            @Override
            public String name() {
                return "DataElemUpdate";
            }

            @Override
            public String create() {
                return "UPDATE " + getDataTableName() +
                        " SET " +
                        "  original=?," +
                        "  kvstamp=?," +
                        "  useinfo=?," +
                        "  corrected=?," +
                        "  controlinfo=?," +
                        "  cfailed=?" +
                        "WHERE " +
                        "  stnr=? AND " +
                        "  dato=? AND " +
                        "  paramid=? AND " +
                        "  typeid=? AND " +
                        "  xlevel=? AND " +
                        "  sensor=?";
            }

            @Override
            public int exec(PreparedStatement s) throws SQLException {
                s.setObject(1,d.original, Types.NUMERIC, 1);
                s.setObject(2,new java.sql.Timestamp(d.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setString(3,d.useinfo);
                s.setObject(4,d.corrected, Types.NUMERIC, 1);
                s.setString(5,d.controlinfo);
                s.setString(6,d.cfailed);
                s.setObject(7,d.stationID, Types.NUMERIC);
                s.setObject(8,new java.sql.Timestamp(d.obstime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(9,d.paramID, Types.NUMERIC);
                s.setObject(10,d.typeID, Types.NUMERIC);
                s.setObject(11,d.level, Types.NUMERIC);
                s.setObject(12,d.sensor, Types.NUMERIC);

                return s.executeUpdate();
            }
        };
    }

    public IExec createDataInsertQuery( DataElem elem ){
        logger.debug("Insert into a SQL92 database("+getDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final private DataElem d=elem;

            @Override
            public String name() {
                return "DataElemInsert";
            }

            @Override
            public String create() {
                return "INSERT INTO " + getDataTableName() +
                        "(stnr,dato,original,kvstamp,paramid,typeid,xlevel,sensor,useinfo,corrected,controlinfo,cfailed) values (" +
                        "?,?,?,?,?,?,?,?,?,?,?,?)";
            }

            @Override
            public int exec(PreparedStatement s) throws SQLException {
                s.clearParameters();
                s.setObject(1, d.stationID, Types.NUMERIC);
                s.setObject(2,new java.sql.Timestamp(d.obstime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(3,d.original, Types.NUMERIC, 1);
                s.setObject(4,new java.sql.Timestamp(d.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(5,d.paramID, Types.NUMERIC);
                s.setObject(6,d.typeID, Types.NUMERIC);
                s.setObject(7,d.level, Types.NUMERIC);
                s.setObject(8,d.sensor, Types.NUMERIC);
                s.setString(9,d.useinfo);
                s.setObject(10,d.corrected, Types.NUMERIC, 1);
                s.setString(11,d.controlinfo);
                s.setString(12,d.cfailed);
                return s.executeUpdate();
            }
        };
    }

    public IExec createTextDataUpdateQuery( TextDataElem elem ){
        logger.debug("Update textData in a SQL92 database ("+getTextDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final TextDataElem e=elem;
            @Override
            public String name() {
                return "TextDataElemUpdate";
            }

            @Override
            public String create() {
                return
                        "UPDATE " + getTextDataTableName() +
                                " SET " +
                                "  original=?," +
                                "  tbtime=?" +
                                "WHERE " +
                                "  stationid=? AND " +
                                "  obstime=? AND " +
                                "  paramid=? AND " +
                                "  typeid=?";
            }

            @Override
            public int exec(PreparedStatement s) throws SQLException {
                s.setString(1,e.original);
                s.setObject(2,new java.sql.Timestamp(e.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(3,e.stationID, Types.NUMERIC);
                s.setObject(4,new java.sql.Timestamp(e.obstime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(5,e.paramID, Types.NUMERIC);
                s.setObject(6,e.typeID, Types.NUMERIC);
                return s.executeUpdate();
            }
        };
    }


    public IExec createTextDataInsertQuery( TextDataElem elem ) {
        logger.debug("Insert textData into a SQL92 database ("+getTextDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);

        return new IExec() {
            private final TextDataElem e=elem;
            @Override
            public String name() {
                return "TextDataElemInsert";
            }

            @Override
            public String create() {
                return "INSERT INTO "+getTextDataTableName() +
                        "(stationid,obstime,original,paramid,tbtime,typeid) "+
                        "values (?,?,?,?,?,?)";

            }

            @Override
            public int exec(PreparedStatement s) throws SQLException {
                s.setObject(1, e.stationID, Types.INTEGER);
                s.setObject(2,new java.sql.Timestamp(e.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setString(3, e.original);
                s.setObject(4,e.paramID, Types.NUMERIC);
                s.setObject(5,new java.sql.Timestamp(e.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(6,e.typeID, Types.NUMERIC);
                return s.executeUpdate();

            }
        };
    }

    public String dateString( Instant time ) {
        return "'" + time +"'";
    }


}
