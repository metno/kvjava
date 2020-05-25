/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: Kl2KvApp.java,v 1.1.2.6 2007/09/27 09:02:19 paule Exp $                                                       

  Copyright (C) 2007 met.no

  Contact information:
  Norwegian Meteorological Institute
  Box 43 Blindern
  0313 OSLO
  NORWAY
  email: kvalobs-dev@met.no

  This file is part of KVALOBS

  KVALOBS is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 
  of the License, or (at your option) any later version.
  
  KVALOBS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License along 
  with KVALOBS; if not, write to the Free Software Foundation Inc., 
  51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package no.met.kvalobs.kl2kv;

import no.met.kvalobs.kl.KlApp;
import no.met.kvutil.PropertiesHelper;
import no.met.kvalobs.kl.IKlSql;
import no.met.kvalobs.kl.KlSqlFactory;

import org.apache.log4j.Logger;

public class Kl2KvApp extends KlApp
{

	static Logger logger=Logger.getLogger(Kl2KvApp.class);
	public enum Kl2KvTblType {KLTblType, Kv2KvTblType};
    String stations;
    String Kv2KvTablename="T_KL2KVALOBS_HIST";
    String KlTablename="kl2kvalobs";
    String tablename;
    static IKlSql sqlHelper=null;

    public Kl2KvApp(String[] args, PropertiesHelper prop, Kl2KvTblType tblType){
    	super(args, prop, false, true);
    	
    	PropertiesHelper conf=getConf();
    	
    	String dbdriver=conf.getProperty("kl.dbdriver");
        String datatable=conf.getProperty("kl.datatable","kv2klima");
        String textdatatable=conf.getProperty("kl.textdatatable","T_TEXT_DATA");
        Kv2KvTablename=conf.getProperty("kl2kv.table.kv2kv", "T_KL2KVALOBS_HIST");
        KlTablename=conf.getProperty("kl2kv.table.kl", "kl2kvalobs");

        if( tblType == Kl2KvTblType.KLTblType)
            tablename = KlTablename;
        else
            tablename = Kv2KvTablename;

        sqlHelper=KlSqlFactory.createQuery(dbdriver,datatable,textdatatable);
    }
    
    public String getTablename(){
    	return tablename;
    }

    static public IKlSql getSqlHelper() {
        return sqlHelper;
    }
}
