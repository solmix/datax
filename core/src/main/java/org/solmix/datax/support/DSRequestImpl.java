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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSCall;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.FreeResourcesHandler;
import org.solmix.datax.RequestContext;
import org.solmix.datax.application.Application;
import org.solmix.datax.application.ApplicationManager;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class DSRequestImpl extends PagedBean implements DSRequest
{

    private static final Logger LOG= LoggerFactory.getLogger(DSRequestImpl.class);
    
    private String dataServiceId;
    
    private String appId;

    private DataService dataService;

    private  String operationId;

    private boolean validated = false;

    FreeResourcesHandler freeResourcesHandler;

    RequestContext requestContext;

    boolean requestStarted;

    private DSCall dsc;
    
    private Boolean freeOnExecute ;
    
    private Object rawValues;

    private Object rawOldValues;
    
    @Resource
    private DataServiceManager dataServiceManager;
    
    @Resource
    private ApplicationManager applicationManager;
    
    public DSRequestImpl(){
    }
    
    @Override
    public DSResponse execute() throws DSCallException {
        DSResponse response = validateDSRequest();
        if(response!=null){
            return prepareReturn(response);
        }
        try {
           
            OperationInfo oi = getOperationInfo();
            /*if(oi.getInvoker()!=null){
                response = DMIDataService.execute(this, dsc, requestContext);
            }*/
            response=getApplication().execute(this, requestContext);
        } finally {
            if (isFreeOnExecute()) {
                this.freeResources();
                if (dsc != null)
                    dsc.freeDataSources();
            }
        }
        return response;
    }
    /**
     * @return
     */
    @Override
    public Application getApplication() {
        return applicationManager.findByID(getAppId());
    }
    /**
     * @return
     */
    @Override
    public OperationInfo getOperationInfo() {
       DataServiceInfo dsi= getDataService().getDataServiceInfo();
       return dsi.getOperationInfo(getOperationId());
    }
    
    private DSResponse prepareReturn(DSResponse _dsResponse){
        if (isFreeOnExecute()) {
            freeResources();
            if (dsc != null)
                dsc.freeDataSources();
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
     * @see org.solmix.datax.DSRequest#setDSCall(org.solmix.datax.DSCall)
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
            int index = operationId.lastIndexOf(".");
            if (index != -1) {
                return operationId.substring(0,index);
            }
        }
        return dataServiceId;
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
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId=operationId;
    }

    
    public Object getRawValues() {
        return rawValues;
    }

    
    public void setRawValues(Object rawValues) {
        this.rawValues = rawValues;
    }

    
    public Object getRawOldValues() {
        return rawOldValues;
    }

    
    public void setRawOldValues(Object rawOldValues) {
        this.rawOldValues = rawOldValues;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getValues()
     */
    
    @Override
    public Map<String, Object> getValues() {
        Object values = getRawValues();
        return getValuesInternal(values);
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
     * @see org.solmix.datax.DSRequest#getOldValues()
     */
    @Override
    public Map<String, Object> getOldValues() {
        Object values = getOldValues();
        return getValuesInternal(values);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getOldValueSets()
     */
    @Override
    public List<?> getOldValueSets() {
        return DataUtils.makeListIfSingle(getRawOldValues());
    }

}