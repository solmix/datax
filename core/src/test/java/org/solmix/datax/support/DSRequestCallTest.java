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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.validation.ErrorReport;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月18日
 */

public class DSRequestCallTest
{

    Container c;

    @Before
    public void setup() {
        c = ContainerFactory.getDefaultContainer(true);
        Assert.assertNotNull(c);
    }
    
    @Test
    public void test() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest fetch=dsm.createDSRequest();
        fetch.setOperationId("com.call.ds.fetch");
        
      
        try {
           DSResponse res= fetch.execute();
           res.getStatus();
          
        } catch (DSCallException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testValidateRequest() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Map<String,Object> values= new HashMap<String,Object>();
        values.put("text", "aaa");
        values.put("float", "0.12a");
        values.put("date", "2012sd21-2a");
        add.setOperationId("com.call.ds.add");
        add.setRawValues(values);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_VALIDATION_ERROR,addres.getStatus());
            Object[] errors=addres.getErrors();
            Assert.assertNotNull(errors);
            Assert.assertEquals(1, errors.length);
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
    @Test
    public void testValidateRequestWithBean() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Bean b = new Bean();
        b.setText("aaa");
        b.setFloat2("0.12a");
        b.setDate("2012sd21-2a");
        add.setOperationId("com.call.ds.add");
        add.setRawValues(b);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_VALIDATION_ERROR,addres.getStatus());
            Object[] errors=addres.getErrors();
            Assert.assertNotNull(errors);
            
            Assert.assertEquals(1, errors.length);
           ErrorReport er =(ErrorReport) errors[0];
           Assert.assertNotNull(er.getErrors("date"));
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
   public class Bean{
        private String text;
        private String date;
        private String float2;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public String getFloat2() {
            return float2;
        }
        
        public void setFloat2(String float2) {
            this.float2 = float2;
        }
        
    }
}
