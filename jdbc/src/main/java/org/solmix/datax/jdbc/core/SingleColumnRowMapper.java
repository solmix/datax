package org.solmix.datax.jdbc.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.solmix.commons.util.NumberUtils;
import org.solmix.datax.jdbc.helper.JdbcHelper;



public class SingleColumnRowMapper<T> implements RowMapper<T> {

	private Class<T> requiredType;


	/**
	 * Create a new SingleColumnRowMapper.
	 * @see #setRequiredType
	 */
	public SingleColumnRowMapper() {
	}

	/**
	 * Create a new SingleColumnRowMapper.
	 * @param requiredType the type that each result object is expected to match
	 */
	public SingleColumnRowMapper(Class<T> requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * Set the type that each result object is expected to match.
	 * <p>If not specified, the column value will be exposed as
	 * returned by the JDBC driver.
	 */
	public void setRequiredType(Class<T> requiredType) {
		this.requiredType = requiredType;
	}


	/**
	 * Extract a value for the single column in the current row.
	 * <p>Validates that there is only one column selected,
	 * then delegates to {@code getColumnValue()} and also
	 * {@code convertValueToRequiredType}, if necessary.
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 * @see #getColumnValue(java.sql.ResultSet, int, Class)
	 * @see #convertValueToRequiredType(Object, Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Validate column count.
		ResultSetMetaData rsmd = rs.getMetaData();
		int nrOfColumns = rsmd.getColumnCount();
		if (nrOfColumns != 1) {
			throw new IncorrectResultSizeJdbcException(1, nrOfColumns);
		}

		// Extract column value from JDBC ResultSet.
		Object result = getColumnValue(rs, 1, this.requiredType);
		if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
			// Extracted value does not match already: try to convert it.
			try {
				return (T) convertValueToRequiredType(result, this.requiredType);
			}
			catch (IllegalArgumentException ex) {
				throw new TypeMismatchDataAccessException(
						"Type mismatch affecting row number " + rowNum + " and column type '" +
						rsmd.getColumnTypeName(1) + "': " + ex.getMessage());
			}
		}
		return (T) result;
	}

	/**
	 * Retrieve a JDBC object value for the specified column.
	 * <p>The default implementation calls
	 * {@link JdbcHelper#getResultSetValue(java.sql.ResultSet, int, Class)}.
	 * If no required type has been specified, this method delegates to
	 * {@code getColumnValue(rs, index)}, which basically calls
	 * {@code ResultSet.getObject(index)} but applies some additional
	 * default conversion to appropriate value types.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @param requiredType the type that each result object is expected to match
	 * (or {@code null} if none specified)
	 * @return the Object value
	 * @throws SQLException in case of extraction failure
	 * @see #getColumnValue(java.sql.ResultSet, int)
	 */
	protected Object getColumnValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
		if (requiredType != null) {
			return JdbcHelper.getResultSetValue(rs, index, requiredType);
		}
		else {
			// No required type specified -> perform default extraction.
			return getColumnValue(rs, index);
		}
	}

	/**
	 * Retrieve a JDBC object value for the specified column, using the most
	 * appropriate value type. Called if no required type has been specified.
	 * <p>The default implementation delegates to {@code JdbcHelper.getResultSetValue()},
	 * which uses the {@code ResultSet.getObject(index)} method. Additionally,
	 * it includes a "hack" to get around Oracle returning a non-standard object for
	 * their TIMESTAMP datatype. See the {@code JdbcHelper#getResultSetValue()}
	 * javadoc for details.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @return the Object value
	 * @throws SQLException in case of extraction failure
	 */
	protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcHelper.getResultSetValue(rs, index);
	}

	/**
	 * Convert the given column value to the specified required type.
	 * Only called if the extracted column value does not match already.
	 * <p>If the required type is String, the value will simply get stringified
	 * via {@code toString()}. In case of a Number, the value will be
	 * converted into a Number, either through number conversion or through
	 * String parsing (depending on the value type).
	 * @param value the column value as extracted from {@code getColumnValue()}
	 * (never {@code null})
	 * @param requiredType the type that each result object is expected to match
	 * (never {@code null})
	 * @return the converted value
	 * @see #getColumnValue(java.sql.ResultSet, int, Class)
	 */
	@SuppressWarnings("unchecked")
	protected Object convertValueToRequiredType(Object value, Class<?> requiredType) {
		if (String.class.equals(requiredType)) {
			return value.toString();
		}
		else if (Number.class.isAssignableFrom(requiredType)) {
			if (value instanceof Number) {
				// Convert original Number to target Number class.
				return NumberUtils.convertNumberToTargetClass(((Number) value), (Class<Number>) requiredType);
			}
			else {
				// Convert stringified value to target Number class.
				return NumberUtils.parseNumber(value.toString(),(Class<Number>) requiredType);
			}
		}
		else {
			throw new IllegalArgumentException(
					"Value [" + value + "] is of type [" + value.getClass().getName() +
					"] and cannot be converted to required type [" + requiredType.getName() + "]");
		}
	}

}
