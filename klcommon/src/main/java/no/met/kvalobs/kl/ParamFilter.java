/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: ParamFilter.java,v 1.1.2.5 2007/09/27 09:02:19 paule Exp $                                                       

  Copyright (C) 2007 met.no

  Contact information:
  Norwegian Meteorological Institute
  Box 43 Blindern
  0313 OSLO
  NORWAY
  email: kvalobs-dev@met.no

  This file is part of KVALOBS

  KVALOBS is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 
  of the License, or (at your option) any later version.
  
  KVALOBS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License along 
  with KVALOBS; if not, write to the Free Software Foundation Inc., 
  51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package no.met.kvalobs.kl;

import java.sql.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.Comparable;

import no.met.kvutil.dbutil.*;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.TextDataElem;
import no.met.kvutil.dbutil.IQuery;
import org.apache.log4j.Logger;

/**
 * The ParamFilter class is a helper class used to filter data on parameter.
 *
 * It use two helper tables from the database it is connected to. These tables
 * are T_KV2KLIMA_TYPEID_PARAM_FILTER and T_KV2KLIMA_PARAM_FILTER. The first
 * table, T_KV2KLIMA_TYPEID_PARAM_FILTER, contains values that is common for all
 * typeids. The seccond table, T_KV2KLIMA_PARAM_FILTER, contains values that is
 * specific for one stationid/typeid combination. Values in
 * T_KV2KLIMA_PARAM_FILTER, may add or remove to the values in the
 * T_KV2KLIMA_TYPEID_PARAM_FILTER table. If a paramid is negativ it is removed
 * from the filter and a postiv paramid add to the filter. A timespan must be
 * given for when the values is valid. The timespan is given with a from date
 * (fdato) and a to date (tdato). Note that for the add/remove to function as
 * specified the fdato and tdato must match exactly.
 *
 * If a typeid in the filter table is negativ, ie less than 0 it only blocks
 * inncomming data with a negativ typeid. While a positiv typeid in the
 * filtertables blocks both data with nagativ and positiv typeids.
 *
 * The fdato and tdato is valid for the timespan [fdato,tdato].
 *
 * <b>Examle of filter setup</b><br/>
 * If you generally dont want data for params SA (112) and SD (18) for typeid
 * 302, but you want SD for station 18700. <br>
 *
 * <pre>
     insert into  T_KV2KLIMA_TYPEID_PARAM_FILTER VALUES(302, 112, 0, 0, NULL, NULL);
     insert into  T_KV2KLIMA_TYPEID_PARAM_FILTER VALUES(302,  18, 0, 0, NULL, NULL);

     insert into  T_KV2KLIMA_PARAM_FILTER VALUES(18700, 302, -18, 0, 0, NULL, NULL);
 * </pre>
 *
 * And if you want to block RR_X (117) from station 18700, you add this line to
 * the table T_KV2KLIMA_PARAM_FILTER VALUES.
 *
 * <pre>
 *
     insert into  T_KV2KLIMA_PARAM_FILTER VALUES(18700, 302, 117, 0, 0, NULL, NULL);
 * </pre>
 *
 * @author borgem
 *
 */
public class ParamFilter {

	static Logger logger = Logger.getLogger(ParamFilter.class);

	long stationid;
	HashMap<Long, LinkedList<ParamElem>> types;
//	DbConnection con;
	Matrics matrics;

	static class ParamElem implements Comparable<Object> {
		final static long MILIS_IN_YEAR = 31536000000L;
		final static long MIN_YEAR = -1900 * MILIS_IN_YEAR;
		final static long MAX_YEAR = 6000 * MILIS_IN_YEAR;
		long typeid;
		int paramid;
		int sensor;
		int level;
		Timestamp fdato;
		Timestamp tdato;

		ParamElem(long typeid, int paramid, int sensor, int level, Timestamp fdato, Timestamp tdato) {
			this.typeid = typeid;
			this.paramid = paramid;
			this.sensor = sensor;
			this.level = level;

			if (fdato == null)
				this.fdato = new Timestamp(MIN_YEAR);
			else
				this.fdato = fdato;

			if (tdato == null)
				this.tdato = new Timestamp(MAX_YEAR);
			else
				this.tdato = tdato;

		}

