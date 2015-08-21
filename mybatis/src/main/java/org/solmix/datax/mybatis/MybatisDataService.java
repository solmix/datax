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

package org.solmix.datax.mybatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.TransactionFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.attachment.Pageable;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.jdbc.ConcurrencyRequestException;
import org.solmix.datax.jdbc.DataSourceInfo;
import org.solmix.datax.jdbc.DataSourceService;
import org.solmix.datax.jdbc.GetConnectionException;
import org.solmix.datax.jdbc.RoutingRequest;
import org.solmix.datax.jdbc.dialect.SQLDialect;
import org.solmix.datax.jdbc.dialect.SQLDialectFactory;
import org.solmix.datax.jdbc.support.ConnectionTransaction;
import org.solmix.datax.jdbc.support.ConnectionWrapperedTransaction;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.mybatis.page.PagedParameter;
import org.solmix.datax.router.DataServiceRouter;
import org.solmix.datax.router.RequestToken;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.support.DSResponseImpl;
import org.solmix.datax.transaction.Transaction;
import org.solmix.datax.transaction.TransactionService;
import org.solmix.datax.util.DataTools;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月9日
 */

public class MybatisDataService extends BaseDataService implements DataService
{

    private SqlSessionFactory sqlSessionFactory;

    private DataSourceService dataSourceService;
    
    private DataSource defaultDataSource;
    
    private SQLDialect defaultDialect;

    private DataServiceRouter<RequestToken> dataServiceRouter;

    public MybatisDataService(DataServiceInfo info, Container container, DataTypeMap prop)
    {
        super(info, container, prop);
    }

    @Override
    protected DSResponse executeDefault(DSRequest req, OperationType type) throws DSCallException {

        if (isPartitionEnable()) {
            return executePartition(req,type);
        } else {
            DataSource dataSource = getDefaultDataSource();
           return executeWithDataSource(dataSource,req,type);
        }
    }

    protected DSResponse executeWithDataSource(DataSource dataSource, DSRequest req, OperationType type) throws DSCallException {
        SqlSession session = null;
        boolean usedTransaction = false;
        if (req.getDSCall() != null && this.canJoinTransaction(req)) {
            usedTransaction = true;
            DSCall dsc = req.getDSCall();
            TransactionService ts = dsc.getTransactionService();
            Transaction transaction = ts.getResource(dataSource);
            req.setPartsOfTransaction(true);
            // dsc中已经存在该DataSource的事物对象
            if (transaction != null) {
                if (transaction instanceof ConnectionTransaction) {
                    Connection conn = (Connection) transaction.getTransactionObject();
                    if (conn != null) {
                        session = sqlSessionFactory.openSession(conn);
                    } else {
                        session = sqlSessionFactory.openSession();
                    }
                    if (session != null && this.canStartTransaction(req, false)) {
                        ts.bindResource(dataSource, new ConnectionWrapperedTransaction<SqlSession>(session, session.getConnection()));
                    }
                } else if (transaction instanceof ConnectionWrapperedTransaction) {
                    Object wrap = ((ConnectionWrapperedTransaction<?>) transaction).getWrappedTransactionObject();
                    if (wrap instanceof SqlSession) {
                        session = (SqlSession) wrap;
                    }
                }
                // dsc中不存在该DataSource的事物对象
            } else {
                if (this.canStartTransaction(req, false)) {
                    session = sqlSessionFactory.openSession(false);
                    if (session != null) {
                        ts.bindResource(dataSource, new ConnectionWrapperedTransaction<SqlSession>(session, session.getConnection()));
                    }
                }
            }

        } else {
            session = sqlSessionFactory.openSession(true);
        }
        try {
            if (DataTools.isFetch(type)) {
                return executeFetch(req,session,dataSource);
            } else if (DataTools.isRemove(type)) {
                return executeRemove(req,session);
            } else if (DataTools.isUpdate(type)) {
                return executeUpdate(req,session);
            } else if (DataTools.isAdd(type)) {
                return executeAdd(req,session);
            }else{
                return notSupported(req);
            }
        } finally {
            if (usedTransaction && session != null) {
                session.close();
            }
        }
    }

