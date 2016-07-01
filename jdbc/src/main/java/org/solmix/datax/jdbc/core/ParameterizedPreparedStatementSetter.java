package org.solmix.datax.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterizedPreparedStatementSetter<T> {
	void setValues(PreparedStatement ps, T argument) throws SQLException;
}