		ParamElem(long typeid, int paramid, int sensor, int level) {
			this.typeid = typeid;
			this.paramid = paramid;
			this.sensor = sensor;
			this.level = level;
			this.fdato = new Timestamp(MIN_YEAR);
			this.tdato = new Timestamp(MAX_YEAR);
		}

		ParamElem(java.sql.ResultSet rs) {
			init(rs);
		}

		boolean init(java.sql.ResultSet rs) {
			try {
				typeid = rs.getInt("typeid");
				paramid = rs.getInt("paramid");
				sensor = rs.getInt("sensor");
				level = rs.getInt("xlevel");
				fdato = rs.getTimestamp("fdato");
				tdato = rs.getTimestamp("tdato");

				if (fdato == null)
					this.fdato = new Timestamp(MIN_YEAR);

				if (tdato == null)
					this.tdato = new Timestamp(MAX_YEAR);

				return true;
			} catch (SQLException ex) {
				logger.error(
						"SQL problems: cant fetch data from a resultset!\n" + ex + " SQLState: " + ex.getSQLState());
				typeid = 0;
				paramid = 0;
				sensor = 0;
				level = 0;
			}

			return false;
		}

		boolean isOk() {
			return paramid != 0;
		}

		@Override
		public String toString() {
			return "{ tid:" + typeid + ", pid:" + paramid + ", sen:" + sensor + ", lev:" + level + ",fdate:" + fdato
					+ ", tdate:" + tdato + "}";
		}

		public int compareTo(Object obj) {
			ParamElem e = (ParamElem) obj;

			if (typeid < e.typeid)
				return -1;

			if (typeid > e.typeid)
				return 1;

			if (paramid < e.paramid)
				return -1;

			if (paramid > e.paramid)
				return 1;

			if (sensor < e.sensor)
				return -1;

			if (sensor > e.sensor)
				return 1;

			if (level < e.level)
				return -1;

			if (level > e.level)
				return 1;

			return fdato.compareTo(e.fdato);
		}
	}

	/**
	 * Remove or add an ParamElement to a list. If an paramid is negativ, remove
	 * the element from the list if an element exist in the list. Elements that
	 * is greater than zero is added to the list if it not allready exist in the
	 * list.
	 *
	 * The paramid to the elemet is given with the absolute value of paramid.
	 *
	 * @param list
	 *            The list toa add or remove an element from.
	 * @param pe
	 *            The ParamElement to add/remove to the list.
	 */
	void addOrRemoveFromList(LinkedList<ParamElem> list, ParamElem pe) {
		boolean remove = false;
		int pid = Math.abs(pe.paramid);

		if (pe.paramid < 0)
			remove = true;

		ListIterator<ParamElem> it = list.listIterator(0);

		while (it.hasNext()) {
			ParamElem param = it.next();

			if (param.typeid == pe.typeid && param.paramid == pid && param.sensor == pe.sensor
					&& param.level == pe.level && param.fdato.compareTo(pe.fdato) == 0
					&& param.tdato.compareTo(pe.tdato) == 0) {
				if (remove)
					it.remove();

				return;
			}
		}

		if (!remove)
			list.add(pe);
	}

	no.met.kvutil.dbutil.IQuery createKv2KklimaTypeidParamFilterQuery( long typeid_) {
		return new IQuery() {
			final long typeid=typeid_;
			@Override
			public ResultSet exec(PreparedStatement s) throws SQLException {
				s.setObject(1,typeid, Types.NUMERIC);
				return s.executeQuery();
			}

			@Override
			public String name() {
				return "Kv2KklimaTypeidParamFilterQuery";
			}

			@Override
			public String create() {
				return "SELECT * FROM T_KV2KLIMA_TYPEID_PARAM_FILTER WHERE abs(typeid)=?";
			}
		};
	}

	IQuery createKv2KlimaParamFilterQuery(long stationid, long typeid) {
		return new IQuery() {
			final long sid=stationid;
			final long tid=typeid;
			@Override
			public ResultSet exec(PreparedStatement s) throws SQLException {
				s.setObject(1,tid,Types.NUMERIC);
				s.setObject(2,sid,Types.NUMERIC);
				return s.executeQuery();
			}

			@Override
			public String name() {
				return "Kv2KlimaParamFilterQuery";
			}

			@Override
			public String create() {
				return "SELECT * FROM T_KV2KLIMA_PARAM_FILTER WHERE abs(typeid)=? AND stnr=?";
			}
		};
	}


