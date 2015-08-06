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
package org.solmix.datax.jdbc.driver;

import java.sql.Connection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.datax.jdbc.JdbcDataService;
import org.solmix.datax.jdbc.sql.SQLTable;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public abstract class SQLDriver
{
    protected static Logger log = LoggerFactory.getLogger(SQLDriver.class.getName());


    protected boolean quoteColumnNames;

    protected SQLTable table;

    protected String dbName;

    protected String dbType;

    protected Connection connection;

//    private ConnectionManager connectionManager;

    private JdbcDataService jds;

    private boolean useColumnLabelInMetadata;

    public String getQualifiedSchemaSeparator() {
        return ".";
    }

    public Object sqlOutTransform(String _columnName, String _rsName, String _tableName) {
        return null;
    }

    public Object sqlOutTransform(Object relatedColumn, Object relatedColumn2, Object relatedTable) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param columnName
     * @return
     */
    public String escapeColumnName(String columnName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param columnName
     * @param valueMap
     * @return
     */
    public Object getExpressionForSortBy(String columnName, Map valueMap) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param __f
     * @return
     */
    public boolean fieldAssignableInline(FieldInfo __f) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * @param value
     * @param field
     * @return
     */
    public Object sqlInTransform(Object value, FieldInfo field) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public Object escapeClause() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param lowerCase
     * @param filterStyle
     * @return
     */
    public Object escapeValueForFilter(String lowerCase, String filterStyle) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param value
     * @param b
     * @return
     */
    public String escapeValueUnquoted(String value, boolean b) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public int getMaximumSetSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}
