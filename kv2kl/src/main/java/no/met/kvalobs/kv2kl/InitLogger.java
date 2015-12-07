package no.met.kvalobs.kv2kl;

import java.nio.file.Path;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import no.met.kvutil.PropertiesHelper;

public class InitLogger {
	static public Properties getLogProperties(Properties conf, Path logPath){
		String loglevelDefault = conf.getProperty("log.level", "debug");
		String loglevelFilter  = conf.getProperty("log.level.filter", loglevelDefault);
		String maxsizeDefault = conf.getProperty("log.maxsize", "10MB");
		String maxsizeFilter = conf.getProperty("log.maxsize.filter", maxsizeDefault);
		String path = conf.getProperty("log.path",logPath.toString());
		String maxBackupDafault=conf.getProperty("log.maxbackup","1");
		String maxBackupFilter=conf.getProperty("log.maxbackup.filter",maxBackupDafault);
		
		Properties prop=new Properties();
		prop.setProperty("log4j.rootLogger", loglevelDefault+", stdout, R");
		prop.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
	    prop.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.stdout.layout.ConversionPattern","%5p [%t] - %m%n");
		prop.setProperty("log4j.appender.R","org.apache.log4j.RollingFileAppender");
		prop.setProperty("log4j.appender.R.File", path+"/kv2kl.log");
		prop.setProperty("log4j.appender.R.MaxFileSize", maxsizeDefault);
		prop.setProperty("log4j.appender.R.MaxBackupIndex", maxBackupDafault);
		prop.setProperty("log4j.appender.R.layout","org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.R.layout.ConversionPattern","%d [%t] %-5p %c %x - %m%n");
		
		prop.setProperty("log4j.logger.filter", loglevelFilter+", B");
		prop.setProperty("log4j.appender.B","org.apache.log4j.RollingFileAppender");
		prop.setProperty("log4j.appender.B.File",path+"/kv2kl_filter.log");
		prop.setProperty("log4j.appender.B.MaxFileSize",maxsizeFilter);
		prop.setProperty("log4j.appender.B.MaxBackupIndex", maxBackupFilter);
		prop.setProperty("log4j.appender.B.layout","org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.B.layout.ConversionPattern","%d [%t] %-5p %c %x - %m%n");
		return prop;
	}
	
	static public Properties getLogProperties(String file, Path logPath){
		return getLogProperties(PropertiesHelper.loadFile(file), logPath);
	}
}
