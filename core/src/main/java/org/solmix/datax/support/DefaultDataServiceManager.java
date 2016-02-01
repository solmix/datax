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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.xml.XMLParsingException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceFactory;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataServiceNoFoundException;
import org.solmix.datax.DataxRuntimeException;
import org.solmix.datax.application.ApplicationManager;
import org.solmix.datax.call.DSCallFactory;
import org.solmix.datax.call.support.DefaultDSCallFactory;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.DefaultRepository;
import org.solmix.datax.repository.RepositoryService;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.xml.XMLDataServiceBuilder;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.extension.ExtensionLoader;
import org.solmix.runtime.resource.InputStreamResource;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */
@ThreadSafe
public class DefaultDataServiceManager implements DataServiceManager,ContainerAware
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDataServiceManager.class);

    private static final String DEFAULT_XML_LOCATION = "META-INF/dataservices/*.xml";

    private ConcurrentHashMap<String, DataServiceHolder> cached = new ConcurrentHashMap<String, DefaultDataServiceManager.DataServiceHolder>();

    private ConcurrentHashMap<Class<?>, Object> proxys = new ConcurrentHashMap<Class<?>, Object>();
    
    private boolean init;

    /** 加载该路径下的配置 */
    private String location;

    /** 是否加载默认目录的配置 */
    private boolean loadDefault=true;

    private List<String> resources;

    private String basePackage;

    private Container container;

    private Map<String, Object> properties = Collections.emptyMap();

    private RepositoryService repositoryService;

    private DSCallFactory dscFactory;
    
    private String defaultServerType;
    
    private ApplicationManager applicationManager;

    private ExtensionLoader<DataServiceFactory> extensionLoader;

    public DefaultDataServiceManager()
    {
    }
    public DefaultDataServiceManager(Container container)
    {
        setContainer( container) ;
    }
    @Override
    public void setContainer(Container container) {
        this.container = container;
        if (container != null) {
            container.setExtension(this, DataServiceManager.class);
            setupExtensions();
            extensionLoader = container.getExtensionLoader(DataServiceFactory.class);
        }
    }
    private void setupExtensions() {
        ResourceManager rm = container.getExtension(ResourceManager.class);
        if (rm != null) {
            rm.addResourceResolver(new DataServiceResolver(this));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#getRepositoryService()
     */
    @Override
    public RepositoryService getRepositoryService() {
        ensureInit();
        return repositoryService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#setRepositoryService(org.solmix.datax.repository.RepositoryService)
     */
    @Override
    public void setRepositoryService(RepositoryService service) {
        if (this.repositoryService != null && this.repositoryService != service) {
            // 设置了不同的repository，重新初始化
            init = false;
            this.repositoryService = service;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataServiceManager#setConfigLocation(java.lang.String)
     */
    @Override
    public void setConfigLocation(String location) {
        this.location = location;
        this.init = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("DataService proxy interface is null!");
        }
        Object proxy=  proxys.get(serviceClass);
        if(proxy==null){
            if (!serviceClass.isInterface()) {
                throw new IllegalArgumentException("DataService proxy Type:["
                    + serviceClass.getName() + "] is not a interface!");
            }
            if (!serviceClass.isAnnotationPresent(org.solmix.datax.annotation.DataService.class)) {
                throw new IllegalArgumentException("DataService proxy Type:["
                    + serviceClass.getName() + "] ,without @,"
                    + org.solmix.datax.annotation.DataService.class.getSimpleName() + " Annotation!");
            }
            DataServiceProxy<T> dsp = new DataServiceProxy<T>(serviceClass,new DataxSessionImpl(this));
            Object instance = dsp.newInstance();
            proxys.putIfAbsent(serviceClass, instance);
            return (T)proxys.get(serviceClass);
        }else{
            return (T)proxy;
        }
    }

    @Override
    public DataService getDataService(String serviceName) {
        ensureInit();
        DataServiceHolder holder = cached.get(serviceName);
        if (holder == null) {
            DataServiceInfo dsi=getDataServiceInfo( serviceName);
            boolean singleton = true;
            if (DataServiceInfo.SCOPE_PROPERTY.equals(dsi.getScope())) {
                singleton = false;
            }
            holder = new DataServiceHolder(singleton);
            cached.putIfAbsent(serviceName, holder);
            if(singleton){
               return instanceSingletonDataService(holder,dsi);
            }else{
                return instanceDataService(dsi);
            }
        }else{
            if(holder.singleton){
                if(holder.getService()!=null){
                    return holder.getService();
                }else{
                    DataServiceInfo dsi=getDataServiceInfo( serviceName);
                    return instanceSingletonDataService(holder, dsi);
                }
            }else{
                DataServiceInfo dsi=getDataServiceInfo( serviceName);
                return instanceDataService(dsi);
            }
        }
    }
    
    private DataServiceInfo getDataServiceInfo(String serviceName) {
        DataServiceInfo dsi = null;
        try {
            dsi = repositoryService.getDataService(serviceName);
        } catch (Exception e) {
            throw new DataServiceNoFoundException("Can't found DataServiceInfo with serviceId:" + serviceName, e);
        }
        if (dsi == null) {
            throw new DataServiceNoFoundException("Can't found DataServiceInfo with serviceId:" + serviceName);
        }
        return dsi;
    }

    
    private DataService instanceSingletonDataService(DataServiceHolder holder, DataServiceInfo dsi) {
        DataService ds;
        synchronized (holder) {
            ds = instanceDataService(dsi);
            holder.setServiceIfAbsent(ds);
        }
        return ds;
    }
    
    private DataService instanceDataService(DataServiceInfo info){
        DataServiceFactory dsf= extensionLoader.getExtension(info.getServerType());
        DataService ds= dsf.instance(info,getProperties());
        if(ds instanceof BaseDataService){
            ((BaseDataService)ds).setDSCallFactory(dscFactory);
        }
        return ds;
    }

    public boolean getLoadDefault() {
        return loadDefault;
    }

    public void setLoadDefault(boolean loadDefault) {
        this.loadDefault = loadDefault;
    }

    public List<String> getResources() {
        return resources;
    }

    /**
     * 设置默认的<code>serverType</code>, 当配置中不指定<code>serverType</code>时使用,如果不设置则为"base".
     * 
     * @param defaultServerType
     */
    public String getDefaultServerType() {
        if (defaultServerType == null) {
            defaultServerType = BaseDataServiceFactory.BASE;
        }
        return defaultServerType;
    }

    /**
     * 设置默认的<code>serverType</code>, 当配置中不指定<code>serverType</code>时使用,如果不设置则为"base".
     * 
     * @param defaultServerType
     */
    public void setDefaultServerType(String defaultServerType) {
        this.defaultServerType = defaultServerType;
    }

    
    @Override
    public DSCallFactory getDSCallFactory() {
        return dscFactory;
    }

    
    public void setDscFactory(DSCallFactory dscFactory) {
        this.dscFactory = dscFactory;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public void addResource(String resource) {
        if (this.resources == null) {
            this.resources = new ArrayList<String>();
        }
        resources.add(resource);
    }

    /** 确保已经初始化 */
    private void ensureInit() {
        if (!init) {
            init();
        }
    }

    /**
     * 
     */
//    @PostConstruct
    public synchronized void init() {
        if (init) {
            return;
        }
        // 未设置，使用默认repository。
        if (repositoryService == null) {
            buildDefaultRepository();
        }
        //设置默认的manager。
        
        if(dscFactory==null){
            dscFactory= container.getExtension(DSCallFactory.class);
            if(dscFactory==null){
                DefaultDSCallFactory factory=new DefaultDSCallFactory();
                factory.init();
                dscFactory=factory;
                container.setExtension(dscFactory, DSCallFactory.class);
            }
        }
        init = true;
    }

    private void buildDefaultRepository() {
        String xmlLocation = location;

        Map<String, InputStream> xmlResources = new LinkedHashMap<String, InputStream>();
        // 先加载指定的XML
        xmlResources.putAll(loadDefinitionXmlDataServiceConfig());

        if (loadDefault && xmlLocation == null) {
            xmlLocation = DEFAULT_XML_LOCATION;
        }
        if (xmlLocation != null) {
            xmlResources.putAll(lookupXmlDataServiceConfig(xmlLocation));
        }
       
        DefaultRepository repository = new DefaultRepository(this);
        if (this.basePackage != null) {
            scanClassDefinition(basePackage, repository);
        }

        try {
            for (String xml : xmlResources.keySet()) {
                XMLDataServiceBuilder builder = null;
                try {
                    builder = new XMLDataServiceBuilder(xmlResources.get(xml), repository, getProperties(), xml, container, getDefaultServerType());
                } catch (XMLParsingException e) {
                    throw new BuilderException("Error validate xml file:" + xml, e);
                }
                builder.build();
            }
        } finally {
            for (InputStream input : xmlResources.values()) {
                IOUtils.closeQuietly(input);
            }
        }
        Collection<ReferenceResolver> resolvers = repository.getReferenceResolvers();
        if (!resolvers.isEmpty()) {
            StringBuilder resolverError = new StringBuilder();
            for (ReferenceResolver resolver : resolvers) {
                resolverError.append(resolver.toString()).append(" Failed ;");
            }
            throw new BuilderException(resolverError.toString());
        }
        this.repositoryService = repository;
    }

    /**
     * 扫描注解
     */
    protected void scanClassDefinition(String basePackage, DefaultRepository repository) {
        // TODO Auto-generated method stub
        //XXX
    }

    /**
     * 加载制定xml配置
     */
    private Map<String, InputStream> loadDefinitionXmlDataServiceConfig() {
        if (this.resources == null)
            return Collections.emptyMap();
        ResourceManager rm = container.getExtension(ResourceManager.class);
        Map<String, InputStream> configs = new LinkedHashMap<String, InputStream>();
        try {
            for (String xml : this.resources) {
                InputStreamResource ism = rm.getResourceAsStream(xml);
                if (ism == null) {
                    LOG.warn("Can't found definition xml configuration {}", xml);
                } else {
                    configs.put(ism.getURI().toString(), ism.getInputStream());
                }
            }
        } catch (IOException e) {
            throw new DataxRuntimeException("load definition dataservice configuration file failed.", e);
        }
        return configs;
    }

    /**
     * 查找xml
     */
    private Map<String, InputStream> lookupXmlDataServiceConfig(String xmlLocation) {
        ResourceManager rm = container.getExtension(ResourceManager.class);
        Map<String, InputStream> configs = new LinkedHashMap<String, InputStream>();
        try {
            InputStreamResource[] resources = rm.getResourcesAsStream(xmlLocation);
            if (resources != null) {
                for (InputStreamResource resource : resources) {
                    configs.put(resource.getURI().toString(), resource.getInputStream());
                }
            }
        } catch (IOException e) {
            throw new DataxRuntimeException("lookup dataservice configuration file failed.", e);
        }
        return configs;

    }

    private class DataServiceHolder
    {

        boolean singleton;


        DataService service;

        DataServiceHolder( boolean singleton)
        {
            this.singleton = singleton;
        }

        DataService getService() {
            return service;
        }

        synchronized DataService setServiceIfAbsent(DataService service) {
            if (this.service == null) {
                this.service = service;
            }
            return this.service;
        }
        
    }

   
    @Override
    public DSRequest createDSRequest() {
        DSRequestImpl  request= new DSRequestImpl();
        request.setDataServiceManager(this);
        request.setApplicationManager(getApplicationManager());
        return request;
    }

    protected void injectObject(Object obj){
        ResourceManager rm=  container.getExtension(ResourceManager.class);
        if(rm!=null){
            ResourceInjector in= new ResourceInjector(rm);
            in.inject(obj);
        }
    }
    
    @Override
    public DSResponse createDsResponse(DSRequest request) {
        DSResponse response= new DSResponseImpl(request);
        //TODO
        //injectObject(response);
        return response;
    }

    
    public ApplicationManager getApplicationManager() {
        if(applicationManager==null){
            synchronized (this) {
                applicationManager=container.getExtension(ApplicationManager.class);
            }
        }
        return applicationManager;
    }

    
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    
    
}
