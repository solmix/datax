package org.solmix.datax.jdbc.core;


public interface InterruptibleBatchPreparedStatementSetter extends BatchPreparedStatementSetter {

	/**
	 * Return whether the batch is complete, that is, whether there were no
	 * additional values added during the last {@code setValues} call.
	 * <p><b>NOTE:</b> If this method returns {@code true}, any parameters
	 * that might have been set during the last {@code setValues} call will
	 * be ignored! Make sure that you set a corresponding internal flag if you
	 * detect exhaustion <i>at the beginning</i> of your {@code setValues}
	 * implementation, letting this method return {@code true} based on the flag.
	 * @param i index of the statement we're issuing in the batch, starting from 0
	 * @return whether the batch is already exhausted
	 * @see #setValues
	 * @see org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter#setValuesIfAvailable
	 */
	boolean isBatchExhausted(int i);

}
