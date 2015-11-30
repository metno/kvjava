package no.met.kvclient;

import java.util.concurrent.atomic.AtomicBoolean;

public class ListenerEventRunner implements Runnable{
	ListenerEventQue que;
	AtomicBoolean inShutdown;
	boolean isInterupted;
	public ListenerEventRunner(AtomicBoolean inShutdown){
		this.inShutdown = inShutdown;
		que=new ListenerEventQue(10);
	}
	
	public ListenerEventRunner(AtomicBoolean inShutdown, ListenerEventQue que){
		this.isInterupted = false;
		this.inShutdown = inShutdown;
		
		if( que==null)
			this.que=new ListenerEventQue(10);
		else
			this.que=que;
	}
	
	void runEvent(int timeout){
		ListenerEvent event=null;
		try {
			event = que.getObject(timeout);
			if( event!=null){
				event.run();
			}
		} catch (InterruptedException e) {
			isInterupted=true;
		}
	}
	
	public ListenerEventQue getQue() {
		return que;
	}
	
	@Override
	public void run(){
		String myName=Thread.currentThread().getName();
		System.err.println("Starting: "+myName);
		while(!inShutdown.get() && ! isInterupted){
			runEvent(100);
		}
		//Drain the que before ending.
		while(que.size()>0)
			runEvent(100);
		
		System.err.println("Terminating: "+myName + " Interupted: " + (isInterupted?"true":"false"));
	}
}
