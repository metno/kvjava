package no.met.kvclient;

public class ListenerEventRunner implements Runnable{
	ListenerEventQue que;
	Boolean inShutdown;
	
	public ListenerEventRunner(Boolean inShutdown){
		this.inShutdown = inShutdown;
		que=new ListenerEventQue(10);
	}
	
	public ListenerEventRunner(Boolean inShutdown, ListenerEventQue que){
		this.inShutdown = inShutdown;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
