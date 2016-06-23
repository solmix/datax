package org.solmix.datax.jdbc.core;


public class EmptyResultJdbcException extends IncorrectResultSizeJdbcException {

	private static final long serialVersionUID = -4322870579793895173L;

	public EmptyResultJdbcException(int i) {
		super(i,0);
	}

}
