package no.met.kvclient.service;

public interface SendDataToKv {
	public class Result{
		public enum EResult { OK, NODECODER, DECODEERROR, NOTSAVED, ERROR };
		EResult res;
		String  message;
		
		public Result( EResult res, String message){
			this.res = res;
			this.message = message;
		}
		
		public EResult getResult() { return res;}
		public String getMessage(){ return message;}
	}
	
	Result sendData( String data, String decoder);
}
