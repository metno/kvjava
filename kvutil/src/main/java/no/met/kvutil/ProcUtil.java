package no.met.kvutil;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ProcUtil {
	static long pid = -1;
	
	static public long getPid() {
		if( pid > -1 )
			return pid;
		
		String sPid=System.getProperty("USEPID");
		
		if( sPid!=null) {
			try{
				pid = Long.valueOf(sPid);
			}catch( NumberFormatException ex) {
				System.out.println("Define USEPID is not a number '"+sPid+"'." );
			}
		}
		
		if( pid > -1)
			return pid;
		
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
 
		String jvmName = runtimeBean.getName();
		// System.out.println("JVM Name = " + jvmName);
		
		try {
			pid = Long.valueOf(jvmName.split("@")[0]);
			// System.out.println("JVM PID  = " + pid + " PPID: " +sPid);
		}
		catch( NumberFormatException ex){
		}
 
		return pid;
	}
	
	static public String getStackTrace() {
		return getStackTrace(null);
	}
	
	static public String getStackTrace(String comment) {
		try {
			if( comment != null )
				throw new Exception("---- " + comment +" ------");
			else
				throw new Exception("------- STACKTRACE ------------");
//			System.out.println("\n\n          ProcUtil.getStacktrace()    \n\n");
//			StackTraceElement st[] = Thread.currentThread().getStackTrace();
//			
//			if(st.length > 1) {
//				// The top st[0] element is the Thread.currentThread().getStackTrace() element
//				// and the second st[1] is this method. We just remove them from the stacktrace.
//				st=Arrays.copyOfRange(st, 2, st.length);
//			}
//		
//			if( st.length > 0 )
//				return Arrays.toString(st);
		}
		catch( SecurityException ex ) {
			ex.printStackTrace();
		}
		catch( Exception ex) {
			ex.printStackTrace();
			return Arrays.toString(ex.getStackTrace());
		}
		
		return "<No stack trace avalable>";
	}
	
	static public boolean isProcRunning(long pid) throws IOException, InterruptedException, Exception{
		Runtime rt=Runtime.getRuntime();
		
		Process proc=rt.exec("kill -0 "+ pid);
		
		int exitCode = proc.waitFor();
		
		if( exitCode == 0 )
			return true;
		else if( exitCode == 1)
			return false;
		else
			throw new Exception("Unknown exit code from 'kill -0 " + pid + "'.");
	}
	
	static public boolean isProcRunning(String pidfile) throws IOException, InterruptedException, Exception{
		Path p=Paths.get(pidfile);
		
		if( ! Files.exists(p) )
			return false;
		
		String pid=new String(Files.readAllBytes(p)).trim();
				
		if( pid.isEmpty() ) {
			Files.deleteIfExists(p);
			return false;
		}
		
		System.err.println("pid from file: '"+pid+"'.");
		
		if(isProcRunning(Long.parseLong(pid))) 
			return true;
		
		Files.deleteIfExists(p);
		return false;
	}

	
	static public int getPeakThreadCount() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		 
		int peakThreadCount = bean.getPeakThreadCount();
		//System.out.println("Peak Thread Count = " + peakThreadCount);
		return peakThreadCount;
	}
}
