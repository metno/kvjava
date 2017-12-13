package no.met.kvclient.service;

public interface SendDataToKv {
	public class Result{
		public enum EResult { OK, NODECODER, DECODEERROR, NOTSAVED, ERROR};
		EResult res;
		String  message;
		String messageid;

		public Result( EResult res, String message, String messageid){
			this.res = res;
			this.message = message;
			this.messageid=messageid;

			if( messageid==null )
				this.messageid = "";

			if( message == null)
				this.message="";
		}

		public Result( EResult res, String message){
			this(res, message, null);
		}
		
		public EResult getResult() { return res;}
		public String getMessage(){ return message;}
		public String getMessageid(){ return messageid; }
		public Boolean isOk(){ return res==EResult.OK;}

		@Override
		public String toString()
		{
			return res.name()+ ": " + (message.isEmpty()?"(none)":message);
		}
	}
	
	Result sendData( String data, String decoder) throws Exception;
}
