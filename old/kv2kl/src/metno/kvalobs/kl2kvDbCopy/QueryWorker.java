package metno.kvalobs.kl2kvDbCopy;

import java.sql.*;

public class QueryWorker {
	
	protected class Status {
		private boolean first;
		private boolean last;
		Status() {
			first = false;
			last = false;
		}
		
		void setLast( boolean f ) {
			last = f;
		}
		
		void setFirst( boolean f ) {
			first = f;
		}
		
		boolean getLast() {
			return last;
		}
		
		boolean getFirst() {
			return first;
		}
	}

	public QueryWorker(){
	}
	
	public ResultSet init() throws SQLException {
		return null;
	}

	
	public boolean next(ResultSet res, Status status) throws SQLException {
			return false;
	}

	public void close(ResultSet res) throws SQLException {
	}

	final public boolean run( IProcessQuery process ) {
		java.sql.ResultSet res=null;
		Status status = new Status();

		try {
			res = init();
		
			if( res == null )
				return false;
			
			while( next( res, status ) ) {
				if( status.getLast() )
					process.atLast();
				
				if( status.getFirst() ) 
					process.atFirst(res);

				process.atEach(res);
			}
			
			if( status.getLast() )
				process.atLast();
			
			
		}
		catch( java.sql.SQLException ex ) {
			try {
				close( res );
			}
			catch( java.sql.SQLException ex1 ){
				ex1.printStackTrace();
			}
			return false;
		}
		finally {
			try {
				close( res );
			}
			catch( java.sql.SQLException ex ){
				ex.printStackTrace();
			}
		}
		
		return true;
	}
	
}
