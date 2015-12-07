package no.met.kvalobs.kl2kv;

import java.nio.file.Path;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import no.met.kvutil.PropertiesHelper;
/*
 * 
#Konfigurasjon av log4j oppsettet til kl2kv.
#log4j.rootLogger=debug, stdout, R
log4j.rootLogger=info, stdout, R

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout

# Pattern to output the caller's file name and line number.
log4j.appender.stdout.layout.ConversionPattern=%5p [%t] - %m%n

log4j.appender.R=org.apache.log4j.RollingFileAppender
log4j.appender.R.File=${KVALOBS}/var/log/kl2kvnew.log

log4j.appender.R.MaxFileSize=10MB
# Keep one backup file
log4j.appender.R.MaxBackupIndex=1

log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=%d [%t] %-5p %c %x - %m%n

 */



public class InitLogger {
	static public Properties getLogProperties(Properties conf, Path logPath){
		String loglevelDefault = conf.getProperty("log.level", "debug");
		String maxsizeDefault = conf.getProperty("log.maxsize", "10MB");
		String path = conf.getProperty("log.path",logPath.toString());
		String maxBackupDafault=conf.getProperty("log.maxbackup","1");
		
		Properties prop=new Properties();
		prop.setProperty("log4j.rootLogger", loglevelDefault+", stdout, R");
		prop.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
	    prop.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.stdout.layout.ConversionPattern","%5p [%t] - %m%n");
		prop.setProperty("log4j.appender.R","org.apache.log4j.RollingFileAppender");
		prop.setProperty("log4j.appender.R.File", path+"/kl2kv.log");
		prop.setProperty("log4j.appender.R.MaxFileSize", maxsizeDefault);
		prop.setProperty("log4j.appender.R.MaxBackupIndex", maxBackupDafault);
		prop.setProperty("log4j.appender.R.layout","org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.R.layout.ConversionPattern","%d [%t] %-5p %c %x - %m%n");
		
		
		return prop;
	}
	
	static public Properties getLogProperties(String file, Path logPath){
		return getLogProperties(PropertiesHelper.loadFile(file), logPath);
	}
}
