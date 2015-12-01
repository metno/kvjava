package no.met.kvalobs.kl;

public class QueryFactory {
	static public IQuery createQuery( String driver, String dataTableName, String textDataTableName ){
		if(driver.indexOf("oracle")>-1)
    		return new OracleQuery( dataTableName, textDataTableName );
    	else
    		return new DefaultQuery( dataTableName, textDataTableName );
	}
	
	static public IQuery createQuery( String driver ){
		return createQuery( driver, null, null );
	}
}