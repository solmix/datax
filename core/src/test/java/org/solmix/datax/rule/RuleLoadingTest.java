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
package org.solmix.datax.rule;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.router.RequestToken;
import org.solmix.datax.router.RoutingResult;
import org.solmix.datax.router.support.DefaultDataServiceRouter;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.extension.AssembleBeanSupport;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月9日
 */

public class RuleLoadingTest
{
    protected Container container;
    DefaultDataServiceRouter router;
    @Test
    public void test(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "1234");
        RequestToken param= new RequestToken("com.example.datax.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db3","db4"});
    }
    @Test
    public void test2(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "11234");
        RequestToken param= new RequestToken("com.example.datax.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db1","db2"});
    }
    @Test
    public void test3(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "1234");
        RequestToken param= new RequestToken("com.example1.datax1.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db5","db6"});
    }
    @Test
    public void test4(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "11234");
        RequestToken param= new RequestToken("com.example1.datax1.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db7","db8"});
    }
    @Test
    public void test5(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "1234");
        RequestToken param= new RequestToken("com.example2.datax.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db9","db10"});
    }
    @Test
    public void test6(){
        Map<String,Object> args = new HashMap<String,Object> ();
        args.put("org_no", "11234");
        RequestToken param= new RequestToken("com.example2.datax.fetch",args);
        RoutingResult res =router.route(param);
        Object[] resources=res.getResourceIds().toArray();
        Assert.assertArrayEquals(resources, new String[]{"db11","db12"});
    }
    @Before
    public void setup() {
        container = ContainerFactory.getDefaultContainer(true);
        router = new DefaultDataServiceRouter();
        router.setConfigLocation("META-INF/rule/*.xml");
        router.setContainer(container);
        container.getExtension(AssembleBeanSupport.class).assemble(router);
        
    }

    @After
    public void tearDown() {
        if (container != null) {
            container.close();
        }
    }
}
