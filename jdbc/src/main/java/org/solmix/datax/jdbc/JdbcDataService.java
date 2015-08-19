/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.datax.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallCompleteCallback;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.jdbc.driver.SQLDriver;
import org.solmix.datax.jdbc.mode.JdbcDataServiceInfo;
import org.solmix.datax.jdbc.sql.SQLGenerationException;
import org.solmix.datax.jdbc.sql.SQLOrderClause;
import org.solmix.datax.jdbc.sql.SQLSelectClause;
import org.solmix.datax.jdbc.sql.SQLTable;
import org.solmix.datax.jdbc.sql.SQLTableClause;
import org.solmix.datax.jdbc.sql.SQLValuesClause;
import org.solmix.datax.jdbc.sql.SQLWhereClause;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.util.DataTools;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class JdbcDataService extends BaseDataService implements DSCallCompleteCallback
{
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataService.class.getName());

    protected volatile SQLTable table;
    
    protected volatile SQLDriver driver;
    
    
    public JdbcDataService(DataServiceInfo info, Container container, DataTypeMap prop)
    {
        super(info, container, prop);
    }
    
    @Override
    public JdbcDataServiceInfo getDataServiceInfo(){
        return null;
    }
    
    @Override
    protected DSResponse executeDefault(DSRequest req,OperationType type)throws DSCallException {
        if(LOG.isDebugEnabled()){
            StringBuilder info = new StringBuilder();
            info.append("Performing ")
            .append(type)
            .append(" operation with \n")
            .append(" values:").append(req.getRawValues());
            LOG.debug(info.toString());
        }
        OperationInfo oi=  req.getOperationInfo();
      
        Object tmp=oi.getProperty(JdbcExtProperty.QUALIFY_COLUMN_NAMES);
        boolean qualifyColumnNames=DataUtils.asBoolean(tmp);
        /*****************************************************************************
         * Prepare for generate sql statement.
         ******************************************************************************/
        Map<String, Object> context = getClausesContext(req, qualifyColumnNames,oi);
        
        return null;
    }

    private Map<String, Object> getClausesContext(DSRequest req, boolean qualifyColumnNames, OperationInfo oi) {
        Map<String, Object> context = new HashMap<String, Object>();
        
        OperationType type =oi.getType();
        List<String> relateTables = null;
        List<String> relateCriterias = null;
        
        List<FieldInfo> fields= getDataServiceInfo().getFields();
        Object selfTableName = getTable().getName();
        for(FieldInfo field:fields){
            String foreign = field.getForeignKey();
            if (foreign != null) {
                if (relateTables == null)
                    relateTables = new ArrayList<String>();
                relateTables.add(foreign.substring(0, foreign.indexOf(".")));
                if (relateCriterias == null)
                    relateCriterias = new ArrayList<String>();
                Object fieldName = null;
                switch (type) {
                    case FETCH:
                        fieldName = field.getProperty("customSelectExpression");
                        break;
                    case UPDATE:
                        fieldName = field.getProperty("customUpdateExpression");
                        break;
                    default:
                        break;
                }
                if (fieldName == null)
                    fieldName = field.getName();
                if (field.getProperty("tableName") != null)
                    selfTableName = field.getProperty("tableName");
                relateCriterias.add(new StringBuilder().append(selfTableName).append(".").append(fieldName).append(" = ").append(foreign).toString());
            }
        }
        
        SQLTableClause tableClause = new SQLTableClause(this);
        tableClause.setRelatedTables(relateTables);
        context.put("defaultTableClause", tableClause.getSQLString());
        
        if (type == OperationType.FETCH || type == OperationType.CUSTOM) {
            SQLSelectClause selectClause = new SQLSelectClause(req, this, qualifyColumnNames);
            List<String> customValue=getExtensionFields(oi,JdbcExtProperty.CUSTOM_VALUE_FIELDS_NODE);
            selectClause.setCustomValueFields(customValue);
            context.put("defaultSelectClause", selectClause.getSQLString());
            SQLOrderClause orderClause = new SQLOrderClause(req, this, qualifyColumnNames);
            orderClause.setCustomValueFields(customValue);
            if (orderClause.size() > 0)
                context.put("defaultOrderClause", orderClause.getSQLString());
        }
        
        if (DataTools.isAdd(type) || DataTools.isUpdate(type)) {
            SQLValuesClause valuesClause = new SQLValuesClause(req,this);
            if (valuesClause.size() > 0)
                if (DataTools.isUpdate(type)) {
                    context.put("defaultValuesClause", valuesClause.getSQLStringForUpdate());
                } else {
                    context.put("defaultValuesClause", valuesClause.getSQLStringForInsert());
                }
            context.put("batchUpdateReturnValue", valuesClause.getReturnValues());
        }
        
        if (!DataTools.isAdd(type)) {
            String textMatchStyle = null;
            if (req.getAttribute("textMatchStyle")!=null)
                textMatchStyle = req.getAttribute("textMatchStyle").toString();
            SQLWhereClause whereClause = new SQLWhereClause(qualifyColumnNames, req, this, false, textMatchStyle);
            whereClause.setCustomCriteriaFields(getExtensionFields(oi, JdbcExtProperty.CUSTOM_CRITERIA_FIELDS_NODE));
            whereClause.setExcludeCriteriaFields(getExtensionFields(oi, JdbcExtProperty.EXCLUDE_CRITERIA_FIELDS_NODE));
            whereClause.setRelatedCriterias(relateCriterias);
            if (DataTools.isRemove(type) && whereClause.isEmpty())
                throw new SQLGenerationException("empty where clause on delete operation - would  destroy table - ignoring.");
            context.put("defaultWhereClause", whereClause.getSQLString());
        }
        return context;
    }
  
    public List<String> getExtensionFields(OperationInfo oi,String extension) {
        return JdbcExtProperty.getExtensionFields(oi, extension);
    }

    public SQLTable getTable() {
        return table;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.call.DSCallCompleteCallback#onSuccess(org.solmix.datax.call.DSCall)
     */
    @Override
    public void onSuccess(DSCall call) {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.call.DSCallCompleteCallback#onFailure(org.solmix.datax.call.DSCall, boolean)
     */
    @Override
    public void onFailure(DSCall call, boolean transactionFailure) throws DSCallException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return
     */
    public SQLDriver getDriver() {
        return driver;
    }

    /**
     * @param dataSources
     * @return
     */
    public static Map<String, String> getField2ColumnMap(List<JdbcDataService> dataSources) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param dataSources
     * @return
     */
    public static Map<String, String> getColumn2TableMap(List<JdbcDataService> dataSources) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param dataSources
     * @param sortBy
     * @return
     */
    public static Map<String, Object> getCombinedValueMaps(List<JdbcDataService> dataSources, List<String> sortBy) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public Map getExpandedDs2NativeFieldMap() {
        // TODO Auto-generated method stub
        return null;
    }


    public Map getSequences() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param nativeFieldName
     * @return
     */
    public String escapeColumnName(String nativeFieldName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param columnName
     * @return
     */
    public String getNextSequenceValue(String columnName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param __fieldName
     * @param object
     * @return
     */
    public String sqlValueForFieldValue(String __fieldName, Object object) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param dataSources
     * @param primaryKeysOnly
     * @return
     */
    public static Map getColumn2TableMap(List dataSources, boolean primaryKeysOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param dataSources
     * @param primaryKeysOnly
     * @return
     */
    public static Map getField2ColumnMap(List dataSources, boolean primaryKeysOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public SQLDialect getDialect() {
        // TODO Auto-generated method stub
        return null;
    }

}
