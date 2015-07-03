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

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;
import org.solmix.datax.DataxException;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.event.EventService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月3日
 */

public class BaseDataService implements DataService
{
    private Container container;
    private DataTypeMap properties;
    private DataServiceInfo info;
    
    private EventService eventService;
    
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
        
        
    }
    
    @Override
    public void freeResources() {

    }

    
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerType() {
        return BaseDataServiceFactory.BASE;
    }

    @Override
    public DSResponse execute(DSRequest req) throws DataxException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public EventService getEventService() {
        return eventService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
   

}
