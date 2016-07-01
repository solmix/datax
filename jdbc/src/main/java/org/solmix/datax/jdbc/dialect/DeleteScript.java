package org.solmix.datax.jdbc.dialect;

public class DeleteScript {
	private String sql;
	private int[] jdbcTypes;
	private String[] jdbcTypeNames;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int[] getJdbcTypes() {
		return jdbcTypes;
	}

	public void setJdbcTypes(int[] jdbcTypes) {
		this.jdbcTypes = jdbcTypes;
	}

	public String[] getJdbcTypeNames() {
		return jdbcTypeNames;
	}

	public void setJdbcTypeNames(String[] jdbcTypeNames) {
		this.jdbcTypeNames = jdbcTypeNames;
	}
}
