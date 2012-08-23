package metno.kvalobs.kl2kvDbCopy;

import java.io.ObjectInputStream.GetField;
import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import metno.util.MiGMTTime;
import metno.util.MiTime;

public class KvData {
	static Logger logger = Logger.getLogger( KvData.class );
	private int stationid_;
	private int typeid_;
	private MiGMTTime obstime_;
	private java.util.LinkedList<DataElement> data;
	
	class DataElement {
		
		int stationid;
		MiGMTTime obstime;
		int typeid;
		int paramid;
		String sensor;
		int level;
		float original;
		float corrected;
		MiGMTTime tbtime;
		String useinfo;
		String controlinfo;
		String cfailed;
		
		
		public DataElement( java.sql.ResultSet res) {
			try {
				stationid = res.getInt( 1 );
				obstime = new  MiGMTTime( res.getTimestamp( 2 ) );
				typeid = res.getInt( 3 );

				if( ! ( stationid_ == stationid && 
						typeid_ == typeid &&
						obstime_.compareTo( obstime ) == 0) ) {
					stationid = Integer.MIN_VALUE;
					typeid = Integer.MIN_VALUE;
					obstime = null;
					return;
				}
				
				paramid = res.getInt( 4 );
				sensor = res.getString( 5 );
				level = res.getInt( 6 );
				original = res.getFloat( 7 );
				corrected = res.getFloat( 8 );
				tbtime = new MiGMTTime( res.getTimestamp( 9 ) );
				
				useinfo = res.getString( 10 );
				if( res.wasNull() )
					useinfo = "";
				
				controlinfo = res.getString( 11 );
				if( res.wasNull() )
					controlinfo = "";
				
				cfailed = res.getString( 12 );
				if( res.wasNull() )
					cfailed = "";
			} catch (SQLException e) {
				logger.error( e.getMessage() );
				e.printStackTrace();
				stationid = Integer.MIN_VALUE;
				typeid = Integer.MIN_VALUE;
				obstime = null;
			}
		}
		
		
		public boolean isValid() {
			return stationid != Integer.MIN_VALUE &&
				   typeid != Integer.MIN_VALUE &&
				   obstime != null;
		}
		
		public String toString() {
			if( ! isValid() )
				return "(INVALID)";
			return stationid +"," + 
				obstime.toString( MiTime.FMT_ISO_WITH_MILLIS ) + "," +
				typeid + "," +
				paramid + "," +
				sensor + "," +
				level + "," +
				original + "," +	
				corrected + "," +
				tbtime.toString( MiTime.FMT_ISO_WITH_MILLIS ) + "," +
				useinfo + "," +
				controlinfo + "," +
				cfailed;
		}
	}
	
	public KvData( int stationid, int typeid, MiGMTTime obstime ) {
		this.stationid_ = stationid;
		this.typeid_ = typeid;
		this.obstime_ = obstime;
		data = new LinkedList<KvData.DataElement>();
	}

	public int getStationid() { return stationid_; }
	public int getTypeid() { return typeid_; }
	public MiGMTTime getObstime() { return obstime_; }

	int getNValidData() { 
		int n=0;
		for( DataElement e : data ) {
			if( e.isValid() )
				n += 1;
		}
		return n;
	}

	
	public void addData( java.sql.ResultSet res ) {
		data.add( new KvData.DataElement( res ) );
	}
	
	public String toString() {
		StringBuilder b=new StringBuilder();
		
		b.append( stationid_ + ", " + typeid_ + ", " + obstime_.toString(MiTime.FMT_ISO_WITH_MILLIS) + ": " + data.size() );
		
		for( DataElement e : data ) {
			b.append("\n   " + e.toString() );
		}
		
		return b.toString();
	}
	
	/**
	 * Expect that there is now data for this dataset in the
	 * kvalobs database.
	 * 
	 * @param con
	 */
	public boolean insertKvData( KlDbConnection con ) {
		DataElement cur=null;
		try {
			for ( DataElement e : data) {
				cur=e;
				if( e.isValid() )
					con.insertKvData( e.stationid, e.obstime, e.typeid, e.paramid,
							          e.sensor, e.level, e.original, e.corrected, e.tbtime,
							          e.useinfo, e.controlinfo, e.cfailed);
			}
			return true;
		} catch (SQLException ex) {
			System.out.println("FAILED: (INSERT): " + cur.stationid+", " +
		                       cur.obstime+", " + cur.typeid + ", " +
					           cur.paramid + ", " + cur.sensor +", "
					           + cur.level + ". Reason: " + ex.getMessage() );
			ex.printStackTrace();
			return false;
		}
	}
}
