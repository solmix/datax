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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月6日
 */

public class HSQLDialect extends SQLDialect
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#sqlOutTransform(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String sqlOutTransform(String columnName, String remapName, String tableName) {
        String output = escapeColumnName(columnName);
        if (remapName != null && !remapName.equals(columnName))
            output = (new StringBuilder()).append(output).append(" AS ").append(escapeColumnName(remapName)).toString();
        if (tableName != null)
            output = (new StringBuilder()).append(tableName).append(".").append(output).toString();
        return output;
    }
    
    @Override
    protected String getExpressionForSortBy(String column, Map<String, String> valueMap) {
        if (valueMap == null || valueMap.size() == 0)
            return column;
        else
            return caseExpression(column, valueMap.entrySet().iterator());
    }
    
    private String caseExpression(String column, Iterator<Entry<String, String>> entries) {
        if (!entries.hasNext()) {
            return column;
        } else {
            Entry<String, String> entry = entries.next();
            String actualValue = entry.getKey();
            String displayValue = entry.getValue();
            return (new StringBuilder()).append("CASEWHEN(").append(column).append(
                "='").append(actualValue).append("', '").append(displayValue).append(
                "', ").append(caseExpression(column, entries)).append(")").toString();
        }
    }

    @Override
    public String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns, String orderClause) {
         throw new UnsupportedOperationException();
    }

    @Override
    public String limitQuery(String query, long startRow, long batchSize, List<String> outputColumns) {
        return (new StringBuilder()).append("SELECT LIMIT ").append(startRow).append(
            " ").append(batchSize).append(" ").append(
            query.substring("SELECT".length())).toString();
    }
    @Override
    public String escapeValueUnquoted(Object value, boolean escapeForFilter) {
        if (value == null)
            return null;
        String escaped =value.toString().replace("'", "''") ;
        if (escapeForFilter) {
            escaped =escaped.replace("%", "\\%") ;
            escaped =escaped.replace("_", "\\_%") ;
        }
        return escaped;
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
            return (new StringBuilder()).append("'").append(
                escapeValueUnquoted(value.toString(), false)).append("'").toString();
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
}
