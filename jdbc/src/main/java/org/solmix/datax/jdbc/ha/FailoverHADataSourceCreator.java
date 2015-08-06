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

package org.solmix.datax.jdbc.ha;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.datax.jdbc.DataSourceInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月6日
 */

public class FailoverHADataSourceCreator implements HADataSourceCreator
{

    private static final Logger LOG = LoggerFactory.getLogger(FailoverHADataSourceCreator.class);

    private ConcurrentMap<ScheduledFuture<?>, ScheduledExecutorService> schedulerFutures = new ConcurrentHashMap<ScheduledFuture<?>, ScheduledExecutorService>();

    private List<ExecutorService> jobExecutorRegistry = new ArrayList<ExecutorService>();

    private boolean passiveFailover = false;

    private boolean positiveFailover = true;

    private long monitorPeriod = 15 * 1000;

    private int initialDelay = 0;

    private String detectingSql;

    private long detectingTimeout = 15 * 1000;

    private long recheckInterval = 5 * 1000;

    private int recheckTimes = 3;

    @Override
    public DataSource createHADataSource(DataSourceInfo info) throws Exception {
        DataSource activeDataSource = info.getTargetDataSource();
        DataSource standbyDataSource = info.getStandbyDataSource();
        if (activeDataSource == null && standbyDataSource == null) {
            throw new IllegalArgumentException("must have at least one data source active.");
        }
        if (activeDataSource == null || standbyDataSource == null) {
            LOG.warn("only one data source is available for use, so no HA support.");
            if (activeDataSource == null) {
                return standbyDataSource;
            }
            return activeDataSource;
        }

        return null;
    }

    @PostConstruct
    public void init() {

    }

    @PreDestroy
    public void destroy() {

    }

    public boolean isPassiveFailover() {
        return passiveFailover;
    }

    public void setPassiveFailover(boolean passiveFailover) {
        this.passiveFailover = passiveFailover;
    }

    public boolean isPositiveFailover() {
        return positiveFailover;
    }

    public void setPositiveFailover(boolean positiveFailover) {
        this.positiveFailover = positiveFailover;
    }

}
