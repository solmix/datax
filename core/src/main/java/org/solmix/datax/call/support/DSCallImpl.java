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

package org.solmix.datax.call.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.NotThreadSafe;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallCompleteCallback;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.datax.support.DSResponseImpl;
import org.solmix.datax.transaction.TransactionException;
import org.solmix.datax.transaction.TransactionFailedException;
import org.solmix.datax.transaction.TransactionService;
import org.solmix.datax.util.DataTools;

/**
 * 支持带事物机制的请求执行，但是只支持简单的事物，当出错时自动回滚，正常执行完毕时自动提交
 * <br><B>注意：</b><br>
 * 不是所有的DataService都支持如同jdbc的事物机制，如需这写机制可以采用其他方式去实现。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月22日
 */
@NotThreadSafe
public class DSCallImpl implements DSCall
{
    private static final Logger LOG = LoggerFactory.getLogger(DSCallImpl.class);
    
    private List<DSRequest> requests = new ArrayList<DSRequest>();

    private TransactionPolicy transactionPolicy;

    private Long transactionNum;
    
    private Map<Object, Object> attributes;
    
    private  Map<DSRequest, DSResponse> responseMap = new LinkedHashMap<DSRequest, DSResponse>();

    private  HashSet<DSCallCompleteCallback> callbacks = new HashSet<DSCallCompleteCallback>();
    
    private final TransactionService transactionService;
    
    public enum STATUS
    {
        INIT , BEGIN , SUCCESS , FAILED , CLOSED;
    }

    private STATUS status;

    private boolean exceptionBroken =true  ;

    public DSCallImpl(TransactionService transactionManager)
    {
        this(STATUS.INIT,transactionManager,null);
    }
    
    public DSCallImpl(STATUS status,TransactionService transactionManager,TransactionPolicy policy)
    {
        this.status = status;
        this.transactionPolicy=policy;
        this.transactionService=transactionManager;
    }
    
    public void addRequest(DSRequest req) {
        if(!requests.contains(req)){
            requests.add(req);
        }
    }


    @Override
    public List<DSRequest> getRequests() {
        return requests;
    }

    @Override
    public DSResponse getResponse(DSRequest req) {
        return this.responseMap.get(req);
    }
    @Override
    public DSResponse getResponseByOperationId(String operationId) {
        if(operationId==null){
            return null;
        }
        for(DSRequest req:requests){
           if(operationId.endsWith( req.getOperationId())){
               return getResponse(req);
           }
        }
        return null;
    }
    @Override
    public DSResponse getResponseByOperationLocalId(String operationId) {
        if (operationId == null) {
            return null;
        }
        for (DSRequest req : requests) {
            OperationInfo oi = req.getOperationInfo();
            if (oi != null && operationId.endsWith(oi.getLocalId())) {
                return getResponse(req);
            }
        }
        return null;
    }
 
    @Override
    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    @Override
    public void setTransactionPolicy(TransactionPolicy transactionPolicy) throws TransactionException {
        if (status != STATUS.INIT){
            throw new TransactionException("dscall already started.");
        }
        this.transactionPolicy = transactionPolicy;
    }

    @Override
    public boolean queueIncludesUpdates(DSRequest req) {
        if (requests == null)
            return false;
        for (DSRequest request : requests) {
            if (request.equals(req)) {
                return false;
            }
            if (DataTools.isModificationRequest(req)){
                return true;
            }
        }
        return false;
    }

    public Long getTransactionNum() {
        return transactionNum;
    }

    public void setTransactionNum(Long transactionNum) {
        this.transactionNum = transactionNum;
    }

    @Override
    public void registerCallback(DSCallCompleteCallback callback) {
        if (!callbacks.contains(callback)){
            callbacks.add(callback);
        }
    }

   
    @Override
    public DSResponse execute(DSRequest request) throws DSCallException {
        if(status==STATUS.INIT){
            status=STATUS.BEGIN;
        }
        if (status != STATUS.BEGIN) {
            throw new TransactionException("Transaction not started ,you should call method begin()");
        }
        request.setDSCall(this);
        addRequest(request);
        request.setCanJoinTransaction(true);
        DSResponse res = null;
        try {
            res = request.execute();
        } catch (Throwable e) {
            if(exceptionBroken){
                try {
                    transactionFailed(request, res);
                } catch (Exception e1) {
                    throw new TransactionFailedException("transaction rollback failure with rollback Exception:" + e1.getMessage()
                        + " with Root Exception:" + e.getMessage());
                }
                throw new TransactionFailedException("transaction breaken case by:"+e.getMessage(), e);
            }else{
                res = new DSResponseImpl(request);
                res.setRawData(e.getMessage());
                res.setStatus(Status.STATUS_FAILURE);
                LOG.error("DSRequest execute Failed:",e);
            }
            
        }
        if(exceptionBroken){
            boolean transactionFailure = isXAFailure(request, res);
            if (transactionFailure) {
                transactionFailed(request, res);
                throw new TransactionFailedException("transaction breaken because of one request failure.");
            }
        }
        responseMap.put(request, res);
        return res;
    }
    
    protected void transactionFailed(DSRequest request, DSResponse resp) throws DSCallException  {
        if (request.isPartsOfTransaction()) {
            if (resp != null && resp.getStatus() == DSResponse.Status.STATUS_SUCCESS){
                resp.setStatus(DSResponse.Status.STATUS_TRANSACTION_FAILED);
            }
        }
        onFailure();
    }
    
    protected void onSuccess()  {
        status = STATUS.SUCCESS;
        for (DSCallCompleteCallback callback : callbacks) {
            callback.onSuccess(this);
        }
        getTransactionService().commit();
    }
    
