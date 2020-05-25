package no.met.kvclient.service.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.service.*;
import no.met.kvutil.NotImplementedException;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.Tuple2;
import no.met.kvutil.dbutil.DbConnection;
import no.met.kvutil.dbutil.DbConnectionMgr;
import no.met.kvutil.dbutil.IQuery;


public class SqlDataQuery implements KvDataQuery {
	ListenerEventQue que;
	DbConnectionMgr mgr;

	void setDbMgr(PropertiesHelper prop, String prefix){
        try {
            mgr=new DbConnectionMgr(prop, prefix, 2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            mgr=null;
        }
    }


	public SqlDataQuery(PropertiesHelper prop) {
        this(prop,new ListenerEventQue(Integer.parseInt(prop.getProperty("que.size", "10"))));
    }

	public SqlDataQuery(PropertiesHelper prop, ListenerEventQue que) {
		this.que = que;
        setDbMgr(prop, "kv");
	}

	public DbConnectionMgr getSqlDbConnectionMgr() {
        return mgr;
    }

	@Override
	public DataIterator getData(WhichDataList whichData) throws Exception {

		DbConnection[] con = mgr.waitForDbConnection(60, 2);
        return new SqlDataIterator(con, whichData, 100);
	}

    @Override
    public boolean getKvData(WhichDataList whichData, KvDataEventListener listener) throws Exception {
        //TODO: This must go in its own thread
        ObsDataList dl;
        DataIterator it = getData(whichData);

        do {
            dl=it.next();
            if( dl!=null)
                listener.callListener(this, null, dl);
        }while( dl!=null);

        return true;
    }

    static class Connections implements AutoCloseable {
        DbConnection[] con;
        DbConnectionMgr mgr;
        Connections(DbConnectionMgr mgr, int nConnections, int secsToWait) throws Exception {
            this.mgr=mgr;
            con = this.mgr.waitForDbConnection(secsToWait, nConnections);
        }

        @Override
        public void close() throws SQLException {
            mgr.releaseDbConnection(con);
        }
    }

    @Override
	public Optional<StationList> getStations() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplemented: getStaions()");
	}

	@Override
	public Optional<ParamList> getParams() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplemented: getParams()");
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
		throw new NotImplementedException("NotImplemented: getModelData(WhichDataList whichData)");
		//return Optional.ofNullable(null);
	}

	@Override
	public Optional<RejectedIterator> getRejectdecode(RejectDecodeInfo decodeInfo) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getRejectdecode(RejectDecodeInfo decodeInfo)");
	}

	@Override
	public Optional<Reference_stationList> getReferenceStation(long stationid, short paramsetid) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getReferenceStation(long stationid, short paramsetid)");
	}

	@Override
	public Optional<Obs_pgmList> getObsPgm(StationIDList stationIDList, boolean aUnion) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getObsPgm(StationIDList stationIDList, boolean aUnion)");
	}

	@Override
	public Optional<TypeList> getTypes() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getTypes()");
	}

	@Override
	public Optional<OperatorList> getOperator() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getOperator()");
	}

	@Override
	public Optional<Station_paramList> getStationParam(long stationid, long paramid, long day) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("NotImplement: getStationParam(long stationid, long paramid, long day)");
	}

    @Override
    public PropertiesHelper getInfo() {
        PropertiesHelper prop=new PropertiesHelper();
        prop.setProperty("kv.dbconnect", mgr.getDbconnect());
        prop.setProperty("kv.dbuser", mgr.getDbuser());
        prop.setProperty("kv.dbdriver", mgr.getDbdriver());
        prop.put("kv.dbmanager", mgr);
        return prop;
    }

    @Override
	public void stop() {
		// TODO Auto-generated method stub
        mgr.closeDbDriver();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

}
