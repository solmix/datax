package org.solmix.datax.jdbc.core;


public interface DisposableSqlTypeValue extends SqlTypeValue {
	void cleanup();

}
