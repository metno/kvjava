package no.met.kvclient.service;

import no.met.kvclient.ListenerEventQue;

public class DataSubscribeInfo {
	public StationIDList ids; // Send info for all stations if the list is
								// empty.
	public StatusId status;
	public QcIdList qc; // Send info for all QC's if the list is empty.
	public ListenerEventQue que; // Run the listener in the thread that pulls
									// data from this que

	public DataSubscribeInfo(StationIDList ids, StatusId status, QcIdList qc, ListenerEventQue que) {
		this.ids = ids;
		this.status = status;
		this.qc = qc;
		this.que = que;
	}

	public DataSubscribeInfo(StationIDList ids, StatusId status, QcIdList qc) {
		this(ids, status, qc, null);
	}

	public DataSubscribeInfo(DataSubscribeInfo other) {
		this.ids = other.ids;
		this.status = other.status;
		this.qc = other.qc;
	}

	public DataSubscribeInfo() {
		ids = new StationIDList();
		qc = new QcIdList();
		status = StatusId.All;
		que = null;
	}

	public void setStatusId(StatusId statusid) {
		this.status = statusid;
	}

	public void addStationId(long stationid) {
		ids.addLast(new Long(stationid));
	}

	public void addQcId(QcId qcId) {
		qc.addLast(qcId);
	}

	// Test if this data element is among the data defined by this
	// DataSubscriberInfo.
	public boolean has(DataElem data) {
		boolean possibly = false;
		int controlTypeFlag = data.useinfo.length() > 0 ? Integer.parseInt(data.useinfo.substring(0, 1)) : -1;
		int qcFlag = data.useinfo.length() > 2 ? Integer.parseInt(data.useinfo.substring(2, 3)) : -1;

		if (ids.isEmpty()) {
			possibly = true;
		} else {
			for (java.lang.Long id : ids) {
				if (id.longValue() == data.stationID) {
					possibly = true;
					break;
				}
			}
		}

		if (!possibly)
			return false;

		if (controlTypeFlag > -1 && !qc.isEmpty()) {
			boolean qcRes = false;
			for (QcId qcid : qc) {
				if (qcid.has(controlTypeFlag)) {
					qcRes = true;
					break;
				}
			}
			if (!qcRes)
				return false;
		}

		return status.has(qcFlag);
	}

}
