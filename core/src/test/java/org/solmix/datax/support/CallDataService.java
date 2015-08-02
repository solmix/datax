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

import javax.annotation.Resource;

import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;
import org.solmix.datax.annotation.Param;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.service.MockDataService;
import org.solmix.datax.support.DSRequestCallTest.Bean;
import org.solmix.runtime.Container;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月18日
 */

public class CallDataService
{

    @Resource
    private RequestContext context;
    @Resource
    private MockDataService mockDataService;
    public String fetch1(){
        return "hello";
    }
    
    public MockDataService getRequestContext(){
        return context.get(MockDataService.class);
    }
    public MockDataService getInjectResource(){
        return mockDataService;
    }
    
    public Map<String,Object> fetchWithParams(
        @Param(expression="$values.text") String name,
        @Param(expression="$values") Bean key,
        @Param(expression="$dsrequest") Object request,
        @Param(expression="$ds.id") String id,
        @Param(expression="$container") Object c2,
        @Param(expression="$xxxx") Object mock,
        Container c1,
        DSCall dscall,
        DataService ds,
        DSRequest dsrequest){
        Map<String,Object> res = new HashMap<String, Object>();
        res.put(name, key);
        res.put("request", request);
        res.put("dsrequest", dsrequest);
        res.put("dsc", dscall);
        res.put("ds", ds);
        res.put("dsid", id);
        res.put("c1", c1);
        res.put("c2", c2);
        res.put("mock", mock);
        return res;
    }
    
    public void add1(@Param String name){
        
    }
    
    
    public boolean addforv(DSRequest req){
        return true;
    }
    
    public String addfort(DSRequest req){
        return req.getValues().get("text").toString();
    }
}
