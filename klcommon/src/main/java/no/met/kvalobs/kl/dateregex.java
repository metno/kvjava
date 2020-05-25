package no.met.kvalobs.kl;
	
import java.util.List;
import no.met.kvalobs.kl.TimeDecoder;

	


public class dateregex {

		
	public static void main( String[] args ) {
		System.out.println("args[0: '"+args[0] +"'");
		//TimeRange time = decodeObstimes( args[0], null );
		List<TimeRange> times = TimeDecoder.decodeTimeList( args[0] );
		
		/*
		if( time != null ) 
			System.out.println("time: " + time );
		*/
		
		if( times != null ) {
			for( TimeRange time : times ) {
				System.out.println("time: " + time );
			}
		}
		
	}
}