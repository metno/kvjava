package no.met.kvutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

import org.apache.log4j.Logger;

public class PidFileUtil {
	static Logger logger = Logger.getLogger(PidFileUtil.class);
	static Path pidFile = null;


	static public void setPidFile(Path pidf) {
		pidFile=pidf;
	}

	static public Path getPidFile() {
		return pidFile;
	}


	static public void createPidFile(String pidfile){
		Path p = Paths.get(pidfile);
		createPidFile(p);
	}

	synchronized static public void createPidFile(Path pidfile){
		if(pidFile!=null)
			return;

		if(pidfile==null){
			System.out.println("FATAL: pidfile name NOT given!");
			logger.fatal("FATAL: pidfile name NOT given!");
			System.exit(1);
		}

		long pid=ProcUtil.getPid();

		if( pid < 0 ) {
			System.out.println("FATAL: Cant get the applications pid (process id)!");
			logger.fatal("FATAL: Cant get the applications pid (process id)!");
			System.exit(1);
		}

		try {
			System.err.println(" ***** pidfile 1: '" + pidfile +"'.");
			if( ProcUtil.isProcRunning(pidfile) ) {
				System.err.println("FATAL: The pidfile '"+pidfile+"' allready exist!" +
						"And the process the pid is referancing is running.");
				logger.fatal("FATAL: The pidfile '"+pidfile+"' allready exist!" +
						"And the process the pid is referancing is running.");
				System.exit(1);
			}

			System.err.println(" ***** pidfile: '" + pidfile +"'.");
			pidFile= pidfile;
			System.err.println(" ***** pidfile: pid:'" + pid +"'.");
			pidFile = Files.write(pidFile, (""+pid+"\n").getBytes(), StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE);
			System.out.println("Writing pidfile '" + pidFile.toString() + "' with pid '"+pid+"'!");
			logger.info("Writing pidfile '" + pidFile.toString() + "' with pid '"+pid+"'!");
		}
		catch( java.io.IOException ex ) {
			ex.printStackTrace();
			System.err.println("FATAL: createPidFile: " + ex.getMessage() );
			logger.fatal("FATAL: createPidFile: " + ex.getMessage() );
			System.exit(1);
		}
		catch( java.lang.SecurityException ex ) {
			ex.printStackTrace();
			System.err.println("FATAL: createPidFile: " + ex.getMessage() );
			logger.fatal("FATAL: createPidFile: " + ex.getMessage() );
			System.exit(1);
		}
		catch( Exception ex ){
			ex.printStackTrace();
			System.err.println("FATAL: createPidFile: " + ex.getMessage() );
			logger.fatal("FATAL: " + ex.getMessage() );
			System.exit(1);
		}
	}

	/**
	 * Create a pid file. The filepath and pid must be set as property in the
	 * start script of the application.
	 *
	 * The path property is PIDFILE and the pid property is USEPID.
	 *
	 * The property is set with the -D switch on the java commandline.
	 *
	 * If the pidfile exist or an error occure in the creation of the pidfile
	 * the application terminate.
	 */
	static synchronized public void createPidFile() {
		if(pidFile == null) {
			String pidFilename = System.getProperties().getProperty("PIDFILE");

			if (pidFilename == null) {
				System.out.println("FATAL: Property variable PIDFILE is unset!");
				logger.fatal("FATAL: Property variable PIDFILE is unset!");
				System.exit(1);
			} else {
				pidFile = Paths.get(pidFilename);
			}
		}
	}


	/**
	 * Remove a previous created pidfile.
	 * 
	 * @see
	 */
	static synchronized public void removePidFile() {
		try {
			if (pidFile != null) {
				System.out.println("Removing pidfile '" + pidFile+ "!");
				logger.info("Removing pidfile '" + pidFile+ "!");
				Files.deleteIfExists(pidFile);
			}
		} catch (java.lang.SecurityException ex) {
			System.out.println("SecurityException: Removing pidfile '" + pidFile + "!");
			// NOOP
		}
		catch(IOException ex) {
			System.out.println("Exception: Removing pidfile '" + pidFile + "! ("+ex.getMessage()+")");
		}
	}


	static public synchronized boolean isProcessRunning(Path pidfile) throws InternalError {
		try {
			long pid = Long.parseLong(Files.readAllBytes(pidfile).toString());
			Runtime runtime = Runtime.getRuntime();
			Process killProcess = runtime.exec(new String[] { "kill", "-0", "" + pid });
			long killProcessExitCode = killProcess.waitFor();
			return killProcessExitCode == 0;
		} catch (NumberFormatException ex) {
			System.out.println(
					"Cant parse the pid in file '" + pidfile + "' to a number. Reason: " + ex.getMessage());
			logger.error("Cant parse the pid in file '" + pidfile + "' to a number. Reason: " + ex.getMessage());
			throw new InternalError(
					"Cant parse the pid in file '" + pidfile + "' to a number. Reason: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("Cant read PIDFILE '" + pidfile + "'.");
			logger.error("Cant read PIDFILE '" + pidfile + "'. Reason: " + ex.getMessage());
			throw new InternalError("Cant read PIDFILE '" + pidfile + "'. Reason: " + ex.getMessage());
		} catch (Exception e) {
			System.out.println("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
			logger.error("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
			throw new InternalError("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
		}
	}



	static public synchronized boolean isProcessRunning() throws InternalError {
		Path pidf=pidFile;
		if( pidf == null ) {
			String pidFilename = System.getProperties().getProperty("PIDFILE");
			if (pidFilename == null) {
				System.out.println("No pidfile specified in the property PIDFILE!");
				throw new InternalError("No pidfile specified in the property PIDFILE!");
			}
			
			pidf = FileSystems.getDefault().getPath(pidFilename);
		}
		return isProcessRunning(pidf);
	}

}
