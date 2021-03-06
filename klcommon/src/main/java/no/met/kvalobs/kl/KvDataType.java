package no.met.kvalobs.kl;

import java.time.Instant;
import java.util.Iterator;
import java.util.TreeSet;

public class KvDataType  implements Comparable<KvDataType>{
	long type;
	TreeSet<KvDataObstime> container = null;
	
	public KvDataType( long type )
	{
		this.type = type;
		
		try {
			container = new TreeSet<KvDataObstime>(); 
		}
		catch( NullPointerException e) {
			container = null;
		}
	}
	
	public int compareTo( KvDataType type ) {
		return (int)(this.type -type.type);
	}
	
	public KvDataObstime findObstime(  Instant obstime ) {
		Iterator<KvDataObstime> it = container.iterator();
		KvDataObstime kvObstime;
		
		while( it.hasNext() ) {
			kvObstime = it.next();
			
			if( kvObstime.getObstime().compareTo( obstime ) == 0 )
				return kvObstime;
		}
		
		return null;
	}
	
	
	public long getType() {
		return type;
	}

	public boolean add( KvDataObstime obstime ) {
		if(container == null )
			return false;
		
		return container.add( obstime );
	}
	
	Iterator<KvDataObstime> iterator() {
		if( container == null )
			return null;
		
		return container.iterator();
	}
}
