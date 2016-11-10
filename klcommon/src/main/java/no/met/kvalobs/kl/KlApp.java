/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: KlApp.java,v 1.1.2.5 2007/09/27 09:02:19 paule Exp $                                                       

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

import no.met.kvutil.ProcUtil;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvclient.kafka.KafkaApp;
import no.met.kvclient.service.SendDataToKv;
import no.met.kvclient.service.SendDataToKv.Result.EResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class KlApp extends KafkaApp  implements SendDataToKv 
{
	
	static Logger logger=Logger.getLogger(KlApp.class);

    DbConnectionMgr conMgr=null;
    //String           kvserver;
    File             pidFile=null;
    String dataTableName=null;
    String textDataTableName=null;
    String foreignDataTableName=null;
    String foreignTextDataTableName=null;
    boolean enableConMgr=true;
    static String           kvpath=null;
	static String           appName=null;

    static public PropertiesHelper getConfile(String conf){
    	if(conf==null){
    		logger.fatal("INTERNAL: No configuration file is given! (null)!");
    		System.exit(1);
    	}
    	
    	if(conf.length()==0){
    		logger.fatal("INTERNAL: No configuration file is given!");
    		System.exit(1);
    	}
    
    	String path=KlApp.getKvpath();

    	String confFile=conf;
    	
    	File f=new File(confFile);
    	
    	if(!f.exists()){
    		int i=conf.lastIndexOf('/');
    		String fn;
    		
    		if(i>=0)
    			fn=conf.substring(i+1);
    		else
    			fn=conf;
    		
    		confFile=path+"/etc/"+fn;
    		
    		f=new File(confFile);
    		
    		if(!f.exists()){
    			logger.fatal("Cant find configurationfile: "+confFile);
    			System.exit(1);
    		}
    	}

    	PropertiesHelper prop=PropertiesHelper.loadFile(confFile);
    	
    	if(prop==null){
    		logger.fatal("Cant load configurationfile: "+confFile);
			System.exit(1);
    	}
    	return prop;
    }
    
	
    
    public KlApp(String[] args, String conffile, boolean usingSwing){
    	this(args, conffile, null, usingSwing, true);
    }
    public KlApp(String[] args, String conffile, String kvserver, boolean usingSwing){
    	this(args, conffile, kvserver, usingSwing, true);
    }

    public KlApp(String[] args, PropertiesHelper confProp, boolean usingSwing){
        this(args, confProp, null, usingSwing, true);
    }
    public KlApp(String[] args, PropertiesHelper confProp, String kvserver, boolean usingSwing){
        this(args, confProp, kvserver, usingSwing, true);
    }

	static public String getAppName(){return appName;}
	static public String setAppName(String name){return appName=name;}

	public String getDataTableName() {
    	return dataTableName;
    }
    
    public String getTextDataTableName() {
    	return textDataTableName;
    }
    
    public String getForeignDataTableName() {
    		return foreignDataTableName;
    }
    
    public String getForeignTextDataTableName() {
		return foreignTextDataTableName;
    }

    public PropertiesHelper getKlConnectionsProperties(PropertiesHelper conf) {
		PropertiesHelper prop=conf.removePrefix("kl.db*");

		System.err.println(prop.toString("KlConnectionProperties:"));

		return prop;
	}


	public KlApp(String[] args, PropertiesHelper confProp, String kvserver, boolean usingSwing, boolean enableConMgr){
		super(confProp);
		PropertiesHelper conf = getConf();
		dataTableName = conf.getProperty("kl.datatable", "kv2klima" );
		textDataTableName = conf.getProperty("kl.textdatatable", "T_TEXT_DATA" );
		foreignDataTableName = conf.getProperty( "kl.foreign_datatable" );
		foreignTextDataTableName = conf.getProperty( "kl.foreign_textdatatable" );
		this.enableConMgr = enableConMgr;
		try {
			if( this.enableConMgr)
				conMgr=new DbConnectionMgr(getKlConnectionsProperties(conf));
		} catch (IllegalArgumentException e1) {
			logger.fatal("Missing properties in the configuration file: " + e1.getMessage());
			try {
				shutdown(); // Stop kafka
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProcUtil.getStackTrace(" exit(1) ");
			System.exit(1);
		} catch (ClassNotFoundException e1) {
			logger.fatal("Cant load databasedriver: "+e1.getMessage());
			try {
				shutdown();// Stop kafka
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProcUtil.getStackTrace(" exit(1) ");
			System.exit(1);
		}


		if( ! this.enableConMgr ) {
			System.out.println("Database setup: The Connection Manager is diasabled.");
		} else {
			System.out.println("Database setup: ");
			System.out.println("     dbuser: " + conMgr.getDbuser());
			System.out.println("   dbdriver: "+conMgr.getDbdriver());
			System.out.println("  dbconnect: "+conMgr.getDbconnect());
		}
		System.out.println("");
		System.out.println("Load data into tables: ");
		System.out.println("      data: " + dataTableName );
		System.out.println("  textdata: " + textDataTableName );
		System.out.println("");
		//  	System.out.println("Using kvalobs server: ");
//    	System.out.println("          kvserver: "+this.kvserver);
	}



	/**
     * 
     * @param args
     * @param conffile The name of the config file.
     * @param usingSwing
     */
    public KlApp(String[] args, String conffile, String kvserver, boolean usingSwing, boolean enableConMgr){
		this(args,getConfile(conffile), kvserver, usingSwing, enableConMgr);

    }

    public void setDbIdleTime(int secs){
    	if( conMgr != null )
    		conMgr.setIdleTime(secs);
    }
    
    public void setDbTimeToLive(int secs){
    	if( conMgr != null )
    		conMgr.setTimeToLive(secs);
    }
    
    public int getDbIdleTime(){
    	if( conMgr != null )
    		return conMgr.getIdleTime();
    	else
    		return 60; // Return a dummy value
    }
    
    public int getDbTimeToLive(){
    	if( conMgr != null )
    		return conMgr.getTimeToLive();
    	else
    		return 60; // Return a dummy value.
    }
    
    public int checkForConnectionsToClose(){
    	if( conMgr != null )
    		return conMgr.checkForConnectionsToClose();
    	else
    		return 0;
    }
    

    /**
     * If we have a database connection, release it. We are about to exit so
     * there should be now users of this connection so we release it 
     * unconditional.
     */ 
    protected void onExit(){
    	
    	System.err.println("*** KlApp.onExit: \n" + ProcUtil.getStackTrace());
    	if( conMgr != null )
    		conMgr.closeDbDriver();
    }
    
    public DbConnection newDbConnection(){
        
    	if( conMgr != null ) {
    		logger.warn("The ConnectionMgr is disabled.");
    		logger.debug(ProcUtil.getStackTrace());
    		return null;
    	}
    	
        try {
            return conMgr.newDbConnection();
        } catch (SQLException e) {
            logger.warn("Cant create a new database connection: "+
                         e.getMessage());
            return null;
        }
    }
 
    public void  releaseDbConnection(DbConnection con){
        String msg;
        
        try {
        	if( conMgr == null ){
        		logger.warn("The ConnectionMgr is disabled.");
        		logger.debug(ProcUtil.getStackTrace());
        		return;
        	}
            conMgr.releaseDbConnection(con);
            return;
        } catch (IllegalArgumentException e) {
            msg=e.getMessage();
        } catch (IllegalStateException e) {
            msg=e.getMessage();
        } catch (SQLException e) {
            msg=e.getMessage();
        }

        logger.warn("Cant release the database connection: "+msg);
    }
    
    public DbConnectionMgr getConnectionMgr() {
    	if( conMgr == null ){
    		logger.warn("The ConnectionMgr is disabled.");
    		logger.debug(ProcUtil.getStackTrace());
    		return null;
    	}

    	return conMgr;
    }
    	
    
	synchronized public static String getKvpath(){

    	if(kvpath!=null)
    		return kvpath;
    	
    	kvpath=System.getProperties().getProperty("KVDIST");
    	
    	if(kvpath==null){
    		System.out.println("Environment variable KVDIST is unset, using HOME!");
    		kvpath=System.getProperties().getProperty("user.home");
	    
    		if(kvpath==null){
    			System.out.println("Hmmmm. No 'user.home', exiting!");
    			logger.fatal("Environment variable KVALOBS is unset, using HOME!");
    			ProcUtil.getStackTrace(" exit(1) ");
    			System.exit(1);
    		}
    	}
	
    	if(kvpath.charAt(kvpath.length()-1)=='/'){
    		kvpath=kvpath.substring(0, kvpath.length()-1);
    	}

    	System.out.println("Using <" + kvpath + "> as KVALOBS path!");

    	return kvpath;
    }
	
	
	/**
	 * Create a pid file. The filepath and pid must be set as property
	 * in the start script of the application.
	 * 
	 * The path property is PIDFILE and the pid property is USEPID.
	 * 
	 * The property is set with the -D switch on the java commandline.
	 * 
	 * If the pidfile exist or an error occure in the creation of the
	 * pidfile the application terminate.
	 */
	synchronized public void createPidFile(){
    	if(pidFile!=null)
    		return;
    	
    	String pidFilename=System.getProperties().getProperty("PIDFILE");
    	
    	if(pidFilename==null){
    		System.out.println("FATAL: Property variable PIDFILE is unset!");
    		logger.fatal("FATAL: Property variable PIDFILE is unset!");
    		System.exit(1);
    	}
    	
    	createPidFile(pidFilename);
	}

	synchronized public void createPidFile(String pidfile){
    	if(pidFile!=null)
    		return;
    	    	
    	if(pidfile==null){
    		System.out.println("FATAL: pidfile name NOT given!");
    		logger.fatal("FATAL: pidfile name NOT given!");
    		System.exit(1);
    	}
	
    	long pid=ProcUtil.getPid();
    	
    	if( pid < 0 ) {
    		System.out.println("FATAL: Cant get the applications pid (process id)!");
    		logger.fatal("FATAL: Cant get the applications pid (process id)!");
    		System.exit(1);
    	}
    	
    	try {
    		System.err.println(" ***** pidfile 1: '" + pidfile +"'.");
        	if( ProcUtil.isProcRunning(pidfile) ) {
        		System.err.println("FATAL: The pidfile '"+pidfile+"' allready exist!" +
    					"And the process the pid is referancing is running.");
    			logger.fatal("FATAL: The pidfile '"+pidfile+"' allready exist!" +
    					"And the process the pid is referancing is running.");
    			System.exit(1);
        	}

        	System.err.println(" ***** pidfile: '" + pidfile +"'.");
        	Path p=Paths.get(pidfile);
        	System.err.println(" ***** pidfile: pid:'" + pid +"'.");
        	p=Files.write(p, (""+pid+"\n").getBytes(), StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE);
    		System.out.println("Writing pidfile '" + p.toString() + "' with pid '"+pid+"'!");
    		logger.info("Writing pidfile '" + p.toString() + "' with pid '"+pid+"'!");
    	}
    	catch( java.io.IOException ex ) {
    		ex.printStackTrace();
    		System.err.println("FATAL: createPidFile: " + ex.getMessage() );
    		logger.fatal("FATAL: createPidFile: " + ex.getMessage() );
    		System.exit(1);
    	}
    	catch( java.lang.SecurityException ex ) {
    		ex.printStackTrace();
    		System.err.println("FATAL: createPidFile: " + ex.getMessage() );
    		logger.fatal("FATAL: createPidFile: " + ex.getMessage() );
    		System.exit(1);
    	}
    	catch( Exception ex ){
    		ex.printStackTrace();
    		System.err.println("FATAL: createPidFile: " + ex.getMessage() );
    		logger.fatal("FATAL: " + ex.getMessage() );
    		System.exit(1);
    	}
	}
	
	/**
	 * Remove a previous created pidfile.
	 * 
	 * @see createPidFile
	 */
	synchronized public void removePidFile() {
		try {
			if( pidFile != null ) {
	    		System.out.println("Removing pidfile '" + pidFile.getName() + "!");	
				logger.info("Removing pidfile '" + pidFile.getName() + "!");
				pidFile.delete();
			}
		}
		catch( java.lang.SecurityException ex ) {
			System.out.println("SecurityException: Removing pidfile '" + pidFile.getName() + "!");	
			//NOOP
		}
	}



	@Override
	public Result sendData(String data, String decoder) {
		// TODO Implement the http rest interface to kvdatainputd.
		return new Result(EResult.ERROR,"Not implemented");
	}
}
