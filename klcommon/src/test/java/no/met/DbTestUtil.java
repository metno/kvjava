/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: DbTestUtil.java,v 1.1.2.2 2007/09/27 09:02:20 paule Exp $                                                       

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
package no.met;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;

import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvutil.FileUtil;

public class DbTestUtil {

	static public boolean listDbTable(DbConnectionMgr mgr, String tablename){
    	DbConnection con=null;
        
        try{
            con=mgr.newDbConnection();
            
            ResultSet rs=con.execQuery("SELECT * FROM "+tablename);
            int c=rs.getMetaData().getColumnCount();
            int n=0;
            
            String val;
            
            if(c>0)
            	System.out.println(rs.getMetaData().getTableName(1));
            
            for(int i=1; i<=c; i++){
            	val=rs.getMetaData().getColumnLabel(i);
            	n+=val.length();

                if(i>1){
                    System.out.print(",");
                    n++;
                }

                System.out.print(val);
            }
            
            System.out.println();
            
            for(int i=0; i<n; i++)
            	System.out.print("-");
            
            System.out.println();
            
            while(rs.next()){
                for(int i=1; i<=c; i++){
                    if(i>1)
                        System.out.print(",");
                    val=rs.getString(i);
                    
                    if(val==null)
                        System.out.print("NULL");
                    else
                        System.out.print(val);
                }
                System.out.println();
            }
            
            for(int i=0; i<n; i++)
            	System.out.print("-");
            
            System.out.println();
            System.out.println();

            mgr.releaseDbConnection(con);
        }
        catch(Exception e){
            
            if(con!=null){
                try{
                    mgr.releaseDbConnection(con);
                }
                catch (Exception ex) {
                }
            }
            
            return false;
        }
        return true;
    }
	
	static public boolean runSqlFromFile(DbConnectionMgr mgr, String filename){
    	String buf=FileUtil.readFile2Str(filename);
    	
    	if(buf==null)
    		return false;
        //System.err.println("\n --runSqlFromFile: '"+filename + "'--------\n"+buf+"\n--------------------------\n");
        return runSqlFromString(mgr, buf);
	}
	
	
	static public boolean runSqlFromString(DbConnectionMgr mgr, String sqlstmt){
        try(DbConnection con=mgr.newDbConnection()){
            //System.err.println("\n --- runSqlFromString ------\n"+sqlstmt+"\n--------------------------\n");
            con.exec(sqlstmt);
            return true;
        }
        catch(Exception e){
        	e.printStackTrace(); 
        }
                
        return false;
    }
    
	static public void deleteDir(String path){
    	Path dbdir=FileSystems.getDefault().getPath(path);
        
        if(Files.exists(dbdir)){
            if(Files.isDirectory(dbdir)){
            	  try {
					Files.walkFileTree(dbdir, new SimpleFileVisitor<Path>() {
					         @Override
					         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					             throws IOException
					         {
					             Files.delete(file);
					             return FileVisitResult.CONTINUE;
					         }
					         @Override
					         public FileVisitResult postVisitDirectory(Path dir, IOException e)
					             throws IOException
					         {
					             if (e == null) {
					                 Files.delete(dir);
					                 return FileVisitResult.CONTINUE;
					             } else {
					                 // directory iteration failed
					                 throw e;
					             }
					         }
					     });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}
}