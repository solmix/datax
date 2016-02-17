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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.datax.DSRequest;
import org.solmix.datax.model.BatchOperations;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.ForwardInfo;
import org.solmix.datax.model.InvokerInfo;
import org.solmix.datax.model.LookupType;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.MethodArgInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.ParamInfo;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.datax.model.TransformerInfo;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.service.MockDataService;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public class DefaultDataServiceManagerTest
{


    Container c;

    @Before
    public void setup() {
        System.out.println(new MonitorServiceImpl().getMonitorInfo().getUsedMemory());
        c = ContainerFactory.getThreadDefaultContainer(true);
        
    }

  
    @Test
    public void testCreateDSRequest() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        DSRequest ds= dsm.createDSRequest();
        Assert.assertNotNull(ds.getApplication());
    }
    @Test
    public void testCreateDSResponse() {
        
    }
    @Test
    public void testDefinitionResources() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/ds2.xml");
       
        DataServiceInfo dsi = dsm.getRepositoryService().getDataService("com.example.ds.aa");
        assertNotNull(dsi);
        assertEquals(3, dsi.getFields().size());
        FieldInfo fi = dsi.getFields().get(1);
        assertNotNull(fi.getValueMap());
        assertEquals(1, dsi.getFields().get(0).getValidators().size());
        assertEquals("text", fi.getValueMap().get("11"));
        DataServiceInfo aab = dsm.getRepositoryService().getDataService("com.example.ds.aab");
        OperationInfo oi = aab.getOperationInfo("#fetch");
        assertNotNull(oi);
        
        assertNotNull(oi.getInvoker());
        assertEquals(1,oi.getTransformers().size());
        ParamInfo pi = oi.getParams().get("key");
        assertNotNull(pi);
        OperationInfo ai = aab.getOperationInfo("com.example.ds.aab.add");
        assertNotNull(ai);
        BatchOperations bo = ai.getBatch();
        assertNotNull(bo);
    }
    
//    @Test(expected=BuilderException.class)
    public void testXMLBatchException() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        //配置了batch就不能配置其他的选项
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/ds3.xml");
       
        dsm.init();
        
    }
