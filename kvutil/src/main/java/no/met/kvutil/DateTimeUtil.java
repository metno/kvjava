/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: MiTime.java,v 1.1.2.4 2007/09/27 09:02:43 paule Exp $                                                       

  Copyright (C) 2007 met.no

  Contact information:
  Norwegian Meteorological Institute
  Box 43 Blindern
  0313 OSLO
  NORWAY
  email: kvalobs-dev@met.no

  This file is part of KVALOBS

  KVALOBS is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 
  of the License, or (at your option) any later version.
  
  KVALOBS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License along 
  with KVALOBS; if not, write to the Free Software Foundation Inc., 
  51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package no.met.kvutil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeUtil {
	public static DateTimeFormatter FMT_PARSE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");
	public static DateTimeFormatter FMT_PARSE_xx = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssxx");
    public static DateTimeFormatter FMT_PARSE_DESIMAL_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSx");
    public static DateTimeFormatter FMT_PARSE_DESIMAL_SECOND_xx = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSxx");

	public static DateTimeFormatter ISO_WITH_MICRO_SECOND_xx = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSxx");

	/** yyyy-MM-dd HH:mm:ss */
	public static final String FMT_ISO = "yyyy-MM-dd HH:mm:ss";

	/** yyyy-MM-dd HH:mm:ss z */
	public static final String FMT_ISO_z = "yyyy-MM-dd HH:mm:ssZ";

	/** yyyy-MM-dd HH:mm:ss.SSS */
	public static final String FMT_ISO_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

	/** yyyyMMddHHmmss */
	public static final String FMT_COMPACT_TIMESTAMP = "yyyyMMddHHmmss";
	/** yyyyMMddHHmm */
	public static final String FMT_COMPACT_TIMESTAMP_1 = "yyyyMMddHHmm";
	/** yyyyMMddHH */
	public static final String FMT_COMPACT_TIMESTAMP_2 = "yyyyMMddHH";

	static Pattern datePattern = Pattern
			.compile("^ *(\\d{4})-(\\d{1,2})-(\\d{1,2})((T| +)(\\d{1,2})(:\\d{1,2}){0,2})? *");


	static public OffsetDateTime plusMicosecond(OffsetDateTime dt, long microSecond) {
		return dt.plusNanos(1000*microSecond);
	}
	static public OffsetDateTime minusMicosecond(OffsetDateTime dt, long microSecond) {
		return dt.minusNanos(1000*microSecond);
	}

	/**
	 * Create a string representation of the date in the given format, fmt.
	 * 
	 * @param format
	 *            The format we want the string in.
	 * @return An string in the format given by fmt.
	 * 
	 * @see java.time.format.DateTimeFormatter for an description of fmt.
	 */
	static public String toString(OffsetDateTime dt, String format) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return dt.format(fmt);
	}

	static public String toString(ZonedDateTime dt, String format) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return dt.format(fmt);
	}

	static public String toString(LocalDateTime dt, String format) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return dt.format(fmt);
	}


	static public String toString(OffsetDateTime dt, DateTimeFormatter fmt) {
		return dt.format(fmt);
	}

	static public String toString(ZonedDateTime dt, DateTimeFormatter fmt) {
		return dt.format(fmt);
	}

	static public String toString(LocalDateTime dt, DateTimeFormatter fmt) {
		return dt.format(fmt);
	}

	static public String toString(OffsetDateTime dt) {
		return dt.format(DateTimeFormatter.ISO_INSTANT);
	}

	static public String toString(ZonedDateTime dt) {
		return dt.format(DateTimeFormatter.ISO_INSTANT);
	}

	static public String toString(LocalDateTime dt) {
		return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	static public String toString(Instant dt, DateTimeFormatter fmt) {
		return dt.atOffset(ZoneOffset.UTC).format(fmt);
	}

	static public String nowToString() {
		return toString(Instant.now(), FMT_PARSE);
	}
	static public String nowToString(DateTimeFormatter fmt) {
		return toString(Instant.now(), fmt);
	}

	static public OffsetDateTime parse(String dt, String format) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		return OffsetDateTime.parse(dt, fmt);
	}

	static public OffsetDateTime parse(String dt, DateTimeFormatter fmt) {
		return OffsetDateTime.parse(dt, fmt);
	}

	static public int firstNotIn(int startAt, String str, String validSet) {
		int n = str.length();
		
		if (startAt >= n)
			return -1;

		for( int i = startAt; i<n; i++ ) {
			if( ! validSet.contains(str.subSequence(i, i+1)))
				return i;
		}
		return n;
	}

	static int getDesimalSecondsAsMs(StringHolder res) {
		String ts=res.getValue();
		int point=ts.indexOf('.');
		
		if( point < 0 ) 
			return 0;
		
		int i = firstNotIn(point+1, ts, "0123456789");
		
		if( i < 0 )
			return 0;
		
		//Remove the decimal point part.
		String sub = ts.substring(0, point);
		
		if( i < ts.length())
			sub.concat(ts.substring(i));
		
		res.setValue(sub);
		
		sub=ts.substring(point+1, i);
		
		return Math.round(Float.parseFloat("0."+sub)*1000);
	}
	
	/**
	 * 
	 * @param timestamp
	 *            time as a string
	 * @return True if the timestamp could be parsed as a timestamp in
	 *         accordance with fmt. False otherwise.
	 * @throws java.time.format.DateTimeParseException
	 * @see java.text.SimpleDateFormat for an description of fmt.
	 */
	public static OffsetDateTime parse(String timestamp) {
		String ts=timestamp.trim();
		ts = timestamp.replace('T', ' ');
		ts = ts.replace("z", "+00");
		ts = ts.replace("Z", "+00");
			

		if (ts.lastIndexOf('+') < 0 && ts.lastIndexOf('-') <= 9)
			ts = ts + "+00";

		//System.err.println("DateTimeUtil.parse: '" + ts +"' ("+timestamp+")");
		return OffsetDateTime.parse(ts, FMT_PARSE);
	}
	
	public static Instant parseToInstant(String timestamp) {
		
		StringHolder ts=new StringHolder(timestamp);
		int millis=getDesimalSecondsAsMs(ts);
		OffsetDateTime dt=parse(ts.getValue());
		
		return dt.toInstant().plusMillis(millis); 
	}


	static public Instant parseOptHms(String time, StringBuilder remaining) {
		int year, month, day, hour, min, second;

		if (time == null) {
			System.out.println("No time string (time==null)!");
			return null;
		}

		if (remaining != null)
			remaining.delete(0, remaining.length());

		Matcher match = datePattern.matcher(time);

		hour = 0;
		min = 0;
		second = 0;
		System.out.println("Match: '" + time + "'");

		if (!match.lookingAt()) {
			System.out.println("NOT a valid timestamp, '" + time + "'!");
			return null;
		}

		if (remaining != null) {
			remaining.delete(0, remaining.length());
			String matched = match.group();
			String notMatched = time.substring(matched.length());
			notMatched = notMatched.trim();
			remaining.append(notMatched);
		}

		year = Integer.parseInt(match.group(1));
		month = Integer.parseInt(match.group(2));
		day = Integer.parseInt(match.group(3));
		time = match.group(4);

		if (time != null) {
			String hms[] = time.substring(1).split(":");

			if (hms.length > 0)
				hour = Integer.parseInt(hms[0]);

			if (hms.length > 1)
				min = Integer.parseInt(hms[1]);

			if (hms.length > 2)
				second = Integer.parseInt(hms[2]);
		}

		if (month < 1 || month > 12) {
			System.out.println(" Invalid month: '" + month + "' must be in range [1,12].");
			return null;
		}

		if (day < 1 || day > 31) {
			System.out.println(" Invalid day: '" + day + "' must be in range [1,31].");
			return null;
		}

		if (hour < 0 || hour > 23) {
			System.out.println(" Invalid hour: '" + hour + "' must be in range [0,23].");
			return null;
		}

		if (min < 0 || min > 23) {
			System.out.println(" Invalid min: '" + min + "' must be in range [0,59].");
			return null;
		}

		if (second < 0 || second > 23) {
			System.out.println(" Invalid second: '" + second + "' must be in range [0,59].");
			return null;
		}

		return Instant.from(OffsetDateTime.of(year, month, day, hour, min, second, 0, ZoneOffset.UTC));
	}

}
