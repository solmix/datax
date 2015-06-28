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
package org.solmix.datax.model;

import java.util.List;

import javax.annotation.Resource;

import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.solmix.runtime.resource.ResourceManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月26日
 */

public class TestDataServiceInfo extends DataServiceInfo
{

    /**
     * @param id
     * @param fields
     */
    public TestDataServiceInfo(String id, List<FieldInfo> fields)
    {
        super(id);
    }

    public static class Parser extends BaseXmlNodeParser<TestDataServiceInfo>{

        @Resource
        private ResourceManager resourceManager;
        
        
        public ResourceManager getResourceManager() {
            return resourceManager;
        }

        @Override
        public TestDataServiceInfo parse(XMLNode node,XmlParserContext context){
            return new TestDataServiceInfo(null,null);
        }
           
       }
}
