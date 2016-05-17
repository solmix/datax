package org.solmix.datax.jdbc.core;


public class SqlParameterValue extends SqlParameter {
	private final Object value;


	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * @param value the value object
	 */
	public SqlParameterValue(int sqlType, Object value) {
		super(sqlType);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * @param typeName the type name of the parameter (optional)
	 * @param value the value object
	 */
	public SqlParameterValue(int sqlType, String typeName, Object value) {
		super(sqlType, typeName);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue, supplying the SQL type.
	 * @param sqlType SQL type of the parameter according to {@code java.sql.Types}
	 * @param scale the number of digits after the decimal point
	 * (for DECIMAL and NUMERIC types)
	 * @param value the value object
	 */
	public SqlParameterValue(int sqlType, int scale, Object value) {
		super(sqlType, scale);
		this.value = value;
	}

	/**
	 * Create a new SqlParameterValue based on the given SqlParameter declaration.
	 * @param declaredParam the declared SqlParameter to define a value for
	 * @param value the value object
	 */
	public SqlParameterValue(SqlParameter declaredParam, Object value) {
		super(declaredParam);
		this.value = value;
	}


	/**
	 * Return the value object that this parameter value holds.
	 */
	public Object getValue() {
		return this.value;
	}
}
