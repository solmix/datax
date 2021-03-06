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

import java.util.Collections;
import java.util.Map;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceFactory;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.builder.BaseXmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;
import org.solmix.runtime.event.EventService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月2日
 */
@Extension( BaseDataServiceFactory.BASE)
@ThreadSafe
public class BaseDataServiceFactory implements DataServiceFactory,ContainerAware
{

    public static final String BASE = "base";

    private  XmlNodeParserProvider provider;

    private Object eventServicelock = new Object();

    private EventService eventService;

    private Container container;
    
    private boolean eventEnable = false;

    public BaseDataServiceFactory()
    {
    }
    
    /**
     * 前一个版本的Schema派生功能，
     * 这里通过org.solmix.datax.support.DefaultDataServiceManager.scanClassDefinition(String, DefaultRepository)的方式实现。
     */

    @Override
    public DataService instance(DataServiceInfo info, Map<String, Object> properties) {
        DataTypeMap prop = new DataTypeMap(Collections.unmodifiableMap(properties));
        BaseDataService bds = instanceBaseDataService(info, container, prop);
        if(isEventEnable()) {
        	bds.setEventService(getEventService());
        }
        return bds;
    }
    
    protected BaseDataService instanceBaseDataService(DataServiceInfo info,Container container,DataTypeMap prop){
        return new BaseDataService(info, container, prop);
    }
    @Override
    public EventService getEventService() {
        if (eventService == null) {
            synchronized (eventServicelock) {
                eventService = container.getExtension(EventService.class);
            }
        }
        return eventService;
    }

    @Override
    public XmlNodeParserProvider getXmlNodeParserProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.ContainerAware#setContainer(org.solmix.runtime.Container)
     */
    @Override
    public void setContainer(Container container) {
        this.container=container;
        provider = new BaseXmlNodeParserProvider(container);
    }

	public boolean isEventEnable() {
		return eventEnable;
	}

	public void setEventEnable(boolean eventEnable) {
		this.eventEnable = eventEnable;
	}
    
    

}
