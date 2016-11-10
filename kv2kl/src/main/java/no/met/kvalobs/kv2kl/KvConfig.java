package no.met.kvalobs.kv2kl;

import no.met.kvutil.PropertiesHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class KvConfig {
	public String configfile;
	public String statefile;
    public String credfile;
	public String appName="";
	public String rundir="";
	public String etcdir="";
	public String logdir="";
	public String libdir="";
	public String kvdist="";
	public String pid="";
    public String confname="";

	public PropertiesHelper conf;

	static PropertiesHelper loadFromFile(PropertiesHelper prop, Path file, boolean fileMustExist) throws IOException{
		if( Files.exists(file) && Files.isReadable(file)){
			InputStream in = Files.newInputStream(file);
			prop.load(in);
			in.close();
		} else if( fileMustExist)
			throw new NoSuchFileException("Cant load config file '"+file+"");

		return prop;
	}

	PropertiesHelper readConf(String baseAppName) throws IOException{
		PropertiesHelper common=loadFromFile(new PropertiesHelper(), Paths.get(etcdir, baseAppName+"-common.conf"), false);
		return loadFromFile(new PropertiesHelper(common), Paths.get(configfile), true);
	}

	void getCredentials(PropertiesHelper conf) throws Exception {
        Path f = Paths.get(credfile);

        if( ! Files.exists(f) || ! Files.isRegularFile(f) || ! Files.isReadable(f)) {
			System.err.println("Cant read the credentials file '"+credfile+"'.");
			throw new Exception("Cant read the credential file '"+f+"'. Check that you run as the correct user. Check the owner of the file '" + credfile +"'.");
		}

        Set<PosixFilePermission> perm=Files.getPosixFilePermissions(f);

        if( perm.contains(PosixFilePermission.GROUP_EXECUTE) || perm.contains(PosixFilePermission.OTHERS_EXECUTE) ||
                perm.contains(PosixFilePermission.OWNER_EXECUTE) ||
                perm.contains(PosixFilePermission.GROUP_WRITE) || perm.contains(PosixFilePermission.OTHERS_WRITE) ||
                perm.contains(PosixFilePermission.GROUP_READ) || perm.contains(PosixFilePermission.OTHERS_READ)) {
            System.err.println("The credential file '"+f+"' must be readable and writeable only by the owner, ie the mode must be 0600.");
            throw new Exception("The credential file '"+f+"'must be readable and writeable only by the owner, ie the mode must be 0600.");
        }

        PropertiesHelper prop=loadFromFile(new PropertiesHelper(), f, true);

        String dbuser="default.kl.dbuser";
        String dbpasswd="default.kl.dbpasswd";

        if( !confname.isEmpty()) {
            dbuser=dbuser.replaceFirst("default", confname);
            dbpasswd=dbpasswd.replaceFirst("default", confname);;
        }

		System.err.println("Reading credential for: "+dbuser);
		System.err.println("Reading credential for: "+dbpasswd);

		conf.setProperty("kl.dbuser",prop.getProperty(dbuser, ""));
        conf.setProperty("kl.dbpasswd",prop.getProperty(dbpasswd, ""));
    }

	static public KvConfig config(String baseAppName) {
		Properties p = System.getProperties();
		KvConfig conf = new KvConfig();
		conf.kvdist = p.getProperty("KVDIST", "");
		conf.appName = p.getProperty("KVAPP_NAME", "");
		conf.rundir = p.getProperty("RUNDIR", "");
		conf.etcdir = p.getProperty("ETCDIR", "");
		conf.logdir = p.getProperty("LOGDIR", "");
		conf.libdir = p.getProperty("LIBDIR", "");
		conf.pid = p.getProperty("USEPID", "");
		conf.configfile = conf.etcdir+"/" + conf.appName + ".conf";
		conf.statefile = conf.libdir+"/" + conf.appName + ".properties";
        conf.credfile = conf.etcdir+"/" + baseAppName+"-credentials.conf";
        conf.setConfname(conf.appName);

        try {
            conf.conf = conf.readConf(baseAppName);
            conf.getCredentials(conf.conf);
        }
        catch( Exception ex) {
            System.err.println("Failed to load configuration file(s).\n"+ex.getMessage() );
            System.exit(1);
        }
		return conf;
	}

    void setConfname(String appName) {
        int n = appName.indexOf('-');
        if( n > 0 )
            confname = appName.substring(n+1);
    }


    public Path getPidPath() {
		return Paths.get(rundir +"/" + appName + ".pid");
	}
	
	public Path getConfPath() {
		return Paths.get(configfile);
	}

	public Path getStatePath() {
		return Paths.get(statefile);
	}
	
	@Override
	public String toString() {
		String res=
		 "   AppName: " + appName +"\n"
       + "  confname: " + confname+"\n"
	   + "    kvdist: " + kvdist + "\n"
	   + "    etcdir: " + etcdir +"\n"
	   + "    logdir: " + logdir +"\n"
	   + "    libdir: " + libdir +"\n"
	   + "    rundir: " + rundir + "\n"
	   + "       pid: " + pid +"\n"
	   + "   pidfile: " + getPidPath() + "\n"
	   + "configfile: " + getConfPath() + "\n"
	   + " statefile: " + getStatePath();

		if( ! conf.isEmpty() ) {
			res += "\nAll config properties:";

			for(String prop : conf.getSortedStringKeys()) {
				String val;
                if(prop.endsWith("passwd"))
					val="*********";  //Do not print passwords to the screen.
				else
					val = conf.getProperty(prop);
                res += "\n   " + prop + ": " + val;
            }
		}
		res +="\n";
		return res;
	}
}
