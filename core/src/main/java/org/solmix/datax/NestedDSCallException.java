package org.solmix.datax;

public class NestedDSCallException extends DataxRuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7135730258048957795L;

	public NestedDSCallException() {

	}

	/**
	 * @param string
	 * @param e
	 */
	public NestedDSCallException(String string, DSCallException e) {
		super(string, e);
	}

	public NestedDSCallException(String string) {
		super(string);
	}

	/**
	 * @param e
	 */
	public NestedDSCallException(Throwable e) {
		super(e);
	}
}
