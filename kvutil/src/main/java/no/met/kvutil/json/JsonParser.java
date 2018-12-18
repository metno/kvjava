/*
  Kvalobs - Free Quality Control Software for Meteorological Observations

  $Id$

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
package no.met.kvutil.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import no.met.kvutil.FileUtil;

import java.io.IOException;
import java.io.Reader;

public class JsonParser {

    JsonContext context=new JsonContext();

    public JsonParser() {}


    public boolean parse(String filename) throws IllegalStateException, IOException, NumberFormatException {
        context.clear();
        Reader reader = FileUtil.getFileReader(filename);

        if( reader==null)
            return false;

        JsonReader jr = new JsonReader(reader);
        if( jr==null) {
            System.out.println("Failed to get an JsonReader");
            return false;
        }

        jr.setLenient(true);
        String currentName="";
        JsonToken token;
        boolean done=false;
        while (! done /*jr.hasNext()*/) {
            token = jr.peek();
            //System.out.println("Token: " + token);
            switch (token) {
                case BEGIN_ARRAY:
                    jr.beginArray();
                    context.startArray(currentName);
                    currentName="";
                    beginArray(context.path());
                    break;

                case BEGIN_OBJECT:
                    jr.beginObject();
                    context.startObject(currentName);
                    currentName="";
                    beginObject(context.path());
                    break;

                case BOOLEAN:
                    boolean bv=jr.nextBoolean();
                    String v=bv?"true":"false";

                    //System.out.println(context.path() + ":" + currentName + " BOOLEAN = " + bv + " ("+ jr.getPath()+")");
                    if( context.inObject())
                        objectValue(context.path(), currentName, v, false);
                    else
                        arrayValue(context.path(), v, false);
                    currentName="";
                    break;

                case END_ARRAY:
                    jr.endArray();
                    endArray(context.path());
                    context.endArray();
                    currentName="";
                    break;

                case END_DOCUMENT:
                    currentName="";
                    done=true;
                    break;

                case END_OBJECT:
                    jr.endObject();
                    endObject(context.path());
                    context.endObject();
                    currentName="";
                    break;

                case NAME:
                    currentName = jr.nextName();
                    break;

                case NULL:
                    jr.nextNull();
                    if( context.inObject())
                        objectValue(context.path(), currentName, null, false);
                    else
                        arrayValue(context.path(), null, false);
                    currentName="";
                    break;

                case NUMBER:
                    String n = jr.nextString();
                    if( context.inObject())
                        objectValue(context.path(), currentName, n, true);
                    else
                        arrayValue(context.path(), n, true);
                    currentName="";
                    break;

                case STRING:
                    String sv=jr.nextString();
                    if( context.inObject())
                        objectValue(context.path(), currentName, sv, false);
                    else
                        arrayValue(context.path(), sv, false);
                    currentName="";
                    break;

                 default:
                     System.out.println("Oh what: "+ jr.getPath());
            }
        }


        return true;
    }

    public void beginObject(String path) {
        System.out.println("beginObject: " + path);
    }

    public void endObject(String path) {
        System.out.println("endObject: " + path);
    }

    public void beginArray(String path) {
        System.out.println("beginArray: " + path);
    }

    public void endArray(String path) {
        System.out.println("endArray: " + path);
    }

    public void objectValue(String path, String key, String value, boolean isNumber) {
        if( value == null)
            value = "(null)";
        System.out.println("objectValue: " + path + ": " + key + "='" +value +"' isNumber: "+isNumber);
    }

    public void arrayValue(String path, String value, boolean isNumber) {
        if( value == null)
            value = "(null)";
        System.out.println("arrayValue: " + path + ": '" +value +"' isNumber: "+isNumber);
    }

}