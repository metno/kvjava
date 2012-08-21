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
		this.from = from;
		this.to = to;
	}
	
	
	
	@Override
	public ResultSet init() throws SQLException {
		if( con == null )
			throw new java.sql.SQLException( "No db connection.");
		
		return con.select( from, to );
	}

	@Override
	public boolean next(ResultSet res, Status status) throws SQLException {
		int stationid;
		int typeid;
		MiGMTTime obstime;

		if( res.next() ) {
			stationid = res.getInt( 1 );
			obstime = new  MiGMTTime( res.getTimestamp( 2 ) );
			typeid = res.getInt( 3 );

			if( curStationid == stationid && 
				curTypeid == typeid &&
    			curObstime != null && curObstime.compareTo( obstime )==0 ) {
    			count++;
    			status.setFirst( false );
    			status.setLast( false );
    			return true;
			} else if( curStationid != Integer.MIN_VALUE &&
				       curTypeid != Integer.MIN_VALUE ) {
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
			System.out.println( curStationid +", " + curTypeid + ", " +
					curObstime.toString() + " : " + count ); 

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
