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

package org.solmix.datax.jdbc.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.jdbc.DataSourceInfo;
import org.solmix.datax.jdbc.DataSourceService;
import org.solmix.datax.jdbc.RoutingRequestProcessor;
import org.solmix.datax.jdbc.ha.HADataSourceCreator;
import org.solmix.datax.jdbc.ha.NonHADataSourceCreator;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月6日
 */

public class DefaultDataSourceService implements DataSourceService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDataSourceService.class);
    private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private HADataSourceCreator haDataSourceCreator;
    private RoutingRequestProcessor routingRequestProcessor;
    private Set<DataSourceInfo> dataSourceInfos= new HashSet<DataSourceInfo>();
    @Override
    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }
    
    @Override
    public Set<DataSourceInfo> getDataSourceInfos() {
        return dataSourceInfos;
    }
    
    public void setDataSourceInfos(Set<DataSourceInfo> dataSourceInfos) {
        this.dataSourceInfos = dataSourceInfos;
    }

    
    public HADataSourceCreator getHaDataSourceCreator() {
        return haDataSourceCreator;
    }

    
    public void setHaDataSourceCreator(HADataSourceCreator haDataSourceCreator) {
        this.haDataSourceCreator = haDataSourceCreator;
    }

    @PostConstruct
    public void init() throws Exception{
        if(getHaDataSourceCreator()!=null){
            setHaDataSourceCreator(new NonHADataSourceCreator());
        }
        if(dataSourceInfos.isEmpty()){
            return;
        }
        for(DataSourceInfo info:getDataSourceInfos()){
            Assert.isNotNull(info.getId());
            Assert.isNotNull(info.getTargetDataSource());
            DataSource dataSourceToUse = info.getTargetDataSource();

            if (info.getStandbyDataSource() != null) {
                dataSourceToUse = getHaDataSourceCreator().createHADataSource(info);
            }

            dataSources.put(info.getId(),  dataSourceToUse);
        }
    }
    
    @PreDestroy
    public void destroy(){
        for(DataSourceInfo dsi:dataSourceInfos){
            if(dsi.getExecutorService()!=null){
                ExecutorService executor=  dsi.getExecutorService();
                try {
                    executor.shutdown();
                    executor.awaitTermination(5, TimeUnit.MINUTES);
                    executor = null;
                } catch (InterruptedException e) {
                    LOG.warn("interrupted when shuting down the query executor:\n{}", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.jdbc.DataSourceService#getDataSourceInfo(java.lang.String)
     */
    @Override
    public DataSourceInfo getDataSourceInfo(String key) {
        for(DataSourceInfo dsi:dataSourceInfos){
            if(dsi.getId().equals(key)){
                return dsi;
            }
        }
        return null;
    }

    
    @Override
    public RoutingRequestProcessor getRoutingRequestProcessor() {
        return routingRequestProcessor;
    }

    
    public void setRoutingRequestProcessor(RoutingRequestProcessor routingRequestProcessor) {
        this.routingRequestProcessor = routingRequestProcessor;
    }
  
}
