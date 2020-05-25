package no.met.kvalobs.kl;
import java.time.Instant;

import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;

public interface IQuery {
	
	public String getDataTableName();
	public String getTextDataTableName();
	public String setDataTableName( String tableName );
	public String setTextDataTableName( String tableName );
	
	/*Data*/
	public String createDataUpdateQuery( DataElem elem );
	public String createDataInsertQuery(DataElem elem);

	/*TextData*/
	public String createTextDataUpdateQuery( TextDataElem elem );
	public String createTextDataInsertQuery(TextDataElem elem);
	
	/*Helper*/
	public String dateString( Instant time );
}
