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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataServiceNoFoundException;
import org.solmix.datax.FreeResourcesHandler;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.RequestContext;
import org.solmix.datax.application.Application;
import org.solmix.datax.application.ApplicationManager;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.runtime.transaction.TransactionException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class DSRequestImpl  implements DSRequest,Cloneable
{

    private static final Logger LOG= LoggerFactory.getLogger(DSRequestImpl.class);
    
    private String dataServiceId;
    
    private String appId;

    private DataService dataService;

    private  String operationId;
    
    private Boolean joinTransaction;
    
    private boolean partsOfTransaction;
    
    private boolean validated = false;

    FreeResourcesHandler freeResourcesHandler;

    RequestContext requestContext;

    boolean requestStarted;
    
    boolean  invoked;

    private DSCall dsc;
    
    private Boolean freeOnExecute ;
    
    private Object rawValues;

    
    private DataServiceManager dataServiceManager;
    
    private ApplicationManager applicationManager;
    
    private Map<String ,Object> attributes;
    
    private Map<Class<?> ,Object> attachments;
    
    public DSRequestImpl(){
    }
    
    @Override
    public DSRequestImpl clone(){
        DSRequestImpl req=null;
        try {
            req= (DSRequestImpl) super.clone();
        } catch (CloneNotSupportedException e) {
           //ignore
        }
        return req;
    }
    
    @Override
    public DSResponse execute() throws DSCallException {
        DSResponse response = validateDSRequest();
        if(response!=null){
            return prepareReturn(response);
        }
        try {
            response=getApplication().execute(this, requestContext);
        } finally {
            if (isFreeOnExecute()) {
                this.freeResources();
                if (dsc != null)
                    dsc.freeResources();
            }
        }
        return response;
    }
    /**
     * @return
     */
    @Override
    public Application getApplication() {
        return applicationManager.findByID(getApplicationId());
    }
    /**
     * @return
     */
    @Override
    public OperationInfo getOperationInfo() {
        DataService ds = getDataService();
       if(ds==null){
           throw new DataServiceNoFoundException("No found DataService for :"+dataServiceId);
       }
       DataServiceInfo dsi=ds.getDataServiceInfo();
       OperationInfo oi= dsi.getOperationInfo(getOperationId());
       if(oi==null){
           throw new OperationNoFoundException("operation: "+getOperationId()+" not sepcified in:"+getDataServiceId());
       }
       return oi;
    }
    
    private DSResponse prepareReturn(DSResponse _dsResponse){
        if (isFreeOnExecute()) {
            freeResources();
            if (dsc != null){
                dsc.freeResources();
            }
        }
        return _dsResponse;
    }
    public boolean isFreeOnExecute() {
        if(freeOnExecute==null){
            if(getDSCall()!=null)
                return false;
            else
                return true;
        }else{
            return freeOnExecute.booleanValue();
        }
     }

     public void setFreeOnExecute(boolean freeOnExecute) {
         this.freeOnExecute = freeOnExecute;
     }
    private DSResponse validateDSRequest()  {
        if(operationId==null){
            return createResponse(Status.STATUS_VALIDATION_ERROR, "DataService(DS) request operationid must be assigned");
        }
        if (getDataServiceId() == null) {
            return createResponse(Status.STATUS_VALIDATION_ERROR, "DataService id must be assigned");
        }
        return null;
    }
    
    private DSResponse createResponse(Status status, Object... errors)  {
        DSResponse dsResponse = new DSResponseImpl(getDataService(), this);
        dsResponse.setStatus(status);
        dsResponse.setErrors(errors);
        return dsResponse;
    }
   
    @Override
    public DSCall getDSCall() {
        return dsc;
    }
   
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getDataService()
     */
    @Override
    public DataService getDataService() {
        if(dataService==null&&getDataServiceId()!=null){
            dataService=    dataServiceManager.getDataService(getDataServiceId());
            freeResourcesHandler=dataService;
        }
        return dataService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setDataService(org.solmix.datax.DataService)
     */
    @Override
    public void setDataService(DataService service) {
        if(this.dataService!=null){
            this.dataService.freeResources();
        }
        this.dataService=service;
        this.dataServiceId=dataService.getId();
        

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setDSCall(org.solmix.datax.call.DSCall)
     */
    @Override
    public void setDSCall(DSCall call) {
        this.dsc = call;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#isValidated()
     */
    @Override
    public boolean isValidated() {
        return validated;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getDataServiceId()
     */
    @Override
    public String getDataServiceId() {
        if (this.dataServiceId == null && this.dataService != null) {
            this.dataServiceId = dataService.getId();
        }
        if (this.dataServiceId == null && operationId != null) {
          
            this.dataServiceId=getDataServiceIdFormOperationId(this.operationId);
        }
        return dataServiceId;
    }
    
    private String getDataServiceIdFormOperationId(String operationId){
        int index = operationId.lastIndexOf(".");
        if (index != -1) {
            return operationId.substring(0,index);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#registerFreeResourcesHandler(org.solmix.datax.FreeResourcesHandler)
     */
    @Override
    public void registerFreeResourcesHandler(FreeResourcesHandler handler) {
        freeResourcesHandler = handler;
    }




    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getOperationId()
     */
    @Override
    public String getOperationId() {
        if(operationId==null){
            throw new IllegalStateException("operation id must be sepcified");
        }
        if(operationId.indexOf(".")!=-1){
            return operationId;
        }else{
            return new StringBuilder().append(getDataServiceId()).append(".").append(operationId).toString();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getRequestContext()
     */
    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }
    
    @Override
    public void setRequestContext(RequestContext rc){
        this.requestContext=rc;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setRequestStarted(boolean)
     */
    @Override
    public void setRequestStarted(boolean started) {
        this.requestStarted = started;
    }

    @Override
    public boolean isRequestStarted() {
        return requestStarted;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setValidated(boolean)
     */
    @Override
    public void setValidated(boolean validate) {
        this.validated=validate;
    }



    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#freeResources()
     */
    @Override
    public void freeResources() {
        if (freeResourcesHandler != null)
            freeResourcesHandler.freeResources();
        dataService = null;
    }
    
    @Override
    public String getApplicationId() {
        return appId;
    }
    
    @Override
    public void setApplicationId(String appId) {
        this.appId = appId;
    }

    @Override
    public boolean setOperationId(String operationId) {
        if (operationId == null) {
            return false;
        } else if (!operationId.equals(this.operationId)) {
            this.operationId = operationId;
            if (this.dataServiceId != null && !this.dataServiceId.equals(getDataServiceIdFormOperationId(operationId))) {
                this.dataServiceId = null;
                this.dataService = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public Object getRawValues() {
        return rawValues;
    }

    @Override
    public void setRawValues(Object rawValues) {
        this.rawValues = rawValues;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getValues()
     */
    
    @Override
    public Map<String, Object> getValues() {
        Object values = getRawValues();
        Map<String, Object>  result= getValuesInternal(values);
        if(result==null){
            result  = new HashMap<String, Object>();
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> getValuesInternal(Object values) {
        if (values instanceof List<?>) {
            List<?> l = (List<?>) values;
            if (l.size() == 0)
                return null;
            if (l.get(0) instanceof Map<?,?>) {
                if (l.size() == 1) {
                    return (Map<String, Object>) l.get(0);
                } else {
                    LOG.warn("getValues() called on dsRequest containing multiple sets of values, returning first in list.");
                    return (Map<String, Object>) l.get(0);
                }
            } else {
                LOG.debug("getValues() called on dsRequest,and the values is not the List of map.ignore this value.");
                return null;
            }
        } else if (values instanceof Map<?, ?>) {
            return (Map<String, Object>) values;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getValueSets()
     */
    @Override
    public List<?> getValueSets() {
        return DataUtils.makeListIfSingle(getRawValues());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#isCanJoinTransaction()
     */
    @Override
    public Boolean isCanJoinTransaction() {
        return joinTransaction;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setCanJoinTransaction(java.lang.Boolean)
     */
    @Override
    public void setCanJoinTransaction(Boolean canJoinTransaction) {
        if(requestStarted){
            throw new TransactionException("Request processing has started;  join transactions setting cannot be changed");
        }else{
            this.joinTransaction=canJoinTransaction;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setPartsOfTransaction(boolean)
     */
    @Override
    public void setPartsOfTransaction(boolean partsOfTransaction) {
       this.partsOfTransaction=partsOfTransaction;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#isPartsOfTransaction()
     */
    @Override
    public boolean isPartsOfTransaction() {
        return partsOfTransaction;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#isInvoked()
     */
    @Override
    public boolean isInvoked() {
        return invoked;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setInvoked(boolean)
     */
    @Override
    public void setInvoked(boolean invoked) {
       this.invoked=invoked;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        if(attributes!=null){
           return attributes.get(name);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        if(name==null){
            return;
        }
        if(attributes==null){
                attributes=new HashMap<String, Object>();
        }
        if(value==null){
            attributes.remove(name);
        }else{
            attributes.put(name, value);
        }
        
    }

    
    public DataServiceManager getDataServiceManager() {
        return dataServiceManager;
    }

    
    public void setDataServiceManager(DataServiceManager dataServiceManager) {
        this.dataServiceManager = dataServiceManager;
    }

    
    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

   
    @Override
    public <T> void addAttachment(Class<T> classKey, T instance) {
        if(instance==null||classKey==null){
            return;
        }
        if(attachments==null){
            attachments=new HashMap<Class<?>, Object>();
        }
        attachments.put(classKey, instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttachment(Class<T> classKey) {
        if (attachments != null) {
            return (T) attachments.get(classKey);
        }
        return null;
    }

}