    protected DSResponse executeAdd(DSRequest req,SqlSession session) throws DSCallException{
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        String statement = getMybatisStatement(req);
        Object value=req.getRawValues();
        int result = session.update(statement, value);
        res.setAffectedRows(new Long(result));
        res.setRawData(result);
        return res;
    }
    protected DSResponse executeUpdate(DSRequest req,SqlSession session)throws DSCallException {
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        String statement = getMybatisStatement(req);
        Object value=req.getRawValues();
        int result = session.update(statement, value);
        res.setAffectedRows(new Long(result));
        res.setRawData(result);
        return res;
    }

    protected DSResponse executeRemove(DSRequest req,SqlSession session)throws DSCallException {
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        String statement = getMybatisStatement(req);
        Object value=req.getRawValues();
        int result = session.delete(statement, value);
        res.setAffectedRows(new Long(result));
        res.setRawData(result);
        return res;
    }

    @SuppressWarnings("rawtypes")
    protected DSResponse executeFetch(DSRequest req,SqlSession session,DataSource dataSource)throws DSCallException {
        DSResponse res = new DSResponseImpl(req,Status.STATUS_SUCCESS);
        String mybatisStatement = getMybatisStatement(req);
        
        Pageable pageable= req.getAttachment(Pageable.class);
        boolean paged = false;
        if(DataTools.isPaged(pageable)){
            paged=true;
        }
        Object parameter = null;
        if (paged) {
            
            // initial SqlDriver for limit & count sql
            if (defaultDialect == null) {
                try {
                    defaultDialect = SQLDialectFactory.getInstance().getSQLDialect(dataSource);
                } catch (Exception e) {
                    throw new DSCallException( "Can't instance SQLDialect for datasource:"+dataSource, e);
                }
            }
            parameter = new PagedParameter(req, res,
                req.getRawValues(), defaultDialect, session,req.getAttachment(Pageable.class));
        } else {
            parameter = req.getRawValues();
        }
        final List<Object> results = new ArrayList<Object>();
        // log start time
        long _$ = System.currentTimeMillis();
        session.select(mybatisStatement, parameter, new ResultHandler() {

            @Override
            public void handleResult(ResultContext context) {
                results.add(context.getResultObject());
            }
        });
        // fire time event.
        if (isEventEnable()) {
            createAndFireTimeMonitorEvent((System.currentTimeMillis() - _$),
                "SQL  query statement [" + mybatisStatement + "],params [" + 
                    req.getRawValues() + "] Query total rows: " + results.size());
        }
        res.setRawData(results);
        if(paged){
            Pageable page=  req.getAttachment(Pageable.class);
            int start = page.getStartRow();
            int end =start+results.size();
            page.setEndRow(end);
            res.addAttachment(Pageable.class, page);
        }
        return res;
    }

    protected DSResponse executePartition(DSRequest req, OperationType type) throws DSCallException{
        RequestToken token = new RequestToken(req.getOperationId(), req.getRawValues());
        SortedMap<String, DataSource> dsMap = lookupDataSourceByRouter(token);
        //找不到，找到一个
        DataSource target;
        if (dsMap == null || dsMap.size() == 0 || dsMap.size() == 1) {
            if (dsMap.size() == 1) {
                target = dsMap.values().iterator().next();
            } else {
                target = getDefaultDataSource();
            }
            return executeWithDataSource(target, req, type);
        }else{
            return executeInConcurrency(dsMap, req, type);
        }
    }
    
