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
package org.solmix.datax.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.MockDataServiceInfo;
import org.solmix.datax.model.MockDataServiceInfo.Parser;
import org.solmix.datax.repository.builder.BaseXmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlNodeParser;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月26日
 */

public class XmlNodeParserProviderTest
{
    static Container container;
    @BeforeClass
    public static void setup(){
        container= ContainerFactory.getDefaultContainer(true);
    }
    @AfterClass
    public static void tearDown(){
        if(container!=null)
            container.close();
    }
    @Test
    public void test(){
        BaseXmlNodeParserProvider provider = new BaseXmlNodeParserProvider(container){
            protected void config() {
                bind("/datax/services/service", MockDataServiceInfo.Parser.class);
             }
        };
        XmlNodeParser<DataServiceInfo> p= provider.getXmlNodeParser("/datax/services/service", DataServiceInfo.class);
        assertNotNull(p);
        assertTrue((p.getClass().isAssignableFrom(Parser.class)));
        assertNotNull(Parser.class.cast(p).getResourceManager());
        assertTrue(p.parse(null, null) instanceof DataServiceInfo );
        assertTrue(p.parse(null, null) instanceof MockDataServiceInfo );
    }

}
