package no.met.kvutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

public class PidFileUtil {
	static Logger logger = Logger.getLogger(PidFileUtil.class);
	static File pidFile = null;

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
		String pid = null;
		String pidFilename = null;

		if (pidFile != null)
			return;

		pidFilename = System.getProperties().getProperty("PIDFILE");

		if (pidFilename == null) {
			System.out.println("FATAL: Property variable PIDFILE is unset!");
			logger.fatal("FATAL: Property variable PIDFILE is unset!");
			System.exit(1);
		}

		pid = System.getProperties().getProperty("USEPID");

		if (pid == null) {
			System.out.println("FATAL: Property variable USEPID is unset!");
			logger.fatal("FATAL: Property variable USEPID is unset!");
			System.exit(1);
		}

		pidFile = new File(pidFilename);

		try {
			if (!pidFile.createNewFile()) {
				logger.fatal("FATAL: The pidfile '" + pidFilename + "' allready exist!"
						+ "If an instance of the application is not running" + " remove the file and try again.");
				System.exit(1);
			}

			FileWriter fw = new FileWriter(pidFile, true);
			fw.write(pid);
			fw.close();
			System.out.println("Writing pidfile '" + pidFilename + "' with pid '" + pid + "'!");
			logger.info("Writing pidfile '" + pidFilename + "' with pid '" + pid + "'!");
		} catch (java.io.IOException ex) {
			logger.fatal("FATAL: " + ex.getMessage());
			System.exit(1);
		} catch (java.lang.SecurityException ex) {
			logger.fatal("FATAL: " + ex.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Remove a previous created pidfile.
	 * 
	 * @see createPidFile
	 */
	static synchronized public void removePidFile() {
		try {
			if (pidFile != null) {
				System.out.println("Removing pidfile '" + pidFile.getName() + "!");
				logger.info("Removing pidfile '" + pidFile.getName() + "!");
				pidFile.delete();
			}
		} catch (java.lang.SecurityException ex) {
			System.out.println("SecurityException: Removing pidfile '" + pidFile.getName() + "!");
			// NOOP
		}
	}

	static public synchronized boolean isProcessRunning() throws InternalError {
		String pidFilename = System.getProperties().getProperty("PIDFILE");
		try {
			if (pidFilename == null) {
				System.out.println("No pidfile specified in the property PIDFILE!");
				throw new InternalError("No pidfile specified in the property PIDFILE!");
			}
			
			Path path = FileSystems.getDefault().getPath(pidFilename);
			long pid = Long.parseLong(Files.readAllBytes(path).toString());
			Runtime runtime = Runtime.getRuntime();
			Process killProcess = runtime.exec(new String[] { "kill", "-0", "" + pid });
			long killProcessExitCode = killProcess.waitFor();
			return killProcessExitCode == 0;
		} catch (NumberFormatException ex) {
			System.out.println(
					"Cant parse the pid in file '" + pidFilename + "' to a number. Reason: " + ex.getMessage());
			logger.error("Cant parse the pid in file '" + pidFilename + "' to a number. Reason: " + ex.getMessage());
			throw new InternalError(
					"Cant parse the pid in file '" + pidFilename + "' to a number. Reason: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("Cant read PIDFILE '" + pidFilename + "'.");
			logger.error("Cant read PIDFILE '" + pidFilename + "'. Reason: " + ex.getMessage());
			throw new InternalError("Cant read PIDFILE '" + pidFilename + "'. Reason: " + ex.getMessage());
		} catch (Exception e) {
			System.out.println("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
			logger.error("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
			throw new InternalError("isProcessRunning: Unknown exception. Reason: " + e.getMessage());
		}
	}

}