    LinkedList<ParamElem> loadFromDb(long type, DbConnection con) {
		long typeid = Math.abs(type);
		LinkedList<ParamElem> list = new LinkedList<ParamElem>();
		boolean error = false;

		if (types == null)
			types = new HashMap<Long, LinkedList<ParamElem>>();

		IQuery stmt= createKv2KklimaTypeidParamFilterQuery(typeid);
		logger.debug("TypeidParamFilter::loadFromDb: query: " + stmt.create());

		ResultSet rs = null;

		try {
			rs = con.execQuery(stmt);

			while (rs.next()) {
				ParamElem pe = new ParamElem(rs);

				if (!pe.isOk()) {
					logger.error("Failed to initialize an ParamElem from the database!");
					continue;
				}
				logger.debug("TypeidParamFilter::loadFromDb: add element: " + pe);
				list.add(pe);
			}
		} catch (SQLException ex) {
			logger.error(Instant.now() + " - EXCEPTION: loadFromDb: " + ex + " SQLState: " + ex.getSQLState());
			error = true;
		}

		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				logger.error("Filter.loadFromDb: Exception when closing the ResultSet!\n" + ex + " SQLState: "
						+ ex.getSQLState());
			}
		}

		if (error)
			return null;

		stmt = createKv2KlimaParamFilterQuery(stationid, typeid);
		logger.debug("ParamFilter lookup: " + stmt);

		rs = null;

		try {
			rs = con.execQuery(stmt);

			while (rs.next()) {
				ParamElem pe = new ParamElem(rs);

				if (!pe.isOk()) {
					logger.error("Failed to initialize an ParamElem from the database!");
					continue;
				}
				logger.debug("TypeidParamFilter::loadFromDb: addOrRemove element: " + pe);
				addOrRemoveFromList(list, pe);
			}
		} catch (SQLException ex) {
			logger.error(Instant.now() + " - EXCEPTION: loadFromDb: " + ex + " SQLState: " + ex.getSQLState());
			error = true;
		}

		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				logger.error("Filter.loadFromDb: Exception when closing the ResultSet!\n" + ex + " SQLState: "
						+ ex.getSQLState());
			}
		}

		Collections.sort(list);
		types.put(new Long(typeid), list);

		return list;
	}

    public ParamFilter(long stationid, Matrics matrics) {
        this.stationid = stationid;
        this.matrics = matrics;
        types = null;
    }

	public boolean filter(DataElem data, Timestamp obstime, DbConnection con) {
		return filter(data.paramID, data.typeID, data.level, data.sensor, true, obstime, con);
	}

	public boolean filter(TextDataElem data, Timestamp obstime, DbConnection con) {
		return filter(data.paramID, data.typeID, 0, 0, false, obstime, con);
	}

    synchronized public boolean filter(int paramid, long typeid, int level, int sensor, boolean useLevelAndSensor, Timestamp obstime, DbConnection con) {
		LinkedList<ParamElem> params = null;
        boolean hitCounted=false;

		if (types == null) {
			params = loadFromDb(typeid, con);

			if (params == null) {
				System.out.println("ParamFilter: Unexpected null list!");
				return true;
			}
		} else {
            matrics.paramCount(true);
            hitCounted=true;
        }

		LinkedList<ParamElem> obj = types.get(new Long(Math.abs(typeid)));

		if (obj == null) {
            matrics.paramCount(false);
            params = loadFromDb(typeid, con);
        }else {
            if(!hitCounted)
                matrics.paramCount(true);
            params = obj;
        }

		if (params == null || params.size() == 0)
			return true;

		ListIterator<ParamElem> it = params.listIterator(0);

		while (it.hasNext()) {
			ParamElem param = it.next();

			logger.debug("-- ParamFilter: ParamElem: " + param);

			if (param.paramid == paramid && param.typeid == typeid) {
				if (useLevelAndSensor) {
					if (param.level != level)
						continue;

					if (param.sensor == sensor) {
						if (obstime.compareTo(param.fdato) >= 0 && obstime.compareTo(param.tdato) <= 0)
							return false;
					}
				}
			}
		}

		return true;
	}

}
