package org.solmix.datax.jdbc.core;



public class SqlReturnResultSet extends ResultSetSupportingSqlParameter {

	/**
	 * Create a new instance of the {@link SqlReturnResultSet} class.
	 * @param name name of the parameter, as used in input and output maps
	 * @param extractor ResultSetExtractor to use for parsing the {@link java.sql.ResultSet}
	 */
	public SqlReturnResultSet(String name, ResultSetExtractor<?> extractor) {
		super(name, 0, extractor);
	}

	/**
	 * Create a new instance of the {@link SqlReturnResultSet} class.
	 * @param name name of the parameter, as used in input and output maps
	 * @param handler RowCallbackHandler to use for parsing the {@link java.sql.ResultSet}
	 */
	public SqlReturnResultSet(String name, RowCallbackHandler handler) {
		super(name, 0, handler);
	}

	/**
	 * Create a new instance of the {@link SqlReturnResultSet} class.
	 * @param name name of the parameter, as used in input and output maps
	 * @param mapper RowMapper to use for parsing the {@link java.sql.ResultSet}
	 */
	public SqlReturnResultSet(String name, RowMapper<?> mapper) {
		super(name, 0, mapper);
	}

	/**
	 * Return whether this parameter is an implicit return parameter used during the
	 * results preocessing of the CallableStatement.getMoreResults/getUpdateCount.
	 * <p>This implementation always returns {@code true}.
	 */
	@Override
	public boolean isResultsParameter() {
		return true;
	}
}