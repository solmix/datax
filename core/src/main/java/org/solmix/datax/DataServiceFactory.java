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
package org.solmix.datax;

import java.util.Map;

import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.runtime.Extension;
import org.solmix.runtime.event.EventService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月2日
 */
@Extension
public interface DataServiceFactory
{
    /**
     * 实例DataService。
     * 
     * @param info 每个DataService的配置信息
     * @param properties 通用扩展配置信息
     * @return
     */
    DataService instance(DataServiceInfo info,Map<String, Object> properties);
    
    /**
     * DataServiceInfo信息加载实现
     * 
     * @return
     */
    XmlNodeParserProvider getXmlNodeParserProvider();

    /**
     * EventService服务实现，默认为NullEventService.
     * 
     * @return
     */
    EventService getEventService();
}
