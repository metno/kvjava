package metno.kvalobs.kl;

public class DataHelper {
	IQuery query=null;
	String dbdriver=null;
	char ctlinfo[] = new char[16];
	CKvalObs.CService.DataElem[] dataElem=null;
	CKvalObs.CService.TextDataElem[] textDataElem=null;
	String foreignDataTable=null;
	String foreignTextDataTable=null;
	int index=-1;
	

	public DataHelper( String dbdriver ){
		this( dbdriver, null, null, null, null );
	}
	
	public DataHelper( String dbdriver, 
			   		   String dataTableName, 
					   String textDataTableName ) {
		this( dbdriver, dataTableName, textDataTableName, null, null );
	}
	
	public DataHelper( String dbdriver, 
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
		
		query = QueryFactory.createQuery( dbdriver, dataTableName, textDataTableName );
	}
	
	public DataHelper(CKvalObs.CService.DataElem[] dataElem, 
			          CKvalObs.CService.TextDataElem[] textDataElem,
			          String dbdriver ){
		this( dbdriver );
		init( dataElem, textDataElem );
	}
	
	public void init( CKvalObs.CService.DataElem[] dataElem, 
	                  CKvalObs.CService.TextDataElem[] textDataElem ){
		this.dataElem = dataElem;
		this.textDataElem = textDataElem;
		index = -1;
	}
	
	/**
	 * For foreign stations, ie stations with stationid>100000,
	 * we insert the data in the foreign table if it exist. If it do
	 * not exist we ignore the data.
	 * 
	 * @return the insert query.
	 */
	public String createInsertQuery(){
		String retQ = null;
		String oldtable=null;
		boolean isForeign=false;
		
		if( dataElem != null) {
			if( dataElem[index].stationID > 100000 ) {
				if( foreignDataTable != null ) {
					isForeign = true;
					oldtable = query.setDataTableName( foreignDataTable );
				}
			}
			
			retQ = query.createDataInsertQuery( dataElem[index] );
		
			if( isForeign )
				query.setDataTableName( oldtable );
		} else if( textDataElem != null) {
			if( textDataElem[index].stationID > 100000 ) {
				if( foreignTextDataTable != null ) {
					isForeign = true;
					query.setTextDataTableName( foreignTextDataTable );
				}
			} 
			
			retQ = query.createTextDataInsertQuery( textDataElem[index] );
			
			if( isForeign )
				query.setTextDataTableName( oldtable );
		}
		
		
		return retQ;
	}

	public String createUpdateQuery(){
		String retQ = null;
		String oldtable=null;
		boolean isForeign=false;
		
		if( dataElem != null) {
			if( dataElem[index].stationID > 100000 ) {
				if( foreignDataTable != null ) {
					oldtable = query.setDataTableName( foreignDataTable );
					isForeign = true;
				}
			} 
		
			retQ = query.createDataUpdateQuery( dataElem[index] );
			
			if( isForeign )
				query.setDataTableName( oldtable );
			
		} else if( textDataElem != null) {
			if( textDataElem[index].stationID > 100000 ) {
				if( foreignTextDataTable != null ) {
					oldtable = query.setTextDataTableName( foreignTextDataTable);
					isForeign = true;
				}
			} 
			
			retQ = query.createTextDataUpdateQuery( textDataElem[index] );
			
			if( isForeign )
				query.setTextDataTableName( oldtable );
				
		}
		
		return retQ;
	}
   	
	
	public boolean next() {
		index++;

		//System.out.println(" index: " + index );
		//System.out.println(" #dataElem: "+(dataElem!=null?dataElem.length:"(null)"));
		//System.out.println(" #textDataElem: "+(textDataElem!=null?textDataElem.length:"(null)"));
		
		while( dataElem != null ) { 
			if( index >= dataElem.length ) {
				dataElem = null;
				index=0;
				continue;
			} else { 
				//fhqc == 3 is an hack to disable running of qc1. If it is set 
				//skip this element.
				if( dataElem[index].controlinfo != null && dataElem[index].controlinfo.length() >= 16 ) {
					dataElem[index].controlinfo.getChars( 0, 16, ctlinfo, 0 );
				
					if( ctlinfo[15] == '3' ) {
						index++;
						continue;
					}
				}
					
				return true;
			}
		}
		
		if( textDataElem != null ) {
			if( index >= textDataElem.length ) {
				textDataElem = null;
			} else { 
				return  true;
			}
		}
		
		return false;
	}
	
	public int getStationID() throws NoData {
		if( dataElem != null )
			return dataElem[index].stationID;
		
		if( textDataElem != null )
			return textDataElem[index].stationID;
		
		throw new NoData();
	}
	
	public void setStationID( int sid  ){
		if( dataElem != null ) {
			dataElem[index].stationID = sid;
			return;
		}
		
		if( textDataElem != null ) {
			textDataElem[index].stationID = sid;
			return;
		}
	}
	
	public short getTypeID()throws NoData {
		if( dataElem != null )
			return dataElem[index].typeID_;
		
		if( textDataElem != null )
			return textDataElem[index].typeID_;
		
		throw new NoData();
	}

	public short getParamID()throws NoData {
		if( dataElem != null )
			return dataElem[index].paramID;
		
		if( textDataElem != null )
			return textDataElem[index].paramID;
		
		throw new NoData();
	}
	
	public short getLevel() throws NoData{
		if( dataElem != null )
			return dataElem[index].level;
		
		if( textDataElem != null )
			return 0;
		
		throw new NoData();
	}
	
	public String getSensor() throws NoData{
		if( dataElem != null )
			return dataElem[index].sensor;
		
		if( textDataElem != null )
			return null;

		throw new NoData();
	}

	
	public String getObstime() throws NoData{
		if( dataElem != null )
			return dataElem[index].obstime;
		
		if( textDataElem != null )
			return textDataElem[index].obstime;

		throw new NoData();
	}

	public boolean useLevelAndSensor()throws NoData {
		if( dataElem != null )
			return true;
		
		if( textDataElem != null )
			return false;
		
		throw new NoData();
	}
	
}