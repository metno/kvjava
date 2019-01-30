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

import java.util.*;
import java.util.stream.Collectors;


import no.met.kvutil.Tuple2;
import no.met.kvutil.json.JsonContext;

import static java.lang.Math.abs;


public class TypeRouter {

    List<RouterElement> routes = new LinkedList<>();
    String table = null;
    String textTable = null;
    String foreignTable = null;
    String foreignTextTable = null;
    Boolean enableFilter = null;
    RouterElement defaultRouterElement=null;
    JsonContext context = new JsonContext();

    static public class RouterElement {
        Set<Integer> types = new HashSet<>();
        String tableName = null;
        String textTableName = null;
        String foreignTextTableName = null;
        String foreignTableName = null;
        String name = "";
        Boolean enableFilter = null;
        Boolean skip = false;

        RouterElement() {
        }

        void setName(String name) {
            this.name = name;
        }

        void addType(int typeid) {
            types.add(new Integer(Math.abs(typeid)));
        }

        void setTable(String table) {
            this.tableName = null;
            if( table != null )
                this.tableName = table.trim();
        }

        void setTextTable(String table) {
            this.textTableName=null;
            if ( table != null)
                this.textTableName=table.trim();
        }

        void setForeignTable(String table) {
            this.foreignTableName=null;
            if( table != null)
                this.foreignTableName = table.trim();
        }

        void setForeignTextTable(String table) {
            this.foreignTextTableName=null;
            if( table != null )
                this.foreignTextTableName = table.trim();
        }

        void setEnableFilter(boolean doFilter ){
            enableFilter = new Boolean(doFilter);
        }

        public String getName() {
            return name;
        }

        public String getTable() {
            return tableName;
        }

        public String getTextTable() {
            return textTableName;
        }

        public String getForeignTextTableName() {
            return foreignTextTableName;
        }

        public String getForeignTableName() {
            return foreignTableName;
        }

        int countTypes() {
            return types.size();
        }

        public String getTable(int typeid, boolean isTextData) {
            if (types.contains(typeid)) {
                if (isTextData)
                    return textTableName;
                else
                    return tableName;
            }
            return null;
        }

        public Boolean getEnableFilter(Boolean defValue) {
            if (enableFilter != null) {
                return enableFilter;
            } else if (defValue != null) {
                return defValue;
            } else {
                enableFilter = new Boolean(true);
                return enableFilter;
            }
        }

        public boolean enableFilter(boolean defValue) {
            if (enableFilter != null) {
                return enableFilter.booleanValue();
            }
            return defValue;
        }
    }

    public void addRouterElement(RouterElement re) {
        routes.add(re);
    }


    public RouterElement getTable(long typeid_) {
        Integer typeid = new Integer(Math.abs((new Long(typeid_)).intValue()));
        for (RouterElement r : routes) {
            if (r.types.contains(typeid)) {
                return r;
            }
        }

        if ( defaultRouterElement != null )
            return defaultRouterElement;

        defaultRouterElement = new RouterElement();
        defaultRouterElement.name="(default)";
        defaultRouterElement.tableName=table;
        defaultRouterElement.textTableName = textTable;
        defaultRouterElement.foreignTableName=foreignTable;
        defaultRouterElement.foreignTextTableName=foreignTextTable;
        defaultRouterElement.enableFilter = enableFilter;

        return defaultRouterElement;
    }


    public Tuple2<String, Boolean> getTable(int stationid, int typeid_, boolean isTextData) {
        if (stationid > 100000) {
            if (isTextData) {
                return new Tuple2<String, Boolean>(foreignTextTable, getEnableFilter());
            } else {
                return new Tuple2<String, Boolean>(foreignTable, getEnableFilter());
            }
        }

        int typeid = Math.abs(typeid_);
        for (RouterElement r : routes) {
            String table = r.getTable(typeid, isTextData);
            return new Tuple2<String, Boolean>(table, r.getEnableFilter(enableFilter));
        }

        if (isTextData ) {
            return new Tuple2<String, Boolean>(textTable, getEnableFilter());
        } else if (!isTextData ) {
            return new Tuple2<String, Boolean>(table, getEnableFilter());
        }

        return null;
    }

