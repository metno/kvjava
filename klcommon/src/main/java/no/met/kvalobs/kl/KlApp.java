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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import no.met.kvutil.PidFileUtil;
import no.met.kvutil.ProcUtil;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvclient.kafka.KafkaApp;
import no.met.kvclient.service.SendDataToKv;
import no.met.kvclient.service.SendDataToKv.Result.EResult;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class KlApp extends KafkaApp  implements SendDataToKv
{
	
	static Logger logger=Logger.getLogger(KlApp.class);

    static DbConnectionMgr conKlMgr =null;
	static DbConnectionMgr conKvMgr =null;
	File             pidFile=null;
    String dataTableName=null;
    String textDataTableName=null;
    String foreignDataTableName=null;
    String foreignTextDataTableName=null;
    boolean enableKlConMgr =true;
    boolean enableFilter=true;
    static String           kvpath=null;
	static String           appName=null;
	static String           confname=null;
	String kvHost;

    static public PropertiesHelper getConfile(String conf){
    	if(conf==null){
    		logger.fatal("INTERNAL: No configuration file is given! (null)!");
    		System.exit(1);
    	}
    	
    	if(conf.length()==0){
    		logger.fatal("INTERNAL: No configuration file is given!");
    		System.exit(1);
    	}
    
    	String path= KlApp.getKvpath();

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
    	this(args, conffile, usingSwing, true);
    }

    public KlApp(String[] args, PropertiesHelper confProp, boolean usingSwing){
        this(args, confProp, usingSwing, true);
    }

	static public String getAppName(){return appName;}
	static public String getConfName(){return confname;}
	static public String setAppName(String name){return appName=name;}

	public boolean getEnableFilter() {
		String sEnableFilter=getConf().getProperty("kl.filter.enable", "true").trim();

		if( !sEnableFilter.isEmpty() ) {
			if( sEnableFilter.startsWith("t") || sEnableFilter.startsWith("T") )
				enableFilter = true;
			else
				enableFilter = false;
		}

		return enableFilter;
	}

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

    public PropertiesHelper getConnectionsProperties(PropertiesHelper conf, String prefix) {
		PropertiesHelper prop=conf.removePrefix(prefix);
		System.err.println(prop.toString("ConnectionProperties (" + prefix +"): "));
		return prop;
	}


	public KlApp(String[] args, PropertiesHelper confProp, boolean usingSwing, boolean enableKlConMgr){
		super(confProp);
		PropertiesHelper conf = getConf();
		dataTableName = conf.getProperty("kl.datatable", "kv2klima" ).trim();
		textDataTableName = conf.getProperty("kl.textdatatable", "T_TEXT_DATA" ).trim();
		foreignDataTableName = conf.getProperty( "kl.foreign_datatable","" ).trim();
		foreignTextDataTableName = conf.getProperty( "kl.foreign_textdatatable","" ).trim();
		appName = conf.getProperty("appname", "").trim();
		confname= conf.getProperty("confname", "").trim();
		this.enableKlConMgr = enableKlConMgr;
		kvHost=conf.getProperty("kvhost", "localhost:8090").trim();
		conKlMgr=getConnectionMgr(conf, "kl");
		conKvMgr=(DbConnectionMgr)super.getInfo().get("kv.dbmanager");

		if(foreignDataTableName.isEmpty())
			foreignDataTableName=null;

		if(foreignTextDataTableName.isEmpty())
			foreignTextDataTableName=null;


		if( ! this.enableKlConMgr) {
			System.out.println("Database setup: The Connection Manager is diasabled.");
		} else {
			System.out.println("Database setup: ");
			System.out.println("     dbuser: " + conKlMgr.getDbuser());
			System.out.println("   dbdriver: "+ conKlMgr.getDbdriver());
			System.out.println("  dbconnect: "+ conKlMgr.getDbconnect());
		}
		System.out.println("");
		System.out.println("Load data into tables: ");
		System.out.println("      data: " + dataTableName );
		System.out.println("  textdata: " + textDataTableName );
		System.out.println("  foreign_data: " + (foreignDataTableName!=null?foreignDataTableName:"(Not set)") );
		System.out.println("  foreign_textdata: " + (foreignTextDataTableName!=null?foreignTextDataTableName:"(Not set)") );
		System.out.println("");
	}

	private DbConnectionMgr getConnectionMgr(PropertiesHelper conf, String prefix) {
		try {
			if( this.enableKlConMgr)
				return new DbConnectionMgr(conf, prefix, 1);
		} catch (Exception e1) {
			logger.fatal("Cant load databasedriver: "+e1.getMessage());
			try {
				shutdown(); // Stop kafka
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ProcUtil.getStackTrace(" exit(1) ");
			System.exit(1);
		}
		return null;
	}


	/**
     * 
     * @param args
     * @param conffile The name of the config file.
     * @param usingSwing
     */
    public KlApp(String[] args, String conffile, boolean usingSwing, boolean enableKlConMgr){
		this(args,getConfile(conffile), usingSwing, enableKlConMgr);

    }

    public void setDbIdleTime(int secs){
    	if( conKlMgr != null )
    		conKlMgr.setIdleTime(secs);
    }
    
    public void setDbTimeToLive(int secs){
    	if( conKlMgr != null )
    		conKlMgr.setTimeToLive(secs);
    }
    
    public int getDbIdleTime(){
    	if( conKlMgr != null )
    		return conKlMgr.getIdleTime();
    	else
    		return 60; // Return a dummy value
    }
    
    public int getDbTimeToLive(){
    	if( conKlMgr != null )
    		return conKlMgr.getTimeToLive();
    	else
    		return 60; // Return a dummy value.
    }
    
    public int checkForConnectionsToClose(){
    	if( conKlMgr != null )
    		return conKlMgr.checkForConnectionsToClose();
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

		try {
			Unirest.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to shutdown the Unirest (Http Client) ("+e.getMessage()+")." );
		}

		if( conKlMgr != null )
    		conKlMgr.closeDbDriver();
		if( conKvMgr != null)
			conKvMgr.closeDbDriver();


    }
    
    public DbConnection newKlDbConnection(){
        
    	if( conKlMgr == null ) {
    		logger.warn("The ConnectionMgr is disabled.");
    		logger.debug(ProcUtil.getStackTrace());
    		return null;
    	}
    	
        try {
            return conKlMgr.newDbConnection();
        } catch (SQLException e) {
            logger.warn("Cant create a new database connection: "+
                         e.getMessage());
            return null;
        }
    }

    public void releaseKlDbConnection(DbConnection con){
        String msg;
        
        try {
        	if( conKlMgr == null ){
        		logger.warn("The ConnectionMgr is disabled.");
        		logger.debug(ProcUtil.getStackTrace());
        		return;
        	}
            conKlMgr.releaseDbConnection(con);
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
    
    static public DbConnectionMgr getKlConnectionMgr() {
    	if( conKlMgr == null ){
    		logger.warn("The ConnectionMgr is disabled.");
    		logger.debug(ProcUtil.getStackTrace());
    		return null;
    	}

    	return conKlMgr;
    }

	static public DbConnectionMgr getKvConnectionMgr() {
		return conKvMgr;
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
    	if(PidFileUtil.getPidFile() != null)
    		return;

		PidFileUtil.createPidFile(pidfile);

	}
	
	/**
	 * Remove a previous created pidfile.
	 */
	synchronized public void removePidFile() {
		PidFileUtil.removePidFile();
	}



	@Override
	public Result sendData(String data, String decoder) throws Exception{
		String url = "http://"+kvHost+"/v1/observation";
		String contenType="text/plain";
		String body=decoder+"\n"+data;
		HttpResponse<JsonNode> response;

		try {

			response = Unirest.post(url)
            	.header("accept", "application/json")
                .header("Content-Type", contenType)
                .body(body)
                .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.err.println("Failed to send data to: "+ url +"\n"+decoder+"\n"+data);
			throw new Exception(e.getMessage());
		}

		if( response.getStatus() != 200 ) {
			throw new Exception("HttpError: " + response.getStatusText() + " (" + response.getStatus()+"). url: " + url +".");

		}

		JsonNode json=response.getBody();
		JSONObject obj = json.getObject();
		EResult eres;
		int rc=obj.isNull("res")?-1:obj.getInt("res");
		String msg = obj.isNull("message")?"":obj.getString("message");
		String msgid = obj.isNull("messageid")?"":obj.getString("messageid");
		switch( rc ) {
			case 0: eres=EResult.OK; break;
			case 1: eres=EResult.NODECODER; break;
			case 2: eres=EResult.DECODEERROR; break;
			case 3: eres=EResult.NOTSAVED; break;
			case 4: eres=EResult.ERROR; break;
			default:
				throw new Exception("Unknown response from kvalobs. Result code: " + rc+ ". Message: " + msg);
		}

		return new Result(eres, msg, msgid);
	}
}
