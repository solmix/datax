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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.util.DataTools;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月6日
 */

public class OracleDialect extends SQLDialect
{

   
    @Override
    public String sqlOutTransform(String columnName, String remapName, String tableName) {

        String output = escapeColumnName(columnName);
        if (remapName != null && !columnName.equals(remapName))
            output = (new StringBuilder()).append(output).append(" AS ").append(escapeColumnName(remapName)).toString();
        if (tableName != null)
            output = (new StringBuilder()).append(tableName).append(".").append(output).toString();
        return output;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#getExpressionForSortBy(java.lang.String, java.util.Map)
     */
    @Override
    protected String getExpressionForSortBy(String column, Map<String, String> valueMap) {
        if (valueMap == null || valueMap.size() == 0)
            return column;
        String expr = (new StringBuilder()).append("DECODE(").append(column).toString();
        for (Iterator<String> e = valueMap.keySet().iterator(); e.hasNext();) {
            String actualValue = e.next();
            String displayValue = valueMap.get(actualValue);
            expr = (new StringBuilder()).append(expr).append(", '").append(actualValue).append("', '").append(displayValue).append("'").toString();
        }

        expr = (new StringBuilder()).append(expr).append(", ").append(column).append(")").toString();
        return expr;
    }

    @Override
    public String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns, String orderClause) {
         throw new UnsupportedOperationException();
    }

    @Override
    public String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns) {
        StringBuilder out = new StringBuilder();
        if (DataUtils.isNotNullAndEmpty(outputColumns)) {
            for (int i = 0; i < outputColumns.size(); i++) {
                  out.append( outputColumns.get(i));
                if (i < outputColumns.size() - 1)
                  out.append(", ");
            }

        }
        query = (new StringBuilder()).append("SELECT ").append(out.length()==0 ? "*" : out.toString()).append(" FROM (SELECT /*+ FIRST_ROWS(").append(totalRows).append(
            ") */ a.*, rownum myrownum FROM ").append("(").append(query).append(") a where rownum <=").append(startRow + totalRows).append(")").append(
            " WHERE myrownum > ").append(startRow).toString();
        return query;
    }
    
    @Override
    public String sqlInTransform(Object value, FieldInfo field) {
        if (field != null && (field.getType() == FieldType.DATE || field.getType() == FieldType.DATETIME)) {
            String dateTime = null;
            String format = field.getDateFormat();
            if (value instanceof Date) {
                long timeStamp = ((Date) value).getTime();
                dateTime = (new Timestamp(timeStamp)).toString();
                int periodIndex;
                if ((periodIndex = dateTime.lastIndexOf(".")) != -1)
                    dateTime = dateTime.substring(0, periodIndex);
            } else {
                dateTime = value.toString();
            }
            return (new StringBuilder()).append("TO_DATE(").append(escapeValue(dateTime)).append(",'").append(format).append("')").toString();
        } else if (value instanceof Boolean || DataTools.typeIsBoolean(value.toString())) {
            return value.equals(Boolean.TRUE) || value.equals("true") ? "'1'" : "'0'";
        } else {
            return super.sqlInTransform(value, field);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#escapeValue(java.lang.Object)
     */
    @Override
    public String escapeValue(Object value) {
        if (value == null)
            return null;
        else
            return (new StringBuilder()).append("'").append(escapeValueUnquoted(value.toString(), false)).append("'").toString();
    }

    @Override
    public String escapeValueForFilter(Object value, String filterStyle) {
        if (value == null)
            return null;
        String rtn = "'";
        if (!"startsWith".equals(filterStyle))
            rtn = (new StringBuilder()).append(rtn).append("%").toString();
        return (new StringBuilder()).append(rtn).append(
            escapeValueUnquoted(value, true)).append("%'").toString();
    }


    @Override
    public String formatValue(Object value) {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#escapeValueUnquoted(java.lang.Object, boolean)
     */
    @Override
    protected String escapeValueUnquoted(Object value, boolean escapeForFilter) {
        if (value == null)
            return null;
        String escaped =value.toString().replace("'", "''") ;
        if (escapeForFilter) {
            escaped =escaped.replace("%", "\\%") ;
            escaped =escaped.replace("_", "\\_%") ;
        }
        return escaped;
    }
}
