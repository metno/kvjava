package no.met.kvclient.service.sql;

import java.util.Optional;
import java.util.Properties;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.service.DataIterator;
import no.met.kvclient.service.ModelDataIterator;
import no.met.kvclient.service.Obs_pgmList;
import no.met.kvclient.service.OperatorList;
import no.met.kvclient.service.ParamList;
import no.met.kvclient.service.Reference_stationList;
import no.met.kvclient.service.RejectDecodeInfo;
import no.met.kvclient.service.RejectedIterator;
import no.met.kvclient.service.StationIDList;
import no.met.kvclient.service.StationList;
import no.met.kvclient.service.Station_paramList;
import no.met.kvclient.service.TypeList;
import no.met.kvclient.service.WhichData;
import no.met.kvclient.service.WhichDataList;
import no.met.kvclient.service.KvDataQuery;

public class SqlDataQuery implements KvDataQuery {
	ListenerEventQue que;

	public SqlDataQuery(Properties prop) {
		que = new ListenerEventQue(Integer.parseInt(prop.getProperty("que.size", "10")));
	}

	public SqlDataQuery(Properties prop, ListenerEventQue que) {
		this.que = que;
	}

	@Override
	public Optional<DataIterator> getData(WhichDataList whichData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getKvData(WhichDataList whichData, KvDataEventListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<StationList> getStations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ParamList> getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ModelDataIterator> getModelData(WhichDataList whichData) {
		StringBuilder q = new StringBuilder();
		q.append("select * from model_data");

		if (whichData != null && !whichData.isEmpty()) {
			boolean first = true;
			q.append(" where ");
			for (WhichData e : whichData) {
				if (!first)
					q.append(" or ");
				else
					first = false;

				q.append("(stationid=" + e.stationid + " and ");

				if (e.fromTime.compareTo(e.toTime) == 0)
					q.append("obstime='" + e.fromTime + "')");
				else
					q.append("obstime between '" + e.fromTime + "' and '" + e.toTime + "')");
			}
		}
		String sql=q.toString();
		//TODO: Do the database query.
		return Optional.ofNullable(null);
	}

	@Override
	public Optional<RejectedIterator> getRejectdecode(RejectDecodeInfo decodeInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Reference_stationList> getReferenceStation(long stationid, short paramsetid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Obs_pgmList> getObsPgm(StationIDList stationIDList, boolean aUnion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<TypeList> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<OperatorList> getOperator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Station_paramList> getStationParam(long stationid, long paramid, long day) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

}
