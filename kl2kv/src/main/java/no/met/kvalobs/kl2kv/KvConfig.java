package no.met.kvalobs.kl2kv;

import no.met.kvclient.KvBaseConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KvConfig extends KvBaseConfig{
	public Path statefile;


	public KvConfig(String baseAppName, String useThisConfFile) throws IOException{
		super(baseAppName, useThisConfFile);
		statefile = Paths.get(libdir,  appName + ".properties");
		getDbCredentials("kl", credProp);
	}

	static public KvConfig config(String baseAppName, String useThisConfFile) {
		try {
			return new KvConfig(baseAppName, useThisConfFile);
		}
		catch( Exception ex ) {
			System.err.println("Error reading config: " + ex.getMessage());
			System.exit(1);
		}
		return null;
	}


	static public KvConfig config(String baseAppName) {
		return config(baseAppName, null);
	}


	@Override
	protected String toStringExtra(){
		return "    statefile: " + getStatePath() +"\n";
	}

    public Path getStatePath() {
        return statefile;
    }

}
