package metno.kvalobs.kl2kvDbCopy;

import java.sql.ResultSet;
import java.sql.SQLException;

import metno.util.MiGMTTime;
import metno.util.MiTime;

public class KlCopyQuery implements IProcessQuery {
	int count;
	int stationid;
	int typeid;
	MiGMTTime obstime;

	
	public KlCopyQuery() {
	}
	

	@Override
	public void atFirst(ResultSet res) throws SQLException {
		stationid = res.getInt( 1 );
		obstime = new  MiGMTTime( res.getTimestamp( 2 ) );
		typeid = res.getInt( 3 );
		count=1;
	}

	@Override
	public void atEach(ResultSet res) throws SQLException {
		count++;
	}

	@Override
	public void atLast() throws SQLException {
		System.out.println( stationid +", " + typeid + ", " +
				obstime.toString(MiTime.FMT_ISO_WITH_MILLIS) + " : " + count ); 

	}

}
