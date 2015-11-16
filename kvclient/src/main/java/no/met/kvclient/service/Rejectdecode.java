package no.met.kvclient.service;

public class Rejectdecode {
	public String message;
	public String tbtime;
	public String decoder;
	public String comment;
	public boolean is_fixed;

	public Rejectdecode(String message, String tbtime, String decoder, String comment, boolean is_fixed) {
		this.message = message;
		this.tbtime = tbtime;
		this.decoder = decoder;
		this.comment = comment;
		this.is_fixed = is_fixed;
	}

	public Rejectdecode(Rejectdecode other) {
		this.message = other.message;
		this.tbtime = other.tbtime;
		this.decoder = other.decoder;
		this.comment = other.comment;
		this.is_fixed = other.is_fixed;
	}
}
