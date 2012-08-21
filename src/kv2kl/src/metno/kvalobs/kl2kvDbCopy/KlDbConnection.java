package metno.kvalobs.kl2kvDbCopy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import metno.util.MiGMTTime;

import org.apache.log4j.Logger;

public class KlDbConnection {
	static Logger logger = Logger.getLogger(DbConnectionFactory.class);
	private DbConnection con;
	
	public KlDbConnection( DbConnection con_ ) {
		con = con_;
	}
	
	void close() {
		con.close();
	}
	
	ResultSet select( MiGMTTime from, MiGMTTime to ) {
		final String q="SELECT stnr as stationid, dato as obstime," +
				 "       typeid, paramid, sensor, xlevel," +
				 "       original,corrected, kvstamp as tbtime," +
				 "       useinfo, controlinfo, cfailed " +
				 "FROM T_ORIGINALDATA "+
				 "WHERE " +
				 "  dato>=? and dato<? " +
				 "ORDER BY dato, stnr, typeid";
		
		try {
			PreparedStatement statement = con.createStatement( "selectByTimeInterval", q ); 
			statement.setTimestamp(1, from.getTimestamp() );
			statement.setTimestamp(2, to.getTimestamp() );
			return statement.executeQuery();
		} catch (SQLException e) {
			logger.error( "SELECT: " + e.getMessage() );
			return null;
		}
	}
}
