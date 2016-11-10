package no.met.kvalobs.kl;

import java.time.Instant;
import java.util.Iterator;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.DataElemList;
import no.met.kvclient.service.TextDataElem;
import no.met.kvclient.service.TextDataElemList;
import no.met.kvutil.dbutil.IExec;

public class KlDataHelper {
    IKlSql query=null;
    String dbdriver=null;
    char ctlinfo[] = new char[16];
    DataElemList dataElem=null;
    TextDataElemList textDataElem=null;
    DataElem currentDataElem=null;
    TextDataElem currentTextDataElem=null;
    Iterator<DataElem> itData;
    Iterator<TextDataElem> itTextData;
    String foreignDataTable=null;
    String foreignTextDataTable=null;

    public KlDataHelper( String dbdriver ){
        this( dbdriver, null, null, null, null );
    }

    public KlDataHelper( String dbdriver,
                       String dataTableName,
                       String textDataTableName ) {
        this( dbdriver, dataTableName, textDataTableName, null, null );
    }

    public KlDataHelper( String dbdriver,
                       String dataTableName,
                       String textDataTableName,
                       String foreignDataTable,
                       String foreignTextDataTable){
        this.foreignDataTable = foreignDataTable;
        this.foreignTextDataTable = foreignTextDataTable;

        if( this.foreignDataTable=="" )
            this.foreignDataTable = null;

        if( this.foreignTextDataTable=="" )
            this.foreignTextDataTable = null;

        query = KlSqlFactory.createQuery( dbdriver, dataTableName, textDataTableName );
    }

    public KlDataHelper(DataElemList dataElem,
                      TextDataElemList textDataElem,
                      String dbdriver ){
        this( dbdriver );
        init( dataElem, textDataElem );
    }

    public void init( DataElemList dataElem,
                      TextDataElemList textDataElem ){
        this.dataElem = dataElem;
        this.textDataElem = textDataElem;
        itData = null;
        itTextData = null;
        currentDataElem = null;
        currentTextDataElem = null;

        if( this.dataElem != null)
            itData = this.dataElem.iterator();

        if( this.textDataElem != null )
            itTextData = this.textDataElem.iterator();

        //index = -1;
    }

    /**
     * For foreign stations, ie stations with stationid>100000,
     * we insert the data in the foreign table if it exist. If it do
     * not exist we ignore the data.
     *
     * @return the insert query.
     */
    public IExec createInsertQuery(){
        IExec retQ = null;
        String oldtable=null;
        boolean isForeign=false;

        if( currentDataElem != null) {
            if( currentDataElem.stationID > 100000 ) {
                if( foreignDataTable != null ) {
                    isForeign = true;
                    oldtable = query.setDataTableName( foreignDataTable );
                }
            }

            retQ = query.createDataInsertQuery( currentDataElem );

            if( isForeign )
                query.setDataTableName( oldtable );
        } else if( currentTextDataElem!= null) {
            if( currentTextDataElem.stationID > 100000 ) {
                if( foreignTextDataTable != null ) {
                    isForeign = true;
                    query.setTextDataTableName( foreignTextDataTable );
                }
            }

            retQ = query.createTextDataInsertQuery( currentTextDataElem );

            if( isForeign )
                query.setTextDataTableName( oldtable );
        }

        return retQ;
    }

    public IExec createUpdateQuery(){
        IExec retQ = null;
        String oldtable=null;
        boolean isForeign=false;

        if( currentDataElem != null) {
            if( currentDataElem.stationID > 100000 ) {
                if( foreignDataTable != null ) {
                    oldtable = query.setDataTableName( foreignDataTable );
                    isForeign = true;
                }
            }

            retQ = query.createDataUpdateQuery( currentDataElem );

            if( isForeign )
                query.setDataTableName( oldtable );

        } else if( currentDataElem != null) {
            if( currentDataElem.stationID > 100000 ) {
                if( foreignTextDataTable != null ) {
                    oldtable = query.setTextDataTableName( foreignTextDataTable);
                    isForeign = true;
                }
            }

            retQ = query.createTextDataUpdateQuery( currentTextDataElem );

            if( isForeign )
                query.setTextDataTableName( oldtable );

        }

        return retQ;
    }


    public boolean next() {
        currentDataElem = null;
        currentTextDataElem=null;

        while( itData != null && itData.hasNext()) {
            currentDataElem = itData.next();
            //fhqc == 3 is an hack to disable running of qc1. If it is set
            //skip this element.
            if( currentDataElem.controlinfo != null && currentDataElem.controlinfo.length() >= 16 ) {
                currentDataElem.controlinfo.getChars( 0, 16, ctlinfo, 0 );

                if( ctlinfo[15] == '3' ) {
                    continue;
                }
            }

            return true;
        }

        itData = null;

        if( itTextData != null && itTextData.hasNext()) {
            currentTextDataElem = itTextData.next();
        }

        itTextData=null;
        return false;
    }

    public long getStationID() throws NoData {
        if( currentDataElem != null )
            return currentDataElem.stationID;

        if( currentTextDataElem != null )
            return currentTextDataElem.stationID;

        throw new NoData();
    }

    public void setStationID( long sid  ){
        if( currentDataElem != null ) {
            currentDataElem.stationID = sid;
            return;
        }

        if( currentTextDataElem != null ) {
            currentTextDataElem.stationID = sid;
            return;
        }
    }

    public long getTypeID()throws NoData {
        if( dataElem != null )
            return currentDataElem.typeID;

        if( currentTextDataElem != null )
            return currentTextDataElem.typeID_;

        throw new NoData();
    }

    public int getParamID()throws NoData {
        if( currentDataElem != null )
            return currentDataElem.paramID;

        if( textDataElem != null )
            return currentTextDataElem.paramID;

        throw new NoData();
    }

    public int getLevel() throws NoData{
        if( currentDataElem != null )
            return currentDataElem.level;

        if( currentTextDataElem != null )
            return 0;

        throw new NoData();
    }

    public int getSensor() throws NoData{
        if( currentDataElem != null )
            return currentDataElem.sensor;

        if( currentTextDataElem != null )
            return 0;

        throw new NoData();
    }


    public Instant getObstime() throws NoData{
        if( currentDataElem != null )
            return currentDataElem.obstime;

        if( currentTextDataElem != null )
            return currentTextDataElem.obstime;

        throw new NoData();
    }

    public boolean useLevelAndSensor()throws NoData {
        if( currentDataElem != null )
            return true;

        if( currentTextDataElem != null )
            return false;

        throw new NoData();
    }

}


