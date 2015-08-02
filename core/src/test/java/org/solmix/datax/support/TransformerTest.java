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

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.solmix.commons.util.DateUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.service.MockDataService;
import org.solmix.datax.validation.ErrorMessage;
import org.solmix.datax.validation.ErrorReport;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月1日
 */

public class TransformerTest
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
    public void testDefaultScucess() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        MappedRequestContext mrc = new MappedRequestContext();
        MockDataService mock = new MockDataService();
        mrc.put("dateUtil",new DateUtils());
        add.setRequestContext(mrc);
        Map<String,Object> values= new LinkedHashMap<String,Object>();
        values.put("text", "aaa");
        values.put("boolean", "true");
        values.put("integer", "1");
        values.put("date", "2012-21-2");
        values.put("time", "12:22:01");
        values.put("datetime", "2012-21-2 12:22:01");
        values.put("datetime2", "2012-21-02 12:22:01");
        values.put("sequence", "1");
        values.put("intEnum", "2");
        values.put("enum", "bbb");
       
       
        add.setOperationId("com.transformer.default.add");
        add.setRawValues(values);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_SUCCESS,addres.getStatus());
          String res= addres.getSingleResult(String.class);
          assertEquals(res, "aaa-transformRequest-transformResponse");
          @SuppressWarnings("unchecked")
        Map<String,Object> request=(Map<String, Object>) add.getRawValues();
          assertEquals("5", request.get("integer").toString());
          assertEquals("5.1", request.get("float").toString());
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
  
}
