/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: FileUtil.java,v 1.1.2.3 2007/09/27 09:02:43 paule Exp $                                                       

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
package no.met.kvutil;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


public class FileUtil {

	/**
	 * Read the contest from a file and return it as a string.
	 * 
	 * @param file2read The file to read.
	 * @return A string on success and null otherwise.
	 */
	static public String readFile2Str(String file2read){
		File f;
		BufferedReader in;
		
		f=new File(file2read);
		
		if(!(f.exists() && f.isFile() && f.canRead()))
				return null;
				
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return null;
		}

		int size=(int)f.length();

		StringBuffer sb=new StringBuffer(size);
		char[] buf=new char[256];
		int nRead;
		
		try {
			while((nRead=in.read(buf)) >-1){
				sb.append(buf, 0, nRead);
			}
			
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
			
			return null;
		}
	}
	
	
	/**
	 * Writes a string to a file.
	 * 
	 * @param file2write The name of the file to write the string.
	 * @param buf The string to write to the file.
	 * @return true on success and false ortherwise.
	 */
	static public boolean writeStr2File(String file2write, String buf){
		BufferedWriter out;
				
		try {
			out = new BufferedWriter(new FileWriter(file2write));
			out.write(buf, 0, buf.length());
			out.close();
			return true;
		} catch (IOException e) {
			System.out.println("IOException: FileUtil.writeStr2File: "+e.getMessage());
			return false;
		}
	}


	static public Reader getFileReader(String filename){
		File f;
		BufferedReader in;

		f=new File(filename);

		if(!(f.exists() && f.isFile() && f.canRead()))
			return null;

		try {
			return  new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	static public boolean appendStr2File(String file2write, String buf){
		try {
			OutputStream out = Files.newOutputStream(Paths.get(file2write), CREATE, APPEND);
			out.write(buf.getBytes());
			out.close();
			return true;
		} catch (IOException e) {
			System.out.println("IOException: FileUtil.appendStr2File: "+e.getMessage());
			return false;
		}
	}

	static public boolean checkDir(String path, boolean creatIfNotExist) throws FileAlreadyExistsException, IOException {
		Path p = FileSystems.getDefault().getPath(path);
		
		if( Files.exists(p) ) {
			if( Files.isDirectory(p) )
				return true;
			else
				throw new FileAlreadyExistsException("The path '"+path +"' exist, but is not a directory.");
		} else if( ! creatIfNotExist ) {
			return false;
		} else { // Try to create the directory.
			Files.createDirectories(p);
		}
		
		return true;
	}

	static public Path searchFile(String filename, List<String> pathList) {
		Path fpath = Paths.get(filename);
		if( fpath.isAbsolute() ) {
			if( Files.exists(fpath) && Files.isReadable(fpath) && Files.isRegularFile(fpath) ) {
				return fpath;
			} else {
				return null;
			}
		} 

		for( String p : pathList ) {
			Path file=Paths.get(p, filename);

			if( Files.exists(file) && Files.isReadable(file ) && Files.isRegularFile(file) ) {
				return file;
			}
		}

		return null;
	}

}
