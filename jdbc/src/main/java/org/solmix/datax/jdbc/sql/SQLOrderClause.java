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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.jdbc.JdbcDataService;
import org.solmix.datax.jdbc.JdbcExtProperty;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.model.FieldInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class SQLOrderClause
{
    private final List<JdbcDataService> dataSources;

    private final Map<String,String> remapTable;

    private Map<String,String> column2TableMap;

    private final Map<String,Object> valueMaps;

    private final List<String> sortBy;

    private final boolean qualifyColumnNames;

    private List<String> customValueFields;

    public SQLOrderClause(DSRequest request, JdbcDataService dataSource, boolean qualifyColumnNames)
    {
       column2TableMap = new HashMap<String,String>();
       customValueFields = null;
       this.dataSources = DataUtils.makeList(dataSource);
       this.qualifyColumnNames = qualifyColumnNames;
       sortBy = new ArrayList<String>();
       List<String> realStorBy = JdbcExtProperty.getExtensionFields(request.getOperationInfo(),JdbcExtProperty.SORT_BY_FIELDS);
       String _field;
       if (realStorBy != null)
       {
          for (Object order : realStorBy)
          {
             _field = (String) order;
             if (_field.startsWith("_"))
                _field = _field.substring(1);
             sortBy.add(_field);
          }
       }
       remapTable = JdbcDataService.getField2ColumnMap(dataSources);
       if (dataSources.size() > 1)
          column2TableMap = JdbcDataService.getColumn2TableMap(dataSources);
       valueMaps = JdbcDataService.getCombinedValueMaps(dataSources, sortBy);
    }

    public List<String> getCustomValueFields()
    {
       return customValueFields;
    }

    public void setCustomValueFields(List<String> fields)
    {
       customValueFields = fields;
    }

    public String getSQLString() 
    {
       if (sortBy == null)
       {
          return "";
       }
       StringBuffer result = new StringBuffer();
       boolean __descending = false;
       SQLDialect driver = dataSources.get(0).getDialect();
       for (String fieldName : sortBy)
       {
          boolean customCheck = false;
          if (fieldName.startsWith("_"))
          {
             fieldName = fieldName.substring(1);
             if (this.customValueFields == null)
                continue;
             for (String field : customValueFields)
             {
                if (fieldName.equals(field))
                   customCheck = true;
             }
             __descending = true;
          }
          FieldInfo field = null;
          String overrideTableName = null;
          boolean exclude = false;
          for (JdbcDataService ds : dataSources)
          {
             field = ds.getDataServiceInfo().getField(fieldName);
             if (field != null)
             {
                if (customCheck /*&& field.isCustomSQL()*/)
                   exclude = true;
                if (field.getProperty("tableName") != null)
                   overrideTableName = field.getProperty("tableName").toString();

                if (!exclude)
                {
                   String columnName = remapTable.get(fieldName);
                   Map valueMap = (Map) valueMaps.get(fieldName);
                   if (columnName == null)
                      columnName = fieldName;
                   String tableName = overrideTableName;
                   if (tableName == null)
                      tableName = column2TableMap.get(fieldName);
                   columnName = driver.escapeColumnName(columnName);
                   if (tableName == null && qualifyColumnNames)
                   {
                      DataService firstDS = dataSources.get(0);
                      if (firstDS instanceof JdbcDataService)
                         tableName = ((JdbcDataService) firstDS).getTable().getName();
                   }
                   String unqualifiedColumnName = columnName;
                   if (!qualifyColumnNames && overrideTableName != null)
                      columnName = (new StringBuilder()).append(overrideTableName).append(".").append(columnName).toString();
                   else if (tableName != null)
                      columnName = (new StringBuilder()).append(tableName).append(".").append(columnName).toString();
                   if (result.length() != 0)
                      result.append(", ");
                   if (field != null && (field.getCustomSelectExpression() != null))
                      result.append(unqualifiedColumnName);
                   else
                      result.append(driver.getExpressionForSortBy(columnName, valueMap));
                   result.append(__descending ? " DESC" : "");
                }
             }
          }
       }
       return result.toString();
    }

    public int size()
    {
       return (sortBy != null) ? 1 : 0;
    }
}
