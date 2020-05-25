/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: klfilterTest.java,v 1.1.2.5 2007/09/27 09:02:20 paule Exp $                                                       

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

import java.util.TimeZone;

import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvclient.service.DataElem;
import no.met.kvutil.DateTimeUtil;
import no.met.kvutil.StringHolder;

import static no.met.DbTestUtil.*;

import org.junit.*;

import static org.junit.Assert.*;

import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.PropertyConfigurator;


public class klfilterTest {
    static final String logconf = "klcommon/src/test/java/no/met/klfilterTest_log.conf";
    static final String insertParamFilterSql = "klcommon/src/test/java/no/met/insert_into_typeid_param_filter.sql";
    static final String dbdriver = "org.hsqldb.jdbcDriver";
    static final String dbconnect = "jdbc:hsqldb:file:target/hsqldb/klfiltertest/db";
    static final String dbpasswd = "";
    static final String dbuser = "sa"; //Default user in a HSQLDB.
    static final String createFilterTables = "etc/sql/create_kv2kl_filter_tables.sql";
    DbConnectionMgr mgr = null;

    @BeforeClass
    public static void setUpAndLoadTheDb() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        PropertyConfigurator.configure(logconf);
    }

    @Before
    public void setUp() {
        mgr = fillWithData();
    }

    @After
    public void tearDown() {
        if (mgr != null) {
            try {
                mgr.closeDbDriver();
            } catch (Exception ex) {
                System.out.println("Cant close the DbManager!" + ex.getMessage());
            }
        }

        deleteDir("target/hsqldb/klfiltertest");
        mgr = null;
    }

    boolean listTestTables(DbConnectionMgr mgr) {
        boolean ok = true;

        if (!listDbTable(mgr, "t_kv2klima_filter"))
            ok = false;

        if (!listDbTable(mgr, "T_KV2KLIMA_TYPEID_PARAM_FILTER"))
            ok = false;

        if (!listDbTable(mgr, "T_KV2KLIMA_PARAM_FILTER"))
            ok = false;

        return ok;
    }

    DbConnectionMgr fillWithData() {
        if (mgr != null) {
            mgr.closeDbDriver();
        }
        String cols=" (STNR,STATUS,FDATO,TDATO,TYPEID,NYTT_STNR) ";
        String insert = "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18700,'T',NULL,NULL,308,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18700,'D',NULL,NULL,3,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18700,'D',NULL,NULL,6,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18700,'D',NULL,NULL,10,18701);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18500,'D',NULL,'2006-01-01 03:00:00',10,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18500,'D','2006-02-01 03:00:00', '2006-02-20 12:00:00',10,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18500,'D','2006-02-20 14:00:00',NULL,10,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18600,'D',NULL,'2006-01-01 03:00:00',10,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "18800,'D','2006-01-01 03:00:00', NULL, 10,NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "4460,'D','2007-10-31 00:00:00','2007-11-01 07:00:00', 330, NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "4460,'D','2007-11-01 08:00:00', NULL, 342, NULL);" +
                "insert into t_kv2klima_filter"+cols+"VALUES(" +
                "88000,'D','2008-01-01 00:00:00', NULL, 42, NULL);";

        deleteDir("target/hsqldb/klfiltertest");

        try {
            mgr = new DbConnectionMgr(dbdriver, dbuser, dbpasswd, dbconnect, 1);
        } catch (Exception ex) {
            fail("Cant create connection: " + ex.getMessage());
        }

        boolean ok = true;


        if (ok && !runSqlFromFile(mgr, createFilterTables)) {
            System.out.println("Cant execute SQL from file: " + createFilterTables);
            ok = false;
        }


        if (ok && !runSqlFromString(mgr, insert)) {
            System.out.println("Cant insert data into the table: T_KV2KLIMA_FILTER");
            ok = false;
        }

        if (ok && !runSqlFromFile(mgr, insertParamFilterSql))
            ok = false;


        if (!ok) {
            try {
                mgr.closeDbDriver();
            } catch (Exception ex) {
                System.out.println("Cant close the DbManager!" + ex.getMessage());
            }

            fail("Cant load the database with testdata!");
            return null;
        }

        return mgr;
    }

    DataElem
    fillWithStationData(long sid, long tid, String obstime) {

        return new DataElem(
                sid, DateTimeUtil.parse(obstime).toInstant(), 1.2, 211,
                DateTimeUtil.parse("2006-03-09 18:00:00").toInstant(),
                tid, 0, 0, 1.2,
                "0123456789012345", "0123456789012345",
                "");
    }


    DataElem
    fillWithStationData(long sid, long tid, int pid, String obstime) {

        return new DataElem(
                sid, DateTimeUtil.parse(obstime).toInstant(), 1.2, pid,
                DateTimeUtil.parse("2006-03-09 18:00:00").toInstant(),
                tid, 0, 0, 1.2,
                "0123456789012345", "0123456789012345",
                "");
    }

    DataElem getDataElem(long sid, long tid, String obstime,
                         int paramid, int sensor, int level) {

        return new DataElem(
                sid, DateTimeUtil.parse(obstime).toInstant(), 1.2, paramid,
                DateTimeUtil.parse("2006-03-09 18:00:00").toInstant(),
                tid, sensor, level, 1.2,
                "0123456789012345", "0123456789012345",
                "");
    }

    @Test
    public void missingTableEntryForType() {
        StringHolder msg = new StringHolder();
        DbConnection con = null;
        boolean ret;

        assertNotNull(mgr);
        //assertTrue(listTestTables(mgr));

        try {
            con = mgr.newDbConnection();
        } catch (Exception e) {
            fail("Unexpected exception!");
        }

        assertNotNull(con);

        Filter filter = new Filter(con);

        ret = filter.filter(fillWithStationData(18700, 6, "2006-03-09 18:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18700, 6, "2006-03-09 18:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18700, 1, "2006-03-09 18:00:00"), msg);

        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18700, 1, "2006-03-09 18:00:00"), msg);

        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18700, 6, "2006-03-09 18:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18700, 6, "2006-03-09 18:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2005-12-09 18:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-01-01 03:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-01-01 04:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-01 02:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-01 03:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-18 02:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-20 12:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-20 13:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-20 14:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2006-02-20 11:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18500, 10, "2008-02-20 11:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18600, 10, "2003-02-20 11:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18600, 10, "2006-01-01 03:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18600, 10, "2006-01-01 04:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18600, 10, "2007-01-01 04:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18800, 10, "2006-01-01 02:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(18800, 10, "2006-01-01 03:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(18800, 10, "2007-01-01 04:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(4460, 342, "2007-11-01 07:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(4460, 342, "2007-11-01 08:00:00"), msg);
        assertTrue(ret);

        ret = filter.filter(fillWithStationData(4460, 342, "2007-10-31 18:00:00"), msg);
        assertFalse(ret);

        ret = filter.filter(fillWithStationData(4460, 330, "2007-10-31 18:00:00"), msg);
        assertTrue(ret);


        try {
            mgr.releaseDbConnection(con);
        } catch (Exception e) {
            fail("Unexpected exception!");
        }

    }

    @Test
    public void paramFilter() {
        StringHolder msg = new StringHolder();
        boolean ret;

        assertNotNull(mgr);
        // assertTrue(listTestTables(mgr));

        try( DbConnection con=mgr.newDbConnection()) {
            Filter filter = new Filter(con);

            ret = filter.filter(getDataElem(18700, 3, "2006-03-09 18:00:00", 88, 0, 0),
                    msg);
            assertTrue(ret);


            ret = filter.filter(getDataElem(18700, 3, "2001-03-09 18:00:00", 104, 0, 0),
                    msg);
            assertFalse(ret);


            ret = filter.filter(getDataElem(18700, 3, "2005-01-02 23:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-03 00:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-03 01:00:00", 104, 0, 0),
                    msg);

            assertTrue(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-13 23:00:00", 104, 0, 0),
                    msg);

            assertTrue(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-31 23:00:00", 104, 0, 0),
                    msg);

            assertTrue(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-02-01 00:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-02 23:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2006-01-01 00:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2006-01-01 01:00:00", 104, 0, 0),
                    msg);

            assertTrue(ret);

            ret = filter.filter(getDataElem(18700, 3, "2006-01-01 02:00:00", 104, 0, 0),
                    msg);

            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2006-03-09 18:00:00", 104, 0, 0),
                    msg);
            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-03 00:00:00", 110, 0, 0),
                    msg);
            assertFalse(ret);

            //test nagativ typeid. Should be accepted.
            ret = filter.filter(getDataElem(18700, -3, "2005-01-03 00:00:00", 110, 0, 0),
                    msg);

            System.out.println(" ret : " + ret);
            assertTrue(ret);

            //Test the upper limit of [fdato,tdato>, the tdato part.

            ret = filter.filter(getDataElem(18700, 3, "2005-01-03 00:00:00", 104, 0, 0),
                    msg);
            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-02 23:59:59", 104, 0, 0),
                    msg);
            assertFalse(ret);


            //Test the lower limit of [fdato,tdato>. The fdato part.

            ret = filter.filter(getDataElem(18700, 3, "2005-01-03 00:00:00", 111, 0, 0),
                    msg);
            assertFalse(ret);

            ret = filter.filter(getDataElem(18700, 3, "2005-01-02 23:59:59", 111, 0, 0),
                    msg);
            assertTrue(ret);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception!");
        }
    }

    @Test
    public void testStation() {
        StringHolder msg = new StringHolder();
        boolean ret;

        assertNotNull(mgr);

        try (DbConnection con = mgr.newDbConnection()) {
            assertNotNull(con);
            Filter filter = new Filter(con);

            ret = filter.filter(fillWithStationData(18700, 308,
                    "2006-03-09 18:00:00"),
                    msg);
            assertFalse(ret);
            Kv2KlimaFilter dbKv2KlimaFilterElem = Filter.cache.getKv2KlimaFilter(18700, 308);
            assertNotNull(dbKv2KlimaFilterElem);
            assertNotNull(dbKv2KlimaFilterElem.getStatus());
            assertTrue(dbKv2KlimaFilterElem.getStatus().length() != 0);
            assertTrue(dbKv2KlimaFilterElem.getStatus().charAt(0) == 'T');
            ret = filter.filter(fillWithStationData(18700, 308,
                    "2006-03-09 18:00:00"),
                    msg);
            assertFalse(ret);


            ret = filter.filter(fillWithStationData(18700, 6,
                    "2006-03-09 18:00:00"),
                    msg);
            assertTrue(ret);

            ret = filter.filter(fillWithStationData(18700, 6,
                    "2006-03-09 18:00:00"),
                    msg);
        } catch (Exception e) {
            fail("Unexpected exception!");
        }
    }

    @Test
    public void negativTypeid() {
        StringHolder msg = new StringHolder();
        boolean ret;

        assertNotNull(mgr);
        //assertTrue(listTestTables(mgr));

        try (DbConnection con = mgr.newDbConnection()) {

            assertNotNull("Test1", con);

            Filter filter = new Filter(con);

            ret = filter.filter(fillWithStationData(88000, -42, "2007-12-31 23:00:00"), msg);
            assertFalse("Test2", ret);

            ret = filter.filter(fillWithStationData(88000, -42, "2008-01-01 00:00:00"), msg);
            assertTrue("Test3", ret);

            ret = filter.filter(fillWithStationData(88000, -42, "2008-01-01 19:00:00"), msg);
            assertFalse("Test4", ret);

            ret = filter.filter(fillWithStationData(88000, 42, "2008-01-01 19:00:00"), msg);
            assertTrue("Test5", ret);

            ret = filter.filter(fillWithStationData(88000, 42, 210, "2008-01-01 19:00:00"), msg);
            assertTrue("Test6", ret);

            ret = filter.filter(fillWithStationData(88000, -42, 210, "2008-01-01 19:00:00"), msg);
            assertFalse("Test7", ret);

            ret = filter.filter(fillWithStationData(88000, -42, 209, "2008-01-01 19:00:00"), msg);
            assertTrue("Test8", ret);

            ret = filter.filter(fillWithStationData(88000, 42, 209, "2008-01-01 19:00:00"), msg);
            assertFalse("Test9", ret);
        } catch (Exception e) {
            fail("Unexpected exception!");
        }
    }

    @Test
    public void nyttStNr() {
        StringHolder msg = new StringHolder();
        DbConnection con = null;
        boolean ret;

        //    System.out.println("Test: nyttStNr");
        assertNotNull(mgr);
        //assertTrue(listTestTables(mgr));

        try {
            con = mgr.newDbConnection();
        } catch (Exception e) {
            fail("Unexpected exception!");
        }

        assertNotNull(con);

        Filter filter = new Filter(con);
        DataElem de = fillWithStationData(18700, 10,
                "2006-03-09 18:00:00");
        ret = filter.filter(de, msg);
        assertTrue(ret);
        assertTrue(de.stationID == 18701);

        filter = new Filter(con);
        de = fillWithStationData(18700, -10,
                "2006-03-09 18:00:00");
        ret = filter.filter(de, msg);
        assertTrue(ret);
        assertTrue(de.stationID == 18701);

        try {
            mgr.releaseDbConnection(con);
        } catch (Exception e) {
            fail("Unexpected exception!");
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(klfilterTest.class);
    }

}
