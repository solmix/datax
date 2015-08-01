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
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.validation.ErrorMessage;
import org.solmix.datax.validation.ErrorReport;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月1日
 */

public class ValidatorTest
{
    Container c;

    @Before
    public void setup() {
        c = ContainerFactory.getDefaultContainer(true);
        Assert.assertNotNull(c);
    }
    @Test
    public void testDefaultFail() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Map<String,Object> values= new LinkedHashMap<String,Object>();
//        values.put("text", null);
        values.put("boolean", "asd");
        values.put("integer", "1.2");
        values.put("float", "0.12a");
        values.put("date", "2012sd21-2a");
        values.put("time", "12:22:01 122a");
        values.put("datetime", "2012-21-2 12:22:01 122a");
        values.put("sequence", "aaa");
        values.put("intEnum", "aaa");
        values.put("enum", "ddd");
       
       
        add.setOperationId("com.validate.default.add");
        add.setRawValues(values);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_VALIDATION_ERROR,addres.getStatus());
            Object[] errors=addres.getErrors();
            Assert.assertNotNull(errors);
            Assert.assertEquals(1, errors.length);
            ErrorReport re = (ErrorReport)errors[0];
           ErrorMessage fl= (ErrorMessage) re.get("float");
           assertTrue(fl.toString().indexOf("arememts:[0.12a]")!=-1);
           
           ErrorMessage f2= (ErrorMessage) re.get("date");
           assertTrue(f2.toString().indexOf("arememts:[2012sd21-2a]")!=-1);
           for(String key:values.keySet()){
               assertNotNull(key+" is not validate failed",re.get(key));
           }
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
    @Test
    public void testDefaultScucess() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Map<String,Object> values= new LinkedHashMap<String,Object>();
        values.put("text", "aaa");
        values.put("boolean", "true");
        values.put("integer", "1");
        values.put("float", "0.12");
        values.put("date", "2012-21-2");
        values.put("time", "12:22:01");
        values.put("datetime", "2012-21-2 12:22:01");
        values.put("datetime2", "2012-21-02 12:22:01");
        values.put("sequence", "1");
        values.put("intEnum", "2");
        values.put("enum", "bbb");
       
       
        add.setOperationId("com.validate.default.add");
        add.setRawValues(values);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_SUCCESS,addres.getStatus());
           
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
    @Test
    public void testValidateScucess() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Map<String,Object> values= new LinkedHashMap<String,Object>();
        values.put("text", "aaa");
        values.put("boolean", "true");
        add.setOperationId("com.validate.validator.add");
        add.setRawValues(values);
        try {
            DSResponse addres= add.execute();
            Assert.assertEquals(Status.STATUS_SUCCESS,addres.getStatus());
           
        } catch (DSCallException e) {
            Assert.fail(e.getMessage());
        }
    }
}
