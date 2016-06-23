package org.solmix.datax.jdbc.core;

import java.sql.SQLException;

public class InvalidResultSetAccessException extends JdbcException {

	public InvalidResultSetAccessException(String string) {
		super(string);
	}

	public InvalidResultSetAccessException(Throwable se) {
		super(se);
	}

}
