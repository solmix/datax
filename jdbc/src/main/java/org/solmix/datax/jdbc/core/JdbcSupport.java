/*
 * Copyright 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.datax.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.jdbc.SQLRuntimeException;
import org.solmix.datax.jdbc.helper.DataSourceHelper;
import org.solmix.datax.jdbc.helper.JdbcHelper;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月5日
 */

public class JdbcSupport
{

    private static final Logger LOG = LoggerFactory.getLogger(JdbcSupport.class);

    private DataSource dataSource;
    
    private boolean ignoreWarnings = true;

   
    private int fetchSize = 0;

    private int maxRows = 0;
    
    private int queryTimeout = 0;

    public JdbcSupport()
    {

    }

    public JdbcSupport(DataSource datasource)
    {
        this.dataSource = datasource;
    }

    
    
    public int getQueryTimeout() {
        return queryTimeout;
    }

    
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public boolean isIgnoreWarnings() {
        return ignoreWarnings;
    }

    
    public void setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }

    
    public int getFetchSize() {
        return fetchSize;
    }

    
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    
    public int getMaxRows() {
        return maxRows;
    }

    
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL update [" + sql + "]");
        }
        return execute(new StatementCallback<Integer>() {

            @Override
            public Integer doInStatement(Statement stmt) throws SQLException {
                int rows = stmt.executeUpdate(sql);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SQL update affected " + rows + " rows");
                }
                return rows;
            }
        });
    }

    public void execute(final String sql) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL statement [" + sql + "]");
        }
        execute(new StatementCallback<Object>() {

            @Override
            public Object doInStatement(Statement stmt) throws SQLException {
                stmt.execute(sql);
                return null;
            }
        });
    }

    public <T> T execute(String callString, CallableStatementCallback<T> action) throws SQLException {
        return execute(new SimpleCallableStatementCreator(callString), action);
    }

    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws SQLException {

        Assert.isNotNull(csc, "CallableStatementCreator must not be null");
        Assert.isNotNull(action, "Callback object must not be null");
        if (LOG.isDebugEnabled()) {
            String sql = getSql(csc);
            LOG.debug("Calling stored procedure" + (sql != null ? " [" + sql + "]" : ""));
        }

        Connection con = DataSourceHelper.getConnection(getDataSource());
        CallableStatement cs = null;
        try {
            Connection conToUse = con;
            cs = csc.createCallableStatement(conToUse);
            applyStatementSettings(cs);
            CallableStatement csToUse = cs;
            T result = action.doInCallableStatement(csToUse);
            handleWarnings(cs);
            return result;
        } catch (SQLException ex) {
          
            String sql = getSql(csc);
            csc = null;
            JdbcHelper.closeStatement(cs);
            cs = null;
            DataSourceHelper.releaseConnection(con, getDataSource());
            con = null;
            throw ex;
        } finally {
           
            JdbcHelper.closeStatement(cs);
            DataSourceHelper.releaseConnection(con, getDataSource());
        }
    }
    protected void handleWarnings(Statement stmt) throws SQLException {
          if (isIgnoreWarnings()) {
                if (LOG.isDebugEnabled()) {
                      SQLWarning warningToLog = stmt.getWarnings();
                      while (warningToLog != null) {
                            LOG.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() + "', error code '" +
                                        warningToLog.getErrorCode() + "', message [" + warningToLog.getMessage() + "]");
                            warningToLog = warningToLog.getNextWarning();
                      }
                }
          }
          else {
                handleWarnings(stmt.getWarnings());
          }
    }
    protected void handleWarnings(SQLWarning warning) throws SQLException {
        if (warning != null) {
              throw new SQLException("Warning not ignored", warning);
        }
  }
    protected void applyStatementSettings(Statement stmt) throws SQLException {
        int fetchSize = getFetchSize();
        if (fetchSize > 0) {
              stmt.setFetchSize(fetchSize);
        }
        int maxRows = getMaxRows();
        if (maxRows > 0) {
              stmt.setMaxRows(maxRows);
        }
        stmt.setQueryTimeout(getQueryTimeout());
  }
    private static String getSql(Object sqlProvider) {
          if (sqlProvider instanceof SqlProvider) {
                return ((SqlProvider) sqlProvider).getSql();
          }
          else {
                return null;
          }
    }
    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws SQLException {

        Assert.isNotNull(psc, "PreparedStatementCreator must not be null");
        Assert.isNotNull(action, "Callback object must not be null");
        if (LOG.isDebugEnabled()) {
            String sql = getSql(psc);
            LOG.debug("Executing prepared SQL statement" + (sql != null ? " [" + sql + "]" : ""));
        }

        Connection con = DataSourceHelper.getConnection(getDataSource());
        PreparedStatement ps = null;
        try {
            Connection conToUse = con;
            ps = psc.createPreparedStatement(conToUse);
            applyStatementSettings(ps);
            PreparedStatement psToUse = ps;
            T result = action.doInPreparedStatement(psToUse);
            handleWarnings(ps);
            return result;
        } catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
           
            String sql = getSql(psc);
            psc = null;
            JdbcHelper.closeStatement(ps);
            ps = null;
            DataSourceHelper.releaseConnection(con, getDataSource());
            con = null;
            throw ex;
        } finally {
           
            JdbcHelper.closeStatement(ps);
            DataSourceHelper.releaseConnection(con, getDataSource());
        }
    }

    public <T> T execute(StatementCallback<T> action) {

        Connection conn = DataSourceHelper.getConnection(getDataSource());
        Statement stmt = null;
        try {
            Connection conToUse = conn;
            stmt = conToUse.createStatement();
            Statement stmtToUse = stmt;
            T result = action.doInStatement(stmtToUse);
            return result;
        } catch (SQLException ex) {
            JdbcHelper.closeStatement(stmt);
            stmt = null;
            DataSourceHelper.releaseConnection(conn, getDataSource());
            conn = null;
            throw new SQLRuntimeException(ex);
        } finally {
            JdbcHelper.closeStatement(stmt);
            DataSourceHelper.releaseConnection(conn, getDataSource());
        }

    }

    public void query(String sql, Object[] args, RowCallbackHandler rch) throws SQLException {
        query(sql, newArgPreparedStatementSetter(args), rch);
    }
    protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
  }

    public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws SQLException {
        query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
    }

    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws SQLException {
        return query(new SimplePreparedStatementCreator(sql), pss, rse);
    }


    public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws SQLException {

        Assert.isNotNull(rse, "ResultSetExtractor must not be null");
        LOG.debug("Executing prepared SQL query");

        return execute(psc, new PreparedStatementCallback<T>() {

            @Override
            public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
                ResultSet rs = null;
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    rs = ps.executeQuery();
                    ResultSet rsToUse = rs;
                    return rse.extractData(rsToUse);
                } finally {
                    JdbcHelper.closeResultSet(rs);
                  
                }
            }
        });
    }

    private static class SimpleCallableStatementCreator implements CallableStatementCreator, SqlProvider
    {

        private final String callString;

        public SimpleCallableStatementCreator(String callString)
        {
            Assert.isNotNull(callString, "Call string must not be null");
            this.callString = callString;
        }

        @Override
        public CallableStatement createCallableStatement(Connection con) throws SQLException {
            return con.prepareCall(this.callString);
        }

        @Override
        public String getSql() {
            return this.callString;
        }
    }

    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider
    {

        private final String sql;

        public SimplePreparedStatementCreator(String sql)
        {
            Assert.isNotNull(sql, "SQL must not be null");
            this.sql = sql;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            return con.prepareStatement(this.sql);
        }

        @Override
        public String getSql() {
            return this.sql;
        }
    }

    private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Object>
    {

        private final RowCallbackHandler rch;

        public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch)
        {
            this.rch = rch;
        }

        @Override
        public Object extractData(ResultSet rs) throws SQLException {
            while (rs.next()) {
                this.rch.processRow(rs);
            }
            return null;
        }
    }
}
