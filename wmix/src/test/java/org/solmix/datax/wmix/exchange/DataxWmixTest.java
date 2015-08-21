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

package org.solmix.datax.wmix.exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.datax.wmix.AbstractWmixTests;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;
import org.solmix.runtime.threadpool.DefaultThreadPool;
import org.solmix.runtime.threadpool.DefaultThreadPoolManager;

import com.meterware.servletunit.InvocationContext;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月14日 / / /
 */

public class DataxWmixTest extends AbstractWmixTests
{

    @Test
    public void test() throws Exception {

        prepareServlet("datax");
        Assert.assertNotNull(component);
        invokePost("/datax/datax/1?a=b&d=e", getRequest());
        controller.service(request, response);
        response.getOutputStream();
    }
    private int count=0;
    @Test
    public void testAsync() throws Exception {
        final MonitorServiceImpl mi = new MonitorServiceImpl();
        MonitorInfo old = mi.getMonitorInfo();
        final long free = old.getUsedMemory();
        DefaultThreadPoolManager manager = new DefaultThreadPoolManager();
       final DefaultThreadPool pool = (DefaultThreadPool) manager.getDefaultThreadPool();
        pool.setMaxThreads(100);
        pool.setQueueSize(1000);
        pool.setDequeueTimeout(10000);
        prepareServlet("datax");
        int size = 100;
        final CountDownLatch latch = new CountDownLatch(size);
        List<Runnable> runs = new ArrayList<Runnable>();
        for (int i = 0; i < size; i++) {
            final String str = getRequest();

            Runnable run = new Runnable() {

                @Override
                public void run() {
                    try {
                        InvocationContext ipc = invokePostContext("/datax/datax/1?a=b&d=e", str);
                        controller.service(ipc.getRequest(), ipc.getResponse());
                        // System.out.println("Thread"+Thread.currentThread().getId()+":"+latch.getCount());
                        if (latch.getCount() % 1000 == 0) {
                            MonitorInfo bb = mi.getMonitorInfo();
                            System.out.println((bb.getUsedMemory()) / 1000 + "KB");
                            System.out.println(pool.getQueueSize());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            };
            runs.add(run);
        }
        MonitorInfo bb = mi.getMonitorInfo();
        System.out.println((bb.getUsedMemory() - free) / 1000 + "KB");
        long start = System.currentTimeMillis();

        for (Runnable run : runs) {
            pool.execute(run);
        }
        latch.await();
        System.out.println((System.currentTimeMillis() - start));
       /*while(true){
           final String str = getRequest();

           Runnable run = new Runnable() {

               @Override
               public void run() {
                   try {
                       InvocationContext ipc = invokePostContext("/datax/datax/1?a=b&d=e", str);
                       controller.service(ipc.getRequest(), ipc.getResponse());
                       // System.out.println("Thread"+Thread.currentThread().getId()+":"+latch.getCount());
                       count++;
                       if (count % 1000 == 0) {
                           MonitorInfo bb = mi.getMonitorInfo();
                           System.out.println((bb.getUsedMemory()) / 1000 + "KB");
                           System.out.println(pool.getQueueSize());
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   } finally {
                       latch.countDown();
                   }
               }
           };
           pool.execute(run);
           Thread.currentThread().sleep(100);
       }*/
    }

    @Test
    public void testBatch() throws Exception {
        prepareServlet("datax");
        long start = System.currentTimeMillis();
        MonitorServiceImpl mi = new MonitorServiceImpl();
        MonitorInfo old = mi.getMonitorInfo();
        long free = old.getFreeMemory();
        for (int i = 0; i < 1000; i++) {
            invokePost("/datax/datax/1?a=b&d=e", getRequest());
            controller.service(request, response);
        }
        System.out.println((System.currentTimeMillis() - start));
        MonitorInfo bb = mi.getMonitorInfo();
        System.out.println((bb.getFreeMemory() - free) / 1000 + "KB");
    }

    private String getRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"transactionNum\":2, ");
        sb.append("\"operations\":{");
        sb.append("\"elem\":[");
        sb.append("{");
        sb.append("\"appID\":\"builtinApplication\", ");
        sb.append("\"componentId\":\"ftdrmr_summary_tree\", ");
        sb.append("\"operationId\":\"rmr$RmrSummary_fetch\", ");
        sb.append("\"textMatchStyle\":\"exact\", ");
        sb.append("\"requestId\":\"rmr$RmrSummary_request1\",");
        sb.append("\"action\":\"datax.auth.User.fetch\",");
        sb.append("\"operationType\":\"fetch\", ");
        sb.append("\"values\":{");
        sb.append("\"date\":\"2015-08-16\",");
        sb.append("\"VIEW\":\"4\"");
        sb.append("}");
        sb.append("}");
        sb.append("]");
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}
