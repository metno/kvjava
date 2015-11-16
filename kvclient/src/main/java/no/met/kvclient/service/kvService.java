package no.met.kvclient.service;

import java.util.Optional;

public class kvService implements KvSubsribeData, KvDataQuery {
	KvSubsribeData subscribe;
	KvDataQuery query;

	public kvService(KvSubsribeData subscribe, KvDataQuery query) {
		this.subscribe = subscribe;
		this.query = query;
	}

	@Override
	public Optional<DataIterator> getData(WhichDataList whichData) {
		return query.getData(whichData);
	}

	@Override
	public Optional<StationList> getStations() {
		return query.getStations();
	}

	@Override
	public Optional<ParamList> getParams() {
		return query.getParams();
	}

	@Override
	public Optional<ModelDataIterator> getModelData(WhichDataList whichData) {
		return query.getModelData(whichData);
	}

	@Override
	public Optional<RejectedIterator> getRejectdecode(RejectDecodeInfo decodeInfo) {
		return query.getRejectdecode(null);
	}

	@Override
	public Optional<Reference_stationList> getReferenceStation(long stationid, short paramsetid) {
		return query.getReferenceStation(paramsetid, paramsetid);
	}

	@Override
	public Optional<Obs_pgmList> getObsPgm(StationIDList stationIDList, boolean aUnion) {
		return query.getObsPgm(stationIDList, aUnion);
	}

	@Override
	public Optional<TypeList> getTypes() {
		return query.getTypes();
	}

	@Override
	public Optional<OperatorList> getOperator() {
		return query.getOperator();
	}

	@Override
	public Optional<Station_paramList> getStationParam(long stationid, long paramid, long day) {
		return query.getStationParam(stationid, paramid, day);
	}

	@Override
	public SubscribeId subscribeDataNotify(DataSubscribeInfo info, kvDataNotifySubscriber sub) {
		return subscribe.subscribeDataNotify(info, sub);
	}

	@Override
	public SubscribeId subscribeData(DataSubscribeInfo info, kvDataSubscriber sub) {
		return subscribe.subscribeData(info, sub);
	}

	@Override
	public SubscribeId subscribeKvHint(kvHintSubscriber sub) {
		return subscribe.subscribeKvHint(sub);
	}

	@Override
	public void unsubscribe(SubscribeId subid) {
		subscribe.unsubscribe(subid);
	}

}
