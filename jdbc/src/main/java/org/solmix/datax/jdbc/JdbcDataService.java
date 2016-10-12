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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.pager.PageControl;
import org.solmix.commons.timer.StopWatch;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.attachment.SortBy;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallCompleteCallback;
import org.solmix.datax.jdbc.dialect.OracleDialect;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.jdbc.helper.JdbcHelper;
import org.solmix.datax.jdbc.sql.SQLGenerationException;
import org.solmix.datax.jdbc.sql.SQLOrderClause;
import org.solmix.datax.jdbc.sql.SQLSelectClause;
import org.solmix.datax.jdbc.sql.SQLTable;
import org.solmix.datax.jdbc.sql.SQLTableClause;
import org.solmix.datax.jdbc.sql.SQLValuesClause;
import org.solmix.datax.jdbc.sql.SQLWhereClause;
import org.solmix.datax.jdbc.support.ConnectionTransaction;
import org.solmix.datax.jdbc.support.ConnectionWrapperedTransaction;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.router.DataServiceRouter;
import org.solmix.datax.router.RequestToken;
import org.solmix.datax.script.VelocityExpression;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.support.DSResponseImpl;
import org.solmix.datax.util.DataTools;
import org.solmix.runtime.Container;
import org.solmix.runtime.transaction.Transaction;
import org.solmix.runtime.transaction.TransactionService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class JdbcDataService extends BaseDataService implements DSCallCompleteCallback
{
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataService.class.getName());

    protected volatile SQLTable table;
    
    protected volatile SQLDialect dialect;
    
    private DataSourceService dataSourceService;
    
    private DataSource dataSource;
    
    private DataServiceRouter<RequestToken> dataServiceRouter;
    
    public JdbcDataService(DataServiceInfo info, Container container, DataTypeMap prop)
    {
        super(info, container, prop);
    }
    
    @Override
    protected DSResponse executeDefault(DSRequest req,OperationType type)throws DSCallException {
    	DSResponse response;
    	if(isJdbcOperation(type)){
    		response=executeJdbc(req,type);
    	}else{
    		response = super.executeDefault(req, type);
    	}
    	return response;
       
    }

    private DSResponse executeJdbc(DSRequest req, OperationType type) throws DSCallException {
    	 if (isPartitionEnable()) {
             return executePartition(req,type);
         } else {
             DataSource dataSource = getDataSource();
            return executeWithDataSource(dataSource,req,type);
         }
	}


	private DSResponse executePartition(DSRequest req, OperationType type) {
		// TODO Auto-generated method stub
		return null;
	}

	private DSResponse executeWithDataSource(DataSource dataSource,DSRequest req, OperationType type)throws DSCallException
	{
		OperationInfo oi=req.getOperationInfo();
		
		/**生成sql语句的时候字段是否带表名*/
		Object tmp=oi.getProperty(JdbcExtProperty.QUALIFY_COLUMN_NAMES);
		 if (tmp == null) {
			 tmp = autoQualifyColumnNames(req.getDataService().getDataServiceInfo());
	        }
        boolean qualifyColumnNames=DataUtils.asBoolean(tmp);
		List<String> _customCriteriaFields = null,
					 _customValueFields = null,
					 _excludeCriteriaFields = null;
		/**自定义查询字段*/
		String customcf  = (String)oi.getProperty(JdbcExtProperty.CUSTOM_CRITERIA_FIELDS_NODE);
		if(customcf!=null){
			_customCriteriaFields = Arrays.asList(StringUtils.split(customcf, ","));
		}
		
		/**自定义值字段*/
		customcf  = (String)oi.getProperty(JdbcExtProperty.CUSTOM_VALUE_FIELDS_NODE);
		if(customcf!=null){
			_customValueFields = Arrays.asList(StringUtils.split(customcf, ","));
		}
		
		/**自定义排除字段*/
		customcf  = (String)oi.getProperty(JdbcExtProperty.EXCLUDE_CRITERIA_FIELDS_NODE);
		if(customcf!=null){
			_excludeCriteriaFields = Arrays.asList(StringUtils.split(customcf, ","));
		}
		
		/**自定义字段*/
		if(_customCriteriaFields==null){
			customcf  = (String)oi.getProperty(JdbcExtProperty.CUSTOM_FIELDS_NODE);
			if(customcf!=null){
				_customCriteriaFields = Arrays.asList(StringUtils.split(customcf, ","));
			}
		}
		
        Map<String, Object> context = getClausesContext(req, 
        		qualifyColumnNames,
        		oi,
        		_customCriteriaFields,
        		_customValueFields,
        		_excludeCriteriaFields);
        
        String sql  = oi.getExtension(JdbcExtProperty.SQL_NODE);
        //如果自定义了语句，就使用自定义的
        boolean usedCustomSQL=StringUtils.isEmpty(sql);
        if(DataTools.isModificationOperation(type)
        		&&usedCustomSQL
        		&&context.get(JdbcExtProperty.DEFAULT_VALUES_CLAUSE)==null){
        	String __info;
            if (req.getRawValues() == null)
                __info = "Insert, update or replace operation requires non-empty values; check submitted values parameter";
            else
                __info = "Auto generate  Insert, update or replace sql  requires non-empty  ValuesClause; check submitted values in DataSource fields";
            LOG.warn(__info);
            throw new DSCallException(__info);
        }
        VelocityExpression ve = new VelocityExpression(getContainer());
        Map<String, Object> internal =ve.prepareContext(req, req.getRequestContext());
        if(internal!=null){
        	context.putAll(internal);
        }
        String statement = generateSQLStatement(req,oi,ve, context,sql);
        
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        //查询
        if(DataTools.isFetch(oi.getType())){
        	boolean __canPage = DataTools.isPaged(req);
        	//FIXME 可能需要更多的条件控制是否分页
        	if(__canPage){
        		StopWatch timer = new StopWatch();
        		res = executeWindowedSelect(req, ve, context, statement,usedCustomSQL);
        		LOG.debug("SQL QueryTime:{}",timer);
        	}else{
        		executeQuery(req,statement, res);
        	}
        }else{
        	
        }
		return res;
	}
	
	private void executeQuery(DSRequest req,String statement, DSResponse res ) throws DSCallException {
		 pagedQuerySelect(req, statement, null,res);
	}

	private DSResponse executeWindowedSelect(DSRequest req,VelocityExpression ve,
			Map<String, Object> context, String query, boolean usedCustomSQL) throws DSCallException {
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        PageControl page= req.getAttachment(PageControl.class);
		if (usedCustomSQL) {
			String preparedCountQuery = dialect.getRowCountQueryString(query);
			LOG.debug("Executing row count query", preparedCountQuery);
	        String countQuery = ve.evaluateAsString(preparedCountQuery, context);
	        LOG.debug("After Velocity query String", countQuery);
	        StopWatch timer = new StopWatch();
	        Object objCount = executeScalar(countQuery, req);
	        Integer count = new Integer(objCount == null ? "0" : objCount.toString());
	        LOG.debug("SQL window query,Query total rows: {},used :{}",count, timer);
	        if(count==0){
	        	res.addAttachment(PageControl.class, new PageControl(0, 0));
	        	res.setRawData(Collections.emptyList());
	        	return res;
	        }
	        query = dialect.limitQuery(query, page.getPageFirstIndex(), page.getPageSize(), null);
	        pagedQuerySelect(req, query, page,res);
		}else{
			OperationInfo oi=req.getOperationInfo();
			 String selectClause  =getQueryClause(oi,JdbcExtProperty.SELECT_CLAUSE_NODE,JdbcExtProperty.DEFAULT_SELECT_CLAUSE);
			 String valuesClause  =getQueryClause(oi,JdbcExtProperty.VALUES_CLAUSE_NODE,JdbcExtProperty.DEFAULT_VALUES_CLAUSE);
			 String tableClause  =getQueryClause(oi,JdbcExtProperty.TABLE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_TABLE_CLAUSE);
			 String whereClause  =getQueryClause(oi,JdbcExtProperty.WHERE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_WHERE_CLAUSE);
			 String orderClause  =getQueryClause(oi,JdbcExtProperty.ORDER_CLAUSE_NODE,JdbcExtProperty.DEFAULT_ORDER_CLAUSE);
			 String groupClause  =getQueryClause(oi,JdbcExtProperty.GROUP_CLAUSE_NODE,JdbcExtProperty.DEFAULT_GROUP_CLAUSE);
			 String groupWhereClause  =getQueryClause(oi,JdbcExtProperty.GROUPWHERE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_GROUPWHERE_CLAUSE);
			 //查总数
			 String preparedCountQuery = dialect.getRowCountQueryString(selectClause, tableClause, whereClause, groupClause, groupWhereClause, context);
	         LOG.debug("Executing row count query{}", preparedCountQuery);
	         String countQuery = ve.evaluateAsString(preparedCountQuery, context);
	         Object objCount = executeScalar(countQuery, req);
	         Integer count = new Integer(objCount == null ? "0" : objCount.toString());
	         if(count==0){
		        	res.addAttachment(PageControl.class, new PageControl(0, 0));
		        	res.setRawData(Collections.emptyList());
		        	return res;
		        }
	         if(dialect.supportsSQLLimit()){
	        	List<String> outputs= JdbcExtProperty.getExtensionFields(oi, "outputs");
	        	 if(dialect.limitRequiresSQLOrderClause()){
	        		  if (orderClause == null || orderClause.equals("")) {
	                        List<String> pkList = JdbcExtProperty.getPrimaryKeys(getDataServiceInfo());
	                        if (dialect instanceof OracleDialect)
	                            orderClause = "rownum";
	                        else if (!pkList.isEmpty()) {
	                            orderClause = pkList.get(0);
	                            LOG.debug((new StringBuilder()).append("Using PK as default sorter: ").append(orderClause).toString());
	                        } else {
	                            Iterator<String> i = outputs.iterator();
	                            if (i.hasNext())
	                                orderClause = i.next();
	                            LOG.debug((new StringBuilder()).append("Using first field as default sorter: ").append(orderClause).toString());
	                        }
	                    }
		        	 query = dialect.limitQuery(countQuery, page.getPageFirstIndex(), page.getPageSize(), outputs,orderClause);

		         }else{
		        	 query = dialect.limitQuery(countQuery, page.getPageFirstIndex(), page.getPageSize(), outputs);
		         }
	         }
		        pagedQuerySelect(req, query, page,res);

		}
		return res;
	}
	private List<Object> querySelect(String query, DSRequest req) throws DSCallException{
		boolean usedTransaction = usedTransaction(req);
		Connection __currentConn = null;
        Statement s = null;
        ResultSet rs = null;
        try{
        	try{
        		__currentConn=getConnection(req);
	        	s = createFetchStatement(__currentConn);
	            rs = s.executeQuery(query);
        	} catch (SQLException e) {
        		//如果不使用事物机制，不影响别的语句执行，为了容错，可以再数据源中重新取一个connection执行查询
        		if(!usedTransaction){
        			try {
						__currentConn=dataSource.getConnection();
						if(s!=null){
							s.close();
						}
						s = createFetchStatement(__currentConn);
						rs = s.executeQuery(query);
					} catch (SQLException e1) {
						throw new DSCallException();
					}
        		}
        	}
        	
        	List<Object> rows = null;
            try {
                rows = JdbcHelper.toListOfMapsOrBeans(rs,dialect,req.getDataService().getDataServiceInfo());
            } catch (SQLException e) {
                throw new DSCallException("transform resultset to java bean error", e);
            }
            return rows;
        } finally {
            try {
                s.close();
                rs.close();
            } catch (Exception ignored) {
            }
            if (!usedTransaction && __currentConn != null) {
            	JdbcHelper.closeConnection(__currentConn);
            }
        }
	}
	private void pagedQuerySelect(DSRequest req, String query,PageControl page, DSResponse res) throws DSCallException {
		if (LOG.isDebugEnabled()){
			LOG.debug( (new StringBuilder()).append("SQL paged select rows ,page number").append(page.getPageNum())
	                .append(", page size ").append(page.getPageSize()).append(". Query:")
	                		.append(query).toString());
		} 
		boolean usedTransaction = usedTransaction(req);
		Connection __currentConn = null;
        Statement s = null;
        ResultSet rs = null;
        try{
        	try{
        		__currentConn=getConnection(req);
	        	s = createFetchStatement(__currentConn);
	            rs = s.executeQuery(query);
        	} catch (SQLException e) {
        		//如果不使用事物机制，不影响别的语句执行，为了容错，可以再数据源中重新取一个connection执行查询
        		if(!usedTransaction){
        			try {
						__currentConn=dataSource.getConnection();
						if(s!=null){
							s.close();
						}
						s = createFetchStatement(__currentConn);
						rs = s.executeQuery(query);
					} catch (SQLException e1) {
						throw new DSCallException();
					}
        		}
        	}
        	
        	List<Object> rows = null;
            try {
                rows = JdbcHelper.toListOfMapsOrBeans(rs,dialect,req.getDataService().getDataServiceInfo());
            } catch (SQLException e) {
                throw new DSCallException("transform resultset to java bean error", e);
            }
            res.setRawData(rows);
            //分页查询
            if(page!=null){
            	page.setTotalSize(rows.size());
                res.addAttachment(PageControl.class, page);
            }
        } finally {
            try {
                s.close();
                rs.close();
            } catch (Exception ignored) {
            }
            if (!usedTransaction && __currentConn != null) {
            	JdbcHelper.closeConnection(__currentConn);
            }
        }
	}
	
	private Statement createFetchStatement(Connection conn) throws SQLException {
		return conn.createStatement();
	}

	/**是否可使用事物机制*/
	private boolean usedTransaction(DSRequest req){
		return req.getDSCall() != null && this.canJoinTransaction(req);
	}
	/**获取链接*/
	private Connection getConnection(DSRequest req) throws SQLException{
		Connection __currentConn = null;
		if (usedTransaction(req)) {
            DSCall dsc = req.getDSCall();
            TransactionService ts = dsc.getTransactionService();
            Transaction transaction = ts.getResource(dataSource);
            req.setPartsOfTransaction(true);
            if (transaction != null) {
                if (transaction instanceof ConnectionWrapperedTransaction) {
                    Object wrap = ((ConnectionWrapperedTransaction<?>) transaction).getWrappedTransactionObject();
                    if (wrap instanceof Connection) {
                    	__currentConn = (Connection) wrap;
                    }
                } else  if (transaction instanceof ConnectionTransaction) {
                    Connection conn = (Connection) transaction.getTransactionObject();
                    if (conn != null) {
                    	__currentConn =conn;
                    }
                }  
                // dsc中不存在该DataSource的事物对象
            } else {
                if (this.canStartTransaction(req, false)) {
                	__currentConn = dataSource.getConnection();
                    if (__currentConn != null) {
                        ts.bindResource(dataSource, new ConnectionTransaction(__currentConn));
                    }
                }
            }
    	}else{
    		__currentConn = dataSource.getConnection();
    	}
		return  __currentConn;
	}

	private Object executeScalar(String countQuery, DSRequest req) throws DSCallException {
		List<Object> list = querySelect(countQuery, req);
		if (list == null || list.size() == 0) {
            return null;
        } else {
            Map map = (Map) list.get(0);
            return map.get(DataUtils.getSingle(map));
        }
	}

	/**基于模板生成sql语句*/
	private String generateSQLStatement(DSRequest req,OperationInfo oi,VelocityExpression ve,Map<String, Object> context,String customSql) {
		if(!StringUtils.isEmpty(customSql)){
			return ve.evaluateAsString(customSql, context);
		}
		 String selectClause  =getQueryClause(oi,JdbcExtProperty.SELECT_CLAUSE_NODE,JdbcExtProperty.DEFAULT_SELECT_CLAUSE);
		 String valuesClause  =getQueryClause(oi,JdbcExtProperty.VALUES_CLAUSE_NODE,JdbcExtProperty.DEFAULT_VALUES_CLAUSE);
		 String tableClause  =getQueryClause(oi,JdbcExtProperty.TABLE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_TABLE_CLAUSE);
		 String whereClause  =getQueryClause(oi,JdbcExtProperty.WHERE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_WHERE_CLAUSE);
		 String orderClause  =getQueryClause(oi,JdbcExtProperty.ORDER_CLAUSE_NODE,JdbcExtProperty.DEFAULT_ORDER_CLAUSE);
		 String groupClause  =getQueryClause(oi,JdbcExtProperty.GROUP_CLAUSE_NODE,JdbcExtProperty.DEFAULT_GROUP_CLAUSE);
		 String groupWhereClause  =getQueryClause(oi,JdbcExtProperty.GROUPWHERE_CLAUSE_NODE,JdbcExtProperty.DEFAULT_GROUPWHERE_CLAUSE);
		 String statement;
		 if(DataTools.isFetch(oi.getType())){
			 statement = (new StringBuilder()).append("SELECT ").append(selectClause).append(" FROM ").append(tableClause).toString();
	            if (!"$defaultWhereClause".equals(whereClause) || context.get("defaultWhereClause") != null)
	                statement = (new StringBuilder()).append(statement).append(" WHERE ").append(whereClause).toString();
	            if (!"$defaultGroupClause".equals(groupClause))
	                statement = (new StringBuilder()).append(statement).append(" GROUP BY ").append(groupClause).toString();
	            if (!"$defaultGroupWhereClause".equals(groupWhereClause))
	                statement = (new StringBuilder()).append("SELECT * FROM (").append(statement).append(") work WHERE ").append(groupWhereClause).toString();
	            if (req.getAttachment(SortBy.class) != null) {
	            	SortBy o = req.getAttachment(SortBy.class);
	                StringBuilder s = (new StringBuilder()).append(statement);
	                List<String> bys = o.sortby();
	                if(bys!=null&&bys.size()>0){
	                	s.append(" ORDER BY ");
                        for (int i = 0; i < bys.size(); i++) {
                            s.append( bys.get(i).toString());
                            if (i < bys.size())
                                s.append(", ");
                        }
	                }
	                statement = s.toString();
	            } else if (!"$defaultOrderClause".equals(orderClause))
	                statement = (new StringBuilder()).append(statement).append(" ORDER BY ").append(orderClause).toString();
	            LOG.debug("derived query: {}",statement);
		 }else if(DataTools.isAdd(oi.getType())){
			 statement = (new StringBuilder()).append("INSERT INTO ").append(tableClause).append(" ").append(valuesClause).toString();
		 }else if(DataTools.isUpdate(oi.getType())){
			 statement = (new StringBuilder()).append("UPDATE ").append(tableClause).append(" SET ").append(valuesClause).append(" WHERE ").append(
		                whereClause).toString();
		 }else if(DataTools.isRemove(oi.getType())){
			 statement = (new StringBuilder()).append("DELETE FROM ").append(tableClause).append(" WHERE ").append(whereClause).toString();		
		 }else{
			 throw new java.lang.UnsupportedOperationException("unsupported operation :"+oi.getType()+"for jdbcdataservice");
		 }
		return ve.evaluateAsString(statement, context);
	}
	
	private String getQueryClause(OperationInfo oi,String key,String defaultValue){
		String ext = StringUtils.trimToNull(oi.getExtension(key));
		if(ext==null){
			return "$"+defaultValue;
		}else{
			return ext;
		}
	}


	/**检测别命名*/
	private Boolean autoQualifyColumnNames(DataServiceInfo di) {
		List<FieldInfo> fields =di.getFields();
		if(DataUtils.isNotNullAndEmpty(fields)){
			for(FieldInfo field:fields){
				if(DataUtils.isNotNullAndEmpty(field.getForeignKey())){
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	private boolean isJdbcOperation(OperationType type) {
		return DataTools.isAdd(type)||DataTools.isFetch(type)||DataTools.isRemove(type)||DataTools.isUpdate(type);
	}

	private Map<String, Object> getClausesContext(
								DSRequest req, 
								boolean qualifyColumnNames, 
								OperationInfo oi,
								List<String> _customCriteriaFields,
								List<String> _customValueFields ,
								List<String> _excludeCriteriaFields) {
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
                        fieldName = field.getProperty(JdbcExtProperty.CUSTOM_SELECT_EXPRESSION);
                        break;
                    case UPDATE:
                        fieldName = field.getProperty(JdbcExtProperty.CUSTOM_UPDATE_EXPRESSION);
                        break;
                    default:
                        break;
                }
                if (fieldName == null)
                    fieldName = field.getName();
                if (field.getProperty(JdbcExtProperty.TABLE_NAME) != null)
                    selfTableName = field.getProperty(JdbcExtProperty.TABLE_NAME);
                relateCriterias.add(new StringBuilder().append(selfTableName).append(".").append(fieldName).append(" = ").append(foreign).toString());
            }
        }
        
        SQLTableClause tableClause = new SQLTableClause(this);
        tableClause.setRelatedTables(relateTables);
        context.put(JdbcExtProperty.DEFAULT_TABLE_CLAUSE, tableClause.getSQLString());
        
        if (type == OperationType.FETCH || type == OperationType.CUSTOM) {
            SQLSelectClause selectClause = new SQLSelectClause(req, this, qualifyColumnNames);
            List<String> customValue=getExtensionFields(oi,JdbcExtProperty.CUSTOM_VALUE_FIELDS_NODE);
            selectClause.setCustomValueFields(customValue);
            context.put(JdbcExtProperty.DEFAULT_SELECT_CLAUSE, selectClause.getSQLString());
            SQLOrderClause orderClause = new SQLOrderClause(req, this, qualifyColumnNames);
            orderClause.setCustomValueFields(customValue);
            if (orderClause.size() > 0)
                context.put(JdbcExtProperty.DEFAULT_ORDER_CLAUSE, orderClause.getSQLString());
        }
        
        if (DataTools.isAdd(type) || DataTools.isUpdate(type)) {
            SQLValuesClause valuesClause = new SQLValuesClause(req,this);
            if (valuesClause.size() > 0)
                if (DataTools.isUpdate(type)) {
                    context.put(JdbcExtProperty.DEFAULT_VALUES_CLAUSE, valuesClause.getSQLStringForUpdate());
                } else {
                    context.put(JdbcExtProperty.DEFAULT_VALUES_CLAUSE, valuesClause.getSQLStringForInsert());
                }
            context.put("batchUpdateReturnValue", valuesClause.getReturnValues());
        }
        
        if (!DataTools.isAdd(type)) {
            String textMatchStyle = null;
            if (req.getAttribute("textMatchStyle")!=null)
                textMatchStyle = req.getAttribute("textMatchStyle").toString();
            SQLWhereClause whereClause = new SQLWhereClause(qualifyColumnNames, req, this, false, textMatchStyle);
            whereClause.setCustomCriteriaFields(_customCriteriaFields);
            whereClause.setExcludeCriteriaFields(_excludeCriteriaFields);
            whereClause.setRelatedCriterias(relateCriterias);
            if (DataTools.isRemove(type) && whereClause.isEmpty())
                throw new SQLGenerationException("empty where clause on delete operation - would  destroy table - ignoring.");
            context.put(JdbcExtProperty.DEFAULT_WHERE_CLAUSE, whereClause.getSQLString());
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
    	if(call.getTransactionService()!=null)
    		call.getTransactionService().commit();
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.call.DSCallCompleteCallback#onFailure(org.solmix.datax.call.DSCall, boolean)
     */
    @Override
    public void onFailure(DSCall call, boolean transactionFailure) throws DSCallException {
    	if(call.getTransactionService()!=null)
    		call.getTransactionService().rollback();
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
        return this.dialect;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDataServiceRouter(DataServiceRouter<RequestToken> dataServiceRouter) {
        this.dataServiceRouter = dataServiceRouter;
    }

    protected boolean isPartitionEnable() {
        return dataServiceRouter != null && dataSourceService != null;
    }

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
