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

import javax.annotation.Resource;

import org.solmix.datax.DSCall;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.FreeResourcesHandler;
import org.solmix.datax.RequestContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class DSRequestImpl extends PagedBean implements DSRequest
{

    private String dataServiceId;
    private DataService dataService;
    
    private String operationId;
    
   
    private boolean validated = false;
    FreeResourcesHandler freeResourcesHandler;
    
    RequestContext requestContext;
    boolean requestStarted;
    private DSCall dsc;
    @Resource
    private DataServiceManager dataServiceManager;
    @Override
    public DSResponse execute() throws DSCallException {
        DSResponse response = validateDSRequest();
        return null;
    }
    private DSResponse validateDSRequest()  {
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
        if(this.dataServiceId==null&&this.dataService!=null){
            this.dataServiceId=dataService.getId();
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
     * @see org.solmix.datax.DSRequest#isInvoked()
     */
    @Override
    public boolean isInvoked() {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSRequest#getOperationId()
     */
    @Override
    public String getOperationId() {
        return operationId;
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

}
