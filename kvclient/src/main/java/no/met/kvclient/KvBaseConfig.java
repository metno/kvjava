package no.met.kvclient;


import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

public class KvBaseConfig {
    public String configfile="";
    public String credfile="";
    public String includeFile="";
    public String appName="";
    public String rundir="";
    public String etcdir="";
    public String logdir="";
    public String libdir="";
    public String kvdist="";
    public String pid="";
    public String confname="";
    public String baseConfigName; //Used to look up config files.
    public String baseAppName;  //If the app name is on the form name-name2. Set the baseAppName to name.
    public PropertiesHelper credProp;

    public PropertiesHelper conf;

    static public Tuple2<PropertiesHelper, Path> loadFromFile(PropertiesHelper prop, Path file, boolean fileMustExist) throws IOException {

        if( Files.exists(file) && Files.isReadable(file)){
            InputStream in = Files.newInputStream(file);
            prop.load(in);
            in.close();
            return Tuple2.of(prop, file);
        } else if( fileMustExist)
            throw new NoSuchFileException("Cant load config file '"+file+"");
        else
            return null;
    }

    static public Tuple2<PropertiesHelper, Path> loadFromFile(Path file, boolean fileMustExist) throws IOException {
        PropertiesHelper prop=new PropertiesHelper();
        return loadFromFile(prop, file, fileMustExist);
    }

    String addPath(String oldPath, String pathToAdd) {
        if( oldPath==null || oldPath.isEmpty())
            return pathToAdd;
        else
            return oldPath+", " + pathToAdd;
    }

    void loadIncluds(PropertiesHelper prop ) throws Exception{
        String file = prop.getProperty("include_file");
        if( file != null ) {
            Path f = locateFile(Paths.get(file));
            if( f==null)
                throw new NoSuchFileException("include_file: Cant load config file '"+file+"");
            Tuple2<PropertiesHelper,Path> r=loadFromFile(f, true);
            PropertiesHelper ip= r._1;
            includeFile = addPath(includeFile, r._2.toString());
            prop.loadFrom(ip, true);
            loadIncluds(ip);
        }

        file =  prop.getProperty("include_credentials");
        if( file != null ) {
            Path f = locateFile(Paths.get(file));
            if( f==null)
                throw new NoSuchFileException("include_credentials: Cant load config file '"+file+"");
            loadCredentials(f, true );
        }
    }


    PropertiesHelper loadCredentials(Path f, boolean credMustExist) throws Exception {
        if( credProp != null)
            return credProp;
        if( ! Files.exists(f) || ! Files.isRegularFile(f) || ! Files.isReadable(f)) {
            System.err.println("Cant read the credentials file '"+credfile+"'.");
            if( credMustExist )
                throw new Exception("Cant read the credential file '"+f+"'. Check that you run as the correct user. Check the owner of the file '" + credfile +"'.");
            else
                return null;
        }

        Set<PosixFilePermission> perm=Files.getPosixFilePermissions(f);

        if( perm.contains(PosixFilePermission.GROUP_EXECUTE) || perm.contains(PosixFilePermission.OTHERS_EXECUTE) ||
                perm.contains(PosixFilePermission.OWNER_EXECUTE) ||
                perm.contains(PosixFilePermission.GROUP_WRITE) || perm.contains(PosixFilePermission.OTHERS_WRITE) ||
                perm.contains(PosixFilePermission.GROUP_READ) || perm.contains(PosixFilePermission.OTHERS_READ)) {
            System.err.println("The credential file '"+f+"' must be readable and writeable only by the owner, ie the mode must be 0600.");
            throw new Exception("The credential file '"+f+"'must be readable and writeable only by the owner, ie the mode must be 0600.");
        }
        Tuple2<PropertiesHelper,Path> r=loadFromFile(f, true);
        if(credProp!=null)
            credProp.loadFrom(r._1, true);
        else
            credProp=r._1;
        credfile=addPath(credfile,r._2.toString());
        return credProp;
    }

    public void getCredentials(Path f, String prefix, boolean credMustExist) throws Exception {
        if( loadCredentials(f, credMustExist) != null )
              getDbCredentials(prefix, credProp);
    }

    public void getDbCredentials(String dbPrefix, PropertiesHelper fromProp) {
        String dbuser="default."+dbPrefix+".dbuser";
        String dbpasswd="default."+dbPrefix+".dbpasswd";

        if( !confname.isEmpty()) {
            dbuser=dbuser.replaceFirst("default", confname);
            dbpasswd=dbpasswd.replaceFirst("default", confname);;
        }

        System.err.println("Reading credential for: "+dbuser);
        System.err.println("Reading credential for: "+dbpasswd);

        conf.setProperty(dbPrefix+".dbuser",fromProp.getProperty(dbuser, ""));
        conf.setProperty(dbPrefix+".dbpasswd",fromProp.getProperty(dbpasswd, ""));
    }

