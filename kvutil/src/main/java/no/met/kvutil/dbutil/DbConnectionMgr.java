/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: DbConnectionMgr.java,v 1.1.2.8 2007/09/27 09:02:42 paule Exp $                                                       

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
package no.met.kvutil.dbutil;
import no.met.kvutil.MiGMTTime;
import no.met.kvutil.Tuple2;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class DbConnectionMgr
{
    String dbconnect;
    String dbdriver;
    String dbuser;
    String dbpasswd;
    DbConnection[] dbconCache;
    boolean isShutdown;
    int     timeToLive; //How long shall a connection live before it is deleted.
    int     idleTime;   //How long shall a connection be allowed to be inactive
                        //before we delete it.

    /**
     * Instatiate an connectionmanager whit connection data 
     * from the propperties. The following properties are searcehed 
     * for. 
     * 
     * <pre>
     *  * dbdriver - The database driver to use.
     *    dbuser   - The user to use to create a connection.
     *    dbpasswd - The password to use to create a connection.
     *  * dbconnect- The connectstring to use to create a connection.
     *    dbmaxconnections - Max connection to cache.
     * </pre>
     * 
     * Properties marked with * above is mandatory.
     * @throws ClassNotFoundException When the database driver cant 
     *                                be loaded. 
     * @throws IllegalArgumentException If driver or connectstring is missing.
     */
    public DbConnectionMgr(Properties prop)
        throws ClassNotFoundException,IllegalArgumentException{
        this(prop, 1);
    }

    static int maxConnection(Properties prop, String prefix, int maxConnectionsMustBeLE) {
        String name;
        if(prefix== null || prefix.length()==0)
            name="dbmaxconnections";
        else
            name=prefix+".dbmaxconnections";
        int max=Integer.parseInt(prop.getProperty(name, Integer.toString(maxConnectionsMustBeLE)));
        if(max<maxConnectionsMustBeLE)
            max=maxConnectionsMustBeLE;
        return max;
    }

    //Must be called from a synchronized method
    private void increaseConCache(int n) {
        if( n<=dbconCache.length)
            return;  // we only increase.
        DbConnection[]  con=new DbConnection[n];
        for(int i=0; i<dbconCache.length; i++)
            con[i]=dbconCache[i];

        dbconCache=con;
    }

    public DbConnectionMgr(Properties prop, int minimumMaxConnections)
            throws ClassNotFoundException,IllegalArgumentException{
        this(prop.getProperty("dbdriver"),
                prop.getProperty("dbuser", ""),
                prop.getProperty("dbpasswd", ""),
                prop.getProperty("dbconnect"),
                maxConnection(prop, null, minimumMaxConnections));
    }

    public DbConnectionMgr(Properties prop, String prefix, int minimumMaxConnections)
            throws ClassNotFoundException,IllegalArgumentException{
        this(prop.getProperty(prefix+".dbdriver"),
                prop.getProperty(prefix+".dbuser", ""),
                prop.getProperty(prefix+".dbpasswd", ""),
                prop.getProperty(prefix+".dbconnect"),
                maxConnection(prop, prefix, minimumMaxConnections));
    }
    
    
    /**
     *  Convenient constructor to use when most of the connection
     *  properties may not be given. Only the mandatory propertis
     *  is given.
     *   
     * @param dbdriver The database driver to use.
     * @param dbconnectstr The connectstring to use to create a connection.
     * @param maxConnections Max connections to cache.
     * @throws ClassNotFoundException When the database driver cant 
     *                                be loaded. 
     * @throws IllegalArgumentException If driver or connectstring is missing.
     */
    public DbConnectionMgr(String dbdriver, String dbconnectstr, 
                           int maxConnections)
        throws ClassNotFoundException,IllegalArgumentException{
        this(dbdriver, "", "", dbconnectstr, maxConnections);
    }
    
    /**
     * 
     * @param dbdriver The database driver to use.
     * @param dbuser The user to use to create a connection.
     * @param dbpasswd The password to use to create a connection.
     * @param dbconnectstr The connecttring to use to create a connection.
     * @param maxConnections Max connection to cache, default 1.
     * @throws ClassNotFoundException When the database driver cant 
     *                                be loaded. 
     * @throws IllegalArgumentException If driver or connectstring is missing.
     */
    public DbConnectionMgr(String dbdriver, 
                           String dbuser,
                           String dbpasswd,
                           String dbconnectstr,
                           int    maxConnections)
        throws ClassNotFoundException,IllegalArgumentException{

        if(dbdriver==null)
            throw new IllegalArgumentException("DbConnectionMgr: dbdriver==0.");
        
        this.dbdriver=dbdriver.trim();
        
        if(this.dbdriver.length()==0)
            throw new IllegalArgumentException("DbConnectionMgr: dbdriver is an empty string!");
        
     
        if(dbconnectstr==null)
            throw new IllegalArgumentException("DbConnectionMgr: dbconnectstr==0.");
        
        this.dbconnect=dbconnectstr.trim();
        
        if(this.dbconnect.length()==0)
            throw new IllegalArgumentException("DbConnectionMgr: dbconnectstr is an empty string!");
     
        if(dbuser==null)
            this.dbuser="";
        else
            this.dbuser=dbuser.trim();
            
        if(dbpasswd==null)
            this.dbpasswd="";
        else
            this.dbpasswd=dbpasswd.trim();

        if(maxConnections<=0)
            throw new IllegalArgumentException("DbConnectionMgr: maxConnections<=0!");
        
        Class.forName(this.dbdriver);
        
        dbconCache=new DbConnection[maxConnections];
        
        isShutdown=false;
        timeToLive=-1;
        idleTime=-1;
    }
    
    protected void finalize() throws Throwable {
        closeDbDriver();
    }
                                    
    /**
     * This function is used to do some last minute shutdown statement
     * for the database engine used. What to do is specific to each 
     * database engine. 
     * 
     * All connections that is NOT in use is closed! It is an ERROR
     * to call this method if there is still connections in use.
     * 
     * At the moment it is only HSQLDB (http://hsqldb.org) that use this.
     * 
     * Tested database engines is postgresql, oracle and HSQLDB.
     */
     synchronized public void closeDbDriver(){
        if(isShutdown)
            return;
        
        DbConnection con;
        isShutdown=true;
        
        if(dbdriver.compareTo("org.hsqldb.jdbcDriver")==0){
            try{
                con=realNewDbConnection();
            }
            catch(SQLException ex){
                //ex.printStackTrace();
                con=null;
            }
            
            if(con!=null){
                try {
                    con.exec("SHUTDOWN");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                try {
                    con.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        for(int i=0; i<dbconCache.length; i++){
            if(dbconCache[i]!=null){
                if(dbconCache[i].getInuse()){
                    System.out.println("WARNING: Shutdown the DbConnectionMgr, but connections still in use!");
                }else{
                    try {
                        dbconCache[i].closeConnection();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    dbconCache[i]=null;
                }                    
            }
        }
     }   
    
     DbConnection realNewDbConnection()
         throws SQLException{
         Connection con=null;
         Statement statement=null;

         try{
             con = DriverManager.getConnection(dbconnect, 
                                               dbuser, 
                                               dbpasswd);
             
             if(!con.getAutoCommit())
                 con.setAutoCommit(true);
                          
             statement=con.createStatement();
         }
         catch(SQLException ex){
             if(con!=null){
                 try{
                     con.close();
                 }
                 catch(Exception ex_){
                 }   
             }
             
             throw ex;
         }
         
         return new DbConnection(con, statement, dbdriver, this);
     }

     
     /**
      * This method is the same as newDbConnection(int timeoutInSec), but with
      * timeoutInSeconds set to 0, ie wait without limit to the completion.
      * 
      * @return A DbConnection.
      * @throws SQLException
      */
     synchronized public DbConnection newDbConnection()
     	throws SQLException{
    	 return doGetDbConnection(0);
     }
     
     /**
      * Get a new DbConnections. The connections is either taken from 
      * the cache or a new one is created.
      * 
      * The connection must be released with a call to releaseDbConnection.
      * 
      * Null is returned if maxconnections is in use and no IdleConnections
      * is in the cache.
      * 
      * The returned connection is in AutoCommit state. I one want to run in
      * transaction mode a call to setAutoCommit(false) must be issud on the
      * connection.
      *
      * @param timeoutInSec Throw an exception if a operation is not completed in 
      *                      timeoutInSec seconds. 
      * @return A new DbConnection or null if error.
      * @throws SQLException If we could'nt create a new connection to the database.
      */
     synchronized public DbConnection newDbConnection(int timeoutInSec)
         throws SQLException{

         return doGetDbConnection(timeoutInSec);
    }

    private DbConnection doGetDbConnection(int timeoutInSec) throws SQLException {
        checkForConnectionsToClose();

        DbConnection dbcon=null;
        int nullSlot=-1;

        for(int i=0; dbcon==null && i<dbconCache.length; i++){
           if(dbconCache[i]==null){
               if(nullSlot==-1)
                   nullSlot=i;
           }else if(!dbconCache[i].getInuse()){
               dbcon=dbconCache[i];
           }
       }

        if(dbcon!=null){
            dbcon.setInuse(true);
            dbcon.setTimeout(timeoutInSec);
            return dbcon;
        }

        if(nullSlot!=-1){
            dbcon=realNewDbConnection();
            dbcon.setTimeout(timeoutInSec);
            dbconCache[nullSlot]=dbcon;
            return dbcon;
        }

        return null;
    }

    synchronized public DbConnection waitForDbConnection(int secondsToWait ) throws TimeoutException, SQLException, InterruptedException {
        int millis=1000*secondsToWait;
        Instant now=Instant.now();
        Instant waitTo=now.plusMillis(millis);
        while(now.isAfter(waitTo)){
            DbConnection con=doGetDbConnection(0);
            if( con!=null)
                return con;
            else wait(millis);
        }
        throw new TimeoutException();
    }

    synchronized public DbConnection[] waitForDbConnection(int secondsToWait, int nConnections ) throws TimeoutException, SQLException, InterruptedException, Exception {
        int millis=1000*secondsToWait;
        Instant now=Instant.now();
        Instant waitTo=now.plusMillis(millis);
        DbConnection[] con=new DbConnection[nConnections];
        int i=0;


        if( getMaxconnections()<nConnections)
            increaseConCache(nConnections);

        try {
            while (i < con.length && now.isBefore(waitTo)) {
                con[i] = doGetDbConnection(0);
                if (con[i] != null) {
                    if (i+1 == con.length)
                        return con;
                    else
                        i++;
                } else wait(500);
            }
        }
        catch( final Throwable ex) {
            doReleaseConnections(con); // May throw exception
            //if not throw the exception we caught.
            throw ex;
        }

        doReleaseConnections(con); // May throw exception
        //If not throw timeout
        throw new TimeoutException("Timeout: waitForDbConnection: waited: ");
    }


    synchronized public void  releaseDbConnection(DbConnection con)
        throws SQLException, IllegalArgumentException, IllegalStateException{
        doReleaseConnection(con);
    }

    synchronized public void  releaseDbConnection(DbConnection[] con)
            throws SQLException, IllegalArgumentException, IllegalStateException{
        doReleaseConnections(con);
     }

    private void doReleaseConnections(DbConnection[] con) {
        Throwable anException;

        for(int i=0; i<con.length; i++) {
            try {
                if(con[i]!=null)
                    doReleaseConnection(con[i]);
            } catch (final Throwable ex) {
                for(int n=i+1; n<con.length;n++) {
                    try {
                        doReleaseConnection(con[i]);
                    } catch (Exception ignored) {
                    }
                }
                throw ex;
            }
        }
    }

    private void doReleaseConnection(DbConnection con) {
        DbConnection dbcon=null;

        if(con==null)
            throw new IllegalArgumentException("DbConnectionMgr.releaseDbConnection: Expected con!=null!");

        if(!con.getInuse())
            throw new IllegalStateException("Connection allready released!");

        for(int i=0; i<dbconCache.length; i++){
            if(dbconCache[i]==con){
                dbcon=con;
                try{
                    if(!dbcon.getAutoCommit())
                        dbcon.setAutoCommit(true);
                }
                catch(SQLException ex){
                    System.out.println("DbConnectionMgr.releaseDbConnection: Exception in get/setAutoCommit: "+ex.getMessage());
                    try{
                        dbcon.closeConnection();
                    }
                    catch(SQLException e){
                        System.out.println("DbConnectionMgr.releaseDbConnection: Exception (get/setAutoCommit) in close  : "+e.getMessage());
                    }
                    dbconCache[i]=null;
                    checkForConnectionsToClose();
                    notifyAll();
                    return;
                }
                dbconCache[i]=dbcon;
                break;
            }
        }

        if(dbcon==null)
            throw new IllegalArgumentException("Connection was not created by this DbConnectionMgr!");

        try{
            if(!dbcon.getAutoCommit())
                dbcon.setAutoCommit(true);
        }
        catch(SQLException ex){

        }

        dbcon.setInuse(false);

        checkForConnectionsToClose();
        notifyAll();
    }

    public String getDbconnect(){
        return dbconnect;
    }
    public String getDbdriver(){
        return dbdriver;
    }
    public String getDbuser(){
        return dbuser;
    }

    public void setDbuser(String user){
        dbuser=user;
    }
    
    public void setDbpasswd(String pwd){
        dbpasswd=pwd;
    }

    public int getMaxconnections(){
        return dbconCache.length;
    }
    
    /**
     * Returns the count of all connections that is in use.
     *  
     * @return connections in use.
     */
    synchronized public int getConnectionsInuse(){
        int cnt=0;
        
        for(int i=0; i<dbconCache.length; i++){
            if(dbconCache[i]!=null && dbconCache[i].getInuse())
                cnt++;
        }
        
        return cnt;
    }
    
    /**
     * Idle connections is connections that is active but not
     * in use, ie they are cached for reuse.
     * @return
     */
    synchronized public int getIdleConnections(){
        int cnt=0;
        
        for(int i=0; i<dbconCache.length; i++){
            if(dbconCache[i]!=null && !dbconCache[i].getInuse())
                cnt++;
        }
        
        return cnt;
    }
    
    public void setIdleTime(int idleTimeInSeconds){
        idleTime=idleTimeInSeconds;
    }
    
    public void setTimeToLive(int timeToLiveInseconds){
        timeToLive=timeToLiveInseconds;
    }
    
    public int getIdleTime(){
        return idleTime;
    }
    
    public int getTimeToLive(){
        return timeToLive;
    }
    
    synchronized public int checkForConnectionsToClose(){
        int cnt=0;
        MiGMTTime now=new MiGMTTime();
        
        if(timeToLive>=0){
            for(int i=0; i<dbconCache.length; i++){
                if(dbconCache[i]!=null && !dbconCache[i].getInuse()){
                    MiGMTTime tmp=new MiGMTTime(dbconCache[i].getCreated());
                    
                    tmp.addSec(timeToLive);
                    //System.out.println("timeToLive: secsTo: "+tmp.secsTo(now));
                    
                    if(tmp.secsTo(now)>=0){
                        cnt++;
                        try {
                            dbconCache[i].closeConnection();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        dbconCache[i]=null;
                    }
                }
            }
        }

        if(idleTime>=0){
            for(int i=0; i<dbconCache.length; i++){
                if(dbconCache[i]!=null && !dbconCache[i].getInuse()){
                    MiGMTTime tmp=new MiGMTTime(dbconCache[i].getLastAccess());
                    
                    tmp.addSec(idleTime);
                    //System.out.println("idleTime: secsTo: "+tmp.secsTo(now));

                    if(tmp.secsTo(now)>=0){
                        cnt++;
                        
                        try {
                            dbconCache[i].closeConnection();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        dbconCache[i]=null;
                    }
                }
            }
        }

        return cnt;
    }

    public String connectInfo() {
        return "connect: '"+dbconnect + "' user: '"+dbuser+"' driver: '"+dbdriver+"'";
    }
}
