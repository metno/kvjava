package no.met.kvalobs.kl;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import no.met.kvutil.DateTimeUtil;

public class TimeDecoder {
	
	
	/**
	 * Decodes a comma separated list of times an returns a list of TimeRange. The
	 * from and to fields is equal.
	 * 
	 * @param timeListIn
	 * @return A list of TimeRange. 
	 */
	public static List<TimeRange> decodeTimeList( String timeListIn ) 
	{
		List<TimeRange> times = new LinkedList<TimeRange>();
		Instant fromTime;
		Instant toTime;
		String[] timeList = timeListIn.split(",");
		StringBuilder remaining = new StringBuilder();
		
		for( String timeString : timeList ){
			toTime = null;
			fromTime = DateTimeUtil.parseOptHms( timeString, remaining );
			
			System.out.println("timeString: '" + timeString +"' remaining: '" + remaining+"'");
			
			if( fromTime == null ) {
				System.out.println("Inavalid timespec: '" + timeString +"'.");
				return null;
			}

			if( remaining.length() > 2 && remaining.charAt(0) == '-' ) {
				toTime = DateTimeUtil.parseOptHms( remaining.substring( 1 ), null );
				
				if( toTime == null ) {
					System.out.println("Inavalid timespec range: '" + timeString +"'.");
					return null;
				}
			}
			times.add( new TimeRange(fromTime, toTime ) );
		}
		
		return times;
		
	}
	
	/**
	 * Create a TimeRange from a 'fromTime' and 'toTime' given as
	 * strings. If both fromTime and toTime is null, null is returned.
	 * 
	 * If one of them is null, from and to will be equal in the returned TimeRange. 
	 * 
	 * @param fromTime A valid timestamp or null. 
	 * @param toTime A valid timestamp or null.
	 * @return TimeRange. 
	 */
	public static TimeRange decodeToTimeRange( String fromTime, String toTime ) 
	{
	
		if( fromTime == null && toTime == null ) 
			return null;
		
		Instant from;
		Instant to;
		
		if( fromTime == null )
			fromTime = toTime;
		
		if( toTime == null )
			toTime = fromTime;
		
		from = DateTimeUtil.parseOptHms( fromTime, null );
		to = DateTimeUtil.parseOptHms( toTime, null );
		
		//TODO: This should be an exception instead of returning null. 
		if( from == null || to == null )
			return null;
		
		return new TimeRange( from, to );
		
	}
	
	/**
	 * ensureResololution split all TimeRange in the list times where the difference between
	 * fromtime and totime is greater than the resolutionInHours. On return all TimeRange in the
	 * times list has a difference equal or less than  resolutionInHours. The number of elements
	 * in the times list will be greater or equal to the length of the in comming list. The in comming
	 * times list is unchanged.
	 *  
	 * 
	 * @param times List of TimeRange to split.
 	 * @param resolutionInHours The maximum difference between fromTime and toTime.
	 * @return List<TimeRange> A list of TimeRange with a differens less than or equal to r
	 *         resolutionInHours.
	 */
	public static List<TimeRange> ensureResolution( List<TimeRange> times, int resolutionInHours )
	{
		List<TimeRange> retTimes = new LinkedList<TimeRange>();
		Instant fromTime;
		Instant toTime;
		Instant newToTime;
		Instant newFromTime;
		
		for( TimeRange time : times ){
			fromTime = time.getFrom();
			toTime = time.getTo();
			
			
			if( fromTime == null || toTime == null ) {
				if( fromTime == null )
					fromTime = toTime;
				else 
					toTime = fromTime;
				
				retTimes.add( new TimeRange( fromTime ,toTime ) );
				continue;
			}


			newToTime = fromTime;
			newFromTime = fromTime;
			
			while( true ) {
				newToTime = newToTime.plus(Duration.ofHours(resolutionInHours));

				if( newToTime.compareTo( toTime ) < 0 ) {
					retTimes.add(  new TimeRange( newFromTime, newToTime ) );
					newFromTime = newToTime;
					newFromTime = newFromTime.plus(Duration.ofHours(1));
				} else {
					retTimes.add(  new TimeRange( newFromTime, toTime ) );
					break;
				}
			}
		}
		
		return retTimes;		
	}
}
