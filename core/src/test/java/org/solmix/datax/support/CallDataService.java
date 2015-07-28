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

import org.solmix.datax.RequestContext;
import org.solmix.datax.annotation.Param;
import org.solmix.datax.service.MockDataService;



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
    
    public Map<String,Object> fetchWithParams(@Param(expression="") String name,Map key){
        Map<String,Object> res = new HashMap<String, Object>();
        res.put(name, key);
        return res;
    }
    
    public void add1(@Param String name){
        
    }
}
