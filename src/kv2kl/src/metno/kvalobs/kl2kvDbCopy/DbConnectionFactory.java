package metno.kvalobs.kl2kvDbCopy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;

public class DbConnectionFactory {
	static Logger logger = Logger.getLogger(DbConnectionFactory.class);
	class DbParams{
		String dbconnect;
		String dbuser;
		String dbpasswd;
		String dbdriver;
		DbParams( String connect, String user, String passwd,
				 String driver){
			dbconnect = connect;
			dbuser = user;
			dbpasswd = passwd;
			dbdriver = driver;
		}
		String print() {
			return	"Driver:  " + dbdriver +"\n" +
				  "Connect: " + dbconnect+"\n" +
				  "User:    " + dbuser +"\n" +
				  "Passwd:  " + dbpasswd+"\n";
		}
	}
	HashMap<String, DbParams> dbconf;
	
	public DbConnectionFactory( Properties prop ) {
		dbconf = new HashMap<String, DbParams>();
		Set<String> keys = prop.stringPropertyNames();
		HashSet<String> prefix = new HashSet<String>();
		
		for( String key : keys ) {
			String[] arr = key.split("\\.");
			if( arr.length > 2 && arr[1].contentEquals("db") )
				prefix.add( arr[0] );
		}
		
		for( String key : prefix ) {
			String driver = prop.getProperty(key+".db.driver");
			String connect = prop.getProperty(key+".db.connect");
			String user = prop.getProperty(key+".db.user");
			String passwd = prop.getProperty(key+".db.passwd", "");
			
			System.out.println(key+".db.connect: '" + connect +"'");
			if( driver==null || connect == null ||
				user == null )   {
				System.out.println("Missing mandatory parameter.");
				continue;
			}
			
			dbconf.put(key, new DbParams(connect, user, passwd, driver) );
		}
		
		Set<Map.Entry<String, DbParams>> dbconfEntries = dbconf.entrySet();
		
		for( Iterator<Map.Entry<String, DbParams> > itDbconf = dbconfEntries.iterator();
			itDbconf.hasNext(); ) {	
			Map.Entry<String, DbParams> e = itDbconf.next();
		
			System.out.println("---- " + e.getKey() + " ----" );
			System.out.println( e.getValue().print() );

//			try {
//	    		Class.forName( e.getValue().dbdriver );
//	    	} catch(Exception ex) {
//	    		itDbconf.remove();
//	    		System.out.println("FATAL: cant load the database driver <"+
//	    				e.getValue().dbdriver +">!");
//	    	}
		}
	}
	
	DbConnection createConnection( String database ) {
		DbParams db = dbconf.get( database );
		
		if( db == null ) {
			logger.warn("No database definition for <"+database+">.");
			return null;
		}
		
		try{
			System.out.println( db.print() );
			//OracleDataSource ds = new OracleDataSource();
			
			System.out.println("URL: '"+db.dbconnect+"'");
			//ds.setURL(db.dbconnect );
			
//			Class cls = Class.forName( db.dbdriver );
//			DataSource ds = (DataSource) cls.newInstance();
			
			
			Connection conn = DriverManager.getConnection(db.dbconnect, db.dbuser, db.dbpasswd );
			
			return new DbConnection( conn, db.dbdriver );
		}
		catch(SQLException ex){
			logger.error( ex.getMessage() );
			logger.error("DBERROR: cant create a database"+
					 	 "connection <"+database+">!");
		}
//		catch (ClassNotFoundException e) {
//			logger.error("Cant load the class <"+db.dbdriver +">.");
//		}
//		 
//		catch( InstantiationException e ) {
//			logger.error("Cant create an DataSource for <"+db.dbdriver +">. (" + e.getMessage() +")");
//			
//		} 
//		catch( IllegalAccessException e ){
//			logger.error("Cant create an DataSource for <"+db.dbdriver +">. (" + e.getMessage() +").");
//		}
		
		return null;
	}

}
