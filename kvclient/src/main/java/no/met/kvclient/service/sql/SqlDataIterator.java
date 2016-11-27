package no.met.kvclient.service.sql;

import no.met.kvclient.service.*;
import no.met.kvutil.Tuple2;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.dbutil.IQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Iterator;

/**
 * Created by borgem on 13.11.16.
 */
public class SqlDataIterator implements DataIterator {
    DbConnection[] con;
    WhichDataList whichData;
    Iterator<WhichData> wit;
    Tuple2<ResultSet, ResultSet> rs;
    int chunkSize;
    Tuple2<Boolean, DataElem> data;
    Tuple2<Boolean, TextDataElem> textData;

    SqlDataIterator(
            DbConnection[] con,
            WhichDataList whichData,
            int chunkSize) {
        this.con = con;
        this.whichData = whichData;
        this.chunkSize = chunkSize <= 0 ? 1 : chunkSize;
        wit = whichData.iterator();
        data = Tuple2.of(Boolean.FALSE, null);
        textData = Tuple2.of(Boolean.FALSE, null);
        rs = new Tuple2<ResultSet, ResultSet>(null, null);
    }

    DbConnection closeConnection(DbConnection con) {
        try {
            if (con != null)
                con.close();
        } catch (Exception ex) {
            System.err.println("SqlDataIterator.closeConnection (ignored): Exception: " + ex.getMessage());
        }
        return null;
    }

    void closeConnection(DbConnection[] con) {
        for (int i = 0; i < con.length; ++i)
            con[i] = closeConnection(con[i]);
    }


    IQuery createWhichDataQuery(WhichData whichData) {
        return createWhichQuery(false, whichData);
    }

    IQuery createWhichTextDataQuery(WhichData whichData) {
        return createWhichQuery(true, whichData);
    }

    IQuery createWhichQuery(Boolean textData, WhichData whichData) {

        return new IQuery() {
            WhichData wd = whichData;
            String tbl = textData ? "text_data" : "data";

            @Override
            public ResultSet exec(PreparedStatement s) throws SQLException {
                int i = 1;

                if( wd.stationid != wd.stationid2) {
                    s.setInt(i++, (int) wd.stationid);
                    s.setInt(i++, (int) wd.stationid2);
                } else if (wd.stationid != 0) {
                    //       System.err.println(" sid: i: " + i);
                    s.setInt(i++, (int) wd.stationid);
                }

                if (wd.typeid_ != 0) {
                    //     System.err.println(" tid: i: " + i);
                    s.setInt(i++, (int) wd.typeid_);
                }

                if (wd.paramid != 0) {
                    //   System.err.println(" pid: i: " + i);
                    s.setInt(i++, (int) wd.paramid);
                }

                //System.err.println(" fromTime: i: " + i);

                s.setTimestamp(i++, Timestamp.from(wd.fromTime));

                if (wd.fromTime.compareTo(wd.toTime) != 0) {
                    //  System.err.println(" toTime: " + i);
                    s.setTimestamp(i++, Timestamp.from(wd.toTime));
                }
                return s.executeQuery();
            }

            @Override
            public String name() {
                String n = "kvWhich_" + tbl;
                if (wd.stationid == 0)
                    n += "_s0";
                if (wd.stationid2 != wd.stationid)
                    n += "_s1";
                if (wd.typeid_ == 0)
                    n += "_t0";
                if (wd.paramid == 0)
                    n += "_p0";
                if (wd.fromTime.compareTo(wd.toTime) == 0)
                    n += "_o1";
                //System.err.println("name '"+n+"'");
                return n;
            }

            @Override
            public String create() {
                String q = "SELECT * FROM " + tbl + " WHERE ";
                String and = "";

                if( wd.stationid != wd.stationid2) {
                    q += " stationid >= ? AND stationid <= ? ";
                    and = "AND";
                } else if (wd.stationid != 0) {
                    q += " stationid=? ";
                    and = "AND";
                }

                if (wd.typeid_ != 0) {
                    q += and + " typeid=? ";
                    and = "AND";
                }

                if (wd.paramid != 0) {
                    q += and + " paramid=? ";
                    and = " AND ";
                }

                if (wd.fromTime.compareTo(wd.toTime) == 0)
                    q += and + " obstime = ? ORDER BY obstime, stationid,typeid;";
                else
                    q += and + " obstime >= ? AND obstime < ? ORDER BY obstime, stationid,typeid;";
                //System.err.println(q);
                return q;
            }
        };
    }

