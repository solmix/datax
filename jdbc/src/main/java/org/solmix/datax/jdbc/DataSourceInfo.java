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

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月5日
 */

public class DataSourceInfo
{

    private String id;

    private DataSource targetDataSource;

    private DataSource targetDetectorDataSource;

    private DataSource standbyDataSource;

    private DataSource standbyDetectorDataSource;

    private int poolSize = Runtime.getRuntime().availableProcessors() * 5;
    
    private ExecutorService executorService;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataSource getTargetDataSource() {
        return targetDataSource;
    }

    public void setTargetDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    public DataSource getTargetDetectorDataSource() {
        return targetDetectorDataSource;
    }

    public void setTargetDetectorDataSource(DataSource targetDetectorDataSource) {
        this.targetDetectorDataSource = targetDetectorDataSource;
    }

    public DataSource getStandbyDataSource() {
        return standbyDataSource;
    }

    public void setStandbyDataSource(DataSource standbyDataSource) {
        this.standbyDataSource = standbyDataSource;
    }

    public DataSource getStandbyDetectorDataSource() {
        return standbyDetectorDataSource;
    }

    public void setStandbyDetectorDataSource(DataSource standbyDetectorDataSource) {
        this.standbyDetectorDataSource = standbyDetectorDataSource;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }
    
    
    public ExecutorService getExecutorService() {
        return executorService;
    }

    
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataSourceInfo other = (DataSourceInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DataSourceInfo [ID=" + id + ", poolSize=" + poolSize + ", standbyDataSource=" + standbyDataSource + ", standbyDetectorDataSource="
            + standbyDetectorDataSource + ", targetDataSource=" + targetDataSource + ", targetDetectorDataSource=" + targetDetectorDataSource + "]";
    }

}
