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

package org.solmix.datax.wmix;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.wmix.interceptor.SgtInInterceptor;
import org.solmix.datax.wmix.interceptor.SgtOutInterceptor;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Service;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.interceptor.support.MessageSenderInterceptor;
import org.solmix.exchange.model.ArgumentInfo;
import org.solmix.exchange.processor.InFaultChainProcessor;
import org.solmix.exchange.processor.OutFaultChainProcessor;
import org.solmix.runtime.Container;
import org.solmix.wmix.exchange.AbstractWmixEndpoint;
import org.solmix.wmix.exchange.WmixMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月13日
 */

public class DataxEndpoint extends AbstractWmixEndpoint implements Endpoint
{

    private static final Logger LOG = LoggerFactory.getLogger(DataxEndpoint.class);

    private static final long serialVersionUID = 7621213021932655937L;

    private DataxServiceFactory serviceFactory;
    
    private ArgumentInfo argumentInfo;
    
    private DataServiceManager dataServiceManager;
    
    public DataxEndpoint(){
        serviceFactory= new DataxServiceFactory();
    }
    @Override
    protected void prepareInterceptors() {
        setInFaultProcessor(new InFaultChainProcessor(container, getPhasePolicy()));
        setOutFaultProcessor(new OutFaultChainProcessor(container,  getPhasePolicy()));
        getOutInterceptors().add(new MessageSenderInterceptor());
        getOutFaultInterceptors().add(new MessageSenderInterceptor());
        getInInterceptors().add(new SgtInInterceptor());
        getOutInterceptors().add(new SgtOutInterceptor());
    }
    
    @Override
    protected void setContainer(Container container) {
        super.setContainer(container);
        argumentInfo = new ArgumentInfo();
        argumentInfo.setTypeClass(Map.class);
        dataServiceManager=container.getExtension(DataServiceManager.class);
        Assert.assertNotNull(dataServiceManager,"NO found DataServiceManager");
        DataProcessor dataProcessor = container.getExtension(DataProcessor.class);
        serviceFactory.setDataProcessor(dataProcessor);
    }

    @Override
    public void service(WmixMessage message) throws Exception {
        message.put(ArgumentInfo.class, argumentInfo);
        message.getExchange().put(DataServiceManager.class, dataServiceManager);
        message.getExchange().put(Transporter.class, getTransporter());
        getTransporter().invoke(message);
    }

   
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Service createService() {
        
        return serviceFactory.create();
    }
    
}
