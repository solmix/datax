/*
 * Copyright 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.datax.jdbc.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.timer.StopWatch;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.datax.DSCallException;
import org.solmix.datax.jdbc.core.EmptyResultJdbcException;
import org.solmix.datax.jdbc.core.IncorrectResultSizeJdbcException;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.jdbc.support.MetaDataAccessException;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月11日
 */

public class JdbcHelper
{

    public static final int TYPE_UNKNOWN = Integer.MIN_VALUE;

    private static final Logger LOG = LoggerFactory.getLogger(JdbcHelper.class);

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                LOG.debug("Could not close JDBC Connection", ex);
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                LOG.debug("Unexpected exception on closing JDBC Connection", ex);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                LOG.trace("Could not close JDBC Statement", ex);
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                LOG.trace("Unexpected exception on closing JDBC Statement", ex);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                LOG.trace("Could not close JDBC ResultSet", ex);
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                LOG.trace("Unexpected exception on closing JDBC ResultSet", ex);
            }
        }
    }

    public static Object getResultSetValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
        if (requiredType == null) {
            return getResultSetValue(rs, index);
        }

        Object value = null;
        boolean wasNullCheck = false;

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            value = rs.getString(index);
        } else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
            value = rs.getBoolean(index);
            wasNullCheck = true;
        } else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
            value = rs.getByte(index);
            wasNullCheck = true;
        } else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
            value = rs.getShort(index);
            wasNullCheck = true;
        } else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
            value = rs.getInt(index);
            wasNullCheck = true;
        } else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
            value = rs.getLong(index);
            wasNullCheck = true;
        } else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
            value = rs.getFloat(index);
            wasNullCheck = true;
        } else if (double.class.equals(requiredType) || Double.class.equals(requiredType) || Number.class.equals(requiredType)) {
            value = rs.getDouble(index);
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) {
            value = rs.getBytes(index);
        } else if (java.sql.Date.class.equals(requiredType)) {
            value = rs.getDate(index);
        } else if (java.sql.Time.class.equals(requiredType)) {
            value = rs.getTime(index);
        } else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
        } else if (BigDecimal.class.equals(requiredType)) {
            value = rs.getBigDecimal(index);
        } else if (Blob.class.equals(requiredType)) {
            value = rs.getBlob(index);
        } else if (Clob.class.equals(requiredType)) {
            value = rs.getClob(index);
        } else {
            // Some unknown type desired -> rely on getObject.
            value = getResultSetValue(rs, index);
        }

        // Perform was-null check if demanded (for results that the
        // JDBC driver returns as primitives).
        if (wasNullCheck && value != null && rs.wasNull()) {
            value = null;
        }
        return value;
    }

    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }
        if (obj instanceof Blob) {
            obj = rs.getBytes(index);
        } else if (obj instanceof Clob) {
            obj = rs.getString(index);
        } else if (className != null && ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className))) {
            obj = rs.getTimestamp(index);
        } else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            } else {
                obj = rs.getDate(index);
            }
        } else if (obj != null && obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return obj;
    }

    public static Object extractDatabaseMetaData(DataSource dataSource, final String metaDataMethodName) throws MetaDataAccessException {

        Connection con = null;
        try {
            con = dataSource.getConnection();
            if (con == null) {
                // should only happen in test environments
                throw new MetaDataAccessException("Connection returned by DataSource [" + dataSource + "] was null");
            }
            DatabaseMetaData metaData = con.getMetaData();
            if (metaData == null) {
                // should only happen in test environments
                throw new MetaDataAccessException("DatabaseMetaData returned by Connection [" + con + "] was null");
            }
            try {
                Method method = DatabaseMetaData.class.getMethod(metaDataMethodName, (Class[]) null);
                return method.invoke(metaData, (Object[]) null);
            } catch (NoSuchMethodException ex) {
                throw new MetaDataAccessException("No method named '" + metaDataMethodName + "' found on DatabaseMetaData instance [" + metaData
                    + "]", ex);
            } catch (IllegalAccessException ex) {
                throw new MetaDataAccessException("Could not access DatabaseMetaData method '" + metaDataMethodName + "'", ex);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof SQLException) {
                    throw (SQLException) ex.getTargetException();
                }
                throw new MetaDataAccessException("Invocation of DatabaseMetaData method '" + metaDataMethodName + "' failed", ex);
            }
        } catch (SQLException ex) {
            throw new MetaDataAccessException("Error while extracting DatabaseMetaData", ex);
        } catch (AbstractMethodError err) {
            throw new MetaDataAccessException("JDBC DatabaseMetaData method not implemented by JDBC driver - upgrade your driver", err);
        } finally {
           if(con!=null){
               try {
                con.close();
            } catch (SQLException e) {
              
            }
           }
        }
    }
    
    public static boolean supportsBatchUpdates(Connection con) {
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            if (dbmd != null) {
                if (dbmd.supportsBatchUpdates()) {
                    LOG.debug("JDBC driver supports batch updates");
                    return true;
                } else {
                    LOG.debug("JDBC driver does not support batch updates");
                }
            }
        } catch (SQLException ex) {
            LOG.debug("JDBC driver 'supportsBatchUpdates' method threw exception", ex);
        } catch (AbstractMethodError err) {
            LOG.debug("JDBC driver does not support JDBC 2.0 'supportsBatchUpdates' method", err);
        }
        return false;
    }

    public static String commonDatabaseName(String source) {
        String name = source;
        if (source != null && source.startsWith("DB2")) {
            name = "DB2";
        } else if ("Sybase SQL Server".equals(source) || "Adaptive Server Enterprise".equals(source) || "ASE".equals(source)
            || "sql server".equalsIgnoreCase(source)) {
            name = "Sybase";
        }
        return name;
    }

    public static boolean isNumeric(int sqlType) {
        return Types.BIT == sqlType || Types.BIGINT == sqlType || Types.DECIMAL == sqlType || Types.DOUBLE == sqlType || Types.FLOAT == sqlType
            || Types.INTEGER == sqlType || Types.NUMERIC == sqlType || Types.REAL == sqlType || Types.SMALLINT == sqlType || Types.TINYINT == sqlType;
    }

    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

    public static String convertUnderscoreNameToPropertyName(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.substring(1, 2).equals("_")) {
                result.append(name.substring(0, 1).toUpperCase());
            } else {
                result.append(name.substring(0, 1).toLowerCase());
            }
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals("_")) {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(s.toUpperCase());
                        nextIsUpper = false;
                    } else {
                        result.append(s.toLowerCase());
                    }
                }
            }
        }
        return result.toString();
    }
    public static <T> T requiredSingleResult(Collection<T> results) throws IncorrectResultSizeJdbcException {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			throw new EmptyResultJdbcException(1);
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeJdbcException(1, size);
		}
		return results.iterator().next();
	}
    
    public static List<Map<String, ?>> toListOfMaps(ResultSet rs) throws SQLException {
        return toListOfMaps(rs, -1L, false);
    }

  
    public static List<Map<String, ?>> toListOfMaps(ResultSet rs, boolean brokenCursorAPIs) throws SQLException {
        return toListOfMaps(rs, -1L, brokenCursorAPIs);
    }

    public static List<Map<String, ?>> toListOfMaps(ResultSet rs, long numRows) throws SQLException {
        return toListOfMaps(rs, numRows, false);
    }


    public static List<Map<String, ?>> toListOfMaps(ResultSet resultSet, long numRows, boolean hasBrokenCursorAPIs) throws SQLException {
        List<Map<String, ?>> __return = new ArrayList<Map<String, ?>>(128);
        /**
         * get bean class name from datasource.
         */
        StopWatch sw = new StopWatch();
        // If ResultSet is null.
        if (hasBrokenCursorAPIs) {
            if (!resultSet.next())
                return __return;
        } else {
            boolean isBeforeFirst = false;
            boolean isAfterLast = false;
            try {
                isBeforeFirst = resultSet.isBeforeFirst();
                isAfterLast = resultSet.isAfterLast();
            } catch (SQLException ignored) {
            	LOG.debug("isBeforeFirst()/isAfterLast() throwing exceptions .", ignored);
            }
            if ((isBeforeFirst || isAfterLast || resultSet.getRow() == 0) && !resultSet.next())
                return __return;
        }
        long i = 0;
        do {
            if (i >= numRows && numRows != -1L)
                break;
            Map<String, ?> map = toAttributeMap(resultSet);
            __return.add(map);
            /**
             * java.sql.ResultSet.next() move cursor to new row set.
             */
            if (!resultSet.next())
                break;
            i++;

        } while (true);
        if(LOG.isDebugEnabled()){
            LOG.debug("toListOfMaps used {}",sw.toString());
        }
        return __return;

    }
    public static TreeSet<Map<String, ?>> toSetOfMaps(ResultSet resultSet,final String comparatorKey) throws SQLException {
    	return toSetOfMaps(resultSet, -1L,false,comparatorKey);
    }
    
    public static TreeSet<Map<String, ?>> toSetOfMaps(ResultSet resultSet, long numRows, boolean hasBrokenCursorAPIs,final String comparatorKey) throws SQLException {
        TreeSet<Map<String, ?>> __return = new TreeSet<Map<String, ?>>(new Comparator<Map<String, ?>>(){

			@Override
			public int compare(Map<String, ?> o1, Map<String, ?> o2) {
				String column1= (String)o1.get(comparatorKey);
				String column2= (String)o2.get(comparatorKey);
				if(column1==null){
					throw new IllegalArgumentException("Column name is null");
				}
				return column1.compareTo(column2);
			}
			
		});
        /**
         * get bean class name from datasource.
         */
        // If ResultSet is null.
        if (hasBrokenCursorAPIs) {
            if (!resultSet.next())
                return __return;
        } else {
            boolean isBeforeFirst = false;
            boolean isAfterLast = false;
            try {
                isBeforeFirst = resultSet.isBeforeFirst();
                isAfterLast = resultSet.isAfterLast();
            } catch (SQLException ignored) {
            	LOG.debug("isBeforeFirst()/isAfterLast() throwing exceptions .", ignored);
            }
            if ((isBeforeFirst || isAfterLast || resultSet.getRow() == 0) && !resultSet.next())
                return __return;
        }
        long i = 0;
        do {
            if (i >= numRows && numRows != -1L)
                break;
            Map<String, ?> map = toAttributeMap(resultSet);
            __return.add(map);
            /**
             * java.sql.ResultSet.next() move cursor to new row set.
             */
            if (!resultSet.next())
                break;
            i++;

        } while (true);
        return __return;

    }

    public static Map<String, List<Object>> toMapOfLists(ResultSet rs) throws SQLException {
        Map<String, List<Object>> result = new HashMap<String, List<Object>>(128);
        ResultSetMetaData header = rs.getMetaData();
        for (int ii = 1; ii <= header.getColumnCount(); ii++)
            result.put(header.getColumnName(ii), new ArrayList<Object>());
        while (rs.next()) {
            int ii = 1;
            while (ii <= header.getColumnCount()) {
                result.get(header.getColumnName(ii)).add(rs.getObject(ii));
                ii++;
            }
        }
        return result;
    }

    public static List<List<Object>> toFormatList(ResultSet results, List<String> column) throws SQLException {

        List<List<Object>> __return = new ArrayList<List<Object>>(128);
        if (results == null)
            return __return;
        ResultSetMetaData header = results.getMetaData();
        List<Object> _tmp;
        boolean writeFlag = false;
        while (results.next()) {
            int i = 1;
            _tmp = new ArrayList<Object>();
            while (i <= header.getColumnCount()) {
                if (!writeFlag) {
                    if (column == null) {
                        column = new ArrayList<String>();
                    }
                    column.add(header.getColumnName(i));
                }
                _tmp.add(results.getObject(i));
                i++;
            }
            writeFlag = true;
            __return.add(_tmp);
        }
        return __return;

    }

    public static List<Object> toValuesList(ResultSet results, String column) throws SQLException {
        List<Object> valuesList = new ArrayList<Object>(128);
        do {
            if (!results.next())
                break;
            Object value = results.getObject(column.toUpperCase());
            if (value != null)
                valuesList.add(value);
        } while (true);
        return valuesList;
    }

    /**
     * @param <T>
     * @param results
     * @param clz
     * @return
     * @throws SQLException
     */
    public static <T> List<T> toListofBeans(ResultSet results, Class<T> clz) throws SQLException {
        List<?> list = toListOfMaps(results);
        List<T> _return = new ArrayList<T>();
        if (list == null)
            return null;
        try {
            for (Object o : list) {
                Map<Object, Object> data = null;
                if (o instanceof Map<?, ?>) {
                    data = (Map<Object, Object>) o;
                } else {
                    continue;
                }
                T obj = Reflection.newInstance(clz);
                DataUtils.setProperties(data, obj, false);
                _return.add(obj);
            }
        } catch (Exception e) {
        	LOG.error("can not transform data to bean object", e);
        }
        return _return;

    }
    /**将数据库查询集合转换为Map*/
    public static Map<String, ?> toAttributeMap(ResultSet resultSet) throws SQLException {
        return toAttributeMap(resultSet, null, true, null, null);
    }

    /**
     * 将数据库查询集合转换为Map
     * 
     * @param resultSet 数据查询集合
     * @param rsmd 数据集描述
     * @param useColumnLabel
     * @param caseInsensitiveMap 字段映射
     * @param outputs	输出字段
     * @return
     * @throws SQLException
     */
    public static Map<String, ?> toAttributeMap(ResultSet resultSet,  
    		ResultSetMetaData rsmd, 
    		boolean useColumnLabel,
    		Map<String, String> caseInsensitiveMap, 
    		List<String> outputs) throws SQLException {
        if (rsmd == null)
            rsmd = resultSet.getMetaData();
        int count = rsmd.getColumnCount();
        Map<String, Object> __return = new HashMap<String, Object>();
        for (int colCursor = 1; colCursor <= count; colCursor++) {
            String columnName;
            if (useColumnLabel)
                columnName = rsmd.getColumnLabel(colCursor);
            else
                columnName = rsmd.getColumnName(colCursor);
            if (caseInsensitiveMap != null && caseInsensitiveMap.get(columnName) != null)
                columnName = caseInsensitiveMap.get(columnName);
            Object obj = resultSet.getObject(colCursor);
            if (outputs != null && !outputs.contains(columnName))
                continue;
            if (obj == null) {
                __return.put(columnName, obj);
                continue;
            }
            
            __return.put(columnName, obj);
        }
        return __return;
    }
    
	public static List<Object> toObjectList(ResultSet resultSet) throws SQLException {
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int count = rsmd.getColumnCount();
		List<Object> result = new ArrayList<Object>();
		for (int colCursor = 1; colCursor <= count; colCursor++) {
			Object obj = resultSet.getObject(colCursor);
			result.add(obj);
		}
		return result;
	}
	
	public static Object[] toObjectArray(ResultSet resultSet) throws SQLException {
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int count = rsmd.getColumnCount();
		Object[] result = new Object[count];
		for (int colCursor = 1; colCursor <= count; colCursor++) {
			Object obj = resultSet.getObject(colCursor);
			result[colCursor-1]=obj;
		}
		return result;
	}

	public static List<Object> toListOfMapsOrBeans(ResultSet rs,SQLDialect dialect,DataServiceInfo dataServiceInfo)throws SQLException, DSCallException {
		return toListOfMapsOrBeans(rs, dialect, -1L, dialect.hasBrokenCursorAPIs(), dataServiceInfo);
	}
	
	public static List<Object> toListOfMapsOrBeans(ResultSet resultSet,SQLDialect dialect,long rowNum,boolean hasBrokenCursorAPIs,DataServiceInfo dataServiceInfo)throws SQLException, DSCallException {
		List<Object> __return = new ArrayList<Object>();
        Map<String, String> _caseInsensitiveMap = new HashMap<String, String>();
        ResultSetMetaData _rsmd;
        boolean _useColumnLabel = false;
        String _beanClassName = null;
  
        if (hasBrokenCursorAPIs) {
            if (!resultSet.next())
                return __return;
        } else {
            boolean isBeforeFirst = false;
            boolean isAfterLast = false;
            try {
                isBeforeFirst = resultSet.isBeforeFirst();
                isAfterLast = resultSet.isAfterLast();
            } catch (SQLException ignored) {
                LOG.debug("isBeforeFirst()/isAfterLast() throwing exceptions .", ignored);
            }
            if ((isBeforeFirst || isAfterLast || resultSet.getRow() == 0) && !resultSet.next())
                return __return;
        }
        _rsmd = resultSet.getMetaData();
        Object bean= dataServiceInfo.getProperty("bean");
        if(bean!=null){
        	_beanClassName=bean.toString();
        }
        _useColumnLabel= dialect.useColumnLabelInMetadata();
        int count = _rsmd.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String fieldName = null;
            String columnName;
            if (_useColumnLabel)
                columnName = _rsmd.getColumnLabel(i);
            else
                columnName = _rsmd.getColumnName(i);
            
            List<FieldInfo> names = dataServiceInfo.getFields();
            for(FieldInfo name:names){
            	if(name.getName().equalsIgnoreCase(columnName)){
            		  fieldName = name.getName();
                      break;
            	}
            }
            if (fieldName != null)
                _caseInsensitiveMap.put(columnName, fieldName);
            else
                _caseInsensitiveMap.put(columnName, columnName);
        }
        
        long i = 0;
        do {
            if (i >= rowNum && rowNum != -1L)
                break;
            Map<String,?> map = toAttributeMap(resultSet,  _rsmd, _useColumnLabel, _caseInsensitiveMap, null);
            if (DataUtils.isNullOrEmpty(_beanClassName))
                __return.add(map);
            else {
                try {
                    Object beanInstannce = Reflection.newInstance(_beanClassName);
                    DataUtils.setProperties(map, beanInstannce);
                    __return.add(beanInstannce);
                } catch (Exception e) {
                    throw new DSCallException("", e);
                }
            }
            /**
             * java.sql.ResultSet.next() move cursor to new row set.
             */
            if (!resultSet.next())
                break;
            i++;

        } while (true);
		return __return;
	}
}
