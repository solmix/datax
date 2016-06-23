package org.solmix.datax.jdbc.core;

import java.sql.CallableStatement;
import java.sql.SQLException;


public interface SqlReturnType {

	/**
	 * Constant that indicates an unknown (or unspecified) SQL type.
	 * Passed into setTypeValue if the original operation method does
	 * not specify a SQL type.
	 * @see java.sql.Types
	 * @see JdbcOperations#update(String, Object[])
	 */
	int TYPE_UNKNOWN = Integer.MIN_VALUE;


	/**
	 * Get the type value from the specific object.
	 * @param cs the CallableStatement to operate on
	 * @param paramIndex the index of the parameter for which we need to set the value
	 * @param sqlType SQL type of the parameter we are setting
	 * @param typeName the type name of the parameter
	 * @return the target value
	 * @throws SQLException if a SQLException is encountered setting parameter values
	 * (that is, there's no need to catch SQLException)
	 * @see java.sql.Types
	 * @see java.sql.CallableStatement#getObject
	 */
	Object getTypeValue(CallableStatement cs, int paramIndex, int sqlType, String typeName)
			throws SQLException;

}