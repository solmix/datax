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

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.solmix.datax.jdbc.DataSourceInfo;
import org.solmix.datax.jdbc.DataSourceService;
import org.solmix.datax.jdbc.ha.HADataSourceCreator;
import org.solmix.datax.jdbc.ha.NonHADataSourceCreator;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月6日
 */

public class DefaultDataSourceService implements DataSourceService
{

    private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private HADataSourceCreator haDataSourceCreator;
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
    public void init(){
        if(getHaDataSourceCreator()!=null){
            setHaDataSourceCreator(new NonHADataSourceCreator());
        }
        
    }
}
