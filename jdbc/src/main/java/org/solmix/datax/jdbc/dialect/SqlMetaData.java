package org.solmix.datax.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.jdbc.helper.JdbcHelper;
import org.solmix.datax.jdbc.sql.SQLGenerationException;

/**
 * @author solmix
 *
 */
public class SqlMetaData {
	
	private static final String TABLE="TABLE",VIEW="VIEW";
	private static final String COLUMN_NAME="COLUMN_NAME",COLUMN_TYPE_NAME="TYPE_NAME",TABLE_NAME="TABLE_NAME",DATA_TYPE="DATA_TYPE";

	public String database;

    public Connection conn;
    public SqlMetaData(Connection conn){
    	this.conn=conn;
    }
    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    public List<?> mapValuesAsList(List<Map<String, ?>> list) throws SQLException {
        List<Object> results = new ArrayList<Object>();
        Map<String, ?> map;
        for (Iterator<Map<String, ?>> i = list.iterator(); i.hasNext(); results.add(map.values().iterator().next())) {
            map =  i.next();
            if (map.size() != 1)
                throw new SQLException((new StringBuilder()).append("Expected single key/value pair - map size is: ").append(map.size()).toString());
        }

        return results;
    }

    public List<?> mapValuesAsListForKey(Object key, List<Map<String, ?>> list) throws SQLException {
        List<Object> results = new ArrayList<Object>();
        Object result;
        for (Iterator<Map<String, ?>> i = list.iterator(); i.hasNext(); results.add(result)) {
            Map<String, ?> map =  i.next();
            if (map.size() < 1)
                throw new SQLException(
                    (new StringBuilder()).append("Expected at least one  key/value pair - map size is: ").append(map.size()).toString());
            result = map.get(key);
            if (result == null)
                result = map.get(key.toString().toLowerCase());
            if (result == null)
                result = map.get(key.toString().toUpperCase());
            if (result == null)
                throw new SQLException((new StringBuilder()).append("Unable to get value for key: ").append(key.toString()).toString());
        }

        return results;
    }

    public List<Map<String, ?>> mapValuesAsListForKeySet(Set<String> keys, List<Map<String, ?>> list) throws SQLException {
        List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
        Map<String, Object> resultMap;
        label0: for (Iterator<Map<String, ?>> i = list.iterator(); i.hasNext(); results.add(resultMap)) {
        	Map<String, ?> map = i.next();
            if (map.size() < 1)
                throw new SQLException(
                    (new StringBuilder()).append("Expected at least one  key/value pair - map size is: ").append(map.size()).toString());
            resultMap = new HashMap<String, Object>();
            Iterator<String> j = keys.iterator();
            do {
                if (!j.hasNext())
                    continue label0;
                String key = j.next().toString();
                Object result = map.get(key);
                if (result == null)
                    result = map.get(key.toLowerCase());
                if (result == null)
                    result = map.get(key.toUpperCase());
                if (result != null)
                    resultMap.put(key, result);
            } while (true);
        }

        return results;
    }

    public Map<String, String> getProductNameAndVersion() throws SQLException {
        String name = getMetaData().getDatabaseProductName();
        String version = getMetaData().getDatabaseProductVersion();
        Map<String, String> map = new HashMap<String, String>();
        map.put("productName", name);
        map.put("productVersion", version);
        return map;
    }

    public List<?> getCatalogs() throws SQLException {
        return mapValuesAsList(JdbcHelper.toListOfMaps(getMetaData().getCatalogs()));
    }

    public List<?> getSchemas() throws SQLException {
        return mapValuesAsList(JdbcHelper.toListOfMaps(getMetaData().getSchemas()));
    }

    public List<?> getTableTypes() throws SQLException {
        return mapValuesAsList(JdbcHelper.toListOfMaps(getMetaData().getTableTypes()));
    }

    public List<?> getViews(String catalog) throws SQLException {
        return getTablesOfType(catalog, VIEW);
    }

