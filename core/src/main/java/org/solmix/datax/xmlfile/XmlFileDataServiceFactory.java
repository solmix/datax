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
package org.solmix.datax.xmlfile;

import java.util.Collections;
import java.util.Map;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceFactory;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.support.BaseDataServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.event.EventService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月4日
 */
@Extension(name = XmlFileDataServiceFactory.XMLFILE)
@ThreadSafe
public class XmlFileDataServiceFactory extends BaseDataServiceFactory
{
    public static final String XMLFILE = "xmlfile";
    
    /**
     * @param container
     */
    public XmlFileDataServiceFactory(Container container)
    {
        super(container);
    }
    @Override
    protected BaseDataService instanceBaseDataService(DataServiceInfo info,Container container,DataTypeMap prop){
        return  new XmlFileDataService(info, container, prop);
    }
  
}
