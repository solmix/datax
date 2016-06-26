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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.jdbc.JdbcDataService;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.OperationType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月5日
 */

public class SQLValuesClause
{

    private final DSRequest dsRequest;

    private final JdbcDataService dataSource;

    private Map values;

    private List<String> returnValues;

    Map field2ColumnMap;

    private boolean batchUpdate;

    public SQLValuesClause(DSRequest dsRequest, Map<String, Object> values, JdbcDataService dataSource)
    {
        field2ColumnMap = null;
        this.dsRequest = dsRequest;
        this.dataSource = dataSource;
        field2ColumnMap = dataSource.getExpandedDs2NativeFieldMap();
        // if (Assert.isNotNullAndEmpty(constraints) )
        // values = DataUtils.subsetMap(values, constraints);
        this.values = filterValid(values);
    }

    public SQLValuesClause(DSRequest req, JdbcDataService jdbcDataService)
    {
      this(req,req.getValues(),jdbcDataService);
    }

    private Map filterValid(Map data) {
        if (data == null)
            return null;
        Map __return = new HashMap();
        List _valid = dataSource.getDataServiceInfo().getFields();
        List _invalid = new ArrayList();
        for (Object o : data.keySet()) {
            String _name = (String) o;
            if (!_valid.contains(_name))
                _invalid.add(_name);
            else
                __return.put(_name, data.get(_name));
        }
        return __return;
    }

    public String getSQLStringForInsert() {
        if (size() == 0) {
            return "";
        }
        Map<String, String> _temp = new LinkedHashMap<String, String>();
        // StringBuffer __return = new StringBuffer();
        StringBuffer _columnList = new StringBuffer();
        StringBuffer _valueList = new StringBuffer();
        Map sequences = dataSource.getSequences();
        SQLDialect driver = dataSource.getDialect();
        // values = DataUtils.divideMap(values, new ArrayList(sequences.keySet()));
        if (values == null)
            values = new HashMap();
        for (Object key : values.keySet()) {
            String __fieldName = (String) key;
            FieldInfo __f = dataSource.getDataServiceInfo().getField(__fieldName);

            String nativeFieldName = (String) field2ColumnMap.get(__fieldName);
            String __v;
            if (!driver.fieldAssignableInline(__f))
                __v = "?";
            else if (this.batchUpdate) {
                __v = "?";
            } else {
                __v = dataSource.sqlValueForFieldValue(__fieldName, values.get(__fieldName));
            }
            _temp.put(dataSource.escapeColumnName(nativeFieldName), __v);
            addToReturnValue(__fieldName);
        }
        // sequence
        Iterator it = sequences.keySet().iterator();
        do {
            if (!it.hasNext())
                break;
            String fieldName = (String) it.next();
            String columnName = (String) field2ColumnMap.get(fieldName);
            boolean omitValue = false;
            if (values.containsKey(fieldName) && values.get(fieldName) != null) {
                _temp.put(dataSource.escapeColumnName(columnName), dataSource.sqlValueForFieldValue(fieldName, values.get(fieldName)));
            } else if (!omitValue) {
                _temp.put(dataSource.escapeColumnName(columnName), dataSource.getNextSequenceValue(columnName));
            }
        } while (true);
        for (FieldInfo __f : dataSource.getDataServiceInfo().getFields()) {
            Object custom=__f.getProperty("customInsertExpression");
            if (custom != null) {

                String nativeFieldName = (String) field2ColumnMap.get(__f.getName());
                _temp.put(dataSource.escapeColumnName(nativeFieldName), getCustomInsertExpression(__f,custom.toString()));
            }
        }
        if (_temp.isEmpty())
            return null;
        else {
            int i = 0;
            for (String key : _temp.keySet()) {
                i++;
                _columnList.append(key);
                _valueList.append(_temp.get(key));
                if (i < _temp.keySet().size()) {
                    _columnList.append(',');
                    _valueList.append(',');
                }

            }

        }
        return (new StringBuilder()).append("(").append(_columnList.toString()).append(") VALUES (").append(_valueList.toString()).append(")").toString();
    }

    /**
     * @return the returnValues @
     */
    public List<String> getReturnValues() {
        if (this.returnValues == null) {
            OperationType type = this.dsRequest.getOperationInfo().getType();
            if (type == OperationType.UPDATE) {
                getSQLStringForUpdate();
            } else if (type == OperationType.ADD) {
                getSQLStringForInsert();
            }
        }
        return returnValues;
    }

    private void addToReturnValue(String value) {
        if (this.returnValues == null)
            returnValues = new ArrayList<String>();
        returnValues.add(value);
    }

    private String getCustomInsertExpression(FieldInfo field,String custom) {
        return custom;
//        return Velocity.evaluateAsString( custom, Velocity.getStandardContextMap(dsRequest));
    }

    public boolean isEmpty() {
        return values == null;
    }

    public int size() {
        return !isEmpty() ? 1 : 0;
    }

    /**
     * @return
     */
    public String getSQLStringForUpdate() {
        if (size() == 0) {
            return "";
        }
        // Map sequences = dataSource.getTable().getSequences();
        // SQLDriver driver = dataSource.getDriver();
        List primaryKeys = dataSource.getTable().getPrimaryKeys();
        if (primaryKeys != null) {
            Map newValues = DataUtils.divideMap(values, primaryKeys);
            if (newValues != null)
                values = newValues;
        }
        StringBuffer __result = new StringBuffer();
        for (Object key : values.keySet()) {
            String fieldName = (String) key;
            String columnName = (String) field2ColumnMap.get(fieldName);
            if (__result.length() != 0)
                __result.append(", ");
            __result.append(new StringBuilder().append(dataSource.escapeColumnName(columnName)).append("=").toString());
            Object columnValue = values.get(fieldName);
            __result.append(dataSource.sqlValueForFieldValue(fieldName, columnValue));
        }
        if (__result.toString() == null || __result.toString().isEmpty())
            return null;
        else
            return __result.toString();
    }
}
