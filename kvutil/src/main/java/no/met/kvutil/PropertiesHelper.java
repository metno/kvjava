/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: PropertiesHelper.java,v 1.1.2.3 2007/09/27 09:02:43 paule Exp $                                                       

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PropertiesHelper extends Properties {

	private static final long serialVersionUID = 7979045326117714279L;
	String filename;

	public PropertiesHelper() {

	}

	public PropertiesHelper(Properties defaults) {
		super(defaults);
	}

	public PropertiesHelper(String filename) {
		this.filename = filename;
	}

	static String cleanKey( String key ) {
		key = key.trim();

		//Compress all sequences of '.' to one '.'.
		key.replaceAll("\\.{2,}",".");

		//Remove '.' at start
		if(key.startsWith("."))
			key = key.substring(1);

		//Remove '.' at end
		if( key.endsWith("."))
			key = key.substring(0, key.length() - 1);

		return key;
	}

	static int count(String src, String what) {
		int cnt = 0;
		int i = -1;

		do {
			cnt++;
			i = src.indexOf(what, i+1);
		}while(i>=0);

		return cnt;
	}

	public int countSubkeys(String key, StringHolder sh) {
		key = cleanKey(key);
		sh.setValue(key);

		if (key.length() == 0)
			return 0;

		return count(key, ".");
	}

	/*-
	 * Assume a key on the form k1.k2.k3 and return a sub element of the key.
	 * Sub elements of this key is: k1, k2 and k3. The sub element of index 1 is
	 * k1, index 2 is k2, etc.
	 * 
	 * An index of 0 returns the entire key, ie k1.k2.k3.
	 * 
	 * @param key  A key on the form k1.k2.k3.k4 .....
	 * @param index An index of n'te sub element of the key.
	 * @return The n'te sub element of the key. If the index is out of range null
	 *         is returned.
	 */
	public String getSubkey(String key, int index) {
		key = cleanKey(key);

		if (index <= 0)
			return key;

		String subKeys[] = key.split("\\.");

		if ( index > subKeys.length)
			return null;

		return subKeys[index-1].trim();
	}

	static public PropertiesHelper loadFile(Path path ){
		PropertiesHelper prop=new PropertiesHelper();
		if( ! Files.isRegularFile(path) ){
			System.err.println("File do not exist, or is not readable '" + path +"'.");
			return null;
		}
		
		try {
			InputStream s = Files.newInputStream(path);
			prop.load(s);
			return prop;
		}
		catch(IOException ex ){
			System.err.println("Error reading configuration file '"+path +"'. Reason: " + ex.getMessage());
			return null;
		}
	}


	static public PropertiesHelper loadFile(String conf) {
		Path path = FileSystems.getDefault().getPath(conf);
	    return loadFile(path);
	}

	public Properties loadFromFile(String filename) throws FileNotFoundException, IOException {

		File f = new File(filename);
		FileInputStream fin = new FileInputStream(f);

		load(fin);
		fin.close();
		return this;
	}

	public boolean save() {
		return false;
	}

	public void saveToFile(String filename) throws FileNotFoundException, IOException {

		File f = new File(filename);
		FileOutputStream fout = new FileOutputStream(f);

		store(fout, "WARNING Dont create comment in this file, they will be lost!");
		fout.close();
	}

	/*-
	 * Same as apply(String key, String Value), but we look up the value for the
	 * key in the properties.
	 * 
	 * @param key to look up in the property.
	 * @return a string on success and null on failure.
	 */

	public String apply(String key) {
		String val = super.getProperty(key);
		return apply(key, val);
	}

	/*-
	 * apply substitutes some keys in the value string with data from the
	 * Properties. Keys are on the form: ${key}. Where key must be the name of a
	 * property. A special form of key are $n where n is an number. This number
	 * reference an sub key in the key argument to the method. The key must be on
	 * the form k1.k2.k3 etc.
	 * 
	 * <pre>
	 *     Ex.
	 *     
	 *     We have the following properties.
	 *     
	 *     key1=val1
	 *     key2=${$1}-${$2}.dat
	 *     
	 *     if we call apply with the following key and value.
	 *     
	 *     key=file.1870
	 *     value=${key1}
	 *     
	 *     The method will return the string 'val1'.
	 *     
	 *     If we call it with the value=${key2} it will return
	 *     the string 'file-1870.dat'.
	 * </pre>
	 * 
	 * @param key
	 * @param value a string.
	 * @return a string on success and null on failure.
	 */
	public String apply(String key, String value) {
		int ii;
		String k;
		String val2;
		int keyIndex;

		if (value == null)
			return null;

		int i = value.indexOf("${");

		while (i > -1) {
			ii = value.indexOf('}', i);

			if (ii == -1) {
				// Throw an format exception.
				return null;
			}

			k = value.substring(i + 2, ii);
			k = k.trim();

			if (k.startsWith("$")) {
				if (k.length() < 2) {
					// Throw an format error.
				}

				keyIndex = Integer.parseInt(k.substring(1));

				val2 = getSubkey(key, keyIndex);

				if (val2 == null) {
					// Throw an index exception.
					return null;
				}
			} else {
				val2 = super.getProperty(k);

				if (val2 == null) {
					// Throw an form of property error.
					return null;
				}
			}

			value = StringUtil.replace(value, i, ii + 1, val2);
			i = value.indexOf("${");
		}

		return value;
	}

	@Override  public String getProperty(String key) {
		String val = super.getProperty(key);
		return apply(key, val);

	}

	@Override public String getProperty(String key, String defaultVal) {
		String val = super.getProperty(key);
		val = apply(key, val);
        if( val == null)
            return defaultVal;
        else
            return val;
	}

	public String getPropertyApply(String key, String apply) {
		String val = super.getProperty(key);
		return apply(apply, val);
	}

	public String getPropertyApply(String key, String apply, String defaultVal) {
		String val = super.getProperty(key);
		val = apply(apply, val);
		if( val == null)
			return defaultVal;
		else
			return val;
	}


    /**
     * Insert all properties that starts with  the prefix,
     * but with the prefix removed into prop.
     *
     * Ex,
     *   if we have the following properties.
     *   kl.db.passwd=fjg
     *   kl.db.user=hansen
     *   kv.db.passwd=kjdfhg
     *   kv.db.user=truls
     *   kafka.connect=host
     *
     * @param prefix
     * @return
     */
	public Properties removePrefix(String prefix, Properties prop) {
        int from=prefix.length();
        if( prefix.endsWith("*")) {
            prefix=prefix.replaceAll("\\*+", "");
            int i = prefix.lastIndexOf('.');
            if(i<0)
                from=0;
            else
                from=i+1;
        } else if( ! prefix.endsWith(".")) {
            prefix += ".";
            from = prefix.length();
        }

        for( String key : stringPropertyNames()) {
            if( key.startsWith(prefix))
                prop.setProperty(key.substring(from),super.getProperty(key));
        }
        return prop;
    }


    public PropertiesHelper removePrefix(String prefix) {
        PropertiesHelper prop=new PropertiesHelper();
        return (PropertiesHelper) removePrefix(prefix, prop);
    }

	public List<String> getSortedStringKeys() {
        return stringPropertyNames().stream().sorted().collect(Collectors.toList());
	}

    public String toString() {
        return toString("Properties:");
    }

    public String toString(String heading) {
        String res = heading +"\n";

        for(String prop : getSortedStringKeys()) {
			String val;
            if(prop.matches(".*passw.*d.*"))
		    	val="*********";  //Do not print passwords to the screen.
			else
				val = getProperty(prop);
            res += "\n   " + prop + ": " + val;
        }
		return res;
    }

	public String apply(String key, Properties prop) {
		return null;
	}

	public PropertiesHelper loadFrom(Properties prop) {
		return loadFrom(prop, false);
	}

	public PropertiesHelper loadFrom(Properties prop, boolean onlyIfNotExist) {
		Enumeration<?> it = prop.propertyNames();

		while (it.hasMoreElements()) {
			Object k = it.nextElement();
			if( k instanceof String) {
				if (onlyIfNotExist) {
					if (super.getProperty((String) k) == null)
						super.put(k, prop.get(k));
				} else {
					super.put(k, prop.get(k));
				}
			}
		}
		return this;
	}
}