    public List<?> getTables(String catalog) throws SQLException {
        return getTablesOfType(catalog, TABLE);
    }

    public List<?> getTableNamesAndRemarks(String catalog, String schema, List<String> types) throws SQLException {
        return getBasicTableDetailsForType(catalog, schema, types);
    }

    public List<?> getTablesOfType(String catalog, String type) throws SQLException {
        String types[] = { type };
        List<Map<String, ?>> results = JdbcHelper.toListOfMaps(getMetaData().getTables(catalog, null, null, types));
        return mapValuesAsListForKey(TABLE_NAME, results);
    }

    public List<Map<String, ?>> getBasicTableDetailsForType(String catalog, String schema, List<String> types) throws SQLException {
        String typesArr[] = new String[types.size()];
        for (int i = 0; i < types.size(); i++)
            typesArr[i] = types.get(i);

        List<Map<String, ?>> results = JdbcHelper.toListOfMaps(getMetaData().getTables(catalog, schema, null, typesArr));
        Set<String> keys = new HashSet<String>();
        keys.add("TABLE_NAME");
        keys.add("REMARKS");
        keys.add("TABLE_SCHEM");
        keys.add("TABLE_TYPE");
        return mapValuesAsListForKeySet(keys, results);
    }

    public List<?> getColumnNames(String catalog, String table) throws SQLException {
         List<Map<String, ?>> results = JdbcHelper.toListOfMaps(getMetaData().getColumns(catalog, null, table, null));
        return mapValuesAsListForKey(COLUMN_NAME, results);
    }

    public List<?> getPrimaryKeys(String catalog, String table) throws SQLException {
        java.sql.ResultSet rs = getMetaData().getPrimaryKeys(catalog, null, table);
        List<Map<String, ?>> results = JdbcHelper.toListOfMaps(rs);
        return mapValuesAsListForKey(COLUMN_NAME, results);
    }

    public List<Map<String, ?>>  getColumnMetaData(String catalog, String schemaName, String table) throws SQLException {
    	List<Map<String, ?>>  results = JdbcHelper.toListOfMaps(getMetaData().getColumns(catalog, schemaName, table, null));
        if (results == null || results.size() == 0)
            return new ArrayList<Map<String, ?>> ();
        else
            return results;
    }

    public Map<String, ?> getColumnMetaData(String catalog, String schemaName, String table, String column) throws SQLException {
    	List<Map<String, ?>>  results = JdbcHelper.toListOfMaps(getMetaData().getColumns(catalog, schemaName, table, column));
        if (results == null || results.size() == 0)
            return new HashMap<String,Object>();
        else
            return results.get(0);
    }

