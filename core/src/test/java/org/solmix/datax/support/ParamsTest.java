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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月26日
 */

public class ParamsTest
{
    Container c;
    private DefaultDataServiceManager dsm;

    @Before
    public void setup() {
        c = ContainerFactory.getDefaultContainer(true);
        Assert.assertNotNull(c);
        dsm = c.getExtension(DefaultDataServiceManager.class);
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/params.xml");
       
        dsm.init();
    }
    
    @Test
    public void test() throws DSCallException {
//        DSRequest fetch = createDSRequest("com.call.ds.fetch");
//       DSResponse res= fetch.execute();
//       Assert.assertNotNull(res);
    }
    
    private DSRequest createDSRequest(String id){
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest req= dsm.createDSRequest();
        req.setOperationId(id);
        return req;
    }
}
