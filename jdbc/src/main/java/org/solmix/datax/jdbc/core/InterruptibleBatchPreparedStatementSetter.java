package org.solmix.datax.jdbc.core;


public interface InterruptibleBatchPreparedStatementSetter extends BatchPreparedStatementSetter {

	boolean isBatchExhausted(int i);

}
