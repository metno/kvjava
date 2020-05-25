package no.met.kvalobs.kv2kl;

import no.met.kvclient.KvBaseConfig;
import no.met.kvutil.PropertiesHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KvConfig extends KvBaseConfig{
	public Path statefile;


	public KvConfig(String baseAppName) throws IOException{
		super(baseAppName);
		statefile = Paths.get(libdir,  appName + ".properties");
		getDbCredentials("kl", credProp);
	}


	static public KvConfig config(String baseAppName) {
		try {
			return new KvConfig(baseAppName);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			System.err.println("Error reading config: " + ex.getMessage());
			System.exit(1);
		}
		return null;
	}


	@Override
	protected String toStringExtra(){
		return "    statefile: " + getStatePath() +"\n";
	}

    public Path getStatePath() {
        return statefile;
    }

}
