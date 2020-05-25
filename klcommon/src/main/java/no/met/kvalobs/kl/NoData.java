package no.met.kvalobs.kl;

import java.lang.Exception;

public class NoData extends Exception{
	private static final long serialVersionUID = 2615461521734710578L;

	public NoData(){
		super( "EXCEPTION: No more data");
	}
	
	public NoData( String msg ){
		super( msg );
	}
	
}
