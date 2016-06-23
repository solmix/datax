package org.solmix.datax.jdbc.core;

public class TypeMismatchDataAccessException extends JdbcException {
	public TypeMismatchDataAccessException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for TypeMismatchDataAccessException.
	 * @param msg the detail message
	 * @param cause the root cause from the data access API in use
	 */
	public TypeMismatchDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
