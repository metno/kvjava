/*
  Kvalobs - Free Quality Control Software for Meteorological Observations

  $Id: DbConnection.java,v 1.1.2.7 2007/09/27 09:02:42 paule Exp $

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
package no.met.kvutil.dbutil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import no.met.kvutil.MiGMTTime;

public class DbConnection implements AutoCloseable{
    DbConnectionMgr mgr;
	Connection connection;
	Statement statement;
	Map<String,PreparedStatement> preparedStatements;
	String dbdriver;
	MiGMTTime created;
	MiGMTTime lastAccess;
	boolean inuse;

	DbConnection(Connection connection, Statement statement, String dbdriver, DbConnectionMgr mgr) {
		this.dbdriver = dbdriver;
		this.connection = connection;
		this.statement = statement;
		created = new MiGMTTime();
		lastAccess = new MiGMTTime();
		this.preparedStatements = new HashMap<String,PreparedStatement>();
		inuse = true;
        this.mgr = mgr;
	}

    DbConnection(Connection connection, Statement statement, String dbdriver) {
        this(connection, statement, dbdriver, null);
    }

	/**
	 * Only too be called from DbConnectonMgr.
	 *
	 * @throws SQLException
	 */
	void closeConnection() throws SQLException {
		statement.close();
		connection.close();
        for(Map.Entry<String, PreparedStatement> s : preparedStatements.entrySet())
            s.getValue().close();
        preparedStatements.clear();
	}

    @Override
    public void close() throws Exception {
        if( mgr != null )
            mgr.releaseDbConnection(this);
    }

	/**
	 * Use this method for SELECT querys.
	 *
	 * @param query
	 * @return
	 * @throws SQLException
	 * @throws IllegalStateException
	 */
	public java.sql.ResultSet execQuery(String query) throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		lastAccess = new MiGMTTime();

		return statement.executeQuery(query);
	}


    private PreparedStatement getPreparedStatement(ISqlBase query) throws SQLException {
        if (!inuse)
            throw new IllegalStateException("This connection i marked NOT in use!");

        lastAccess = new MiGMTTime();

        PreparedStatement stm=preparedStatements.get(query.name());

        if( stm == null ) {
            stm = connection.prepareStatement(query.create());
            preparedStatements.put(query.name(), stm);
        }

        return stm;
    }

    /**
	 * Use this method for SELECT querys.
	 *
	 * @param query
	 * @return
	 * @throws SQLException
	 * @throws IllegalStateException
	 */
	public java.sql.ResultSet execQuery(IQuery query) throws SQLException, IllegalStateException {
        PreparedStatement stm = getPreparedStatement(query);
		return query.exec(stm);
	}



    public int exec(IExec query) throws SQLException, IllegalStateException {
        PreparedStatement stm = getPreparedStatement(query);
        return query.exec(stm);
    }


    /**
	 * Execute an SQL statement and throw away the result.
	 *
	 * Do not use it for SELECT statement.
	 *
	 * @param sqlstatement
	 * @throws SQLException
	 * @throws IllegalStateException
	 */
	public int exec(String sqlstatement) throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		lastAccess = new MiGMTTime();

		return statement.executeUpdate(sqlstatement);
	}

	public void setAutoCommit(boolean autocommit) throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		connection.setAutoCommit(autocommit);
	}

	public boolean getAutoCommit() throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		return connection.getAutoCommit();
	}

	public void rollback() throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		connection.rollback();
	}

	public void commit() throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		connection.commit();
	}

	/**
	 * Only to be called from DbConnectionMgr.
	 *
	 * @param inuse
	 */
	void setInuse(boolean inuse) {
		this.inuse = inuse;
	}

	/**
	 * Set a time limit on how long an queryExec/exec operation is allowed to
	 * run before it is considred as failed and an exception is thrown.
	 *
	 * @param seconds
	 *            The timeout value.
	 * @return The old timeout value.
	 * @throws SQLException
	 *             If there is a problem with the connection.
	 * @throws IllegalStateException
	 *             If this connection is NOT marked as active. Ie. it is idle in
	 *             the cache.
	 */
	int setTimeout(int seconds) throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		int old = statement.getQueryTimeout();

		if (seconds == old)
			return old;

		statement.setQueryTimeout(seconds);

		return old;
	}

	/**
	 * Get the time limit for how long an queryExec/exec operation is allowed to
	 * run before it is considred as failed and an exception is thrown.
	 *
	 * @return The the timeout value.
	 * @throws SQLException
	 *             If there is a problem with the connection.
	 * @throws IllegalStateException
	 *             If this connection is NOT marked as active. Ie. it is idle in
	 *             the cache.
	 */
	int getTimeout(int seconds) throws SQLException, IllegalStateException {

		if (!inuse)
			throw new IllegalStateException("This connection i marked NOT in use!");

		return statement.getQueryTimeout();
	}

	public boolean getInuse() {
		return inuse;
	}

	public MiGMTTime getLastAccess() {
		return lastAccess;
	}

	public MiGMTTime getCreated() {
		return created;
	}

	public String getDbdriver() {
		return dbdriver;
	}
}
