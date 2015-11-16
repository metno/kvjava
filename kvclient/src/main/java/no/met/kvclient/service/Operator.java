package kvalobs.service;

public class Operator {
	public String username;
	public long userid;

	public Operator(String username, long userid) {
		this.username = username;
		this.userid = userid;
	}

	public Operator(Operator other) {
		this.username = other.username;
		this.userid = other.userid;
	}
}
