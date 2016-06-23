package org.solmix.datax.jdbc.core;


public class IncorrectResultSizeJdbcException extends JdbcException {

	private int expectedSize;

	private int actualSize;
	public IncorrectResultSizeJdbcException(Throwable e) {
		super(e);
	}

	public IncorrectResultSizeJdbcException(int expectedSize, int actualSize) {
		super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
		this.expectedSize = expectedSize;
		this.actualSize = actualSize;
	}

}
