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

package org.solmix.datax.jdbc.dialect;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.Reflection;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.util.DataTools;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月5日
 */
public abstract class SQLDialect
{
    
    public static final Map<String,Class<? extends SQLDialect>> buildIn = new HashMap<String, Class<? extends SQLDialect>>();
    static{
        buildIn.put("h2", H2DBDialect.class);
        buildIn.put("hsqldb", HSQLDialect.class);
        buildIn.put("oracle", OracleDialect.class);
        buildIn.put("sqlserver", SqlServerDialect.class);
        buildIn.put("postgresql", PostgresDialect.class);
        buildIn.put("mysql", MysqlDialect.class);
    }
    public static SQLDialect instance(String dbName) throws Exception{
        Assert.assertNotNull(dbName);
        String dbType=null;
        for(String key:buildIn.keySet()){
            if(dbName.toLowerCase().indexOf(key)!=-1){
                dbType=key;
                break;
            }
        }
        Class<? extends SQLDialect> clz =buildIn.get(dbType);
        SQLDialect dialect=  Reflection.newInstance(clz);
        return dialect;
    }
   
    public Object escapeClause() {
        return "";
    }

    public String escapeColumnName(String columnName) {
        return escapeColumnName(columnName, false);
    }

    public String getRowCountQueryString(String customSql) {
        return new StringBuilder().append("SELECT COUNT(*) FROM (").append(
            customSql).append(") aliasForPage").toString();
    }
    
    public String getRowCountQueryString(String selectClause,
            String tableClause, String whereClause, String groupClause,
            String groupWhereClause, Map<String,Object> context) {
            String __return = "SELECT COUNT(*) FROM ";
            if (!groupClause.equals("$defaultGroupClause"))
                __return = (new StringBuilder()).append(__return).append("(SELECT ").append(
                    selectClause).append(" FROM ").toString();
            __return = (new StringBuilder()).append(__return).append(tableClause).toString();
            if (!whereClause.equals("$defaultWhereClause")
                || context.get("defaultWhereClause") != null)
                __return = (new StringBuilder()).append(__return).append(" WHERE ").append(
                    whereClause).toString();
            if (!groupClause.equals("$defaultGroupClause")) {
                __return = (new StringBuilder()).append(__return).append(
                    " GROUP BY ").append(groupClause).append(") work").toString();
                if (!groupWhereClause.equals("$defaultGroupWhereClause"))
                    __return = (new StringBuilder()).append(__return).append(
                        " WHERE ").append(groupWhereClause).toString();
            }
            return __return;
        }
    
    public String escapeColumnName(String columnName, boolean forceQuoteColumn) {
        if (columnName == null)
            return null;
        if (forceQuoteColumn) {
            return (new StringBuilder()).append(openQuote()).append(columnName.replace("\"", "\"\"")).append(closeQuote()).toString();
        } else
            return columnName.toString();
    }

    public String openQuote() {
        return "\"";
    }

    public String closeQuote() {
        return "\"";
    }
    
    public String getQualifiedSchemaSeparator() {
        return ".";
    }
    public String sqlInTransform(Object columnValue, FieldInfo field) {
        if (columnValue instanceof Date) {
            if (field != null) {
                Object strategy = field.getProperty("sqlStorageStrategy");
                if ("number".equals(strategy) || "text".equals(strategy)) {
                    String sqlFormat = field.getDateFormat();
                    if (sqlFormat == null)
                        sqlFormat = "yyyyMMdd";
                    SimpleDateFormat sdf = new SimpleDateFormat(sqlFormat);
                    String formatted = sdf.format(columnValue);
                    if ("text".equals(strategy))
                        formatted = (new StringBuilder()).append("'").append(
                            formatted).append("'").toString();
                    return formatted;
                } else if (DataTools.typeIsBoolean(columnValue.toString())) {
                    return columnValue.equals(Boolean.TRUE) ? "'Y'" : "'N'";
                }
            }
            long timeStamp = ((Date) columnValue).getTime();
            return escapeValue((new Timestamp(timeStamp)).toString());
        } else
            return escapeValue(columnValue);
    }
    
    public boolean limitRequiresSQLOrderClause() {
        return false;
    }
    public boolean fieldAssignableInline(FieldInfo field) {
        String fieldType = field.getType().value();
        return (!DataTools.isBinary(field)) && !"blob".equals(fieldType)
            && !"clob".equals(fieldType);
    }
    public abstract String escapeValue(Object obj);

    public abstract String escapeValueForFilter(Object value, String filterStyle);

    public abstract String escapeValueUnquoted(Object value, boolean b);
    
    public abstract String formatValue(Object obj);
    
    public abstract String sqlOutTransform(String columnName, String remapName, String tableName);
    
    public abstract String getExpressionForSortBy(String column, Map<String, String> valueMap);

    public abstract String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns, String orderClause);

    public abstract String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns);

    /**
     * @return
     */
    public int getMaximumSetSize() {
        return 0;
    }

	public boolean useColumnLabelInMetadata() {
		return false;
	}

	/**resutset.next()为空时不再读取*/
	public boolean hasBrokenCursorAPIs() {
		return false;
	}
	
	 /**
     * support special database,if sub driver used this method would override
     * this.
     * 
     * @return
     */
	public boolean supportsSQLLimit() {
		return false;
	}
	
	public boolean supportsAsyncCommit(){
		return false;
	}
	public boolean supportsMultiInsertStmt(){
		return false;
	}
	public String getSetAsyncCommitStmt(boolean on){
		return null;
	}
	public boolean supportsDuplicateInsertStmt(){
		return false;
	}
	
	public boolean supportsPLSQL(){
		return false;
	}
	 /**
	     * Returns the table optimize statement for
	     * a particular database
	     *
	     * @param table - name of table
	     * @param cost - based on the database,
	     *               may be table percentage or random number
	     */
	    public abstract String getOptimizeStmt(String table, int cost);
}
