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

import java.util.List;
import java.util.Map;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月6日
 */

public class SqlServerDialect extends SQLDialect
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#sqlOutTransform(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String sqlOutTransform(String columnName, String remapName, String tableName) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#getExpressionForSortBy(java.lang.String, java.util.Map)
     */
    @Override
    protected String getExpressionForSortBy(String column, Map<String, String> valueMap) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#limitQuery(java.lang.String, long, long, java.util.List, java.lang.String)
     */
    @Override
    public String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns, String orderClause) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#limitQuery(java.lang.String, long, long, java.util.List)
     */
    @Override
    public String limitQuery(String query, long startRow, long totalRows, List<String> outputColumns) {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.dialect.SQLDialect#escapeValue(java.lang.Object)
     */
    @Override
    public String escapeValue(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String formatValue(Object value) {
        return value.toString();
    }
}
