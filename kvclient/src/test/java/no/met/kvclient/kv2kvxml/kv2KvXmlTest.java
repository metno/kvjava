/*
	  Kvalobs - Free Quality Control Software for Meteorological Observations 

	  $Id: FileUtilTest.java,v 1.1.2.3 2007/09/27 09:02:44 paule Exp $                                                       

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

package no.met.kvclient.kv2kvxml;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import junit.framework.*;
import no.met.kvclient.service.ObsDataList;

public class kv2KvXmlTest extends TestCase {

	static String testDir="kvclient/src/test/java/no/met/kvclient/kv2kvxml/tests";
	static Path getPath(String file) {
		String p=testDir+"/"+file;
		return FileSystems.getDefault().getPath(p);
	}
	
	public void setUp() {
	}

	public void tearDown() {
	}

	public void testEmptyKvalobsData() {
		Path path=getPath("emptyKvalobsData.xml");
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			assertTrue(data.isEmpty());
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}
	
	public void testEmptyStation() {
		Path path=getPath("emptyStation.xml");
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			assertTrue(data.isEmpty());
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}
	
	public void testEmptyType() {
		Path path=getPath("emptyType.xml");
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			assertTrue(data.isEmpty());
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}

	public void testEmptyObstime() {
		Path path=getPath("emptyObstime.xml");
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			assertTrue(data.isEmpty());
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}

	public void testEmptyKvData() {
		Path path=getPath("emptyKvdata.xml");
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			assertTrue(data.isEmpty());
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}
	
	public void testKvData1() {
		System.out.println("--------- kvalobsdata1.xml ------------");
		Path path=getPath("kvalobsdata1.xml");
		
		
		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			System.err.println(data);
			assertTrue(data.size()==6);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}

	public void testKvData2() {
		System.out.println("--------- kvalobsdata2.xml ------------");
		Path path=getPath("kvalobsdata2.xml");

		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			System.err.println(data);
			assertTrue(data.size()==3);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}
	
	public void testKvDataTbtime1() {
		System.out.println("--------- kvalobsdatatbtime1.xml ------------");
		Path path=getPath("kvalobsdatatbtime1.xml");

		try {
			ObsDataList data = Kv2KvXml.decodeFromPath(path);
			assertNotNull(data);
			System.err.println(data);
			System.err.println("ObsDataSize: " + data.size());
			assertTrue(data.size()==2);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			fail();
		}
	}

}
