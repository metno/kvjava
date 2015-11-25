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
	
	ResultSet selectKlData( MiGMTTime from, MiGMTTime to ) {
		final String q="SELECT stnr as stationid, dato as obstime," +
				 "       typeid, paramid, sensor, xlevel," +
				 "       original,corrected, kvstamp as tbtime," +
				 "       useinfo, controlinfo, cfailed " +
				 "FROM T_ORIGINALDATA "+
				 "WHERE " +
				 "  dato>=? and dato<? " +
				 "ORDER BY dato, stnr, typeid";
		
		try {
			PreparedStatement statement = con.createStatement( "selectKlDataByTimeInterval", q ); 
			statement.setTimestamp(1, from.getTimestamp() );
			statement.setTimestamp(2, to.getTimestamp() );
			return statement.executeQuery();
		} catch (SQLException e) {
			logger.error( "SELECT: " + e.getMessage() );
			return null;
		}
	}
	
	ResultSet selectKvData( int stationid, int typeid, MiGMTTime obstime) {
		final String q="SELECT stationid, obstime," +
				 "       typeid, paramid, sensor, level," +
				 "       original,corrected, tbtime," +
				 "       useinfo, controlinfo, cfailed " +
				 "FROM data "+
				 "WHERE " +
				 "  stationid=? and typeid=? and obstime=?";
		
		try {
			PreparedStatement statement = con.createStatement( "selectKvDataByObstime", q );
			statement.setInt( 1, stationid );
			statement.setInt( 2, typeid );
			statement.setTimestamp( 3, obstime.getTimestamp() );
			return statement.executeQuery();
		} catch (SQLException e) {
			logger.error( "SELECT: " + e.getMessage() );
			return null;
		}
	}

	int insertKvData( int stationid, MiGMTTime obstime, int typeid,
			          int paramid, String sensor, int level, 
			          float original, float corrected, 
			          MiGMTTime tbtime, String useinfo, 
			          String controlinfo, String cfailed) throws SQLException {
		final String q="INSERT INTO data (stationid, obstime," +
				 "       typeid, paramid, sensor, level," +
				 "       original,corrected, tbtime," +
				 "       useinfo, controlinfo, cfailed) " +
				 "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		
			PreparedStatement statement = con.createStatement( "insertKvData", q );
			statement.setInt( 1, stationid );
			statement.setTimestamp( 2, obstime.getTimestamp() );
			statement.setInt( 3, typeid );
			statement.setInt( 4, paramid );
			statement.setString( 5, sensor );
			statement.setInt( 6, level );
			statement.setFloat( 7, original );
			statement.setFloat( 8, corrected );
			statement.setTimestamp( 9, tbtime.getTimestamp() );
			statement.setString( 10, useinfo );
			statement.setString( 11, controlinfo );
			statement.setString( 12, cfailed );
			return statement.executeUpdate();
	}

	int updateKvData( int stationid, MiGMTTime obstime, int typeid,
					  int paramid, String sensor, int level, 
					  float original, float corrected, 
					  String useinfo, 
					  String controlinfo, String cfailed) throws SQLException {
		final String q="UPDATE data " +
					   "SET " +
					   "  original=?, "+
					   "  corrected=?, " +
					   "  useinfo=?, "+
					   "  controlinfo=?, "+
					   "  cfailed=? " +
					   "WHERE "+
					   " stationid=? AND obstime=? AND typeid=? AND " +
					   " paramid=? AND sensor=? AND level=?";
		PreparedStatement statement = con.createStatement( "updateKvData", q );
	
		//SET part
		statement.setFloat( 1, original );
		statement.setFloat( 2, corrected );
		statement.setString( 3, useinfo );
		statement.setString( 4, controlinfo );
		statement.setString( 5, cfailed );

		//WHERE part
		statement.setInt( 6, stationid );
		statement.setTimestamp( 7, obstime.getTimestamp() );
		statement.setInt( 8, typeid );
		statement.setInt( 9, paramid );
		statement.setString( 10, sensor );
		statement.setInt( 11, level );
		return statement.executeUpdate();
	}


}
