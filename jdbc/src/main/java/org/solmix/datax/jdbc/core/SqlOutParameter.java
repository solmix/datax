package org.solmix.datax.jdbc.core;



public class SqlOutParameter extends ResultSetSupportingSqlParameter {

	private SqlReturnType sqlReturnType;


	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public SqlOutParameter(String name, int sqlType) {
		super(name, sqlType);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param scale the number of digits after the decimal point
	 * (for DECIMAL and NUMERIC types)
	 */
	public SqlOutParameter(String name, int sqlType, int scale) {
		super(name, sqlType, scale);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public SqlOutParameter(String name, int sqlType, String typeName) {
		super(name, sqlType, typeName);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 * @param sqlReturnType custom value handler for complex type (optional)
	 */
	public SqlOutParameter(String name, int sqlType, String typeName, SqlReturnType sqlReturnType) {
		super(name, sqlType, typeName);
		this.sqlReturnType = sqlReturnType;
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, ResultSetExtractor<?> rse) {
		super(name, sqlType, rse);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, RowCallbackHandler rch) {
		super(name, sqlType, rch);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, RowMapper<?> rm) {
		super(name, sqlType, rm);
	}


	/**
	 * Return the custom return type, if any.
	 */
	public SqlReturnType getSqlReturnType() {
		return this.sqlReturnType;
	}

	/**
	 * Return whether this parameter holds a custom return type.
	 */
	public boolean isReturnTypeSupported() {
		return (this.sqlReturnType != null);
	}

}