    /**失败处理*/
    protected void onFailure() throws DSCallException {
        status = STATUS.FAILED;
        boolean transactionFailure = false;
        if (requests != null){
          //检查一串请求中是否有失败的
            for (DSRequest req : requests) {
                DSResponse resp = getResponse(req);
                transactionFailure = isXAFailure(req, resp);
                if(transactionFailure){
                    break;
                }
            }
            if (transactionFailure) {
                for (DSRequest req : requests) {
                    if (req.isPartsOfTransaction()) {
                        DSResponse resp = getResponse(req);
                        if (resp != null && resp.getStatus() == DSResponse.Status.STATUS_SUCCESS){
                            resp.setStatus(DSResponse.Status.STATUS_TRANSACTION_FAILED);
                        }
                    }
                }
            }
        }
        if (callbacks != null){
            for (DSCallCompleteCallback callback : callbacks) {
                callback.onFailure(this, transactionFailure);
            }
        }
        getTransactionService().rollback();
    }
    
    /**执行没抛错，检查执行结果是否为成功*/
    protected boolean isXAFailure(DSRequest req, DSResponse res) {
        boolean transactionFailure = false;
        if (res != null && res.getStatus().value() < 0){
            if (req.isRequestStarted()) {
                if (req.isPartsOfTransaction())
                    transactionFailure = true;
            } else {
                DataService ds = req.getDataService();
                if (ds.canJoinTransaction(req) && (ds.canStartTransaction(req, true) || queueIncludesUpdates(req)))
                    transactionFailure = true;
            }
        }
        return transactionFailure;
    }
 

    
    @Override
    public void commit() {
            if (responseMap.size() != requests.size()) {
                throw new TransactionException(new StringBuilder().append("Having ").append(requests.size()).append(" requests,But having ").append(
                    responseMap.size()).append(" responses").toString());
            }
            boolean failure = false;
            for (DSRequest req : requests) {
                DSResponse res = getResponse(req);
                if (res.getStatus().value() < 0) {
                    failure = true;
                    break;
                }
            }
            if (failure) {
                try {
                    onFailure();
                } catch (DSCallException e) {
                    throw new TransactionException("onFailure", e);
                }
            } else {
                onSuccess();
            }
       
    }
  
    @Override
    public Object getAttribute(Object key) {
        if (attributes != null)
            return attributes.get(key);
        return null;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        if (attributes == null)
            attributes = new LinkedHashMap<Object, Object>();
        attributes.put(key, value);

    }

    @Override
    public void removeAttribute(Object key) {
        if (attributes != null){
            attributes.remove(key);
        }
    }

    @Override
    public void freeResources() {
        status=STATUS.CLOSED;
        this.attributes=null;
        this.responseMap=null;
        this.requests=null;
    }

    @Override
    public boolean isClosed(){
        return status==STATUS.CLOSED;
    }

    @Override
    public DSResponse getMergedResponse(MergedType merged)throws DSCallException {
        //如果还没有结束，先结束并提交
        if(status==STATUS.BEGIN){
            commit();
        }
            if(merged==null){
                merged=MergedType.SIMPLE;
            }
            switch (merged) {
                case SIMPLE:
                    return simpleResponse();
                case WRAPPED:
                    return wrappedResponse();
                default:
                   throw new UnsupportedOperationException(merged.value());
               
            }
    }
    
    private DSResponse simpleResponse() {
        Status st=Status.UNSET;
        if(status==STATUS.SUCCESS){
            st=Status.STATUS_SUCCESS;
        }else if(status==STATUS.FAILED){
            st=Status.STATUS_TRANSACTION_FAILED;
        }
        List<DSRequest> canReturn = new ArrayList<DSRequest>();
        for (DSRequest req : requests) {
            if(DataUtils.asBoolean(req.getOperationInfo().getOneway())){
                continue;
            }else{
                canReturn.add(req);
            }
        }
        DSResponse res=null;
        boolean onlyOneRequest =canReturn.size()==1;
        if(onlyOneRequest){
            res= getResponse(canReturn.get(0));
            res.setStatus(st);
        }else{
            res = new DSResponseImpl(st);
            Map<String,Object> mergedData= new LinkedHashMap<String, Object>();
            List<Object> errors = new ArrayList<Object>();
            for (DSRequest req : canReturn) {
                DSResponse resp = getResponse(req);
                mergedData.put(req.getOperationId(), resp.getRawData());
                Object[] error = resp.getErrors();
                if(error!=null&&error.length>0){
                    errors.addAll(Arrays.asList(error));
                }
                res.setRawData(mergedData);
                if(errors.size()>0){
                    res.setErrors(errors.toArray());
                }
            }
        }
        return res;
        
    }
    
    private DSResponse wrappedResponse() {
        List<DSResponse> array= new ArrayList<DSResponse>();
        for (DSRequest req : requests) {
            if(DataUtils.asBoolean(req.getOperationInfo().getOneway())){
                continue;
            }
            DSResponse resp = getResponse(req);
            array.add(resp);
        }
        Status st=Status.UNSET;
        if(status==STATUS.SUCCESS){
            st=Status.STATUS_SUCCESS;
        }else if(status==STATUS.FAILED){
            st=Status.STATUS_TRANSACTION_FAILED;
        }
        DSResponse res = new DSResponseImpl(st);
        res.setRawData(array);
        return res;
    }
    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.call.DSCall#setExceptionBroken(boolean)
     */
    @Override
    public void setExceptionBroken(boolean broken) {
       this.exceptionBroken=broken;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.call.DSCall#isExceptionBroken()
     */
    @Override
    public boolean isExceptionBroken() {
        return exceptionBroken;
    }


   
    
}
