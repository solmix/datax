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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.util.DataTools;
import org.solmix.runtime.Container;
import org.solmix.runtime.event.EventService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月3日
 */

public class BaseDataService implements DataService
{
    private static final Logger LOG = LoggerFactory.getLogger(BaseDataService.class);
    private Container container;
    private DataTypeMap properties;
    private DataServiceInfo info;
    
    private  EventService eventService;
    
    public BaseDataService(DataServiceInfo info,Container container,DataTypeMap prop)
    {
        Assert.assertNotNull(info);
        setContainer(container);
        this.info=info;
        this.properties=prop;
        init();
    }
    
    protected void setContainer(Container container) {
        Assert.assertNotNull(container);
        this.container=container;
    }
    
    protected void init(){
        if(LOG.isTraceEnabled()){
            LOG.trace((new StringBuilder()).append("Creating instance of DataSource '").append(info.getId()).append("'").toString());
        }
    }
    
    @Override
    public void freeResources() {

    }

    
    @Override
    public String getId() {
        return info.getId();
    }

    @Override
    public String getServerType() {
        return BaseDataServiceFactory.BASE;
    }

    @Override
    public DSResponse execute(DSRequest req) throws DSCallException {
        if (req == null) {
            return null;
        }
        req.registerFreeResourcesHandler(this);
        if (req.getDataService() == null && req.getDataServiceId() == null) {
            req.setDataService(this);
        }
        // 配置了invoker优先处理
        OperationInfo oi = info.getOperationInfo(req.getOperationId());
        if (oi == null) {
            throw new OperationNoFoundException("Not found operation：" + req.getOperationId() + " in datasource:" + getId());
        }
        if (oi.getInvoker() != null) {
            DSResponse response = DMIDataService.execute(req, req.getDSCall(), req.getRequestContext());
            if (response != null) {
                return response;
            }
        }
        if (oi.getBatch() != null) {
            return executeBatch(req);
        }
        OperationType type = oi.getType();
        if (type != OperationType.CUSTOM) {
            DSResponse validationFailure = validateDSRequest(req);
            if (validationFailure != null)
                return validationFailure;
        }
        req.setRequestStarted(true);
        if (DataTools.isFetch(type)) {
            return executeFetch(req);
        } else if (DataTools.isRemove(type)) {
            return executeRemove(req);
        } else if (DataTools.isUpdate(type)) {
            return executeUpdate(req);
        } else if (DataTools.isAdd(type)) {
            return executeAdd(req);
        } else {
            return executeCustomer(req);
        }
    }

  
    protected DSResponse executeCustomer(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse notSupported(DSRequest req) {
        OperationInfo oi = info.getOperationInfo(req.getOperationId());
        throw new UnsupportedOperationException(
            new StringBuilder().append("Operation type '")
            .append(oi.getType()).append("' not supported by this DataSource (")
            .append(getServerType()).append(")").toString());
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeAdd(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeUpdate(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeRemove(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeFetch(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeBatch(DSRequest req) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse validateDSRequest(DSRequest req) {
        if (req.isValidated())
            return null;
        req.setValidated(true);
        return null;
    }

    public EventService getEventService() {
        return eventService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
   
    public DataServiceInfo getDataServiceInfo(){
        return info;
    }

    
    public Container getContainer() {
        return container;
    }

    
    public DataTypeMap getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataService#getProperties(java.lang.Object)
     */
    @Override
    public Map<Object, Object> getProperties(Object data) {
        return null;
    }

}
