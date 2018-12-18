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

import java.util.LinkedList;

public class JsonContext {

    private  LinkedList<String> stack;

    public JsonContext() {
        stack=new LinkedList<>();
    }

    public String path() {
        if( stack.isEmpty() ) {
            return "/";
        }

        String ret="";
        for(String e : stack ){
           ret = ret + e;
        }
        return ret;
    }

    public void clear() {
        stack.clear();
    }

    public boolean inObject()throws IllegalStateException {
        if( stack.isEmpty())
            throw new IllegalStateException("Empty context");
        if ( stack.peekLast()=="/")
            return true;
        else
            return false;
    }

    public boolean inArray() throws IllegalStateException{
        if( stack.isEmpty())
            throw new IllegalStateException("Empty context");

        if ( stack.peekLast()=="@")
            return true;
        else
            return false;
    }

    public void startObject(String name ) throws IllegalStateException {

        if ( ! name.isEmpty()) {
            stack.addLast(name);
        }

        stack.addLast("/");

    }

    public void startArray(String name ) throws IllegalStateException {

        if ( ! name.isEmpty()) {
            stack.addLast(name);
        }

        stack.addLast("@");
    }

    public void endObject()throws IllegalStateException {
        String type = pop( 1, "endObject");

        if( stack.isEmpty())
            return;

        String l = stack.peekLast();

        if ( l == "@" || l == "/")
            return;

        stack.removeLast();
    }

    public void endArray()throws IllegalStateException {
        String type = pop(1, "endArray");

        if( stack.isEmpty())
            return;


        String l = stack.peekLast();

        if ( l == "@" || l == "/")
            return;
        stack.removeLast();
    }

    private String pop(int n, String context) throws IllegalStateException{
        if (stack.size() < n ) {
            throw new IllegalStateException(context + ": Size to small " + stack.size() + " to pop "+n+" elements.");
        }
        String ret="";
        while( n > 0) {
            ret=stack.removeLast();
            n--;
        }
        return ret;
    }
}