    private Instant getMinObstime(Tuple2<Boolean, DataElem> d, Tuple2<Boolean, TextDataElem> td) {
        if (!d._1 && !td._1)
            return null;
        else if (!d._1)
            return td._2.obstime;
        else if (!td._1)
            return d._2.obstime;
        else if (d._2.obstime.isBefore(td._2.obstime))
            return d._2.obstime;
        else
            return td._2.obstime;
    }


    void closeResultSet(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception ignored) {
        }

    }

    void closeResultSet(Tuple2<ResultSet, ResultSet> rs) {
        if (rs == null) {
            System.err.println("INTERNAL ERROR (Should not happend): SqlDataIterator.closeResultSet: rs==null");
        }
        closeResultSet(rs._1);
        closeResultSet(rs._2);
        rs._1 = null;
        rs._2 = null;
    }


    Tuple2<ResultSet, ResultSet> queryData() throws Exception {
        WhichData wd;
        if (!wit.hasNext())
            return null;

        wd = wit.next();
        closeResultSet(rs);

        try {
            rs._1 = con[0].execQuery(createWhichDataQuery(wd));
            rs._2 = con[1].execQuery(createWhichTextDataQuery(wd));
            data = getData(rs._1);
            textData = getTextData(rs._2);
        } catch (final Exception ex) {
            ex.printStackTrace();
            closeResultSet(rs);
            throw ex;
        }

        return rs;
    }


    private Tuple2<Boolean, DataElem> getData(ResultSet rd) throws SQLException {
        if (!rd.next())
            return Tuple2.of(Boolean.FALSE, null);
        else
            return Tuple2.of(Boolean.TRUE, new DataElem(rd));
    }

    private Tuple2<Boolean, TextDataElem> getTextData(ResultSet rd) throws SQLException {
        if (!rd.next())
            return Tuple2.of(Boolean.FALSE, null);
        else
            return Tuple2.of(Boolean.TRUE, new TextDataElem(rd));
    }

    private ObsDataList mergeResult(ResultSet rd, ResultSet rtd) throws SQLException {
        ObsDataList dl = new ObsDataList();

        Instant currentObstime = getMinObstime(data, textData);

        while (currentObstime != null) {
            if (!data._1 && !textData._1) {
                currentObstime = null;
            }
            if (data._1 && data._2.obstime.compareTo(currentObstime) == 0) {
                dl.addData(data._2);
                data = getData(rd);
            } else if (textData._1 && textData._2.obstime.compareTo(currentObstime) == 0) {
                dl.addTextData(textData._2);
                textData = getTextData(rtd);
            } else if (dl.size() >= chunkSize) {
                return dl;
            } else {
                currentObstime = getMinObstime(data, textData);
            }
        }
        return dl;
    }

    @Override
    public void destroy() {
        closeConnection(con);
    }

    @Override
    public ObsDataList next() throws Exception {
        ObsDataList dl;

        if (con[0] == null || con[1] == null) {
            System.err.println("SqlDataIterator::next: Internal error, one or both connections is null.");
            destroy();
            return null;
        }

        try {
            do {
                if (!data._1 && !textData._1)
                    rs = queryData();

                if (rs == null) {
                    destroy();
                    return null; // No more data to return
                }

                dl = getChunk(rs);
            } while (dl.isEmpty());
            return dl;
        } catch (final Exception ex) {
            System.err.println("Exception: SqlDataIterator.next(): " + ex.getMessage());
            closeResultSet(rs);
            destroy();
            throw ex;
        }
    }

    private ObsDataList getChunk(Tuple2<ResultSet, ResultSet> rs) throws Exception {
        return mergeResult(rs._1, rs._2);
    }
}