//    @Test
    public void testResolver() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/resolver/ds2.xml");
        dsm.addResource("classpath:META-INF/resolver/ds1.xml");
        
        DataServiceInfo dsi = dsm.getRepositoryService().getDataService("com.example.ds.aa");
        assertNotNull(dsi);
        assertEquals(3, dsi.getFields().size());
        FieldInfo fi = dsi.getFields().get(1);
        assertNotNull(fi.getValueMap());
        assertEquals(1, dsi.getFields().get(2).getValidators().size());
        assertEquals("text", fi.getValueMap().get("11"));
    }
    @Test
    public void testLoading() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        MonitorServiceImpl ms = new MonitorServiceImpl();
        MonitorInfo old = new MonitorServiceImpl().getMonitorInfo();
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/load.xml");
       
        MonitorInfo last = ms.getMonitorInfo();
        System.out.println("memery used:" + (last.getUsedMemory() - old.getUsedMemory()));
        DataServiceInfo dsi = dsm.getRepositoryService().getDataService("load.ds");
        assertEquals("服务描述", dsi.getDescription());
        assertEquals(1, dsi.getFields().size());
        assertEquals(BaseDataServiceFactory.BASE, dsi.getServerType());
        assertEquals("load.ds", dsi.getId());
        assertEquals("true", dsi.getProperty("dropExtraFields"));
        assertEquals(LookupType.CONTAINER, dsi.getLookup());
        assertEquals(DataServiceInfo.SCOPE_SINGLETON, dsi.getScope());
        assertEquals(org.solmix.datax.service.MockDataService.class, dsi.getServiceClass());
        assertEquals("test", dsi.getServiceName());
        assertEquals(new String[]{"admin","check"}, dsi.getRequireRoles());
        assertEquals(new String[]{"resource:fetch","resource:add"}, dsi.getRequires());
        FieldInfo field = dsi.getFields().get(0);
        
        assertEquals("field1", field.getName());
        assertEquals(Boolean.TRUE, field.getCanEdit());
        assertEquals(Boolean.TRUE, field.getCanExport());
        assertEquals(Boolean.TRUE, field.getCanFilter());
        assertEquals(Boolean.TRUE, field.getHidden());
        assertEquals(Boolean.TRUE, field.getPrimaryKey());
        assertEquals(Boolean.TRUE, field.getRequired());
        assertEquals("yyyy-MM-dd", field.getDateFormat());
        assertEquals("字段1", field.getExportTitle());
        assertEquals("load.ds1.field", field.getForeignKey());
        assertEquals(Integer.valueOf(248000), field.getMaxFileSize());
        assertEquals("字段@", field.getTitle());
        assertEquals("1", field.getRootValue());
        assertEquals(FieldType.TEXT, field.getType());
        
        Map<String, String> map= field.getValueMap();
        assertEquals("text", map.get("11"));
        
        ValidatorInfo v= field.getValidators().get(0);
        assertEquals("load.ds.check", v.getId());
        assertEquals(LookupType.CONTAINER, v.getLookup());
        assertEquals("otherattr", v.getProperty("otherattr"));
        
        assertEquals(org.solmix.datax.service.MockValidator.class, v.getClazz());
        assertEquals(Long.valueOf(1), v.getCount());
        assertEquals("验证错误", v.getErrorMessage());
        assertEquals(Boolean.TRUE, v.getExclusive());
        assertEquals("$dataUtils.validate(aa)", v.getExpression());
        assertEquals("####00.##", v.getMask());
        assertEquals(Double.valueOf(100), v.getMax());
        assertEquals(Double.valueOf(2), v.getMin());
        assertEquals("v1", v.getName());
        assertEquals("add", v.getOperator());
        assertEquals(Double.valueOf(20.0), v.getPrecision());
        assertEquals(Boolean.TRUE, v.getServerOnly());
        assertEquals("aa", v.getSubstring());
        assertEquals("length", v.getType());
        assertEquals(Boolean.TRUE, v.getValidateOnChange());
        ValidatorInfo v2= field.getValidators().get(1);
        assertEquals(v2.getId(), v.getId());
        
        
        OperationInfo oi=dsi.getOperationInfo("#fetch");
        assertEquals(dsi.getId()+".fetch", oi.getId());
        assertEquals(Boolean.TRUE, oi.getAutoJoinTransactions());
        assertEquals(Boolean.TRUE, oi.getUsedValidatedValues());
        assertEquals("aaaa", oi.getExtension("customCriteriaFields"));
        
        //oneway extension
        assertEquals(Boolean.TRUE, oi.getOneway());
        
        List<ForwardInfo> forwards=oi.getForwards();
        assertEquals(2, forwards.size());
        ForwardInfo fi = forwards.get(0);
        assertEquals("forward", fi.getName());
        assertEquals("aa.vm", fi.getPath());
        assertEquals("velocity", fi.getScript());
        ParamInfo param =fi.getParams().get("forward1");
        Assert.assertNotNull(param);
        //关联
        OperationInfo oi2=dsi.getOperationInfo("#feth2");
        assertEquals(oi.getId(), oi2.getRefid());
        assertEquals(Boolean.TRUE,oi2.getAutoJoinTransactions());
        
       Map<String,ParamInfo> params= oi.getParams();
       assertEquals("key", params.get("key").getKey());
       assertEquals("v", params.get("key").getValue());
       assertEquals(Boolean.FALSE, params.get("key").getIsOverride());
       assertEquals(Boolean.TRUE, params.get("key2").getIsOverride());
       
       TransformerInfo ti=oi.getTransformers().get(0);
       assertEquals(org.solmix.datax.transformer.MockTransformer.class, ti.getClazz());
       assertEquals(dsi.getId()+".trans", ti.getId());
       assertEquals(LookupType.CONTAINER, ti.getLookup());
       assertEquals("trans", ti.getName());
       
       TransformerInfo ti2=oi.getTransformers().get(1);
       assertEquals(ti2.getName(), ti.getName());
       
       InvokerInfo vi= oi.getInvoker();
       assertEquals(org.solmix.datax.transformer.MockTransformer.class, vi.getClazz());
       assertEquals(LookupType.CONTAINER, vi.getLookup());
       assertEquals("invoker", vi.getName());
       assertEquals("ink", vi.getMethodName());
       //method-arg
      Map<Integer,MethodArgInfo> args= vi.getMethodArgs();
       assertNotNull(args);
       assertEquals(1, args.size());
       MethodArgInfo arg = args.get(5);
       assertNotNull(arg);
       assertEquals("5xxx", arg.getValue());
       assertEquals(5, arg.getOrder());
       assertEquals("$request", arg.getExpression());
       
       BatchOperations bi=dsi.getOperationInfo("#add").getBatch();
       assertEquals(TransactionPolicy.FROM_FIRST_CHANGE, bi.getTransactionPolicy());
       assertEquals(MergedType.WRAPPED, bi.getMergedType());
       OperationInfo afeth= bi.getOperations().get(0);
       assertNotNull(afeth.getBatch());
       
       List<ForwardInfo> bforwards=bi.getForwards();
       assertEquals(1, bforwards.size());
       
       OperationInfo bfeth= bi.getOperations().get(1);
       assertEquals(bfeth.getRefid(), oi.getId());
       
    }
    @Test
    public void testContainerResolver() {
        DefaultDataServiceManager dsm = c.getExtension(DefaultDataServiceManager.class);
        dsm.setLoadDefault(false);
        dsm.addResource("classpath:META-INF/dataservice1/load.xml");
        
        ResourceManager cbp= c.getExtension(ResourceManager.class);
        
        DataServiceInfo dsi=  cbp.resolveResource("load.ds", DataServiceInfo.class);
        assertNotNull(dsi);
        ResourceInjector injector = new ResourceInjector(cbp);
        MockDataService tds = new MockDataService();
        injector.inject(tds);
        assertNotNull(tds.getInfo());
        assertEquals(tds.getInfo().getId(), "load.ds");
        
    }
    @After
    public void tearDown() {
        if (c != null) {
            c.close();
        }
    }

}