	@Override
    protected void finalize() throws Throwable {
        if(conn!=null)
        conn.close();
    }
	
	
	/**
	 * 根据数据库表结构生成Delete语句
	 * 
	 * @param catalog
	 * @param schemaName
	 * @param table 表名
	 * @param conditionKeys 删除条件对应数据库字段名
	 * @return
	 * @throws SQLException
	 */
	public DeleteScript generateDelete(String catalog, String schemaName, String table,String[] conditionKeys,String conditionExp) throws SQLException{
		ResultSet rs=getMetaData().getColumns(catalog, schemaName, table, null);
		TreeSet<Map<String, ?>>  results = JdbcHelper.toSetOfMaps(rs,COLUMN_NAME);
		LinkedHashMap<String,Map<String, ?>> columnMap= new LinkedHashMap<String, Map<String,?>>();
		for(Map<String, ?> column:results){
			String column1= (String)column.get(COLUMN_NAME);
			columnMap.put(column1, column);
		}
		
		StringBuilder tableClause = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		
		tableClause.append("delete from ");
		
		if(StringUtils.isNotEmpty(schemaName)){
			if(StringUtils.isNotEmpty(catalog)){
				tableClause.append(catalog).append(".");
			}
			tableClause.append(schemaName).append(".");
		}
		tableClause.append(table);
		
		DeleteScript script= new DeleteScript();
		if(DataUtils.isNotNullAndEmpty(conditionKeys)||StringUtils.isNotEmpty(conditionExp)){
			whereClause.append(" where ");
		}
		if(DataUtils.isNotNullAndEmpty(conditionKeys)){
			int[] jdbcTypes = new int[conditionKeys.length];
			String[] jdbcTypeNames = new String[conditionKeys.length];
			for(int i=0;i<conditionKeys.length;i++){
				String condition = conditionKeys[i];
				Map<String,?> col = columnMap.get(condition);
				if(col==null){
					throw new ColumnNotFondException("column :"+condition +"is not found in"+table);
				}
				String type = (String)col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[i]=type;
				jdbcTypes[i]=getDataType(col, type);
				whereClause.append(condition).append(" = ? ");
				if(i<conditionKeys.length-1){
					whereClause.append(" and ");
				}
			}
			script.setJdbcTypeNames(jdbcTypeNames);
			script.setJdbcTypes(jdbcTypes);
		}
		if(StringUtils.isNotEmpty(conditionExp)){
			if(whereClause.length()>=10){
				whereClause.append(" and ");
			}
			whereClause.append(conditionExp);
		}
		String sql= new StringBuffer()
		.append(tableClause)
		.append(whereClause)
		.toString();
		script.setSql(sql);
		return script;
	}
	
	/**
	 * 根据数据库表结构生成Select语句
	 * @param catalog
	 * @param schemaName
	 * @param table
	 * @param conditionKeys
	 * @return
	 * @throws SQLException
	 */
	public SelectScript generateSelect(String catalog, 
									   String schemaName, 
									   String table,
									   String[] conditionKeys,String conditionExp) throws SQLException{
		ResultSet rs=getMetaData().getColumns(catalog, schemaName, table, null);
		TreeSet<Map<String, ?>>  results = JdbcHelper.toSetOfMaps(rs,COLUMN_NAME);
		LinkedHashMap<String,Map<String, ?>> columnMap= new LinkedHashMap<String, Map<String,?>>();
		for(Map<String, ?> column:results){
			String column1= (String)column.get(COLUMN_NAME);
			columnMap.put(column1, column);
		}
		int columnLength=results.size();
		StringBuilder selectClause = new StringBuilder();
		StringBuilder tableClause = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		
		selectClause.append("select ");
		
		List<String> columns = new ArrayList<String>();
		Iterator<String> ki=columnMap.keySet().iterator();
		int kcount=0;
		while(ki.hasNext()){
			String key=ki.next();
			kcount++;
			selectClause.append(key);
			columns.add(key);
			if(kcount<columnLength){
				selectClause.append(",");
			}
		}
		
		
		tableClause.append(" from ");
		
		if(StringUtils.isNotEmpty(schemaName)){
			if(StringUtils.isNotEmpty(catalog)){
				tableClause.append(catalog).append(".");
			}
			tableClause.append(schemaName).append(".");
		}
		tableClause.append(table);
		
		SelectScript script= new SelectScript();
		if(DataUtils.isNotNullAndEmpty(conditionKeys)||StringUtils.isNotEmpty(conditionExp)){
			whereClause.append(" where ");
		}
		if(DataUtils.isNotNullAndEmpty(conditionKeys)){
			int[] jdbcTypes = new int[conditionKeys.length];
			String[] jdbcTypeNames = new String[conditionKeys.length];
			for(int i=0;i<conditionKeys.length;i++){
				String condition = conditionKeys[i];
				Map<String,?> col = columnMap.get(condition);
				if(col==null){
					throw new ColumnNotFondException("column :"+condition +"is not found in"+table);
				}
				String type = (String)col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[i]=type;
				jdbcTypes[i]=getDataType(col, type);
				whereClause.append(condition).append(" = ? ");
				if(i<conditionKeys.length-1){
					whereClause.append(" and ");
				}
			}
			script.setJdbcTypeNames(jdbcTypeNames);
			script.setJdbcTypes(jdbcTypes);
		}
		if(StringUtils.isNotEmpty(conditionExp)){
			if(whereClause.length()>=10){
				whereClause.append(" and ");
			}
			whereClause.append(conditionExp);
		}
		String sql= new StringBuffer()
						.append(selectClause)
						.append(tableClause)
						.append(whereClause)
						.toString();
		script.setSql(sql);
		script.setColumns(columns.toArray(new String[]{}));
		
		return script;
	}
	
