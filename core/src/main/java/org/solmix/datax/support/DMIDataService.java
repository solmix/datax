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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.application.Application;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.InvokerInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.exception.InvokerException;

/**
 * Direct Method Invoke DataService.
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月15日
 */

public class DMIDataService
{

    private static final Logger LOG = LoggerFactory.getLogger(DMIDataService.class.getName());

    protected final Container container;

    protected final DSRequest dsRequest;

    protected final Application application;

    public DMIDataService(Container container, DSRequest dsRequest, Application application)
    {
        this.container = container;
        this.dsRequest = dsRequest;
        this.application = application;
    }

    public static DSResponse execute(final Container container, final DSRequest dsRequest) throws DSCallException {
        return execute(container, dsRequest, dsRequest.getApplication());
    }

    public static DSResponse execute(Container container, DSRequest dsRequest, Application application) throws DSCallException {

        return new DMIDataService(container, dsRequest, application).execute();

    }

    /**
     * @return
     */
    public DSResponse execute() {
        dsRequest.setInvoked(true);
        DataServiceInfo ds = dsRequest.getDataService().getDataServiceInfo();
        OperationInfo oi= dsRequest.getOperationInfo();
        if(oi.getInvoker()==null){
            return null;
        }
        InvokerInfo ino = oi.getInvoker();
        String methodName= ino.getMethodName();
        if(methodName==null){
            methodName=oi.getLocalId();
        }
        if(DataUtils.isNullOrEmpty(methodName)){
            throw new InvokerException("Invoker method is null");
        }
        Class<?> serviceClass = ino.getClazz();
        String serviceName= ino.getName();
        if(serviceClass==null){
            serviceClass = ds.getServiceClass();
            serviceName=ds.getServiceName();
        }
        if(serviceClass==null){
            throw new IllegalArgumentException("Invoker service class is null");
        }
        InvokerObject invoker = new InvokerObject(container,dsRequest, ino, serviceClass, serviceName,methodName);
        Object result=null;
        DSResponse dsResponse = null;
        result= invoker.invoke();
        if (result != null && DSResponse.class.isAssignableFrom(result.getClass())) {
            dsResponse = DSResponse.class.cast(result);
        } else {
            dsResponse = new DSResponseImpl(dsRequest, Status.STATUS_SUCCESS);
            dsResponse.setRawData(result);
        }
        return dsResponse;
    }
}
