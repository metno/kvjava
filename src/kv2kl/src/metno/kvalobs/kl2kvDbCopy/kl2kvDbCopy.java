/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: SqlHelper.java,v 1.1.2.2 2007/09/27 09:02:19 paule Exp $                                                       

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
package metno.kvalobs.kl2kvDbCopy;


import java.util.*;
import java.text.*;
import java.sql.*;
import java.io.*;
import metno.util.MiGMTTime;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class kl2kvDbCopy {

	static Logger logger = Logger.getLogger(kl2kvDbCopy.class);
	static Properties loadProperties( String confFile ) {
		FileInputStream in=null;
    	Properties conf=new Properties();

    	try{
    		in=new FileInputStream(confFile);
    	}
    	catch(FileNotFoundException ex){
    	//	logger.error("ERROR: Cant open conf file <"+confFile+">!");
    	}
    	catch(SecurityException ex){
    		//logger.error("NOACCESS: We have not permission to read to open the file <"+confFile+"!");
    	}
    
    	

    	if(in!=null){
    		try{
    			conf.load(in);
    			return conf;
    		}
    		catch(Exception ex){
    			//logger.fatal("ERROR: Cant load configuration from file <"
    			//			       +confFile+">!");
		
    			try{
    				in.close();
    			}
    			catch(Exception ex_){
    			}
		
    			System.exit(1);
    		}

    		try{
    			in.close();
    		}
    		catch(Exception ex){
    		}
    	}

		return null;
	}
	
    public static void main(String[] args)
    {
    	//TimeZone old = TimeZone.getDefault();
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    	String kvpath=System.getProperties().getProperty("KVALOBS");
    	System.out.println("log4j conf: "+kvpath+"/etc/kv2kldbcopy_log.conf");
    	PropertyConfigurator.configure(kvpath+"/etc/kl2kvdbcopy_log.conf");
    	
    	String confFile=kvpath+"/etc/kl2kvdbcopy.conf";
    	//String confFile=System.getProperties().getProperty("user.dir") + "/kl2kvdbcopy.conf";
    	System.out.println("Configuration file: " + confFile );
    	Properties propConf = loadProperties(confFile);
    	
    	DbConnectionFactory conFactory=new DbConnectionFactory( propConf );
    
    	KlDbConnection kldb = new KlDbConnection( conFactory.createConnection("klima") );
    	KlDbConnection kvdb = new KlDbConnection( conFactory.createConnection("kvalobs") );

    	if( kldb == null )
    		System.out.println("kldb==null GRRRRRRRRRRRR");
    	
    	if( kldb == null )
    		System.out.println("kvdb==null GRRRRRRRRRRRR");
    	
    	IProcessQuery klCopyQuery=new KlCopyQuery( kvdb );
    	KlCopyQueryWorker klCopyWorker=new KlCopyQueryWorker(kldb, new MiGMTTime(2012, 7, 19, 6, 0, 0), new MiGMTTime(2012, 7, 19, 7, 0, 0));
    	
    	klCopyWorker.run(klCopyQuery);
    }
	
    
//    public SqlHelper(){
//    	    	try {
//    		Class.forName(dbdriver);
//    	} catch(Exception e) {
//    		logger.fatal("FATAL: cant load the database driver <"+
//    						   dbdriver+">!");
//    		System.exit(1);
//    	}
//	
//    	logger.info("INFO: Database driver <"+dbdriver+"> loaded!");
//    }
//    
//    public DbConnection newDbConnection(){
//    	Connection conn;
//    	Statement statement;
//    	boolean error=false;
//
//    	synchronized(dbMutex){
//
//    		if(dbconn!=null){
//    			if(dbconn.inuse){
//    				logger.error("newDbConnection (ERROR): Mismatch " +
//    								   "newDbConnection/releaseDbConnection");
//    				return null;
//    			}
//
//    			dbconn.lastAccess=new MiGMTTime();
//    			dbconn.inuse=true;
//    			return dbconn;
//    		}
//
//    		logger.info("newDbConnection: Creating a new connection to"+
//    					" the database!");
//	
//    		try{
//    			conn = DriverManager.getConnection(dbconnect, dblogin, dbpasswd);  
//    		}
//    		catch(SQLException ex){
//    			logger.error(ex);
//    			logger.error("DBERROR: cant create a database"+
//    					 	 "connection <"+dbconnect+">!");
//    			return null;
//    		}
//    		catch(Exception ex){
//    			logger.error(ex);
//    			logger.error("DBERROR: (UNEXPECTED??) cant create the data base connection <"+
//    							   dbconnect+">!");
//    			return null;
//    		}
//	    
//    		if(conn==null){
//    			logger.error("DBERROR: (UNEXPECTED???) connection==null!");
//    			return null;
//    		}
//	    
//    		try{
//    			statement=conn.createStatement();
//    		}
//    		catch(SQLException ex){
//    			logger.error(ex);
//    			logger.error("DBERROR: cant create the data base statement!");
//		
//    			try{
//    				conn.close();
//    			}
//    			catch(Exception ex_){
//    			}
//    			return null;
//    		}
//    		catch(Exception ex){
//    			logger.error(ex);
//    			logger.error("DBERROR: (UNEXPECTED??) cant create the data base statement!");
//		
//    			try{
//    				conn.close();
//    			}
//    			catch(Exception ex_){
//    			}
//    			return null;
//    		}
//	    
//    		dbconn=new DbConnection(conn, statement, dbdriver);
//    		dbconn.inuse=true;
//    		dbconn.lastAccess=new MiGMTTime();
//    		return dbconn;
//    	}
//    }
// 
//    private void realReleaseDbConnection(DbConnection con){
//
//    	logger.debug("realReleaseDbConnection: called!");
//	
//    	try{
//    		con.statement.close();
//    	}
//    	catch(Exception ex){
//    	}
//
//    	try{
//    		con.conn.close();
//    	}
//    	catch(Exception ex){
//    	}
//    }
//
//    public void  releaseDbConnection(DbConnection con){
//
//    	synchronized(dbMutex){
//    		if(con==dbconn){
//    			if(!dbconn.inuse){
//    				logger.error("releaseDbConnection (ERROR): Mismatch "
//    						    +"newDbConnection/releaseDbConnection");
//    				return;
//    			}
//		 
//    			dbconn.inuse=false;
//    			dbconn.lastAccess=new MiGMTTime();
//    		}else{
//    			logger.error("releaseDbConnection (ERROR): The " +
//    						 "connection is NOT created by " +
//				   			  "newDbConnetion!");
//    		}
//    	}
//    }
}