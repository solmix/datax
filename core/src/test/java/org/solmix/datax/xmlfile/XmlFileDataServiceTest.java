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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.service.MockDataService;
import org.solmix.datax.support.DefaultDataServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月4日
 */

public class XmlFileDataServiceTest
{
    DefaultDataServiceManager dsm;

    Container c;

    @Before
    public void setup() {
        System.out.println(new MonitorServiceImpl().getMonitorInfo().getUsedMemory());
        c = ContainerFactory.getDefaultContainer(true);
        dsm = c.getExtension(DefaultDataServiceManager.class);
        Assert.assertNotNull(dsm);
    }
    
    @Test
    public void test(){
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/xmlfile.xml");
        DSRequest fetch=dsm.createDSRequest();
        fetch.setOperationId("com.call.file.fetch");
        
        
        try {
           DSResponse res= fetch.execute();
           assertEquals(4, res.getResultList(Map.class).size());
        } catch (Exception e) {
            e.printStackTrace();
       }
    }

    
    @After
    public void tearDown() {
        if (c != null) {
            c.close();
        }
    }
}
