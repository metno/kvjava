package no.met.kvclient;

public class ListenerEventRunner implements Runnable{
	ListenerEventQue que;
	Boolean inShutdown;
	
	protected ListenerEventRunner(Boolean inShutdown){
		this.inShutdown = inShutdown;
		que=new ListenerEventQue();
	}
	
	void runEvent(int timeout){
		ListenerEvent event=que.getEvent(timeout);
		if( event!=null){
			event.run();
		}
	}
	
	public ListenerEventQue getQue() {
		return que;
	}
	
	@Override
	public void run(){
		while(!inShutdown){
			runEvent(100);
		}
		//Drain the que before ending.
		while(que.size()>0)
			runEvent(100);
	}
}
