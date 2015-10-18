/*
 * Copyright 2015 The Solmix Project
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
package org.solmix.datax.support.params;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年10月15日
 */

public class ParamsTest extends Assert
{
    Container c;

    @Before
    public void setup() {
        c = ContainerFactory.getDefaultContainer(true);
        Assert.assertNotNull(c);
    }
    @After
    public void tearDown(){
        if(c!=null){
            c.close();
        }
    }
    
    @Test
    public void test() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest fetch=dsm.createDSRequest();
        fetch.setOperationId("com.params.default.fetch");
        
      
        try {
           DSResponse res= fetch.execute();
           //测试当输入参数为null时，params配置生效
           assertEquals(fetch.getValues().get("integer"), "5");
        } catch (Exception e) {
            e.printStackTrace();
       }
    }
}
