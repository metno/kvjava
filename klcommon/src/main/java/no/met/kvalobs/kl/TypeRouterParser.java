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
package no.met.kvalobs.kl;


/*
TypeRouter is used to configure which database tables each typeid should end up in.
A configuration file in json format configure this type to table routing.

{
   "kv2kl": {
      "type_to_table_filter" : {
         "table": "kl2klima",
         "text_table": "kl2klima",
         "foreign_table": "",
         "foreign_text_table": "",
         "filters": [
            {
               "name" : "svv stasjoner",
               "table": "kv2svv",
               "text_table": "kv2svv",
               "types": [500, 512, 513]
            },
            {
               "name" : "Nedbor stasjoner",
               "table": "kv2nedbor",
               "text_table": "kv2svv",
               "types": [302, 322]
            }
         ]
      }
   }
}


default_table is used if no filter element is found for a given typeid. name is only used for logging.
If no name is used the array index for the filter element is used as name as "filterN", where N is the array index.
*/


import no.met.kvutil.json.JsonParser;

public class TypeRouterParser extends JsonParser {
    String currentPath;
    TypeRouter router;
    int filterIndex=0;

    TypeRouter.RouterElement currentRE;

    public TypeRouterParser() {
    }


    public void beginObject(String path) {
        if( path.compareTo("/kv2kl/type_to_table_filter/filters@/") == 0 ){
            currentRE = new TypeRouter.RouterElement();
        }
    }

    public void endObject(String path) {
        if( path.compareTo("/kv2kl/type_to_table_filter/filters@/") == 0){
            filterIndex++;
            if( currentRE.tableName == null || currentRE.tableName.isEmpty()) {
                System.out.println("WARNING: filter element at index "+(filterIndex-1)+" do not contain a definition for table. Do not save data to DB for this definition.");
            }

            if (currentRE.types.isEmpty()) {
                System.out.println("WARNING: filter element at index "+(filterIndex-1)+" has no types or empty types definition. Ignoring this definition.");
                currentRE = null;
                return;
            }

            if (currentRE.name.isEmpty()){
                System.out.println("INFO: filter element at index "+(filterIndex-1)+" has no name or empty name definition. Using default name 'filter"+(filterIndex-1)+"'.");
                currentRE.name="filter"+(filterIndex-1);
            }

            router.addRouterElement(currentRE);
            currentRE=null;
        }
    }

    public void beginArray(String path) {
    }

    public void endArray(String path) {
    }

    public void objectValue(String path, String key, String value, boolean isNumber) {
        switch( path ) {
            case "/kv2kl/type_to_table_filter/":
                switch( key ){
                    case "table":
                        if ( value != null)
                            router.setDefaultTable(value, false);
                        break;
                    case "text_table":
                        if ( value != null)
                            router.setDefaultTextTable(value, false);
                        break;
                    case "foreign_table":
                        if ( value != null)
                            router.setForeignTable(value, false);
                        break;
                    case "foreign_text_table":
                        if ( value != null)
                            router.setForeignTextTable(value, false);
                        break;
                    case "enable_filter":
                        if ( value != null && !value.isEmpty()) {
                            boolean v = false;
                            if (value.charAt(0)== 't' || value.charAt(0)=='T') {
                                v = true;
                            }
                            router.setEnableFilter(v, false);
                        }
                        break;
                    default:
                        System.out.println("WARNING: Unknown element '"+key+"' in type_to_table_filter.");
                }
                break;
            case "/kv2kl/type_to_table_filter/filters@/":
                switch (key) {
                    case "name":
                        if ( value != null)
                            currentRE.setName(value);
                        break;
                    case "table":
                        if ( value != null)
                            currentRE.setTable(value);
                        else
                            currentRE.setTable("");
                        break;
                    case "text_table":
                        if ( value != null)
                            currentRE.setTextTable(value);
                        else
                            currentRE.setTextTable("");
                        break;
                   case "foreign_table":
                        if ( value != null)
                            currentRE.setForeignTable(value);
                        else
                            currentRE.setForeignTable("");
                        break;
                    case "foreign_text_table":
                        if ( value != null)
                            currentRE.setForeignTextTable(value);
                        else
                            currentRE.setForeignTextTable("");
                        break;
                    case "enable_filter":
                        if ( value != null && !value.isEmpty()) {
                            boolean v = false;
                            if (value.charAt(0)== 't' || value.charAt(0)=='T') {
                                v = true;
                            }
                            currentRE.setEnableFilter(v);
                        }
                        break;
                    default:
                        System.out.println("WARNING: Unknown element '"+key+"' in type_to_table_filter@filter element.");
                }
                break;
        }

    }

    public void arrayValue(String path, String value, boolean isNumber) {
        if( path.compareTo("/kv2kl/type_to_table_filter/filters@/types@")==0) {
            if( isNumber ) {
                try {
                    currentRE.addType(Integer.parseInt(value));
                }
                catch(NumberFormatException ex) {
                    System.out.println("WARNING: type_to_table_filter@filter@types type ("+value+") is not an integer.");
                }
            }
        }
    }

    public TypeRouter parseConf(String filename) throws Exception {
        router = new TypeRouter();
        currentPath="";
        currentRE=null;
        filterIndex=0;
        parse(filename);
        router.fixDefaultForeignTables();
        return router;
    }

}