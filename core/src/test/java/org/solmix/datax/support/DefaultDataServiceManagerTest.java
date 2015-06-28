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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public class DefaultDataServiceManagerTest
{

     DefaultDataServiceManager dsm;
    Container c;
    @Before
    public  void setup() {
         c = ContainerFactory.getDefaultContainer(true);
        dsm = c.getExtension(DefaultDataServiceManager.class);
        Assert.assertNotNull(dsm);
    }

//    @Test
    public void testInit() {
        dsm.init();
    }
    
    @Test
    public void testDefinitionResources() {
        dsm.addResource("classpath:META-INF/dataservice1/ds2.xml");
        dsm.setLoadDefault(false);
        dsm.init();
        DataServiceInfo dsi =dsm.getRepositoryService().getDataService("com.example.ds.aa");
        Assert.assertNotNull(dsi);
        Assert.assertEquals(3, dsi.getFields().size());
    }
    @After
    public void tearDown(){
        if(c!=null){
            c.close();
        }
    }

}
