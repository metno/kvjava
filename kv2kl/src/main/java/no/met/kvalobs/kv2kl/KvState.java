/*
  Kvalobs - Free Quality Control Software for Meteorological Observations

  $Id: Kv2KlApp.java,v 1.1.2.11 2007/09/27 09:02:19 paule Exp $

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

import no.met.kvutil.PropertiesHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class KvState {
    Properties stateProp;
    KvConfig conf;
    Path statefile;
    String kafkaGroup;
    boolean hasChanges;
    static KvState kvState;

    public KvState(KvConfig conf) {
        this.conf = conf;
        hasChanges = false;
    }

    void readStateFromFile(Path statefile) throws IOException {
        this.statefile=statefile;
        stateProp = new Properties();
        if( Files.exists(statefile) && Files.isReadable(statefile)){
            InputStream in = Files.newInputStream(statefile);
            stateProp.load(in);
            in.close();
        }
        hasChanges = false;
        initState(stateProp);
    }

    void initState(Properties stateProp) {
        kafkaGroup=stateProp.getProperty("kafka.group.id", "");
        if(kafkaGroup.isEmpty())
            createKafkaGroupId();

        saveState();
    }


    void createKafkaGroupId() {
        //Create a unique id on the form hostname_appName_user_UID

        String host= null;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            host = "unknown-host";
        }
        String user=System.getProperty("user.name");
        String uuid = UUID.randomUUID().toString();
        host = host.split("\\.")[0];
        kafkaGroup = host + "_" + user +"_" + uuid;
        stateProp.setProperty("kafka.group.id", kafkaGroup);
        hasChanges = true;
    }

    static public KvState loadState(KvConfig conf) {
        kvState = new KvState(conf);
        try {
            kvState.readStateFromFile(conf.getStatePath());
            //Set the kafkagroup variable in the conf file.
            conf.conf.setProperty("kafka.group.id", kvState.getKafkaGroup());
        }
        catch( IOException ex) {
            System.err.println("Cant read state file '"+conf.getStatePath()+"'. It may be corrupt, remove it. ("+ex.getMessage()+")");
            System.exit(1);;
        }
        return kvState;
    }

    public void saveState() {
        try {
            if( !hasChanges)
                return;
            if(statefile!=null) {
                OutputStream out = Files.newOutputStream(statefile);
                stateProp.store(out, null);
                out.close();
                hasChanges=false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getKafkaGroup() {
        return kafkaGroup;
    }
}
