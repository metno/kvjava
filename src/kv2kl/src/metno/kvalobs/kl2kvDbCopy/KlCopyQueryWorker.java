package metno.kvalobs.kl2kvDbCopy;

import java.sql.*;
import metno.util.MiGMTTime;

public class KlCopyQueryWorker extends QueryWorker {
	private MiGMTTime from=null;
	private MiGMTTime to=null;
	private KlDbConnection con=null;
	private int count=0;
	private int curStationid=Integer.MIN_VALUE;
	private int curTypeid=Integer.MIN_VALUE;
	private MiGMTTime curObstime=null;

	
	public KlCopyQueryWorker( KlDbConnection con, 
			MiGMTTime from, MiGMTTime to ) {
		this.con = con;
		this.from = from;
		this.to = to;
	}
	
	
	
	@Override
	public ResultSet init() throws SQLException {
		if( con == null ) {
			throw new java.sql.SQLException( "No db connection.");
		}

		return con.selectKlData( from, to );
	}

	@Override
	public boolean next(ResultSet res, Status status) throws SQLException {
		if( res.next() ) {
			int stationid = res.getInt( 1 );
			MiGMTTime obstime = new  MiGMTTime( res.getTimestamp( 2 ) );
			int typeid = res.getInt( 3 );

			if( curStationid == stationid && 
				curTypeid == typeid &&
    			curObstime != null && curObstime.compareTo( obstime )==0 ) {
    			count++;
    			status.setFirst( false );
    			status.setLast( false );
    			return true;
			} else if( curStationid == Integer.MIN_VALUE &&
				       curTypeid == Integer.MIN_VALUE ) {
				status.setFirst( true );
				status.setLast( false );
			} else {
				status.setFirst( true );
				status.setLast( true );
			}
    		    
			count=1;
			curObstime = obstime;
			curTypeid = typeid;
			curStationid = stationid;

			return true;
		}

		status.setFirst( false );
		
		if( count == 0 )
			status.setLast( false );
		else
			status.setLast( true );
		
		return false;
	}
}
