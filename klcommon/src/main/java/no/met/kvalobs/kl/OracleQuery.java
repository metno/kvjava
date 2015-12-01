package no.met.kvalobs.kl;

import java.time.Instant;
import org.apache.log4j.Logger;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;

public class OracleQuery implements IQuery {
	static Logger logger=Logger.getLogger(SqlInsertHelper.class);
	private String textDataTable;
	private String dataTable;
	
	public OracleQuery(){
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
	
	public OracleQuery( String dataTableName, String textDataTableName ){
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
		logger.debug("Update a Oracle database!sid: "+elem.stationID+" tid: " + 
	             elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
	   	String query="UPDATE "+ getDataTableName() +
	   				 " SET " +
	   				 "  original="+elem.original + "," +
	   				 "  kvstamp=to_date('"+elem.tbtime+"','yyyy-mm-dd hh24:mi:ss')," +
	   				 "  useinfo='"+elem.useinfo + "',"+
	   				 "  corrected="+elem.corrected + "," +
	   				 "  controlinfo='"+elem.controlinfo+"'," +
	   				 "  cfailed='"+elem.cfailed+"' " +
	   				 "WHERE " +
	   				 "  stnr=" + elem.stationID + " AND " +
	   				 "  dato=to_date('" + elem.obstime + "','yyyy-mm-dd hh24:mi:ss') AND " +
	   				 "  paramid=" + elem.paramID + " AND " +
	   				 "  typeid=" + elem.typeID + " AND " +
	   				 "  xlevel=" + elem.level + " AND " +
	   				 "  sensor=" + elem.sensor;
	   	return query;
	}
	
	public String createDataInsertQuery( DataElem elem){
		logger.debug("Insert data into a Oracle database! sid: "+elem.stationID+" tid: " + 
	             elem.typeID + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
	   	String query="insert into " + getDataTableName() + "(stnr,dato,original,kvstamp,paramid,typeid,xlevel,sensor,useinfo,corrected,controlinfo,cfailed) values ("
	   				 +elem.stationID+",to_date('"
	   				 +elem.obstime+"','yyyy-mm-dd hh24:mi:ss'),"
	   				 +elem.original+",to_date('"
	   				 +elem.tbtime+"','yyyy-mm-dd hh24:mi:ss'),"
	   				 +elem.paramID+","
	   				 +elem.level  +","
	   				 +elem.sensor  +",'"
	   				 +elem.useinfo +"',"
	   				 +elem.corrected+",'"
	   				 +elem.controlinfo+"','"
	   				 +elem.cfailed+"')";
	   	return query;
	}
	
	public String createTextDataUpdateQuery( TextDataElem elem ){
		logger.debug("Update textData in a Oracle database! sid: "+elem.stationID+" tid: " + 
	             elem.typeID_ + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
	   	String query="UPDATE "+ getTextDataTableName() +
	   				 " SET " +
	   				 "  original='"+elem.original + "'," +
	   				 "  tbtime=to_date('"+elem.tbtime+"','yyyy-mm-dd hh24:mi:ss')" +
	   				 "WHERE " +
	   				 "  stationid=" + elem.stationID + " AND " +
	   				 "  obstime=to_date('" + elem.obstime + "','yyyy-mm-dd hh24:mi:ss') AND " +
	   				 "  paramid=" + elem.paramID + " AND " +
	   				 "  typeid=" + elem.typeID_;
	   	return query;
	}
	
	public String createTextDataInsertQuery(TextDataElem elem){
		logger.debug("Insert textData into a Oracle database! sid: "+elem.stationID+" tid: " + 
	             elem.typeID_ + " pid: " + elem.paramID+ " obstime: '"+elem.obstime+"' tbtime: "+elem.tbtime);
	   	String query="insert into " + getTextDataTableName() + "(stationid,obstime,original,paramid,tbtime,typeid) values ("
	   				 +elem.stationID+",to_date('"
	   				 +elem.obstime+"','yyyy-mm-dd hh24:mi:ss'),'"
	   				 +elem.original+"',"
	   				 +elem.paramID+",to_date('"
	   				 +elem.tbtime+"','yyyy-mm-dd hh24:mi:ss'),"
	   				 +elem.typeID_
	   				 +")";
	   	return query;
	}
	
	@Override
	public String dateString( Instant time ) {
		String out = "to_date('" + time + "','yyyy-mm-dd hh24:mi:ss')";
		logger.debug("Oracle  time string! in '" + time +" out: '"+out+"'.");
		return out;
	}
}
