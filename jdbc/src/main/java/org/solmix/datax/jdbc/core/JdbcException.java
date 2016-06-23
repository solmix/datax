package org.solmix.datax.jdbc.core;

import org.solmix.datax.jdbc.SQLRuntimeException;


public class JdbcException extends SQLRuntimeException {

	public JdbcException(Throwable e) {
		super(e);
	}

	public JdbcException(String string) {
		super(string);
	}

	public JdbcException(String msg, Throwable cause) {
		super(msg,cause);
	}

	private static final long serialVersionUID = 7915926330958766187L;

}