    void getDbCredentials(String dbPrefix) {
        if( credProp != null )
            getDbCredentials(dbPrefix, credProp);
    }


    static public KvBaseConfig config(String baseConfigName) {
        return new KvBaseConfig(baseConfigName);
    }
    static public KvBaseConfig config(String baseConfigName, String useConfig) {
        return new KvBaseConfig(baseConfigName, useConfig);
    }

    Path locateFile(Path conf) throws IOException {
        if(conf.isAbsolute()) {
            if(Files.exists(conf))
                return conf;
            else
                return null;
        }

        LinkedList<Path> p=new LinkedList<Path>();
        p.add(Paths.get(System.getProperty("user.dir")));
        p.add(Paths.get(System.getProperty("user.home"),".kvalobs"));
        p.add(Paths.get(etcdir));

        for(Path dir : p ) {
            Path file=dir.resolve(conf);
            if(Files.exists(file))
                return file;
        }

        return null;
    }

    public KvBaseConfig(String baseConfigName) {
        this(baseConfigName, null);
    }
    public KvBaseConfig(String baseConfigName, String useConf) {
        Properties p = System.getProperties();

        kvdist = p.getProperty("KVDIST", "");
        appName = p.getProperty("KVAPP_NAME", "");
        rundir = p.getProperty("RUNDIR", "");
        etcdir = p.getProperty("ETCDIR", "");
        logdir = p.getProperty("LOGDIR", "");
        libdir = p.getProperty("LIBDIR", "");
        pid = p.getProperty("USEPID", "");
        this.baseConfigName = baseConfigName;
        baseAppName = getBaseAppName(appName);
        confname = getConfName(appName);
        configfile = baseConfigName + (!confname.isEmpty()?"-":"")+confname +".conf";

//        System.err.println("etcdir: '"+etcdir+"'");
//
//        for( String prop : p.stringPropertyNames()) {
//            System.err.println(prop+": '"+p.getProperty(prop,"")+"'");
//        }

        try {

            if( useConf != null)
                configfile= useConf;
            if( configfile==null)
                throw new Exception("ERROR: No config file!");
            System.err.println("Configfile: " + configfile);
            Path cf=locateFile(Paths.get(configfile)); //Resolve the config file.
            if( cf == null) {
                System.err.println("Cant locate config file: " + configfile);
                throw new Exception("Cant locate config file: " + configfile);
            }

            configfile = cf.toString();
            Tuple2<PropertiesHelper, Path> r = loadFromFile(Paths.get(configfile), true);
            conf = r._1;
            loadIncluds(conf);
            getDbCredentials("kv");
            getDbCredentials("kl");

            PropertiesHelper baseProp=new PropertiesHelper();
            baseProp.setProperty("kvdist", kvdist);
            baseProp.setProperty("appname", appName);
            baseProp.setProperty("rundir", rundir);
            baseProp.setProperty("etcdir", etcdir);
            baseProp.setProperty("logdir", logdir);
            baseProp.setProperty("libdir", libdir);
            baseProp.setProperty("base_config_name", baseConfigName);
            baseProp.setProperty("base_app_name", baseAppName);
            baseProp.setProperty("confname", confname);
            conf.loadFrom(baseProp, true);

        }
        catch( Exception ex) {
            String msg=ex.getMessage();
            if( msg==null)
                System.err.println("@@@@@What the fuck");
            else
                System.err.println("Failed to load configuration file(s).\n"+ex.getMessage() );
            ex.printStackTrace();
            System.exit(1);
        }
     }


    static String getConfName(String appName) {
        int i = appName.indexOf("-");

        if( i>0)
            return appName.substring(i+1);
        else
            return "";
    }

    static String getBaseAppName(String appName) {
        int i = appName.indexOf("-");

        if( i>0)
            return appName.substring(0, i);
        else
            return appName;
    }


    public Path getPidPath() {
        return Paths.get(rundir +"/" + appName + ".pid");
    }

    public Path getConfPath() {
        return Paths.get(configfile);
    }

    protected String toStringExtra(){return "";}
    @Override
    public String toString() {
        String res=
                "   AppName: " + appName +"\n"
                        + "     confname: " + confname+"\n"
                        + " baseapp name: " + baseAppName +"\n"
                        + "       kvdist: " + kvdist + "\n"
                        + "       etcdir: " + etcdir +"\n"
                        + "       logdir: " + logdir +"\n"
                        + "       libdir: " + libdir +"\n"
                        + "       rundir: " + rundir + "\n"
                        + "          pid: " + pid +"\n"
                        + "      pidfile: " + getPidPath() + "\n"
                        + "   configfile: " + configfile + "\n"
                        + "  includefile: " + includeFile + "\n"
                        + "     credfile: " + getConfPath() + "\n"
                        + toStringExtra();

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
