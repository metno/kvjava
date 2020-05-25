/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Station.java,v 1.1.2.2 2007/09/27 09:02:19 paule Exp $                                                       

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
package no.met.kvalobs.kl;

import java.lang.Integer;
import java.util.List;
import java.util.Vector;

public class Range {

	int first;
	int last;
	
	public Range(){
		first=0;
		last=0;
	}
	
	public Range(String first) {
		try {
			this.first=Integer.parseInt(first);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			this.first=-1;
		}
		last=0;
	}
	
	public Range(String first, String last){

		try {
			this.first=Integer.parseInt(first);
			this.last=Integer.parseInt(last);
			
			if(this.last<this.first){
				int t=this.last;
				this.last=this.first;
				this.first=t;
			}
			
			if( this.first <= 0 || this.last <= 0 ) {
				this.first=-1;
				this.last=-1;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			this.first=-1;
			this.last=-1;
		}
	}

	public boolean isInterval(){ return first>=0 && last>0;}
	
	public boolean contains(int id){
		if(isInterval())
			return id>=first && id<=last;
		
		return id==first;
	}
	
	public boolean ok(){ return first>-1 && last>-1;}

	public int getFirst(){ return first;}
	public int getLast(){return last;}
		
	public String toString(){
		if(first==0 && last==0) {
			return "all";
		} else if ( last==0 ) {
			return Integer.toString(first);
		} else {
			return Integer.toString(first) + "-" + Integer.toString(last);
		}
	}
	
	static public String toString( Range[] rangeList){
		String r=null;
		for(Range s : rangeList)
			if(r==null)
				r=s.toString();
			else
				r+=","+s.toString();
		
		return r;
	}
	
	/**
	 * Take a list of strings, where each string specifies
	 * a element or a range of elements.
	 * 
	 * Format of the string:
	 * rangespec: element | elementrange
	 * elementtange: element - element
	 * 
	 * Ex.
	 *  1. A station 14000
	 *  2. An interval 18700-20000
	 *  
	 * @param rangeList A list of range and/or elements
	 * @return A list of Range elements intervals.
	 */
	public static Range[] ranges( List<String> rangeList ){
		if(rangeList==null || rangeList.size()==0){
			Range[] stl=new Range[1];
			stl[0]=new Range();
			return stl;
		}
		
		Vector<Range> stv=new Vector<Range>();
		
		for(String s : (List<String>)rangeList ){
			String[] e=s.split("-");
		
			if(e.length==0) //s is an empty string
				continue;
		
			if(e.length>2) //An invalid specification
				return null;
		
			Range myst;
			
			if(e.length==1)
				myst=new Range(e[0]);
			else
				myst=new Range(e[0], e[1]);
			
			if(!myst.ok())
				return null;
			
			stv.add(myst);
		}
		
		Range[] myst=new Range[stv.size()];
		
		return stv.toArray(myst);
	}
	
}
