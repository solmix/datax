package org.solmix.datax.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.solmix.datax.jdbc.helper.JdbcHelper;

public class SQLMetaData {
	public String database;

    public Connection conn;
    public SQLMetaData(Connection conn){
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
        return getTablesOfType(catalog, "VIEW");
    }

    public List<?> getTables(String catalog) throws SQLException {
        return getTablesOfType(catalog, "TABLE");
    }

    public List<?> getTableNamesAndRemarks(String catalog, String schema, List<String> types) throws SQLException {
        return getBasicTableDetailsForType(catalog, schema, types);
    }

    public List<?> getTablesOfType(String catalog, String type) throws SQLException {
        String types[] = { type };
        List<Map<String, ?>> results = JdbcHelper.toListOfMaps(getMetaData().getTables(catalog, null, null, types));
        return mapValuesAsListForKey("TABLE_NAME", results);
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
        return mapValuesAsListForKey("COLUMN_NAME", results);
    }

    public List<?> getPrimaryKeys(String catalog, String table) throws SQLException {
        java.sql.ResultSet rs = getMetaData().getPrimaryKeys(catalog, null, table);
        List<Map<String, ?>> results = JdbcHelper.toListOfMaps(rs);
        return mapValuesAsListForKey("COLUMN_NAME", results);
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

}
