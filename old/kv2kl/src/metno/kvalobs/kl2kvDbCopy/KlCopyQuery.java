package metno.kvalobs.kl2kvDbCopy;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import metno.kvalobs.kvDataInputClt.kvDataInputCltMain;
import metno.util.MiGMTTime;
import metno.util.MiTime;

public class KlCopyQuery implements IProcessQuery {
	static Logger logger = Logger.getLogger( KlCopyQuery.class );
	int count;
	int stationid;
	int typeid;
	MiGMTTime obstime;
	KvData klData;
	KlDbConnection kvcon;

	
	public KlCopyQuery(KlDbConnection kvdb) {
		kvcon = kvdb;
	}
	

	@Override
	public void atFirst(ResultSet res) throws SQLException {
		stationid = res.getInt( 1 );
		obstime = new  MiGMTTime( res.getTimestamp( 2 ) );
		typeid = res.getInt( 3 );
		klData = new KvData( stationid, typeid, obstime );
		count=0;
	}

	@Override
	public void atEach(ResultSet res) throws SQLException {
		klData.addData(res);
		count++;
	}

	@Override
	public void atLast() throws SQLException {
		int n=0;
		KvData kvData = null;
		
		if( klData.getNValidData() == 0 ) {
			System.out.println( klData );
			return;
		}
		
		try {
			java.sql.ResultSet res = kvcon.selectKvData( klData.getStationid(), 
													   klData.getTypeid(), 
													   klData.getObstime() );
			while( res.next() ) {
				if( n==0 ) {
					kvData = new KvData(klData.getStationid(), klData.getTypeid(), 
										klData.getObstime() );
				}
				n += 1;
				kvData.addData(res);
			}
			
			if( kvData == null ) 
				insertKvData( klData );
			else
				updateKvData( kvData );
//			System.out.println("---- atLast: " + klData.getStationid() + ", "+ 
//								  klData.getTypeid() + ", " +  klData.getObstime() + "---");
//			System.out.print( "klData: " );
//			System.out.println( klData );
//			System.out.print( "kvData: "  );
//			System.out.println( kvData );
//			System.out.println( "---- atLast: END ----" );
		}
		catch( SQLException ex  ) {
			ex.printStackTrace();
		}
		kvData = null;
	}
	
	void insertKvData( KvData kvdata) {
		if( kvdata.insertKvData( kvcon ) )
			System.out.print("OK     - ");
		else
			System.out.print("FAILED - ");
		
		System.out.println( "INSERT: "+"("+kvdata.getNValidData()+")" + kvdata.getStationid()+", "+
				kvdata.getTypeid() + ", " + kvdata.getObstime());
			
	}

	void updateKvData( KvData kvdata ) {
		System.out.println( "UPDATE: "+"("+kvdata.getNValidData()+")" + kvdata.getStationid()+", "+
				kvdata.getTypeid() + ", " + kvdata.getObstime());
	}
}
