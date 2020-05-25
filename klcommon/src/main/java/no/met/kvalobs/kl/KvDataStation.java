package no.met.kvalobs.kl;

import java.util.Iterator;
import java.util.TreeSet;

public class KvDataStation implements Comparable<KvDataStation> {
	long station;
	TreeSet<KvDataType> container = null;
	
	public KvDataStation( long station )
	{
		this.station= station;

		try {
			container = new TreeSet<KvDataType>(); 
		}
		catch( NullPointerException e) {
			container = null;
		}
	}
	
	public int compareTo( KvDataStation station ) {
		return (int)(this.station -station.station);
	}
	
	public KvDataType findType( long typeid ) {
		Iterator<KvDataType> it = container.iterator();
		KvDataType type;
		
		while( it.hasNext() ) {
			type = it.next();
			
			if( type.getType() == typeid )
				return type;
		}
		
		return null;
	}
	
	public long getStation() {
		return station;
	}

	public boolean add( KvDataType type ) {
		if(container == null )
			return false;
		
		return container.add( type );
	}
	
	Iterator<KvDataType> iterator() {
		if( container == null )
			return null;
		
		return container.iterator();
	}
}
