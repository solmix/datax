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
package org.solmix.datax.wmix.exchange;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.RequestContext;
import org.solmix.datax.annotation.Param;
import org.solmix.datax.call.DSCall;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月18日
 */

public class TestDataService
{

    @Resource
    private RequestContext context;
    public String fetch1(){
        return "hello";
    }
    
    public void add1(@Param String name){
        
    }
    
    
    public boolean addforv(DSRequest req){
        return true;
    }
    
    
    
    public String addfort(DSRequest req){
        return req.getValues().get("text").toString();
    }
    
    public Map<String,Object> batchFetch(){
        Map<String,Object> map= new HashMap<String, Object>();
        map.put("step1", "batchFetch");
        return map;
    }
    
    public Map<String,Object> batchAdd(DSCall dsc){
        DSResponse last=dsc.getResponseByOperationLocalId("batchFetch");
        Map<String,Object> map=last.getSingleResult(Map.class);
        map.put("step2", "batchAdd");
        return map;
    }
    public void exception() throws Exception{
        throw new Exception();
    }
}
