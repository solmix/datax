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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.repository.RepositoryService;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月18日
 */
@ThreadSafe
public class DefaultDataServiceManager implements DataServiceManager
{

    private static final String DEFAULT_XML_LOCATION = "META-INF/dataservice/*.xml";

    private boolean init;
    
    private String location;
    
    private List<String> resources;
    
    private String basePackage;
    
    private Container contaier;
    
    public DefaultDataServiceManager(Container c){
        this.contaier=c;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#getRepositoryService()
     */
    @Override
    public RepositoryService getRepositoryService() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#setRepositoryService(org.solmix.datax.repository.RepositoryService)
     */
    @Override
    public void setRepositoryService(RepositoryService service) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#setConfigLocation(java.lang.String)
     */
    @Override
    public void setConfigLocation(String location) {
        this.location=location;
        this.init=false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#addServiceClass(java.lang.Class)
     */
    @Override
    public void addService(Class<?> serviceClass) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#getDataService(java.lang.String)
     */
    @Override
    public DataService getDataService(String serviceName) {
        ensureInit();
        // TODO Auto-generated method stub
        return null;
    }
    
    /**确保已经初始化*/
    private void ensureInit(){
        if(!init){
            init();
        }
    }
    
    /**
     * 
     */
    public synchronized void init() {
        buildRepository();
        init=true;
    }

    private void buildRepository(){
        String xmlLocation=location;
        if(xmlLocation==null){
            xmlLocation=DEFAULT_XML_LOCATION;
        }
        Map<String,InputStream> xmlResources = new HashMap<String,InputStream>();
        xmlResources.putAll(lookupXmlDataServiceConfig(xmlLocation));
        xmlResources.putAll(loadDefinitionXmlDataServiceConfig());
        if(this.basePackage!=null){
            scanClassDefinition(basePackage);
        }
        
    }

    /**
     *扫描注解
     */
    private void scanClassDefinition(String basePackage2) {
        // TODO Auto-generated method stub
        
    }
    /**
     *加载制定xml配置
     */
    private  Map<String,InputStream> loadDefinitionXmlDataServiceConfig() {
        return null;
        // TODO Auto-generated method stub
        
    }
    /**
     *查找xml
     */
    private  Map<String,InputStream> lookupXmlDataServiceConfig(String xmlLocation) {
        return null;
        // TODO Auto-generated method stub
        
    }

}
