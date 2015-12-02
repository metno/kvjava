/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: ParamFilterTest.java,v 1.1.2.5 2007/09/27 09:02:20 paule Exp $                                                       

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

import static no.met.DbTestUtil.*;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;

import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvalobs.kl.ParamFilter;
import no.met.kvalobs.kl.ParamFilter.ParamElem;
import no.met.kvclient.service.DataElem;
import no.met.kvutil.DateTimeUtil;
import org.junit.*;
import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;
import org.apache.log4j.PropertyConfigurator;

public class ParamFilterTest {
	static final String paramlogconf="src/test/java/no/met/ParamFilterTest_log.conf";
	static final String insertParamSql="src/test/java/no/met/insert_into_typeid_param_filter.sql";
    static final String dbdriver="org.hsqldb.jdbcDriver";
    static final String dbconnect="jdbc:hsqldb:file:target/hsqldb/paramfiltertest/db";
    //static final String dbconnect="jdbc:hsqldb:mem:testdb";
    static final String dbpasswd="";
    static final String dbuser="sa"; //Default user in a HSQLDB.
    static final String createKv2KlFilter="../etc/sql/create_kv2kl_filter_tables.sql";
	static DbConnectionMgr mgr=null;
    
    @BeforeClass
    public static void setUpAndLoadTheDb(){
    	
    	PropertyConfigurator.configure(paramlogconf);
    	
        deleteDir("target/hsqldb/paramfiltertest");
        
        try {
        	if( mgr != null ){
        		System.err.println(" @@@@@@@@@@@@@@ A mngr exist");
        	}
        	
            mgr=new DbConnectionMgr(dbdriver, dbuser, dbpasswd, dbconnect, 10);
            
            if(!runSqlFromFile(mgr, createKv2KlFilter)){
            	mgr.closeDbDriver();
            	fail("Cant execute SQL from file: "+createKv2KlFilter);
            	return;
            }
            
            //Load the database with test data from a file.
        	assertTrue("Cant load: "+insertParamSql, 
        			runSqlFromFile(mgr, insertParamSql));
        	
        	//listKlimaTypeidParamFilter(mgr);
        	//listKlimaParamFilter(mgr);
        }catch(Exception ex) {
            if(mgr!=null)
                try{
                    mgr.closeDbDriver();
                }
                catch(Exception e){
                }
            
            fail("Cant load the database with testdata!");
        }
    	
    }
    
    public void setUp(){
    }
  
    public void tearDown(){
    }
    
    
        
    static boolean listKlimaTypeidParamFilter(DbConnectionMgr mgr){
    	return listDbTable(mgr,"T_KV2KLIMA_TYPEID_PARAM_FILTER");
    }

    
    static boolean listKlimaParamFilter(DbConnectionMgr mgr){
    	return listDbTable(mgr,"T_KV2KLIMA_PARAM_FILTER");
    }

    
    
    
    DataElem getDataElem(long sid, long tid, String obstime, 
        		            int paramid, int sensor, int level){
        
        return new DataElem(
                            sid, DateTimeUtil.parse(obstime).toInstant(), 1.2, paramid,
                            DateTimeUtil.parse("2006-03-09 18:00:00").toInstant(), 
                            tid, sensor, level, 1.2, 
                            "0123456789012345", "0123456789012345",
                            ""); 
    }
    
    boolean doFilter(ParamFilter pf, DataElem de){
		Timestamp obstime=Timestamp.from(de.obstime);
		return pf.filter(de, obstime);
    }
    
