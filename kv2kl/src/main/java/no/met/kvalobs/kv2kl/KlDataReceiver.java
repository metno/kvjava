/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: KlDataReceiver.java,v 1.1.2.8 2007/09/27 09:02:19 paule Exp $                                                       

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
package no.met.kvalobs.kv2kl;

import no.met.kvalobs.kl.KlInsertHelper;
import no.met.kvclient.KvDataEvent;
import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.service.ObsDataList;

import org.apache.log4j.Logger;

public class KlDataReceiver implements KvDataEventListener {

    Kv2KlApp app;
    KlInsertHelper insertstmt;
    boolean saveDataToDb = true;
    KvState kvState;
    static Logger logger = Logger.getLogger(KlDataReceiver.class);
    static Logger filterlog = Logger.getLogger("filter");


    public KlDataReceiver(Kv2KlApp app, String backupfile) {
        this(app, null,backupfile, true, true);
    }

    public KlDataReceiver(Kv2KlApp app, KvState kvState, String backupfile, boolean enableFilter, boolean saveDataToDb) {
        this.app = app;
        this.saveDataToDb = saveDataToDb;
        this.kvState = kvState;
        if (!this.saveDataToDb)
            return;

        insertstmt = new KlInsertHelper(app.getKlConnectionMgr(), kvState, backupfile, enableFilter);
        insertstmt.setDataTableName(app.getDataTableName());
        insertstmt.setTextDataTableName(app.getTextDataTableName());
        insertstmt.setForeignDataTable(app.getForeignDataTableName());
        insertstmt.setForeignTextDataTable(app.getForeignTextDataTableName());
    }

    public void kvDataEvent(KvDataEvent event) {
        ObsDataList obsData = event.getObsData();
        if( kvState !=null )
            kvState.updateLastReceivedMessageTime();

        if (saveDataToDb)
            insertstmt.insertData(obsData, null);
        else // This is typical for debug sessions so just write the data to standard out.
            System.out.println(obsData);
    }
}


	
    
