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

package org.solmix.datax.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.jdbc.DataSourceService;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.router.DataServiceRouter;
import org.solmix.datax.router.RequestToken;
import org.solmix.datax.router.support.DefaultDataServiceRouter;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.support.BaseDataServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月9日
 */
@Extension( MybatisDataServiceFactory.MYBATIS)
@ThreadSafe
public class MybatisDataServiceFactory extends BaseDataServiceFactory
{

    public static final String MYBATIS = "mybatis";
    
    private SqlSessionFactory sqlSessionFactory;
    
    private DataSourceService dataSourceService;
    
    private DataServiceRouter<RequestToken> dataServiceRouter;

    @Override
    protected BaseDataService instanceBaseDataService(DataServiceInfo info, Container container, DataTypeMap prop) {
        MybatisDataService ds = new MybatisDataService(info, container, prop);
        ds.setSqlSessionFactory(sqlSessionFactory);
        if (dataServiceRouter == null && dataSourceService != null) {
            dataServiceRouter = new DefaultDataServiceRouter();
        }
        ds.setDataServiceRouter(dataServiceRouter);
        ds.setDataSourceService(dataSourceService);
        return ds;
    }

    
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }


    
    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }


    
    public void setDataServiceRouter(DataServiceRouter<RequestToken> dataServiceRouter) {
        this.dataServiceRouter = dataServiceRouter;
    }
    
    
}