    protected DSResponse executeInConcurrency(SortedMap<String, DataSource> dsMap, DSRequest req, final OperationType type) {
        DSResponse res = new DSResponseImpl(req, Status.STATUS_SUCCESS);
        List<RoutingRequest> requests = new ArrayList<RoutingRequest>();
        boolean usedTransaction = false;
        if (req.getDSCall() != null && this.canJoinTransaction(req)) {
            usedTransaction = true;
        }
        for (String key : dsMap.keySet()) {
            DataSourceInfo dsi = dataSourceService.getDataSourceInfo(key);
            ExecutorService es = dsi.getExecutorService();
            synchronized (es) {
                if (es == null) {
                    es = createExecutorServiceIfNS(dsi);
                }
            }
            RoutingRequest rrequest = new RoutingRequest();
            rrequest.setExecutor(es);
            rrequest.setRequest(req);
            rrequest.setDataSource(dsMap.get(key));
            requests.add(rrequest);
        }

        if (CollectionUtils.isEmpty(requests)) {
            return res;
        }
        List<SqlSessionDepository> des = makeupSessionInConcurrency(requests, usedTransaction);
        // concurrent
        final CountDownLatch latch = new CountDownLatch(des.size());
        List<Future<Object>> futures = new ArrayList<Future<Object>>();
        try {
            for (SqlSessionDepository routing : des) {
                RoutingRequest request = routing.getRequest();
                final DSRequest actualReq = request.getRequest();
                final SqlSession session = routing.getSqlSession();
                futures.add(request.getExecutor().submit(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        try {
                            return executeWithSqlSession(session, actualReq,type);
                        } finally {
                            latch.countDown();
                        }
                    }
                }));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new ConcurrencyRequestException("interrupted when processing data access request in concurrency", e);
            }
        } finally {
            if (!usedTransaction) {
                for (SqlSessionDepository routing : des) {
                    SqlSession session = routing.getSqlSession();
                    session.close();
                }
            }
        }
        prepareResult(futures, type,req,res);

        return res;
    }
    
   
    private void prepareResult(List<Future<Object>> futures,OperationType type, DSRequest req,DSResponse res) {
       
        if (DataTools.isFetch(type)) {
            List<Object> fetchList = new ArrayList<Object>();
            for (Future<Object> future : futures) {
                @SuppressWarnings("unchecked")
                List<Object> so= (List<Object>)getFutureValue(future);
                fetchList.addAll(so);
            }
            res.setRawData(fetchList);
          } else if (DataTools.isRemove(type)) {
              Long affect=0l;
              for (Future<Object> future : futures) {
                  Integer so= (Integer)getFutureValue(future);
                  affect+=so;
              }
              res.setRawData(affect);
              res.setAffectedRows(affect);
          } else if (DataTools.isUpdate(type)) {
              Long affect=0l;
              for (Future<Object> future : futures) {
                  Integer so= (Integer)getFutureValue(future);
                  affect+=so;
              }
              res.setRawData(affect);
              res.setAffectedRows(affect);
          } else if (DataTools.isAdd(type)) {
              Long affect=0l;
              for (Future<Object> future : futures) {
                  Integer so= (Integer)getFutureValue(future);
                  affect+=so;
              }
              res.setRawData(affect);
              res.setAffectedRows(affect);
          }
    }
    
    private Object getFutureValue(Future<Object> future ){
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ConcurrencyRequestException(
                    "interrupted when processing data access request in concurrency", e);
        } catch (ExecutionException e) {
            throw new ConcurrencyRequestException("something goes wrong in processing", e);
        }
    }

    protected Object executeWithSqlSession(SqlSession session,DSRequest req,OperationType type){
        String statement = this.getMybatisStatement(req);
        if (DataTools.isFetch(type)) {
          return  session.selectList(statement, req.getRawValues());
        } else if (DataTools.isRemove(type)) {
            return session.delete(statement, req.getRawValues());
        } else if (DataTools.isUpdate(type)) {
            return session.update(statement, req.getRawValues());
        } else if (DataTools.isAdd(type)) {
            return session.insert(statement, req.getRawValues());
        }
        return null;
        
    }

    protected List<SqlSessionDepository> makeupSessionInConcurrency(List<RoutingRequest> requests, boolean usedTransaction) {
        List<SqlSessionDepository> depos = new ArrayList<SqlSessionDepository>();
        for(RoutingRequest request:requests){
            DataSource dataSource = request.getDataSource();
            DSRequest req=request.getRequest();
            
            SqlSession session=null;
            if(usedTransaction){
                DSCall dsc = req.getDSCall();
                TransactionService ts = dsc.getTransactionService();
                Transaction transaction = ts.getResource(dataSource);
                req.setPartsOfTransaction(true);
                // dsc中已经存在该DataSource的事物对象
                if (transaction != null) {
                    if (transaction instanceof ConnectionTransaction) {
                        Connection conn = (Connection) transaction.getTransactionObject();
                        if (conn == null) {
                            conn=getConnection( dataSource,false);
                        } 
                            session = sqlSessionFactory.openSession(conn);
                        if (session != null && this.canStartTransaction(req, false)) {
                            ts.bindResource(dataSource, new ConnectionWrapperedTransaction<SqlSession>(session, session.getConnection()));
                        }
                    } else if (transaction instanceof ConnectionWrapperedTransaction) {
                        Object wrap = ((ConnectionWrapperedTransaction<?>) transaction).getWrappedTransactionObject();
                        if (wrap instanceof SqlSession) {
                            session = (SqlSession) wrap;
                        }
                    }
                    // dsc中不存在该DataSource的事物对象
                } else {
                    if (this.canStartTransaction(req, false)) {
                        Connection conn =getConnection( dataSource,false);
                        session = sqlSessionFactory.openSession(conn);
                        if (session != null) {
                            ts.bindResource(dataSource, new ConnectionWrapperedTransaction<SqlSession>(session, session.getConnection()));
                        }
                    }
                }
                
              //不使用transaction
            }else{
               Connection conn= getConnection(dataSource,true);
               session=sqlSessionFactory.openSession(conn);
            }
            SqlSessionDepository ss= new SqlSessionDepository();
            ss.setRequest(request);
            ss.setUsedTransaction(usedTransaction);
            ss.setSqlSession(session);
            depos.add(ss);
        }
        return depos;
    }
    
    private Connection getConnection(DataSource dataSource,boolean autoCommit){
        TransactionFactory factory=  sqlSessionFactory.getConfiguration().getEnvironment().getTransactionFactory();
        org.apache.ibatis.transaction.Transaction trans= factory.newTransaction(dataSource, TransactionIsolationLevel.NONE, true);
        try {
            Connection conn= trans.getConnection();
            conn.setAutoCommit(autoCommit);
            return conn;
        } catch (SQLException e) {
           throw new GetConnectionException("Could not get JDBC Connection",e);
        }
    }

    private ExecutorService createExecutorServiceIfNS(DataSourceInfo dsi) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        int poolSize=dsi.getPoolSize();
        if (poolSize < coreSize) {
            coreSize = poolSize;
        }
        ThreadFactory tf = new NamedThreadFactory("MybatisDataService",true) ;
        BlockingQueue<Runnable> queueToUse = new LinkedBlockingQueue<Runnable>(coreSize);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, poolSize, 60,
                TimeUnit.SECONDS, queueToUse, tf, new ThreadPoolExecutor.CallerRunsPolicy());
        dsi.setExecutorService(executor);
        return executor;
    }

    protected SortedMap<String, DataSource> lookupDataSourceByRouter(RequestToken token) {
        SortedMap<String, DataSource> resultMap = new TreeMap<String, DataSource>();

        if (dataServiceRouter!= null && dataSourceService != null) {
           List<String> ids= dataServiceRouter.route(token).getResourceIds();
           if(CollectionUtils.isNotEmpty(ids)){
               Collections.sort(ids);
               for (String dsName : ids) {
                   resultMap.put(dsName, dataSourceService.getDataSources().get(dsName));
               }
           }
        }
        return resultMap;
    }

    /**
     * <li>request是否带有参数
     * <li>operationInfo是否配置
     * <li>没有配置取默认值为operationId
     * @param req
     * @return
     */
    private String getMybatisStatement(DSRequest req) {
        Object mybatisStatement = req.getAttribute("_mybatis_id");
        if (mybatisStatement != null) {
            return mybatisStatement.toString();
        }
        OperationInfo oi = req.getOperationInfo();
        Object statement = oi.getProperty("statement");
        if (StringUtils.isNotEmpty((String) statement)) {
            return statement.toString();
        }
        return oi.getId();
    }
    
    private DataSource getDefaultDataSource(){
        if(defaultDataSource==null){
            defaultDataSource=sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        }
        return defaultDataSource;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
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

    @Override
    public String getServerType() {
        return MybatisDataServiceFactory.MYBATIS;
    }

}