	/**
	 * 根据数据库表结构生Update语句
	 * 
	 * @param catalog
	 * @param schemaName
	 * @param table
	 * @param valueKeys 值对应的数据库字段名
	 * @param conditionKeys 条件对应的数据库字段名
	 * @return
	 * @throws SQLException
	 */
	public UpdateScript generateUpdate(String catalog, 
									   String schemaName,
									   String table,
									   String[] valueKeys,
									   String[] conditionKeys,String conditionExp) throws SQLException {
		ResultSet rs = getMetaData().getColumns(catalog, schemaName, table,
				null);
		TreeSet<Map<String, ?>> results = JdbcHelper.toSetOfMaps(rs,COLUMN_NAME);
		LinkedHashMap<String, Map<String, ?>> columnMap = new LinkedHashMap<String, Map<String, ?>>();
		for (Map<String, ?> column : results) {
			String column1 = (String) column.get(COLUMN_NAME);
			columnMap.put(column1, column);
		}
		StringBuilder updateClause = new StringBuilder();
		StringBuilder valueClause = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		updateClause.append("update ");
		
		if (StringUtils.isNotEmpty(schemaName)) {
			if (StringUtils.isNotEmpty(catalog)) {
				updateClause.append(catalog).append(".");
			}
			updateClause.append(schemaName).append(".");
		}
		updateClause.append(table);
		updateClause.append(" set ");
		int keyLength = valueKeys.length+(conditionKeys==null?0:conditionKeys.length);
		int[] jdbcTypes = new int[keyLength];
		String[] jdbcTypeNames = new String[keyLength];
		int i=0;
		if(DataUtils.isNotNullAndEmpty(valueKeys)){
			for(;i<valueKeys.length;i++){
				String value = valueKeys[i];
				Map<String,?> col = columnMap.get(value);
				if(col==null){
					throw new ColumnNotFondException("column :"+value +"is not found in"+table);
				}
				String type = (String)col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[i]=type;
				jdbcTypes[i]=getDataType(col, type);
				valueClause.append(value).append(" = ? ");
				if(i<valueKeys.length){
					valueClause.append(" and ");
				}
			}
		}else{
			throw new SQLGenerationException("update with no field");
		}
		if(DataUtils.isNotNullAndEmpty(conditionKeys)||StringUtils.isNotEmpty(conditionExp)){
			whereClause.append(" where ");
		}
		if(DataUtils.isNotNullAndEmpty(conditionKeys)){
			for(int j=0;j<conditionKeys.length;j++){
				String condition = conditionKeys[j];
				Map<String,?> col = columnMap.get(condition);
				if(col==null){
					throw new ColumnNotFondException("column :"+condition +"is not found in"+table);
				}
				String type = (String)col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[i+j]=type;
				jdbcTypes[i+j]=getDataType(col, type);
				whereClause.append(condition).append(" = ? ");
				if(j<conditionKeys.length-1){
					whereClause.append(" and ");
				}
			}
			
		}
		if(StringUtils.isNotEmpty(conditionExp)){
			if(whereClause.length()>=10){
				whereClause.append(" and ");
			}
			whereClause.append(conditionExp);
		}
		UpdateScript script= new UpdateScript();
		String sql = new StringBuffer()
				.append(updateClause)
				.append(valueClause)
				.append(whereClause)
				.toString();
		script.setSql(sql);
		script.setJdbcTypeNames(jdbcTypeNames);
		script.setJdbcTypes(jdbcTypes);

		return script;
	}
	
