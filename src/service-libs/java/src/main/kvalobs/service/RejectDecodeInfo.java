package kvalobs.service;

public class RejectDecodeInfo {
	public java.util.LinkedList<String> decodeList = null;
	public String fromTime;
	public String toTime;

	public RejectDecodeInfo(java.util.LinkedList<String> decodeList, String fromTime, String toTime) {
		decodeList = new java.util.LinkedList<>();
		this.fromTime = fromTime;
		this.toTime = toTime;
	}
	public RejectDecodeInfo(String fromTime, String toTime) {
		decodeList = new java.util.LinkedList<>();
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	public RejectDecodeInfo(RejectDecodeInfo other) {
		this.decodeList = other.decodeList;
		this.fromTime = other.fromTime;
		this.toTime = other.toTime;
	}

	public void addDecoder(String decoder) {
		decodeList.addLast(decoder);
	}
}