    public Boolean getEnableFilter() {
        if (enableFilter != null) {
            return enableFilter;
        }
        enableFilter = new Boolean(true);
        return enableFilter;
    }

    public boolean enableFilter(int typeid_) {
        Tuple2<String, Boolean> t = getTable(0, typeid_, false );
        return t._2.booleanValue();
    }


    //List<String> toStringList

    public String toString() {
        String ret="{\n";
        ret += "   \"kv2kl\": {\n";
        ret += "      \"type_to_table_filter\": {\n";
        ret += "         \"table\": "+(table!=null?"\""+table+"\"":"null")+",\n";
        ret += "         \"text_table\": "+(textTable!=null?"\""+textTable+"\"":"null")+",\n";
        ret += "         \"foreign_table\": "+(foreignTable!=null?"\""+foreignTable+"\"":"null")+",\n";
        ret += "         \"foreign_text_table\": "+(foreignTextTable!=null?"\""+foreignTextTable+"\"":"null")+",\n";
        ret += "         \"enable_filter\": " + (enableFilter==null?"null":enableFilter.booleanValue())+"\n";

        if( routes.isEmpty() ) {
            ret += "         \"filters\": []\n";
            ret += "      }\n";
            ret += "   }\n";
            ret += "}";
            return ret;
        }
        ret += "         \"filters\": [\n";


        boolean first=true;
        for( RouterElement e: routes) {
            if ( first )
                first = false;
            else
                ret += ",\n";
            ret += "            {\n";
            ret += "               \"name\": "+(e.name.isEmpty()?"null":"\""+e.name+"\"") +",\n";
            ret += "               \"enable_filter\": " + (e.enableFilter==null?"null":e.enableFilter.booleanValue())+"\n";
            ret += "               \"table\": "+(e.tableName==null?"null":"\""+e.tableName+"\"") +",\n";
            ret += "               \"text_table\": "+(e.textTableName==null?"null":"\""+e.textTableName+"\"") +",\n";
            ret += "               \"foreign_table\": "+(e.foreignTableName==null?"null":"\""+e.foreignTableName+"\"") +",\n";
            ret += "               \"foreign_text_table\": "+(e.foreignTextTableName==null?"null":"\""+e.foreignTextTableName+"\"") +",\n";
            String sl=e.types.stream().map( i -> Integer.toString(i)).collect(Collectors.joining(", "));
            ret += "               \"types\": ["+sl+"]\n";
            ret += "            }";
        }
        ret +="\n";
        ret +="         ]\n";
        ret += "      }\n";
        ret += "   }\n";
        ret += "}";
        return ret;
    }

    public void setDefaultTable(String table, boolean ifNotSet){
        if( !ifNotSet || table==null)
            table = table;

    }

    public void setDefaultTextTable(String table, boolean ifNotSet){
        if( !ifNotSet || textTable==null)
            textTable = table;
    }

    public void setForeignTable(String table, boolean ifNotSet){
        if( !ifNotSet || foreignTable==null)
            foreignTable = table;
    }

    public void setForeignTextTable(String table, boolean ifNotSet){
        if( !ifNotSet || foreignTextTable==null)
            foreignTextTable = table;
    }

    public void setEnableFilter(boolean doFilter, boolean ifNotSet){
        if( !ifNotSet || enableFilter==null) {
            enableFilter = new Boolean(doFilter);
        }
    }


    public String getDefaultTable(){
        return table;
    }

    public String getDefaultTextTable(){
        return textTable;
    }

    public String getForeignTable(){
        return foreignTable ;
    }

    public String getForeignTextTable(){
        return foreignTable;
    }


    public TypeRouter() {
        this.table="kv2klima";
        this.textTable="T_TEXT_DATA";
        this.enableFilter=new Boolean(true);
    }

    protected void fixDefaultForeignTables() {
        int n=0;
        for( RouterElement r : routes) {
            if( r.foreignTableName == null )
                r.foreignTableName = foreignTable;

            if( r.foreignTextTableName == null )
                r.foreignTextTableName = foreignTextTable;

            if( r.enableFilter == null ) {
                r.enableFilter=enableFilter;
            }

            if( r.name==null) {
                r.name="@"+n;
            }
            n++;
        }
    }

}
