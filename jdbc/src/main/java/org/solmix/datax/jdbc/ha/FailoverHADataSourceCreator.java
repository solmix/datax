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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.jdbc.DataSourceInfo;
import org.solmix.runtime.helper.ProxyHelper;

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
        HotSwapInvocationHandler handler = new HotSwapInvocationHandler(activeDataSource);
        
        if(isPositiveFailover()){
            DataSource targetDetectorDataSource = info.getTargetDetectorDataSource();
            DataSource standbyDetectorDataSource = info.getStandbyDetectorDataSource();
            if (targetDetectorDataSource == null || standbyDetectorDataSource == null) {
                throw new IllegalArgumentException(
                        "targetDetectorDataSource or standbyDetectorDataSource can't be null if positive failover is enabled.");
            }
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ExecutorService jobExecutor = Executors.newFixedThreadPool(1);
            jobExecutorRegistry.add(jobExecutor);
            
            FailoverMonitorJob job = new FailoverMonitorJob(jobExecutor);
            job.setHotSwapInvocation(handler);
            job.setMasterDataSource(activeDataSource);
            job.setStandbyDataSource(standbyDataSource);
            job.setMasterDetectorDataSource(targetDetectorDataSource);
            job.setStandbyDetectorDataSource(standbyDetectorDataSource);
            job.setCurrentDetectorDataSource(targetDetectorDataSource);
            job.setDetectingRequestTimeout(getDetectingTimeout());
            job.setDetectingSQL(getDetectingSql());
            job.setRecheckInterval(recheckInterval);
            job.setRecheckTimes(recheckTimes);
            
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(job, initialDelay,
                    monitorPeriod, TimeUnit.MILLISECONDS);
            schedulerFutures.put(future, scheduler);
        }
        
        if(isPassiveFailover()){
            handler.setPassiveFailover(true);
            handler.setMainDataSource(activeDataSource);
            handler.setStandbyDataSource(standbyDataSource);
        }
        
        ClassLoader loader= ClassLoaderUtils.getDefaultClassLoader();
        Object proxy=ProxyHelper.getProxy(loader, new Class[]{DataSource.class}, handler);

        return (DataSource)proxy;
    }

    @PostConstruct
    public void init() {
        if (!isPassiveFailover() && !isPositiveFailover()) {
            return;
        }
        if (StringUtils.isEmpty(detectingSql)) {
            throw new IllegalArgumentException(
                    "A 'detectingSql' should be provided if positive failover function is enabled.");
        }

        if (monitorPeriod <= 0 || detectingTimeout <= 0 || recheckInterval <= 0
                || recheckTimes <= 0) {
            throw new IllegalArgumentException(
                    "'monitorPeriod' OR 'detectingTimeoutThreshold' OR 'recheckInterval' OR 'recheckTimes' must be positive.");
        }

        if (isPositiveFailover()) {
            if ((detectingTimeout > monitorPeriod)) {
                throw new IllegalArgumentException(
                        "the 'detectingTimeoutThreshold' should be less(or equals) than 'monitorPeriod'.");
            }

            if ((recheckInterval * recheckTimes) > detectingTimeout) {
                throw new IllegalArgumentException(
                        " 'recheckInterval * recheckTimes' can not be longer than 'detectingTimeoutThreshold'");
            }
        }
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<ScheduledFuture<?>, ScheduledExecutorService> e : schedulerFutures.entrySet()) {
            ScheduledFuture<?> future = e.getKey();
            ScheduledExecutorService scheduler = e.getValue();
            future.cancel(true);
            shutdownExecutor(scheduler);
        }

        for (ExecutorService executor : jobExecutorRegistry) {
            shutdownExecutor(executor);
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOG.warn("interrupted when shutting down executor service.");
        }
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

    
    public long getMonitorPeriod() {
        return monitorPeriod;
    }

    
    public void setMonitorPeriod(long monitorPeriod) {
        this.monitorPeriod = monitorPeriod;
    }

    
    public int getInitialDelay() {
        return initialDelay;
    }

    
    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    
    public String getDetectingSql() {
        return detectingSql;
    }

    
    public void setDetectingSql(String detectingSql) {
        this.detectingSql = detectingSql;
    }

    
    public long getDetectingTimeout() {
        return detectingTimeout;
    }

    
    public void setDetectingTimeout(long detectingTimeout) {
        this.detectingTimeout = detectingTimeout;
    }

    
    public long getRecheckInterval() {
        return recheckInterval;
    }

    
    public void setRecheckInterval(long recheckInterval) {
        this.recheckInterval = recheckInterval;
    }

    
    public int getRecheckTimes() {
        return recheckTimes;
    }

    
    public void setRecheckTimes(int recheckTimes) {
        this.recheckTimes = recheckTimes;
    }

}
