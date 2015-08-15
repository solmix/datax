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

package org.solmix.datax.jdbc;

import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import org.solmix.datax.DSRequest;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月11日
 */

public class RoutingRequest
{

    private DSRequest request;

    private DataSource dataSource;

    private ExecutorService executor;

    
    public DSRequest getRequest() {
        return request;
    }

    
    public void setRequest(DSRequest request) {
        this.request = request;
    }

    
    public DataSource getDataSource() {
        return dataSource;
    }

    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    
    public ExecutorService getExecutor() {
        return executor;
    }

    
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
    

}
