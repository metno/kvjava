package no.met.kvalobs.kl;

import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import no.met.kvutil.dbutil.IExec;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

public class DefaultKlSql extends KlTblNames implements IKlSql {
    static Logger logger=Logger.getLogger(DefaultKlSql.class);


    public DefaultKlSql(){
        super();
    }

    public DefaultKlSql(String dataTableName, String textDataTableName ){
        super(dataTableName, textDataTableName);
    }

    public IExec createDataUpdateQuery(DataElem elem ){
        logger.debug("Update data: SQL92 database ("+getDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final private DataElem d=elem;
            final private String tableName=getDataTableName();

            @Override
            public String name() {
                return "DataElemUpdate_"+tableName;
            }

            @Override
            public String create() {
                return "UPDATE " + tableName +
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
                s.setObject(1,d.original, Types.NUMERIC, 3);
                s.setObject(2,new java.sql.Timestamp(d.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setString(3,d.useinfo);
                s.setObject(4,d.corrected, Types.NUMERIC, 3);
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

            @Override
            public String toString() {
                return "UPDATE " + tableName +
                        " SET " +
                        "  original=" +d.original+ "," +
                        "  kvstamp="+ d.getQuotedTbTime()+"," +
                        "  useinfo='"+d.useinfo+"'" +
                        "  corrected="+d.corrected+"," +
                        "  controlinfo='"+d.controlinfo+"'," +
                        "  cfailed='"+d.cfailed+"'," +
                        "WHERE " +
                        "  stnr="+d.stationID+" AND " +
                        "  dato="+d.getQuotedObsTime() +" AND " +
                        "  paramid="+d.paramID +" AND " +
                        "  typeid="+d.typeID+" AND " +
                        "  xlevel="+d.level+" AND " +
                        "  sensor="+d.sensor;
            }

        };
    }

    public IExec createDataInsertQuery( DataElem elem ){
        logger.debug("Insert data: SQL92 database("+getDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final private DataElem d=elem;
            final private String tableName = getDataTableName();

            @Override
            public String name() {
                return "DataElemInsert_"+tableName;
            }

            @Override
            public String create() {
                //System.out.println("createDataInsertQuery: IExec: table: " + tableName);
                return "INSERT INTO " + tableName +
                        "(stnr,dato,original,kvstamp,paramid,typeid,xlevel,sensor,useinfo,corrected,controlinfo,cfailed) values (" +
                        "?,?,?,?,?,?,?,?,?,?,?,?)";
            }

            @Override
            public int exec(PreparedStatement s) throws SQLException {
                s.clearParameters();
                s.setObject(1, d.stationID, Types.NUMERIC);
                s.setObject(2,new java.sql.Timestamp(d.obstime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(3,d.original, Types.NUMERIC, 3);
                s.setObject(4,new java.sql.Timestamp(d.tbtime.toEpochMilli()), Types.TIMESTAMP);
                s.setObject(5,d.paramID, Types.NUMERIC);
                s.setObject(6,d.typeID, Types.NUMERIC);
                s.setObject(7,d.level, Types.NUMERIC);
                s.setObject(8,d.sensor, Types.NUMERIC);
                s.setString(9,d.useinfo);
                s.setObject(10,d.corrected, Types.NUMERIC, 3);
                s.setString(11,d.controlinfo);
                s.setString(12,d.cfailed);
                return s.executeUpdate();
            }

            @Override
            public String toString() {
                return "INSERT INTO " + tableName +
                        "(stnr,dato,original,kvstamp,paramid,typeid,xlevel,sensor,useinfo," +
                        "corrected,controlinfo,cfailed) values (" +
                        d.stationID+","+d.getQuotedObsTime()+"," + d.original+"," +
                        d.getQuotedTbTime()+","+ d.paramID+","+d.typeID+"," +d.level+"," +
                        d.sensor+",'"+ d.useinfo+"',"+d.corrected+",'"+d.controlinfo+"','"+d.cfailed+"')";

            }

        };
    }

    public IExec createTextDataUpdateQuery( TextDataElem elem ){
        logger.debug("Update textData: SQL92 database ("+getTextDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
        return new IExec() {
            final private TextDataElem e=elem;
            final private String tableName=getTextDataTableName();

            @Override
            public String name() {
                return "TextDataElemUpdate_"+tableName;
            }

            @Override
            public String create() {
                return
                        "UPDATE " + tableName +
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

            @Override
            public String toString() {
                return "UPDATE " + tableName +
                        " SET " +
                        "  original='" +e.original+ "'," +
                        "  tbtime="+ e.getQuotedTbTime()+" " +
                        "WHERE " +
                        "  stationid="+e.stationID+" AND " +
                        "  obstime="+e.getQuotedObsTime() +" AND " +
                        "  paramid="+e.paramID +" AND " +
                        "  typeid="+e.typeID;
            }
        };
    }


    public IExec createTextDataInsertQuery( TextDataElem elem ) {
        logger.debug("Insert textData: SQL92 database ("+getTextDataTableName()+") sid: "+elem.stationID+" tid: " +
                elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);

        return new IExec() {
            final private  TextDataElem e=elem;
            final private String tableName=getTextDataTableName();

            @Override
            public String name() {
                return "TextDataElemInsert_"+tableName;
            }

            @Override
            public String create() {
                return "INSERT INTO "+tableName +
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

            @Override
            public String toString() {
                return "INSERT INTO "+tableName +
                        "(stationid,obstime,original,paramid,tbtime,typeid) "+
                        "values ("+e.stationID+","+ e.getObsTime()+",'"+e.original+","
                        +e.paramID+","+e.getQuotedTbTime()+","+e.typeID+")";
            }

        };
    }

    public String dateString( Instant time ) {
        return "'" + time +"'";
    }

}
