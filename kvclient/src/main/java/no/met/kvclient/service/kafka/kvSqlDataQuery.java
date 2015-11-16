package no.met.kvclient.service.kafka;

import java.util.Optional;

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
import no.met.kvclient.service.WhichDataList;
import no.met.kvclient.service.KvDataQuery;

public class kvSqlDataQuery implements KvDataQuery {

	@Override
	public Optional<DataIterator> getData(WhichDataList whichData) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

}
