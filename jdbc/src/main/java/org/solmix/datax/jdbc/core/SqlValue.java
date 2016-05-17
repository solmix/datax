package org.solmix.datax.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlValue {
	void setValue(PreparedStatement ps, int paramIndex)	throws SQLException;

	/**
	 * Clean up resources held by this value object.
	 */
	void cleanup();
}
