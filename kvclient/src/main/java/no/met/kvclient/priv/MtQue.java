/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: MtQue.java,v 1.1.2.4 2007/09/27 09:02:42 paule Exp $                                                       

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
package no.met.kvclient.priv;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class MtQue<E> extends  LinkedBlockingQueue<E>
{
	private static final long serialVersionUID = 5244379526689354367L;

	public MtQue()  {
    	super(100);
    }
    
    public MtQue(int capacity)  {
    	super(capacity);
    }
    
    public E getObject(int waitInMillisecond) throws InterruptedException 
    {
    	return poll(waitInMillisecond,TimeUnit.MILLISECONDS);
    }
    
    public void putObject(E element) throws InterruptedException
    {
        if(element==null){
            System.out.println("MtQue::putObject: element==null" );
            return;
        }
        put(element);
    }
    
}
