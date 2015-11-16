package kvalobs.service.impl;

import java.util.Optional;

import kvalobs.service.DataIterator;
import kvalobs.service.ModelDataIterator;
import kvalobs.service.Obs_pgmList;
import kvalobs.service.OperatorList;
import kvalobs.service.ParamList;
import kvalobs.service.Reference_stationList;
import kvalobs.service.RejectDecodeInfo;
import kvalobs.service.RejectedIterator;
import kvalobs.service.StationIDList;
import kvalobs.service.StationList;
import kvalobs.service.Station_paramList;
import kvalobs.service.TypeList;
import kvalobs.service.WhichDataList;
import kvalobs.service.KvDataQuery;

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