	/**
	 *  根据数据库表结构生Insert语句
	 * @param catalog
	 * @param schemaName
	 * @param table
	 * @param columnSequence 数据库字段顺序
	 * @return
	 * @throws SQLException
	 */
	public InsertScript generateInsert(String catalog, 
									   String schemaName,
									   String table,
									   String[] columnSequence) throws SQLException {
		ResultSet rs = getMetaData().getColumns(catalog, schemaName, table,null);
		TreeSet<Map<String, ?>> results = JdbcHelper.toSetOfMaps(rs,COLUMN_NAME);
		LinkedHashMap<String, Map<String, ?>> columnMap = new LinkedHashMap<String, Map<String, ?>>();
		for (Map<String, ?> column : results) {
			String column1 = (String) column.get(COLUMN_NAME);
			columnMap.put(column1, column);
		}
		
		StringBuilder insertClause = new StringBuilder();
		StringBuilder valueClause = new StringBuilder();

		insertClause.append("insert into ");

		if (StringUtils.isNotEmpty(schemaName)) {
			if (StringUtils.isNotEmpty(catalog)) {
				insertClause.append(catalog).append(".");
			}
			insertClause.append(schemaName).append(".");
		}
		insertClause.append(table);
		insertClause.append("(");
		
		InsertScript script = new InsertScript();
		if(DataUtils.isNotNullAndEmpty(columnSequence)){
			int[] jdbcTypes = new int[columnSequence.length];
			String[] jdbcTypeNames = new String[columnSequence.length];
			List<String> columns = new ArrayList<String>();
			for(int i=0;i<columnSequence.length;i++){
				String seq = columnSequence[i];
				Map<String,?> col = columnMap.get(seq);
				if(col==null){
					throw new ColumnNotFondException("column :"+seq +"is not found in"+table);
				}
				String type = (String)col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[i] = type;
				jdbcTypes[i]=getDataType(col, type);
				insertClause.append(seq);
				valueClause.append("?");
				columns.add(seq);
				if (i < columnSequence.length-1) {
					insertClause.append(",");
					valueClause.append(",");
				}
			}
			script.setJdbcTypes(jdbcTypes);
			script.setJdbcTypeNames(jdbcTypeNames);
			script.setColumns(columns.toArray(new String[] {}));
		}else{//如果没指定字段顺序，按照默认排序生成
			int columnLength = results.size();
			int[] jdbcTypes = new int[results.size()];
			String[] jdbcTypeNames = new String[results.size()];
			List<String> columns = new ArrayList<String>();
			Iterator<String> ki = columnMap.keySet().iterator();
			int kcount = 0;
			while (ki.hasNext()) {
				String key = ki.next();
				Map<String, ?> col = columnMap.get(key);
				String type = (String) col.get(COLUMN_TYPE_NAME);
				jdbcTypeNames[kcount] = type;
				jdbcTypes[kcount] =getDataType(col, type);
				kcount++;
				insertClause.append(key);
				valueClause.append("?");
				columns.add(key);
				if (kcount < columnLength) {
					insertClause.append(",");
					valueClause.append(",");
				}
			}
			script.setJdbcTypes(jdbcTypes);
			script.setJdbcTypeNames(jdbcTypeNames);
			script.setColumns(columns.toArray(new String[] {}));
		}
		
		insertClause.append(") values (");
		String sql = new StringBuffer().append(insertClause)
				.append(valueClause).append(")").toString();
		script.setSql(sql);
		return script;
	}

	private int getDataType(Map<String,?> col,String typeName){
		Object type = col.get(DATA_TYPE);
		if(type!=null){
			return Integer.valueOf(type.toString());
		}else{
			return JdbcTypeTranslator.getJdbcType(typeName);
		}
	}
}
