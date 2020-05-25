package no.met.kvalobs.kl;

import java.time.Instant;
import java.util.regex.*;

public class TimeRange {
	Instant from=null;
	Instant to=null;
	
	static Pattern datePattern = Pattern.compile(" *(\\d{4})-(\\d{1,2})-(\\d{1,2})((T| +)(\\d{1,2})(:\\d{1,2}){0,2})? *");
	
	public TimeRange( Instant from, Instant to )
	{
		if( from == null && to == null )
			return;
	
		this.from = from;
		this.to = to;

		if( from == null)
			this.from = this.to;
		else if( to == null )
			this.to = this.from;
		
		if( this.from.isAfter(this.to)) {
			Instant tmp=this.from;
			this.from = this.to;
			this.to = tmp;
		}
	}
	
	public TimeRange( Instant time )
	{
		this.from = time;
		this.to = this.from;
	}

	public Instant getFrom() {
		return from;
	}

	public Instant getTo() {
		return to;
	}
	
	public String toString() {
		if( isEqual() )
			return from.toString();
		else
			return from + " - " + to;
	}
	
	public boolean isEqual() {
		if( to == null || from == null )
			return false;
		
		return to.compareTo(from) == 0;
	}
	
}
