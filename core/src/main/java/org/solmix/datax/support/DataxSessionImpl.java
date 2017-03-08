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

package org.solmix.datax.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.solmix.commons.pager.PageControl;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataxRuntimeException;
import org.solmix.datax.DataxSession;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.ResponseHandler;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月24日
 */

public class DataxSessionImpl implements DataxSession
{

    private DataServiceManager dataServiceManager;

    public DataxSessionImpl(DataServiceManager dataServiceManager)
    {
        this.dataServiceManager = dataServiceManager;
    }

    @Override
    public <T> T fetchOne(String operationId, Class<T> resultType) {
        DSRequest req = createDSRequest(operationId);
        DSResponse response = execute(req,OperationType.FETCH);
        return response.getSingleResult(resultType);
    }

    @Override
    public <T> T fetchOne(String operationId, Object parameter,Class<T> resultType) {
        DSRequest req = createDSRequest(operationId,parameter);
        DSResponse response = execute(req,OperationType.FETCH);
        return  response.getSingleResult(resultType);
    }

    @Override
    public <E> List<E> fetchList(String operationId,Class<E> resultType) {
        return  fetchList(operationId,null,null,resultType);
    }
   
    @Override
    public <E> List<E> fetchList(String operationId, Object parameter,Class<E> resultType) {
        return  fetchList(operationId,parameter,null,resultType);
    }

    @Override
    public <E> List<E> fetchList(String operationId, Object parameter, PageControl page,Class<E> resultType) {
        DSRequest req = createDSRequest(operationId,parameter,page);
        DSResponse response = execute(req,OperationType.FETCH);
        return  response.getResultList(resultType);
    }
    
    @Override
    public <T> T fetch(String operationId, ResponseHandler<T> handler) {
        return fetch(operationId,null,null,handler);
    }
   
    @Override
    public <T> T fetch(String operationId, Object parameter, ResponseHandler<T> handler) {
        return fetch(operationId,parameter,null,handler);
    }

    @Override
    public <T> T fetch(String operationId, Object parameter, PageControl page, ResponseHandler<T> handler) {
        DSRequest req = createDSRequest(operationId,parameter,page);
        DSResponse response = execute(req,OperationType.FETCH);
        return  handler.handle(req,response);
    }
    

    @Override
    public int add(String operationId, Object parameter) {
        DSRequest req = createDSRequest(operationId,parameter);
        DSResponse response = execute(req,OperationType.ADD);
        if(response.getStatus()==Status.STATUS_SUCCESS){
            return response.getAffectedRows();
        }else{
            return 0;
        }
    }

    @Override
    public int update(String operationId) {
        return update(operationId,null);
    }

    @Override
    public int update(String operationId, Object parameter) {
        DSRequest req = createDSRequest(operationId,parameter);
        DSResponse response = execute(req,OperationType.UPDATE);
        if(response.getStatus()==Status.STATUS_SUCCESS){
            return response.getAffectedRows();
        }else{
            return 0;
        }
    }

    @Override
    public int remove(String operationId) {
        return remove(operationId,null);
    }

    @Override
    public int remove(String operationId, Object parameter) {
        DSRequest req = createDSRequest(operationId,parameter);
        DSResponse response = execute(req,OperationType.REMOVE);
        if(response.getStatus()==Status.STATUS_SUCCESS){
            return response.getAffectedRows();
        }else{
            return 0;
        }
    }
    @Override
    public <T> T custom(String operationId, ResponseHandler<T> handler) {
        return fetch(operationId,null,null,handler);
    }
   
    @Override
    public <T> T custom(String operationId, Object parameter, ResponseHandler<T> handler) {
        return fetch(operationId,parameter,null,handler);
    }

    @Override
    public <T> T custom(String operationId, Object parameter, PageControl page, ResponseHandler<T> handler) {
        DSRequest req = createDSRequest(operationId,parameter,page);
        DSResponse response = execute(req,OperationType.CUSTOM);
        return  handler.handle(req,response);
    }
  
    @Override
    public <T> T getService(Class<T> serviceType) {
        return dataServiceManager.getService(serviceType);
    }
    


    @Override
    public List<DSResponse> execute(List<DSRequest> requests) {
        if(requests==null){
            return null;
        }
        List<DSResponse>  responses = new ArrayList<DSResponse>();
        for(DSRequest request :requests){
            responses.add(execute(request,null));
        }
        return responses;
    }



    @Override
    public DataServiceManager getDataServiceManager() {
        return dataServiceManager;
    }

  

    @Override
    public DSResponse execute(DSRequest req){
       return execute(req, null);
    }

    private DSResponse execute(DSRequest req,OperationType type) {
        OperationInfo oi= req.getOperationInfo();
        if(oi==null){
            throw new OperationNoFoundException("No operation");
        }
        if (type != null && oi.getType() != type) {
            throw new OperationNoFoundException("Mismatching operation type,request is[" + type + "],but configured is[" + oi.getType() + "]");
        }

	    try {
	        return req.execute();
	    } catch (DSCallException e) {
	        throw new DataxRuntimeException(e);
	    }
    }
    
    @Override
    public DSRequest createDSRequest(String operationId, Object parameters) {
        return createDSRequest(operationId,parameters,null);
    }
    @Override
    public DSRequest createDSRequest(String operationId) {
        return createDSRequest(operationId,null,null);
    }
    @Override
    public DSRequest createDSRequest(String operationId, Object parameters, PageControl page) {
        DSRequest request = dataServiceManager.createDSRequest();
        request.setOperationId(operationId);
        if (parameters != null) {
            request.setRawValues(parameters);
        }
        if (page != null) {
            request.addAttachment(PageControl.class, page);
        }
        return request;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchOne(String operationId) {
        return fetchOne(operationId,Map.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> fetchOne(String operationId, Object parameter) {
        return fetchOne(operationId,parameter,Map.class);
    }

  
}
