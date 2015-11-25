/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: KvApp.java,v 1.1.2.6 2007/09/27 09:02:41 paule Exp $                                                       

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
package no.met.kvclient;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EventListener;
import java.util.LinkedList;
import no.met.kvclient.priv.*;
import no.met.kvclient.service.*;

public class KvApp {
	final private int nGetDataThreads = 10;
	private long nextSubscriberId = 0;
	// private KvEventThread kvEventThread;
	private ListenerEventQue eventQue=new ListenerEventQue(100);
	private DataSubscribers<KvDataNotifyEventListener> dataNotifyList = new DataSubscribers<>(eventQue);
	private DataSubscribers<KvDataEventListener> dataList = new DataSubscribers<>(eventQue);
	private HintSubscribers hintList = new HintSubscribers();
	private LinkedList<Admin> adminList = new LinkedList<Admin>();
	private kvService service = null;
	private no.met.kvclient.datasource.Data dataInput = null;
	private String kvServer = null;
	//private KvEventQue eventQue = null;
	private ShutdownHook hook = null;
	private boolean isShutdown = false;
	

	// Must be called in by a synchronized method
	private SubscribeId getSubscriberId(String prefix) {
		String id = prefix + nextSubscriberId;
		++nextSubscriberId;
		return new SubscribeId(id);
	}

	// GetDataThreads is accessed by this thread and getDataThreadManager.
	// All access that add og delete an entry in the array must be
	// synchronized.
	private GetDataThread[] getDataThreads = new GetDataThread[nGetDataThreads];

	private GetDataThreadManager getDataThreadManager = null;
	private PrintWriter debuglog = null;
	static public KvApp kvApp = null;

	/**
	 * This is the Application (KvApp) class for the Java interface to the
	 * kvalobs sevice API. This is a singleton class. This means that it must be
	 * one instance only of this class in the program. It is the users
	 * responsibility to ensure this.
	 * <p/>
	 * If we are NOT using SWING (usingSwing==false) and we use the push
	 * interface to kvalobs, ie we are calling at least one of the functions:
	 * subscribeKvDataNotifyListener, subscribeKvDataListener or
	 * subscribeKvHintListener. We have to start our own eventloop. We do that
	 * with a call to the run function. The run functions executes until the JVM
	 * shutsdown. This happens by sending the JVM the one of the terminating
	 * signals, ie SIGTERM, SIGABORT, SIGINT (Ctrl-C), SIGQUIT,.. When we
	 * receives the interupt we clean up the connection with kvalobs and exits.
	 * <p/>
	 * If we use SWING and want the kvalobs events delivered on the SWING event
	 * que set usingSwing=true. In this case dont call run.
	 * 
	 * @param args
	 *            the arguments from the command line.
	 * @param kvServer_
	 *            the name of kvalobs in the CORBA nameserver, ex kvtest (rime),
	 *            kvalobs (main), ...
	 * @param usingSwing
	 *            true use swing (DONT call run), false execute our own mainloop
	 *            (call run).
	 */
	public KvApp(String[] args, String kvServer_, boolean usingSwing, java.util.Properties prop) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--KvDebug")) {
				try {
					debuglog = new PrintWriter(new FileWriter("KvDebug.log", false));
				} catch (IOException ex) {
					System.out.println("Cant open debuglogfile <KvDebug.log>!");
					System.exit(1);
				}
				break;
			}
		}

		hook = new ShutdownHook(this);

		Runtime.getRuntime().addShutdownHook(hook);
		kvServer = kvServer_;
		kvApp = this;

		// We set the daemon flag for the CORBA thread and GetDataThread
		// too ensure that they run while the shutdown is in progress and the
		// ShutdownHook is running.

		getDataThreadManager = new GetDataThreadManager(getDataThreads);
		getDataThreadManager.setDaemon(true);
		getDataThreadManager.start();

		if (!usingSwing) {
			//eventQue = new KvEventQue();
			// TODO: Must start a kafka thread
			// corbaThread = new CorbaThread(args, eventQue, this, prop);
		} else {
			// corbaThread = new CorbaThread(args, null, this, prop);
		}
		// corbaThread.setDaemon(true);
		// System.out.println("CORBA thread deamon: " + corbaThread.isDaemon());

		// corbaThread.start();

		// while (!corbaThread.isInitilalized())
		// ;

	}

	synchronized void removeAllAdmin() {
	}

	synchronized public void setKvServer(String kvserver) {
		kvServer = kvserver;
	}

	synchronized public String getKvServer() {
		return kvServer;
	}

	/**
	 * onExit is called when the run method is about to return. This function
	 * can be used to do some last minute cleanup before the application
	 * terminate.
	 */
	protected void onExit() {
	}

	/**
	 * subscribe to the KvDataNotify event.
	 *
	 * @param subscribeInfo
	 *            which stations are we interested to receives event from.
	 * @param listener
	 *            the endpoint we want the event delivered to.
	 * @return null when we cant register with kvalobs. And a subscriber id on
	 *         success.
	 * 
	 */
	synchronized public SubscribeId subscribeKvDataNotifyListener(KvDataSubscribeInfo subscribeInfo,
			KvDataNotifyEventListener listener) {
		SubscribeId subid = getSubscriberId("data_notify_subscriber_");
		dataNotifyList.addSubscriber(subid, subscribeInfo, listener);

		return subid;
	}

	/**
	 * subscribe to the KvData event.
	 *
	 * @param subscribeInfo
	 *            which stations are we interested to receives event from.
	 * @param listener
	 *            the endpoint we want the event delivred to.
	 * @return null when we cant register with kvalobs. And a subscriber id on
	 *         success.
	 * 
	 */
	synchronized public SubscribeId subscribeKvDataListener(DataSubscribeInfo subscribeInfo,
			KvDataEventListener listener) {
		SubscribeId subid = getSubscriberId("data_subscriber_");
		dataList.addSubscriber(subid, subscribeInfo, listener);

		return subid;
	}

	/**
	 * subscribe to the KvHint event. kvalobs sends a hint when it is started to
	 * all who has registred an interest in this status. It will also send a
	 * hint when it is stopped normaly. If kvalobs is going down abnormally this
	 * signal may or may not be sendt.
	 *
	 * @param listener
	 *            the endpoint we want the event delivred to.
	 * @return null when we cant register with kvalobs. And a subscriber id on
	 *         success.
	 * 
	 */

	synchronized public SubscribeId subscribeKvHintListener(KvHintEventListener listener) {
		SubscribeId subid = getSubscriberId("hint_subscriber_");
		hintList.addSubscriber(subid, listener);
		return subid;
	}

	synchronized public boolean unsubscribeKv(SubscribeId subscriberid) {
		return false;
	}

	synchronized public boolean unsubscribeKvAll() {
		return false;
	}

