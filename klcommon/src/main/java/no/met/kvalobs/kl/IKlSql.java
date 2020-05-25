package no.met.kvalobs.kl;

import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import no.met.kvutil.dbutil.IExec;

import java.time.Instant;

public interface IKlSql {
    public String getDataTableName();
    public String getTextDataTableName();
    public String setDataTableName( String tableName );
    public String setTextDataTableName( String tableName );
    public boolean isDataTableNullOrEmpty();
    public boolean isTextDataTableNullOrEmpty();

    /*Data*/
    public IExec createDataUpdateQuery(DataElem elem );
    public IExec createDataInsertQuery(DataElem elem);

    /*TextData*/
    public IExec createTextDataUpdateQuery( TextDataElem elem );
    public IExec createTextDataInsertQuery(TextDataElem elem);

    /*Helper*/
    public String dateString( Instant time );
}
