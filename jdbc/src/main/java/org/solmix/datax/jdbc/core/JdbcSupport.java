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

import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.commons.util.StringUtils;
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

	private static final String RETURN_RESULT_SET_PREFIX = "#result-set-";

	private static final String RETURN_UPDATE_COUNT_PREFIX = "#update-count-";

    private DataSource dataSource;
    
    private boolean ignoreWarnings = true;

    private int fetchSize = 0;

    private int maxRows = 0;
    
    private int queryTimeout = 0;

	private boolean resultsMapCaseInsensitive = false;

	private boolean skipResultsProcessing = false;
	
	private boolean skipUndeclaredResults = false;

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
    
    public int update(String sql, Object... args) throws JdbcException {
		return update(sql, newArgPreparedStatementSetter(args));
	}
    
    public int update(String sql, Object[] args, int[] argTypes) throws JdbcException {
		return update(sql, newArgTypePreparedStatementSetter(args, argTypes));
	}
    public int update(String sql, PreparedStatementSetter pss) throws JdbcException {
		return update(new SimplePreparedStatementCreator(sql), pss);
	}
    public int update(PreparedStatementCreator psc) throws JdbcException {
		return update(psc, (PreparedStatementSetter) null);
	}
	protected PreparedStatementSetter newArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		return new ArgumentTypePreparedStatementSetter(args, argTypes);
	}
	
	protected int update(final PreparedStatementCreator psc, final PreparedStatementSetter pss)
			throws JdbcException {

		LOG.debug("Executing prepared SQL update");
		return execute(psc, new PreparedStatementCallback<Integer>() {
			@Override
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					int rows = ps.executeUpdate();
					if (LOG.isDebugEnabled()) {
						LOG.debug("SQL update affected " + rows + " rows");
					}
					return rows;
				}
				finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}

    
    public int[] batchUpdate(final String[] sql) throws JdbcException {

    	if (ObjectUtils.isEmptyObject(sql)) {
			throw new IllegalArgumentException("SQL array must not be empty");
		}

			LOG.debug("Executing SQL batch update of {} statements",  sql.length);

		class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {

			private String currSql;

			@Override
			public int[] doInStatement(Statement stmt) throws SQLException, JdbcException {

				int[] rowsAffected = new int[sql.length];

				if (JdbcHelper.supportsBatchUpdates(stmt.getConnection())) {
					for (String sqlStmt : sql) {
						this.currSql = appendSql(this.currSql, sqlStmt);
						stmt.addBatch(sqlStmt);
					}
					try {
						rowsAffected = stmt.executeBatch();
					}
					catch (BatchUpdateException ex) {
						String batchExceptionSql = null;
						for (int i = 0; i < ex.getUpdateCounts().length; i++) {
							if (ex.getUpdateCounts()[i] == Statement.EXECUTE_FAILED) {
								batchExceptionSql = appendSql(batchExceptionSql, sql[i]);
							}
						}
						if (StringUtils.hasLength(batchExceptionSql)) {
							this.currSql = batchExceptionSql;
						}
						throw ex;
					}
				}
				else {
					for (int i = 0; i < sql.length; i++) {
						this.currSql = sql[i];
						if (!stmt.execute(sql[i])) {
							rowsAffected[i] = stmt.getUpdateCount();
						}
						else {
							throw new JdbcException("Invalid batch SQL statement: " + sql[i]);
						}
					}
				}
				return rowsAffected;
			}

			private String appendSql(String sql, String statement) {
				return (StringUtils.isEmpty(sql) ? statement : sql + "; " + statement);
			}

			@Override
			public String getSql() {
				return this.currSql;
			}
		}
		return execute(new BatchUpdateStatementCallback());
	}
    
    public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws JdbcException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing SQL batch update [" + sql + "]");
		}

		return execute(sql, new PreparedStatementCallback<int[]>() {
			@Override
			public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException {
				try {
					int batchSize = pss.getBatchSize();
					InterruptibleBatchPreparedStatementSetter ipss =
							(pss instanceof InterruptibleBatchPreparedStatementSetter ?
							(InterruptibleBatchPreparedStatementSetter) pss : null);
					if (JdbcHelper.supportsBatchUpdates(ps.getConnection())) {
						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i)) {
								break;
							}
							ps.addBatch();
						}
						return ps.executeBatch();
					}
					else {
						List<Integer> rowsAffected = new ArrayList<Integer>();
						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i)) {
								break;
							}
							rowsAffected.add(ps.executeUpdate());
						}
						int[] rowsAffectedArray = new int[rowsAffected.size()];
						for (int i = 0; i < rowsAffectedArray.length; i++) {
							rowsAffectedArray[i] = rowsAffected.get(i);
						}
						return rowsAffectedArray;
					}
				}
				finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
    
	public int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes) throws JdbcException {
		return executeBatchUpdate(sql, batchArgs, argTypes);
	}
	public  int[] executeBatchUpdate(String sql, final List<Object[]> batchValues, final int[] columnTypes) {
		return batchUpdate(
				sql,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Object[] values = batchValues.get(i);
						setStatementParameters(values, ps, columnTypes);
					}

					@Override
					public int getBatchSize() {
						return batchValues.size();
					}
				});
	}

	protected static void setStatementParameters(Object[] values, PreparedStatement ps, int[] columnTypes) throws SQLException {
		int colIndex = 0;
		for (Object value : values) {
			colIndex++;
			if (value instanceof SqlParameterValue) {
				SqlParameterValue paramValue = (SqlParameterValue) value;
				StatementCreatorUtils.setParameterValue(ps, colIndex, paramValue, paramValue.getValue());
			}
			else {
				int colType;
				if (columnTypes == null || columnTypes.length < colIndex) {
					colType = SqlTypeValue.TYPE_UNKNOWN;
				}
				else {
					colType = columnTypes[colIndex - 1];
				}
				StatementCreatorUtils.setParameterValue(ps, colIndex, colType, value);
			}
		}
	}
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws JdbcException {
		return batchUpdate(sql, batchArgs, new int[0]);
	}
    
    public <T> int[][] batchUpdate(String sql, final Collection<T> batchArgs, final int batchSize,
			final ParameterizedPreparedStatementSetter<T> pss) throws JdbcException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing SQL batch update [{}] with a batch size of {}", sql, batchSize);
		}
		return execute(sql, new PreparedStatementCallback<int[][]>() {
			@Override
			public int[][] doInPreparedStatement(PreparedStatement ps) throws SQLException {
				List<int[]> rowsAffected = new ArrayList<int[]>();
				try {
					boolean batchSupported = true;
					if (!JdbcHelper.supportsBatchUpdates(ps.getConnection())) {
						batchSupported = false;
						LOG.warn("JDBC Driver does not support Batch updates; resorting to single statement execution");
					}
					int n = 0;
					for (T obj : batchArgs) {
						pss.setValues(ps, obj);
						n++;
						if (batchSupported) {
							ps.addBatch();
							if (n % batchSize == 0 || n == batchArgs.size()) {
								if (LOG.isDebugEnabled()) {
									int batchIdx = (n % batchSize == 0) ? n / batchSize : (n / batchSize) + 1;
									int items = n - ((n % batchSize == 0) ? n / batchSize - 1 : (n / batchSize)) * batchSize;
									LOG.debug("Sending SQL batch update #" + batchIdx + " with " + items + " items");
								}
								rowsAffected.add(ps.executeBatch());
							}
						}
						else {
							int i = ps.executeUpdate();
							rowsAffected.add(new int[] {i});
						}
					}
					int[][] result = new int[rowsAffected.size()][];
					for (int i = 0; i < result.length; i++) {
						result[i] = rowsAffected.get(i);
					}
					return result;
				} finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
    
    public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters)
			throws JdbcException {

		final List<SqlParameter> updateCountParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> resultSetParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> callParameters = new ArrayList<SqlParameter>();
		for (SqlParameter parameter : declaredParameters) {
			if (parameter.isResultsParameter()) {
				if (parameter instanceof SqlReturnResultSet) {
					resultSetParameters.add(parameter);
				}
				else {
					updateCountParameters.add(parameter);
				}
			}
			else {
				callParameters.add(parameter);
			}
		}
		return execute(csc, new CallableStatementCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
				boolean retVal = cs.execute();
				int updateCount = cs.getUpdateCount();
				if (LOG.isDebugEnabled()) {
					LOG.debug("CallableStatement.execute() returned '" + retVal + "'");
					LOG.debug("CallableStatement.getUpdateCount() returned " + updateCount);
				}
				Map<String, Object> returnedResults = createResultsMap();
				if (retVal || updateCount != -1) {
					returnedResults.putAll(extractReturnedResults(cs, updateCountParameters, resultSetParameters, updateCount));
				}
				returnedResults.putAll(extractOutputParameters(cs, callParameters));
				return returnedResults;
			}
		});
	}
    protected Map<String, Object> extractOutputParameters(CallableStatement cs, List<SqlParameter> parameters)
			throws SQLException {

		Map<String, Object> returnedResults = new HashMap<String, Object>();
		int sqlColIndex = 1;
		for (SqlParameter param : parameters) {
			if (param instanceof SqlOutParameter) {
				SqlOutParameter outParam = (SqlOutParameter) param;
				if (outParam.isReturnTypeSupported()) {
					Object out = outParam.getSqlReturnType().getTypeValue(
							cs, sqlColIndex, outParam.getSqlType(), outParam.getTypeName());
					returnedResults.put(outParam.getName(), out);
				}
				else {
					Object out = cs.getObject(sqlColIndex);
					if (out instanceof ResultSet) {
						if (outParam.isResultSetSupported()) {
							returnedResults.putAll(processResultSet((ResultSet) out, outParam));
						}
						else {
							String rsName = outParam.getName();
							SqlReturnResultSet rsParam = new SqlReturnResultSet(rsName, new ColumnMapRowMapper());
							returnedResults.putAll(processResultSet((ResultSet) out, rsParam));
							if (LOG.isDebugEnabled()) {
								LOG.debug("Added default SqlReturnResultSet parameter named '" + rsName + "'");
							}
						}
					}
					else {
						returnedResults.put(outParam.getName(), out);
					}
				}
			}
			if (!(param.isResultsParameter())) {
				sqlColIndex++;
			}
		}
		return returnedResults;
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected Map<String, Object> processResultSet(ResultSet rs, ResultSetSupportingSqlParameter param) throws SQLException {
		if (rs == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> returnedResults = new HashMap<String, Object>();
		try {
			ResultSet rsToUse = rs;
			if (param.getRowMapper() != null) {
				RowMapper rowMapper = param.getRowMapper();
				Object result = (new RowMapperResultSetExtractor(rowMapper)).extractData(rsToUse);
				returnedResults.put(param.getName(), result);
			}
			else if (param.getRowCallbackHandler() != null) {
				RowCallbackHandler rch = param.getRowCallbackHandler();
				(new RowCallbackHandlerResultSetExtractor(rch)).extractData(rsToUse);
				returnedResults.put(param.getName(), "ResultSet returned from stored procedure was processed");
			}
			else if (param.getResultSetExtractor() != null) {
				Object result = param.getResultSetExtractor().extractData(rsToUse);
				returnedResults.put(param.getName(), result);
			}
		}
		finally {
			JdbcHelper.closeResultSet(rs);
		}
		return returnedResults;
	}
    protected Map<String, Object> extractReturnedResults(CallableStatement cs,
			List<SqlParameter> updateCountParameters, List<SqlParameter> resultSetParameters, int updateCount)
			throws SQLException {

		Map<String, Object> returnedResults = new HashMap<String, Object>();
		int rsIndex = 0;
		int updateIndex = 0;
		boolean moreResults;
		if (!this.skipResultsProcessing) {
			do {
				if (updateCount == -1) {
					if (resultSetParameters != null && resultSetParameters.size() > rsIndex) {
						SqlReturnResultSet declaredRsParam = (SqlReturnResultSet) resultSetParameters.get(rsIndex);
						returnedResults.putAll(processResultSet(cs.getResultSet(), declaredRsParam));
						rsIndex++;
					}
					else {
						if (!this.skipUndeclaredResults) {
							String rsName = RETURN_RESULT_SET_PREFIX + (rsIndex + 1);
							SqlReturnResultSet undeclaredRsParam = new SqlReturnResultSet(rsName, new ColumnMapRowMapper());
							if (LOG.isDebugEnabled()) {
								LOG.debug("Added default SqlReturnResultSet parameter named '" + rsName + "'");
							}
							returnedResults.putAll(processResultSet(cs.getResultSet(), undeclaredRsParam));
							rsIndex++;
						}
					}
				}
				else {
					if (updateCountParameters != null && updateCountParameters.size() > updateIndex) {
						SqlReturnUpdateCount ucParam = (SqlReturnUpdateCount) updateCountParameters.get(updateIndex);
						String declaredUcName = ucParam.getName();
						returnedResults.put(declaredUcName, updateCount);
						updateIndex++;
					}
					else {
						if (!this.skipUndeclaredResults) {
							String undeclaredName = RETURN_UPDATE_COUNT_PREFIX + (updateIndex + 1);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Added default SqlReturnUpdateCount parameter named '" + undeclaredName + "'");
							}
							returnedResults.put(undeclaredName, updateCount);
							updateIndex++;
						}
					}
				}
				moreResults = cs.getMoreResults();
				updateCount = cs.getUpdateCount();
				if (LOG.isDebugEnabled()) {
					LOG.debug("CallableStatement.getUpdateCount() returned " + updateCount);
				}
			}
			while (moreResults || updateCount != -1);
		}
		return returnedResults;
	}
    protected Map<String, Object> createResultsMap() {
		if (isResultsMapCaseInsensitive()) {
			return new LinkedCaseInsensitiveMap<Object>();
		}
		else {
			return new LinkedHashMap<String, Object>();
		}
	}
    public void setResultsMapCaseInsensitive(boolean resultsMapCaseInsensitive) {
		this.resultsMapCaseInsensitive = resultsMapCaseInsensitive;
	}

	/**
	 * Return whether execution of a CallableStatement will return the results in a Map
	 * that uses case insensitive names for the parameters.
	 */
	public boolean isResultsMapCaseInsensitive() {
		return this.resultsMapCaseInsensitive;
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

    public <T> T execute(String callString, CallableStatementCallback<T> action) throws JdbcException {
        return execute(new SimpleCallableStatementCreator(callString), action);
    }
    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws JdbcException {
		return execute(new SimplePreparedStatementCreator(sql), action);
	}


    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws JdbcException {

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
            throw new JdbcException(ex);
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
    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws JdbcException {

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
            throw new JdbcException(ex);
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

    public void query(String sql, Object[] args, RowCallbackHandler rch) throws JdbcException {
        query(sql, newArgPreparedStatementSetter(args), rch);
    }
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws JdbcException {
		return query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper));
	}
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws JdbcException {
		return query(sql, newArgPreparedStatementSetter(args), rse);
	}
    protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
  }

    public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws JdbcException {
        query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
    }

    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws JdbcException {
        return query(new SimplePreparedStatementCreator(sql), pss, rse);
    }

    public <T> List<T> queryForList(String sql, Class<T> elementType) throws JdbcException {
		return query(sql, getSingleColumnRowMapper(elementType));
	}
	public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws JdbcException {
		return query(sql, args, getSingleColumnRowMapper(elementType));
	}
	public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws JdbcException {
		return query(sql, args, getSingleColumnRowMapper(elementType));
	}
	public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws JdbcException {
		return query(sql, args, argTypes, getColumnMapRowMapper());
	}
	public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws JdbcException {
		return query(sql, args, argTypes, new RowMapperResultSetExtractor<T>(rowMapper));
	}
	public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws JdbcException {
		query(sql, newArgTypePreparedStatementSetter(args, argTypes), rch);
	}
	public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws JdbcException {
		return query(sql, newArgTypePreparedStatementSetter(args, argTypes), rse);
	}

    public List<Map<String, Object>> queryForList(String sql) throws JdbcException {
		return query(sql, getColumnMapRowMapper());
	}
    public Map<String, Object> queryForMap(String sql) throws JdbcException {
		return queryForObject(sql, getColumnMapRowMapper());
	}
    public Map<String, Object> queryForMap(String sql, Object... args) throws JdbcException {
		return queryForObject(sql, args, getColumnMapRowMapper());
	}
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws JdbcException {
		return queryForObject(sql, args, argTypes, getColumnMapRowMapper());
	}
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws JdbcException {
		List<T> results = query(sql, rowMapper);
		return JdbcHelper.requiredSingleResult(results);
	}
    
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws JdbcException {
		return query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper));
	}
    
    public void query(String sql, RowCallbackHandler rch, Object... args) throws JdbcException {
		query(sql, newArgPreparedStatementSetter(args), rch);
	}
    
	public void query(String sql, RowCallbackHandler rch) throws JdbcException {
		query(sql, new RowCallbackHandlerResultSetExtractor(rch));
	}
    
    public <T> T queryForObject(String sql, Class<T> requiredType) throws JdbcException {
		return queryForObject(sql, getSingleColumnRowMapper(requiredType));
	}
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws JdbcException {
		return queryForObject(sql, args, getSingleColumnRowMapper(requiredType));
	}
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType)
			throws JdbcException {

		return queryForObject(sql, args, argTypes, getSingleColumnRowMapper(requiredType));
	}
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper)
			throws JdbcException {

		List<T> results = query(sql, args, argTypes, new RowMapperResultSetExtractor<T>(rowMapper, 1));
		return JdbcHelper.requiredSingleResult(results);
	}
    
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws JdbcException {
		return queryForObject(sql, args, getSingleColumnRowMapper(requiredType));
	}
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws JdbcException {
		List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
		return JdbcHelper.requiredSingleResult(results);
	}
    
    public SqlRowSet queryForRowSet(String sql) throws JdbcException {
		return query(sql, new SqlRowSetResultSetExtractor());
	}
    
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws JdbcException {
		return query(sql, new RowMapperResultSetExtractor<T>(rowMapper));
	}
    
    public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws JdbcException {
		Assert.isNotNull(sql, "SQL must not be null");
		Assert.isNotNull(rse, "ResultSetExtractor must not be null");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing SQL query [" + sql + "]");
		}
		class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
			@Override
			public T doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					ResultSet rsToUse = rs;
					return rse.extractData(rsToUse);
				}
				finally {
					JdbcHelper.closeResultSet(rs);
				}
			}
			@Override
			public String getSql() {
				return sql;
			}
		}
		return execute(new QueryStatementCallback());
	}

    protected RowMapper<Map<String, Object>> getColumnMapRowMapper() {
		return new ColumnMapRowMapper();
	}
    protected <T> RowMapper<T> getSingleColumnRowMapper(Class<T> requiredType) {
		return new SingleColumnRowMapper<T>(requiredType);
	}
    public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws JdbcException {

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