//	synchronized public void postKvEvent(no.met.kvclient.priv.Event event) {
//		switch (event.getEventType()) {
//		case DataEvent: {
//			dataList.callListeners(event.getSource(), event.getObsData());
//			dataNotifyList.callListeners(event.getSource(), event.getObsData());
//		}
//			break;
//		case HintEvent:
//			hintList.callListeners(event.getSource(), event.getHint());
//			break;
//		case GetDataEvent: {
//			KvDataEventListener listener = event.getGetDataEventListener();
//			ObsDataList obsData = event.getObsData();
//
//			if (listener != null) {
//				listener.kvDataEvent(new KvDataEvent(event.getSource(), obsData));
//			}
//		}
//			break;
//		default:
//			System.out.println("PostKvEvent: Unknown KvEvent (" + event.getEventType() + ")!");
//		}
//	}

	public no.met.kvclient.service.Station[] getKvStations() {
		return null;
	}

	public no.met.kvclient.service.Param[] getKvParams() {
		return null;
	}

	public no.met.kvclient.service.Types[] getKvTypes() {
		return null;
	}

	/**
	 * Get station_param from kvalobs database.
	 *
	 * @param stationid
	 *            The stationid for whihc to et param
	 * @param paramid
	 *            paramid to get. If paramid is less than 0, all paramid will be
	 *            fetched.
	 * @param day
	 *            Day of year. If day is less than 0: all days.
	 *
	 * @return An array containing the specified station parameters.
	 */
	public no.met.kvclient.service.Station_param[] getKvStationParam(int stationid, int paramid, int day) {
		return null;
	}

	public RejectedIterator getKvRejectdecode(RejectDecodeInfo decodeInfo) {
		return null;
	}

	public no.met.kvclient.service.Obs_pgm[] getKvObsPgm(int[] stationIDList, boolean aUnion) {
		return null;
	}

	/**
	 * getKvData, starts a background thread that pulls data from kvalobs. Use
	 * WhitchData to specify the stations you want data from. A stationid of 0
	 * give you the data from all stations in kvalobs.
	 *
	 * You must also specify a KvDataEventListner that the data shold pe posted
	 * to.
	 *
	 * @param whichData
	 *            the stations and period we want data for.
	 * @param listener
	 *            where we want the data delivred!
	 * @return true on success and false otherwise.
	 */

	synchronized public boolean getKvData(no.met.kvclient.service.WhichDataList whichData, KvDataEventListener listener) {
		return false;
	}

	/**
	 * getKvData, start a pull of data from kvalobs. An iterator that is used to
	 * pull the data from kvalobs i returned. Rember to call 'destroy' on the
	 * iterator when all the data is received. The iterator consumes reources on
	 * the kvalobs server, this resources is released when 'destroy' is called.
	 *
	 * Use WhitchData to specify the stations you want data from. A stationid of
	 * 0 give you the data from all stations in kvalobs.
	 *
	 * @param whichData
	 *            the stations and period we want data for.
	 * @return KvDataIterator to pull the data from kvalobs or null if failed.
	 */
	public KvDataIterator getKvData(no.met.kvclient.service.WhichDataList whichData) {
		return null;
	}

	/**
	 * sendDataToKv, sends data to kvalobs, kvDataInputd. ObsType is used to
	 * tell kvalobs what format the data is coded in. It is used by kvDataInputd
	 * to select the proper decoder to be used to decode the data. <p\>
	 * 
	 * The method return a Result. The Result has two fields.
	 * 
	 * <pre>
	 * - EResult res
	 * - string  message
	 *
	 *  The value of res:
	 *     NODECODER,   there is no decoder for the obsType.
	 *                  The observation is not saved to the database. Don't
	 *                  mind to retry to send the observation until a
	 *                  decoder is written and installed.
	 *     DECODEERROR, cant decode the message. The
	 *                  message is saved to rejectdecode.
	 *     NOTSAVED,    the message is not SAVED to the database,
	 *                  if possible try to resend it later, after 
	 *                  a delay.
	 *     ERROR,       A general error. Look at the 'message'. The
	 *                  observation is not saved to the database.
	 *     OK           The message is decoded and saved to the 
	 *                  database.
	 * </pre>
	 * 
	 * If the value of res is NOT OK a error message is written in message.
	 *
	 * @param data
	 *            the data coded in the format given with obsType.
	 * @param obsType
	 *            the format of the data.
	 * @return A reference to a Result if we successfully connected to kvinput.
	 *         null if we failed to connect with kvinput. kvinput may be down or
	 *         the CORBA nameserver may be down.
	 */

	public no.met.kvclient.datasource.Result sendDataToKv(String data, String obsType) {
		return null;
	}

	/**
	 * sendKlDataToKv, sends data to kvalobs, kvDataInputd. It sends data to be
	 * decoded by the 'kldecoder' in kvDataInputd. <p\> The dataformat is:
	 * 
	 * <pre>
	 *  pc1[(&lt;sensor>,&lt;level>)],...,pcN[(&lt;sensor>,&lt;level>)]
	 *  YYYYMMDDhhmmss,pc1_value,....,pcN_value
	 *  YYYYMMDDhhmmss,pc1_value,....,pcN_value
	 *  ....
	 *  YYYYMMDDhhmmss,pc1_value,....,pcN_value
	 *
	 *  pc - paramcode, the name of the parameter. An underscore indicate that 
	 *                  this is a code value. Suported pc that can have a code 
	 *                  value is: HL and VV. The value vil be converted til 
	 *                  meter.
	 *  If sensor or level is not specified. The default would apply. If both 
	 *  shall take the default value, the paranteses can be left out.
	 * </pre>
	 *
	 * The method return a Result. The Result has two fields.
	 * 
	 * <pre>
	 * - EResult res
	 * - string  message
	 *
	 *  The value of res:
	 *     NODECODER,   there is no decoder for the obsType.
	 *                  The observation is not saved to the database. Don't
	 *                  mind to retry to send the observation until a
	 *                  decoder is written and installed.
	 *     DECODEERROR, cant decode the message. The
	 *                  message is saved to rejectdecode.
	 *     NOTSAVED,    the message is not SAVED to the database,
	 *                  if possible try to resend it later, after 
	 *                  a delay.
	 *     ERROR,       A general error. Look at the 'message'. The
	 *                  observation is not saved to the database.
	 *     OK           The message is decoded and saved to the 
	 *                  database.
	 * </pre>
	 * 
	 * If the value of res is NOT OK a error message is written in message.
	 *
	 * @param data
	 *            the data coded in the format expected by the kldecoder.
	 * @param nationalid
	 *            the nationalid to the data.
	 * @param typeid
	 *            the typeid for the data.
	 * @return A reference to a Result if we successfully connected to kvinput.
	 *         null if we failed to connect with kvinput. kvinput may be down or
	 *         the CORBA nameserver may be down.
	 */

	public no.met.kvclient.datasource.Result sendKlDataToKv(String data, long nationalid, long typeid) {
		String obsType = "kldata/nationalnr=" + Long.toString(nationalid) + "/type=" + Long.toString(typeid);

		return sendDataToKv(data, obsType);
	}

	public void run() {
		ListenerEvent event = null;

		if (eventQue == null) {
			System.out.println("WARNING: We are using the event que to SWING!");
			return;
		}

		while (!isShutdown) {
			try {
				event = eventQue.getObject(1000);
				
				if (event == null)
					continue;

				try {
					System.out.println("KvApp: call postKvEvent!");
					event.run();
					System.out.println("KvApp: return postKvEvent!");
				} catch (Exception ex) {
					if (debuglog != null) {
						debuglog.println("KvApp::run: Exception in postKvEvent!");
						ex.printStackTrace(debuglog);
						debuglog.flush();
					} else {
						System.out.println("KvApp::run: Exception in postKvEvent!");
						ex.printStackTrace();
					}

					// throw away all uncaught exception from "user" code.
				}
			} catch (Exception ex) {
				if (debuglog != null) {
					debuglog.println("KvApp::run: Exception in getEvent!");
					ex.printStackTrace(debuglog);
					debuglog.flush();
				} else {
					System.out.println("KvApp::run: Exception in getEvent!");
					ex.printStackTrace();
				}
			}
		}

		System.out.println("KvApp::run: terminating the eventque!");
	}

	public synchronized void exit() {

		System.out.println("KvApp.exit() called!");

		if (isShutdown)
			return;

		System.out.println("KvApp.exit() called (Shutdown)!");

		unsubscribeKvAll();
		removeAllAdmin();
		// corbaThread.shutdown();
		getDataThreadManager.shutdown();

		boolean retry = true;

		while (retry) {
			retry = false;

			try {
				getDataThreadManager.join();
				System.out.println("exit: getDataThreadManager termineted!");
			} catch (InterruptedException e) {
				System.out.println("exit (getDataThreadManager): join iterupted!");
				retry = true;
			}

		}

		retry = true;

		while (retry) {
			retry = false;

			// try {
			// // corbaThread.join();
			// System.out.println("exit: CorbaThread termineted!");
			// } catch (InterruptedException e) {
			// System.out.println("exit (corbaThread): join iterupted!");
			// retry = true;
			// }

		}

		try {
			// It is two scenaries where exit is called.
			//
			// 1. The "user" code call this function.
			// 2. We are called from the ShutdownHook.
			//
			// In 1 we want to remove the hook so that the function is not
			// called a second time from the hook. There is now way to test
			// if we JWM is in shutdown. So we just call removeShutdownHook
			// and let the JVM decide if we can remove the hook. If we cant
			// it will throw IllegalStateException, but that is just fine.
			//
			if (hook != null) {
				Runtime.getRuntime().removeShutdownHook(hook);
				hook = null;
			}
		} catch (IllegalStateException ex) {
			System.out.println("IllegalStateException: exit: This is ok!");
			// The JVM is in the process to shutdown. We cant remove the hook,
			// but that is ok.
		} catch (SecurityException ex) {
			System.out.println("SecurityException: exit: Hmmm!");
			// We are not allowed to do this.
		}

		onExit();
		isShutdown = true;
	}
}
