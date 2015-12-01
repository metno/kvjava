package no.met.kvalobs.kl;

import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import java.time.Instant;

import org.apache.log4j.Logger;

public class DefaultQuery implements IQuery{
	static Logger logger=Logger.getLogger(DefaultQuery.class);
	
	
	private String textDataTable;
	private String dataTable;
	
	public DefaultQuery(){
		dataTable = "kv2klima";
		textDataTable = "T_TEXT_DATA";
	}
	
	public String setDataTableName( String tableName ) {
		if( tableName != null )
			return null;
		
		String old=dataTable;
		dataTable = tableName;
		return old;
	}
	
	public String setTextDataTableName( String tableName ) {
		if( tableName != null )
			return null;
		
		String old=textDataTable;
		textDataTable = tableName;
		return old;
	}
	
	public DefaultQuery( String dataTableName, String textDataTableName ){
		if( dataTableName != null )
			dataTable = dataTableName;
		else
			dataTable = "kv2klima";
		
		if( textDataTableName != null )
			textDataTable = textDataTableName;
		else
			textDataTable = "T_TEXT_DATA";
	}
	
	public String getDataTableName() {
		return dataTable;
	}
	
	public String getTextDataTableName(){
		return textDataTable;
	}

	
	
	public String createDataUpdateQuery( DataElem elem ){
		logger.debug("Update data in a SQL92 database!");
		String query="UPDATE " +getDataTableName() +
			 " SET " +
			 "  original="+elem.original + "," +
			 "  kvstamp='"+elem.tbtime+"'" +
			 "  useinfo='"+elem.useinfo + "',"+
			 "  corrected="+elem.corrected + "," +
			 "  controlinfo='"+elem.controlinfo+"'," +
			 "  cfailed='"+elem.cfailed+"' " +
			 "WHERE " +
			 "  stnr=" + elem.stationID + " AND " +
			 "  dato='" + elem.obstime + "' AND " +
			 "  paramid=" + elem.paramID + " AND " +
			 "  typeid=" + elem.typeID + " AND " +
			 "  xlevel=" + elem.level + " AND " +
			 "  sensor=" + elem.sensor;
		return query;
	}
	
	public String createDataInsertQuery( DataElem elem ){
		logger.debug("Insert into a SQL92 database!");
		String query="insert into " + getDataTableName() + "(stnr,dato,original,kvstamp,paramid,typeid,xlevel,sensor,useinfo,corrected,controlinfo,cfailed) values ("
				 +elem.stationID+","
				 +"'"+elem.obstime+"',"
				 +elem.original+","
				 +"'"+elem.tbtime+"',"
				 +elem.paramID+","
				 +elem.typeID+","
				 +elem.level  +","
				 +elem.sensor  +",'"
				 +elem.useinfo +"',"
				 +elem.corrected+",'"
				 +elem.controlinfo+"','"
				 +elem.cfailed+"')";
		return query;
	}
	
	public String createTextDataUpdateQuery( TextDataElem elem ){
		logger.debug("Update textData in a SQL92 database!");
		String query="UPDATE " + getTextDataTableName() +
			 " SET " +
			 "  original='"+elem.original + "'," +
			 "  tbtime='"+elem.tbtime+"' " +
			 "WHERE " +
			 "  stationid=" + elem.stationID + " AND " +
			 "  obstime='" + elem.obstime + "' AND " +
			 "  paramid=" + elem.paramID + " AND " +
			 "  typeid=" + elem.typeID_; 
		return query;
	}
	
	public String createTextDataInsertQuery( TextDataElem elem ) {
		logger.debug("Insert textData into a SQL92 database!");
		String query="INSERT INTO "+getTextDataTableName() + "(stationid,obstime,original,paramid,tbtime,typeid) "+
		             "values ("
				 	     +elem.stationID+","
				         +"'"+elem.obstime+"','"
				         +elem.original+"',"
				         +elem.paramID+","
				         +"'"+elem.tbtime+"',"
				         +elem.typeID_
				     +")";
		return query;
	}
	
	public String dateString( Instant time ) {
		return "'" + time +"'";
	}
}
