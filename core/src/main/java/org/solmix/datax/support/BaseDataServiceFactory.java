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

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceFactory;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.builder.BaseXmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.event.EventService;
import org.solmix.runtime.event.support.NullEventService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月2日
 */
@Extension(name=BaseDataServiceFactory.BASE)
public class BaseDataServiceFactory implements DataServiceFactory
{
    public static final String BASE="base";
    private final  XmlNodeParserProvider provider;
    

    Container container;
    public BaseDataServiceFactory(Container container){
        this.container=container;
        provider=new BaseXmlNodeParserProvider(container);
    }
    @Override
    public DataService instance(DataServiceInfo info,Map<String,Object> properties) {
        DataTypeMap prop= new DataTypeMap(Collections.unmodifiableMap(properties));
        BaseDataService bds = new BaseDataService(info,container,prop);
        bds.setEventService(getEventService());
        bds.init();
        return bds;
    }

    
    protected EventService getEventService(){
        EventService service = container.getExtension(EventService.class);
        if(service==null){
            service= new NullEventService();
        }
        return service;
    }
    
    @Override
    public XmlNodeParserProvider getXmlNodeParserProvider() {
        return provider;
    }

}
