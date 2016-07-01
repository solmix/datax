package org.solmix.datax.jdbc.dialect;

public class SelectScript {

	private String sql;
	private int[] jdbcTypes;
	private String[] jdbcTypeNames;
	
	private String[] columns;
	public String[] getJdbcTypeNames() {
		return jdbcTypeNames;
	}
	public void setJdbcTypeNames(String[] jdbcTypeNames) {
		this.jdbcTypeNames = jdbcTypeNames;
	}
	private Object[] args;
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
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	
	
	public String[] getColumns() {
		return columns;
	}
	public void setColumns(String[] columns) {
		this.columns = columns;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(sql!=null){
			sb.append("sql:").append(sql).append("\n");
		}
		if(jdbcTypes!=null){
			sb.append("jdbcTypes:[");
			for(int i=0;i<jdbcTypes.length;i++){
				sb.append(jdbcTypes[i]);
				if(i<jdbcTypes.length){
					sb.append(",");
				}
			}
			sb.append("]\n");
		}
		if(jdbcTypeNames!=null){
			sb.append("jdbcTypeNames:[");
			for(int i=0;i<jdbcTypeNames.length;i++){
				sb.append(jdbcTypeNames[i]);
				if(i<jdbcTypeNames.length){
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	
}
