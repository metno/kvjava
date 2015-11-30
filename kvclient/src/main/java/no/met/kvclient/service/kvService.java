package no.met.kvclient.service;

import java.util.Optional;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;

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
	public boolean getKvData(WhichDataList whichData, KvDataEventListener listener){
		return query.getKvData(whichData, listener);
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
	public SubscribeId subscribeDataNotify(DataSubscribeInfo info, KvDataNotifyEventListener listener){
		return subscribe.subscribeDataNotify(info, listener);
	}
	
	@Override
	public SubscribeId subscribeData(DataSubscribeInfo info, KvDataEventListener listener)
	{
		return subscribe.subscribeData(info, listener);
	}

	
	
	@Override
	public SubscribeId subscribeKvHint(kvHintSubscriber sub) {
		return subscribe.subscribeKvHint(sub);
	}

	@Override
	public void unsubscribe(SubscribeId subid) {
		subscribe.unsubscribe(subid);
	}

	@Override
	public void close() {
		subscribe.close();
		query.close();
	}

}
