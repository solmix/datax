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
package org.solmix.datax.jdbc.sql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.jdbc.JdbcExtProperty;
import org.solmix.datax.jdbc.JdbcDataService;
import org.solmix.datax.jdbc.driver.SQLDriver;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.OperationInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class SQLSelectClause
{

    private final List<JdbcDataService> dataSources;

    private Map<String,String> remapTable;

    private Map<String, String> column2TableMap;

    /**
     * Qualify column to specific Table
     */
    private boolean qualifyColumnNames;

    private List<String> customValueFields;

    public List<String> getCustomValueFields() {
        return customValueFields;
    }

    public void setCustomValueFields(List<String> customValueFields) {
        this.customValueFields = customValueFields;
    }

    public SQLSelectClause(JdbcDataService dataSource)
    {
        this(DataUtils.makeList(dataSource));
    }

    public SQLSelectClause(List<JdbcDataService> dataSources)
    {
        column2TableMap = new HashMap<String, String>();
        qualifyColumnNames = true;
        customValueFields = null;
        this.dataSources = dataSources;
        remapTable = JdbcDataService.getField2ColumnMap(dataSources);
        if (dataSources.size() > 1)
            column2TableMap = JdbcDataService.getColumn2TableMap(dataSources);
    }

    public SQLSelectClause(DSRequest request, JdbcDataService ds, boolean qualifyColumnNames)
    {
        this(request, DataUtils.makeList(ds), qualifyColumnNames);
    }

    public SQLSelectClause(DSRequest request, List<JdbcDataService> dataSources, boolean qualifyColumnNames)
    {
        this(dataSources);
        List<String> outputColumns = computeOutputColumns(request);
        if (outputColumns != null)
            remapTable = DataUtils.subsetMap(remapTable, outputColumns);
        this.qualifyColumnNames = qualifyColumnNames;
    }

    protected List<String> computeOutputColumns(DSRequest request) {
        JdbcDataService ds = dataSources.get(0);
        OperationInfo oi = request.getOperationInfo();
        List<String> outputs= ds.getExtensionFields(oi, JdbcExtProperty.OUTPUTS_NODE);
        if (oi != null && outputs != null) {
           return outputs;
        }
        return null;
    }

    public String getSQLString() {
        return getSQLString((dataSources.get(0)).getDriver());
    }

    public String getSQLString(SQLDriver driver) {

        if (remapTable == null || remapTable.size() == 0) {
            return "*";
        }
        StringBuffer __result = new StringBuffer();
        Iterator<String> e = remapTable.keySet().iterator();
        while (e.hasNext()) {
            boolean qualifyColumnNames = this.qualifyColumnNames;
            boolean _skipCusSQLCheck = false;
            String _rsName = e.next();
            String _columnName = remapTable.get(_rsName);
            String _tableName = qualifyColumnNames ? (String) column2TableMap.get(_columnName) : null;
            if (_tableName == null && qualifyColumnNames) {
                DataService firstDS = dataSources.get(0);
                if (firstDS instanceof JdbcDataService)
                    _tableName = ((JdbcDataService) firstDS).getTable().getName();
            }
            if (customValueFields != null) {
                for (String field : customValueFields) {
                    if (field.equalsIgnoreCase(_rsName)) {
                        _skipCusSQLCheck = true;
                        break;
                    }
                }

            } else {
                _skipCusSQLCheck = true;
            }
            FieldInfo field = null;
            boolean _exclude = false;
            for (JdbcDataService ds : dataSources) {
                field = ds.getDataServiceInfo().getField(_rsName);
                if (field != null) {
                    if (!_skipCusSQLCheck)
                        _exclude = true;
                    else if (field.getProperty("tableName") != null) {
                        _tableName =field.getProperty("tableName").toString();
                        qualifyColumnNames = true;
                    }
                    break;
                }
            }// END datasource.
            if (!_exclude) {
                if (__result.length() != 0)
                    __result.append(", ");
                if (field != null && field.getType().value().equals("relatedCount"))
                    __result.append(aggregationSubSelect(driver, _columnName, _rsName, _tableName, field));
                else if (field != null && field.getProperty("customSelectExpression") != null) {
                    __result.append(customSQLExpression(driver, _columnName, _rsName, _tableName, field, qualifyColumnNames));
                } else
                    __result.append(driver.sqlOutTransform(_columnName, _rsName, _tableName));
            }
        }
        return __result.toString();
    }

    private String aggregationSubSelect(SQLDriver driver, String columnName, String rsName, String tableName, FieldInfo field) {
         Object relatedTable = field.getProperty("relatedTable");
         Object relatedColumn = field.getProperty("relatedColumn");
         Object localField = field.getProperty("localField");
         String subselect = " (SELECT ";
         subselect = (new StringBuilder()).append(subselect).append("COUNT").toString();
         subselect = (new StringBuilder()).append(subselect).append("(").toString();
         subselect = (new StringBuilder()).append(subselect).append("*").toString();
         subselect = (new StringBuilder()).append(subselect).append(") FROM ").toString();
         subselect = (new StringBuilder()).append(subselect).append(field.getProperty("relatedTable")).toString();
         subselect = (new StringBuilder()).append(subselect).append(" WHERE ").toString();
         subselect = (new StringBuilder()).append(subselect).append(driver.sqlOutTransform(localField, localField,
         tableName)).toString();
         subselect = (new StringBuilder()).append(subselect).append(" = ").toString();
         subselect = (new StringBuilder()).append(subselect).append(driver.sqlOutTransform(relatedColumn,
         relatedColumn,
         relatedTable)).toString();
         subselect = (new StringBuilder()).append(subselect).append(") AS ").toString();
         subselect = (new StringBuilder()).append(subselect).append(field.getName()).toString();
         return subselect;
    }

    private String customSQLExpression(SQLDriver driver, String columnName, String rsName, String tableName, FieldInfo field, boolean qualifyColumnNames) {
        String custom = field.getProperty("customSelectExpression").toString();
        if (DataUtils.isNullOrEmpty(custom) && custom.substring(0, 1).equals("$")) {
            if (custom.substring(0, custom.indexOf(":")).equalsIgnoreCase("$value"))
                custom = custom.substring(custom.indexOf(":") + 1);
        } else {
            if (qualifyColumnNames)
                custom = tableName + "." + custom;
        }
        custom = custom + " AS " + field.getName();
        return custom;
    }
}
