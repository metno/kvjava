/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: DbConnection.java,v 1.1.2.2 2007/09/27 09:02:19 paule Exp $                                                       

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

import metno.util.MiGMTTime;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DbConnection{
    Connection conn = null;
    HashMap<String, PreparedStatement> statements=new HashMap<String, PreparedStatement>();
    MiGMTTime timeCreated=null;

    String    dbdriver;

    public  DbConnection( Connection conn_,
    					  String dbdriver_ ) {
    	dbdriver = dbdriver_;
    	conn = conn_; 
    }
    
    public void  addStatement( String id, PreparedStatement statement ){
    	 PreparedStatement s=statements.put( id, statement );
    	 
    	 if( s != null ) {
    		 try {
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	 }
    }
    
    public void close() {
    	for( String key : statements.keySet() )	{
    		PreparedStatement stmt = statements.get( key ); 
    		if( stmt != null ) {
    			try {
    				stmt.close();
    			} catch (SQLException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	statements.clear();
    	if( conn != null ) {
    		try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public PreparedStatement getStatement( String id ){
    	return statements.get( id );
    }
    
    public PreparedStatement createStatement( String id, String preparedStatement )  throws SQLException{
    	if( conn == null ) {
    		throw new SQLException("No connection");
    	}
    	
    	PreparedStatement stmt = statements.get( id );
    	
    	if( stmt != null )
    		return stmt;
    	
    	stmt = conn.prepareStatement( preparedStatement );
    	
    	statements.put( id, stmt );
    	
    	return stmt;
    }
}
