package no.met.kvalobs.kl;

import org.apache.log4j.Logger;

import java.time.OffsetDateTime;


/**
 * Used to implement default common methodes in code that implements the IKlSql interface.
 */
public class KlTblNames {

    static Logger logger=Logger.getLogger(KlTblNames.class);


    private String textDataTable;
    private String dataTable;

    public KlTblNames(){
        dataTable = "kv2klima";
        textDataTable = "T_TEXT_DATA";
    }


    public KlTblNames(String dataTableName, String textDataTableName ){
        if( dataTableName != null  && !dataTableName.isEmpty())
            dataTable = dataTableName;
        else
            dataTable = "kv2klima";

        if( textDataTableName != null && !textDataTableName.isEmpty())
            textDataTable = textDataTableName;
        else
            textDataTable = "T_TEXT_DATA";
    }


    public String setDataTableName( String tableName ) {
        if( tableName == null )
            return null;

        String old=dataTable;
        dataTable = tableName;
        return old;
    }

    public String setTextDataTableName( String tableName ) {
        if( tableName == null )
            return null;

        String old=textDataTable;
        textDataTable = tableName;
        return old;
    }


    public String getDataTableName() {
        return dataTable;
    }

    public String getTextDataTableName(){
        return textDataTable;
    }

}
