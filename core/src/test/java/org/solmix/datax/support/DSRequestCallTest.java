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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataServiceNoFoundException;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.application.ApplicationNotFoundException;
import org.solmix.datax.service.MockDataService;
import org.solmix.datax.validation.ErrorMessage;
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
        } catch (Exception e) {
       }
    }
    private DSRequest createDSRequest(String id){
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest req= dsm.createDSRequest();
        req.setOperationId(id);
        return req;
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
    public void testValidate() {
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequest add=dsm.createDSRequest();
        Map<String,Object> values= new LinkedHashMap<String,Object>();
        values.put("text", "aaa");
        values.put("float", "0.12a");
        values.put("date", "2012sd21-2a");
        add.setOperationId("com.validate.ds.add");
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
   @Test(expected = DataServiceNoFoundException.class)
   public void testDataServiceNoFound() throws DSCallException {
       DSRequest fetch = createDSRequest("com.call.12.aa");
       fetch.execute();
   }
   @Test(expected = OperationNoFoundException.class)
   public void testOperationidNotFound() throws DSCallException {
       DSRequest fetch = createDSRequest("com.call.ds.aa");
       fetch.execute();
   }
    @Test(expected = ApplicationNotFoundException.class)
    public void testappNotFound() throws DSCallException {
        DSRequest fetch = createDSRequest("com.call.ds.fetch");
        fetch.setApplicationId("aaa_");
        fetch.execute();
    }
    @Test
    public void testbatch() throws DSCallException{
        DSRequest custom = createDSRequest("com.call.ds2.custom");
        
        custom.execute();
    }
   @Test
   public void testInvoker() throws DSCallException{
       //lookup=new 
       DSResponse fetch1 = request("com.call.invoke.fetch_1");
       Assert.assertEquals("hello", fetch1.getRawData());
       
     //lookup=container
       DSResponse fetch2 = request("com.call.invoke.fetch_2");
       Assert.assertNotNull(fetch2.getRawData());
       Object o=c.getExtension(DataServiceManager.class).getRepositoryService();
       Assert.assertSame(o, fetch2.getRawData());
       
       //lookup=new 并注入request
       DSRequest getre = createDSRequest("com.call.invoke.getRequestContext");
       MappedRequestContext mrc = new MappedRequestContext();
       MockDataService mock = new MockDataService();
       mrc.put(MockDataService.class,mock);
       getre.setRequestContext(mrc);
       DSResponse getRequestContext= getre.execute();
       Assert.assertSame(mock, getRequestContext.getRawData());
   }
   
   @Test
   public void fetchWithParams() throws DSCallException{
       DSRequest getre = createDSRequest("com.call.invoke.fetchWithParams");
       Bean value= new Bean();
       value.setText("aaa");
       getre.setRawValues(value);
       MappedRequestContext mrc = new MappedRequestContext();
       MockDataService mock = new MockDataService();
       mrc.put(MockDataService.class,mock);
       mrc.put("xxxx", mock);
       getre.setRequestContext(mrc);
       DSResponse getRequestContext= getre.execute();
       Map<Object,Object> result=getRequestContext.getSingleRecord();
       assertEquals(result.get("aaa"), value);
       assertEquals(result.get("request"), getre);
       assertSame(result.get("dsrequest"), result.get("request"));
       assertSame(result.get("c1"), result.get("c2"));
       assertSame(result.get("dsc"), getre.getDSCall());
       assertSame(result.get("ds"), getre.getDataService());
       assertSame(result.get("mock"), mock);
       assertEquals(result.get("dsid"), getre.getDataService().getId());
   }
   
   @Test
   public void getInjectResource() throws DSCallException{
       //lookup=new 使用 requestContext resource resolver
       DSRequest getre = createDSRequest("com.call.invoke.getInjectResource");
       MappedRequestContext mrc = new MappedRequestContext();
       MockDataService mock = new MockDataService();
       mrc.put(MockDataService.class,mock);
       getre.setRequestContext(mrc);
       DSResponse getRequestContext= getre.execute();
       Assert.assertSame(mock, getRequestContext.getRawData());
   }
   
    private DSResponse request(String id) throws DSCallException {
        DSRequest custom = createDSRequest(id);
        return custom.execute();
    }
}