    @Test
    public void loadFromDb(){
    	DbConnection con=null;
    	
    	try{
    		con=mgr.newDbConnection();
    	
    		assertNotNull("Cant create an dbconnection!", con);
    	
    		LinkedList<ParamFilter.ParamElem> tl302=new LinkedList<>();
    		tl302.add(new ParamFilter.ParamElem(302,112,0,0));
    		Collections.sort(tl302);
    	
    		LinkedList<ParamFilter.ParamElem> tl330=new LinkedList<>();
    		tl330.add(new ParamFilter.ParamElem(330,104,1,0));
    		tl330.add(new ParamFilter.ParamElem(330,106,0,0));
    		tl330.add(new ParamFilter.ParamElem(330,125,0,0));
    		Collections.sort(tl330);
    	
    		ParamFilter pf=new ParamFilter(18700, con);
    	    	
    		LinkedList<ParamElem> paramList=pf.loadFromDb(302);
    	
    		System.out.println(pf.types);
    		assertEquals(paramList.toString(), tl302.toString());
    		assertEquals(pf.types.size(), 1);
    	
    		paramList=pf.loadFromDb(330);
    		System.out.println(pf.types);
    		assertEquals(paramList.toString(), tl330.toString());
    		assertEquals(pf.types.size(), 2);
    	
    		paramList=pf.loadFromDb(312);
    		System.out.println(pf.types);
    		assertTrue(paramList.size()==0);
    		assertEquals(pf.types.size(), 3);
    		
    		LinkedList<ParamElem> list=pf.types.get(new Long(312));
    		
    		assertNotNull(list);
    		assertTrue(list.size()==0);

    		paramList=pf.loadFromDb(308);
    		System.out.println(pf.types);
    		assertTrue(paramList.size()==2);
    		assertTrue(pf.types.size()==4);
    		
    		mgr.releaseDbConnection(con);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		fail("Unexpected exception!");
    	}
    }
    
        
    @Test
    public void filter(){
        DbConnection con=null;
        boolean         ret;
        
        assertNotNull(mgr);
        
        try{
           con=mgr.newDbConnection();
        
           assertNotNull("Cant create an dbconnection!", con);
        
           ParamFilter pf=new ParamFilter(18700, con);
    
           ret=doFilter(pf, getDataElem(18700,302,"2006-06-14 12:00:00", 108, 0,0));
   		   //System.out.println("filter 1\n"+pf.types+"\n\n");
           assertTrue(ret);
       
           ret=doFilter(pf, getDataElem(18700,302,"2006-06-14 12:00:00", 112,0,0));
   		   //System.out.println("filter 2\n"+pf.types+"\n\n");
           assertFalse(ret);
           
           ret=doFilter(pf, getDataElem(18700,330,"2006-06-14 12:00:00", 104,1,0));
   		   //System.out.println("filter 3\n"+pf.types+"\n\n");
           assertFalse(ret);
           
           ret=doFilter(pf, getDataElem(18700,312,"2006-06-14 12:00:00", 104,1,0));
   		   //System.out.println("filter 4\n"+pf.types+"\n\n");
           assertTrue(ret);
           
           ret=doFilter(pf, getDataElem(18700,308, "2006-06-14 12:00:00", 22,0,0));
   		   //System.out.println("filter 5\n"+pf.types+"\n\n");
           assertTrue(ret);
           
           ret=doFilter(pf, getDataElem(18700,308, "2006-06-14 12:00:00", 109, 0,0));
   		   //System.out.println("filter 6\n"+pf.types+"\n\n");
           assertFalse(ret);
           
           ret=doFilter(pf, getDataElem(18700, 3,"2005-03-09 18:00:00", 104, 0, 0));
   		   //System.out.println("filter 7\n"+pf.types+"\n\n");
           assertFalse(ret);
     	
       	   ret=doFilter(pf, getDataElem(18700, 3,"2006-03-09 18:00:00", 104, 0, 0));
   		   //System.out.println("filter 8\n"+pf.types+"\n\n");
       	   assertFalse(ret);

       	   
           ret=doFilter(pf, getDataElem(18700, 3,"2005-03-09 18:00:00", 110, 0, 0));
  		   //System.out.println("filter 9\n"+pf.types+"\n\n");
           assertTrue(ret);
           
           
           pf=new ParamFilter(87110, con);
           ret=doFilter(pf, getDataElem(87110, 1, "2006-09-08 06:00:00", 211, 0, 0));
 		   //System.out.println("filter 10\n"+pf.types+"\n\n");
           assertFalse(ret);
           
        }
        catch(Exception e){
        	fail("Unexpected exception!");
        }
    }
    
    
    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(ParamFilterTest.class); 
    }


    
}
