package metno.kvalobs.kl2kvDbCopy;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import metno.kvalobs.kl.*;
import metno.util.*;
import org.apache.log4j.Logger;


import metno.dbutil.DbConnection;
import metno.dbutil.DbConnectionMgr;


public class DbDataHelper {

	  protected String createSelectQuery(Station st, TimeRange obstime ){
	    	IQuery sqlHelper = Kl2KvApp.sqlHelper;
	    	String query="";
	    	String stQuery=st.query();
	    	String typeQuery=null;
	    	String obstQuery=null;
	    	boolean doAnd = false;
	    	
	    	if(stQuery.length() == 0 )
	    		stQuery = null;
	    	
	    	if( getTypeid() != null ) 
	    		typeQuery = " typeid="+getTypeid();
	    	
	    	
	    	if( obstime != null ) {
	    		if( obstime.isEqual() ) 
	    			obstQuery = " obstime=" + sqlHelper.dateString( obstime.getFrom() );
	    		else 
	    			obstQuery = " obstime>=" + sqlHelper.dateString( obstime.getFrom() )
	    			          + " AND obstime<=" + sqlHelper.dateString( obstime.getTo() );
	    	}
	    	
	    	if( stQuery != null || obstQuery != null  || typeQuery != null ) {
	    		query = " WHERE";
	    		
	    		if( stQuery != null ) {
	    			doAnd = true;
	    			query += stQuery;
	    		}
	    
	    		if( typeQuery != null ) {
	    			query += ( doAnd?(" AND" + typeQuery):typeQuery);
	    			doAnd = true;
	    		}
	    
	    		if( obstQuery != null ) {
	    			query += ( doAnd?(" AND" + obstQuery):obstQuery);
	    			doAnd = true;
	    		}
	    		
	    	}
	    	
	   		return "SELECT * FROM "+ getTable()+ query;
	    }

}
