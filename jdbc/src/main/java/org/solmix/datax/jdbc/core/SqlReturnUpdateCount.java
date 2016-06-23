package org.solmix.datax.jdbc.core;

import java.sql.Types;


public class SqlReturnUpdateCount extends SqlParameter {

	/**
	 * Create a new instance of the {@link SqlReturnUpdateCount} class.
	 * @param name name of the parameter, as used in input and output maps
	 */
	public SqlReturnUpdateCount(String name) {
		super(name, Types.INTEGER);
	}


	/**
	 * Return whether this parameter holds input values that should be set
	 * before execution even if they are {@code null}.
	 * <p>This implementation always returns {@code false}.
	 */
	@Override
	public boolean isInputValueProvided() {
		return false;
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