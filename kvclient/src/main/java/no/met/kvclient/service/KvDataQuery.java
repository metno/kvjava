package no.met.kvclient.service;

import java.util.Optional;

import no.met.kvclient.KvDataEventListener;

public interface KvDataQuery {
	Optional<DataIterator> getData(WhichDataList whichData);
	
	boolean getKvData(WhichDataList whichData, KvDataEventListener listener);

	Optional<StationList> getStations();

	Optional<ParamList> getParams();

	Optional<ModelDataIterator> getModelData(WhichDataList whichData);

	/**
	 * getRejectdecode returns an iterator that can be used to get data from the
	 * rejectdecode table.
	 *
	 * \param decodeInfo_ Gives some information on the message we are
	 * interested in.
	 *
	 * - decodeList A list of decoders we wnt rejected message for.If the list
	 * is empty the message from all decoders is returned. - fromTime from this
	 * time to the \a toTime. - toTime from \a fromTime to this time.
	 *
	 * If \a fromTime is empty, it is taken to mean from 00:00 today. if \a to
	 * time is empty, it is taken to mean today current time. ie. if both are
	 * empty all rejected messages to day is received. \param it A iterator that
	 * can be used to traverse the rejceted data. \return true on success and
	 * false otherwise.
	 */
	Optional<RejectedIterator> getRejectdecode(RejectDecodeInfo decodeInfo);

	/**
	 * \param stationid the stationid we want reference stations for. \param
	 * paramsetid, the paramset we want. -1 means all. \param refStationList the
	 * result.
	 */
	Optional<Reference_stationList> getReferenceStation(long stationid, short paramsetid);

	/**
	 * getObsPgm returns the observation program for the stations given in
	 * stationIDList. If the stationIDList is empty the observation program for
	 * all stations is returned.
	 *
	 * If aUnion is true, only one obs_pgm structure is returned, but this
	 * structure is a union made of all parameters for the station. This means
	 * that we should expect at least one data entry for the times in the
	 * returned record.
	 * 
	 * \param obs_pgmList_ the observation program for the stations given in the
	 * stationIDList_. \param stationIdList_ the list of stations we want the
	 * obs_pgm from. \param aUnion do we want a union of the obs_pgm for the
	 * stations.
	 *
	 * \return true on success, false otherwise.
	 */
	Optional<Obs_pgmList> getObsPgm(StationIDList stationIDList, boolean aUnion);

	Optional<TypeList> getTypes();

	/**
	 * \brief getOperator returns a list \em of operators that is alloved to
	 * modify data in the HQC application.
	 *
	 * The operators is defined in the operator table in the kvalobs database.
	 *
	 * \param[out] a list of operators. \return true ons success and false
	 * otherwise.
	 */
	Optional<OperatorList> getOperator();

	Optional<Station_paramList> getStationParam(long stationid, long paramid, long day);
	void close();
}
