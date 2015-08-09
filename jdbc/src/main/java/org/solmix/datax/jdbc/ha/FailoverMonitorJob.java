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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月7日
 */

public class FailoverMonitorJob implements Runnable
{
    private static  final Logger   logger   = LoggerFactory.getLogger(FailoverMonitorJob.class);

    private String                   detectingSQL;
    /**
     * time unit in milliseconds
     */
    private long                     detectingRequestTimeout;
    private HotSwapInvocationHandler hotSwapInvocation;
    private long                     recheckInterval;
    private int                      recheckTimes;

    private DataSource               masterDataSource;
    private DataSource               standbyDataSource;
    private DataSource               masterDetectorDataSource;
    private DataSource               standbyDetectorDataSource;

    /**
     * first time it should be referenced to masterDetectorDataSource.
     */
    private DataSource               currentDetectorDataSource;

    /**
     * Since {@link FailoverMonitorJob} will be scheduled to run in sequence,
     * One executor as instance field is ok.<br>
     * This executor will be used to execute detecting logic asynchronously, if
     * the execution exceeds given timeout threshold, we will check again before
     * switching to standby data source.
     */
    private ExecutorService          executor;
    
    public FailoverMonitorJob(ExecutorService es)
    {
        Assert.isNotNull(es);
        this.executor = es;
    }

    @Override
    public void run() {
        Future<Integer> future = executor.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Integer result = -1;

                for (int i = 0; i < getRecheckTimes(); i++) {
                    Connection conn = null;
                    try {
                        conn = getCurrentDetectorDataSource().getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(getDetectingSQL());
                        pstmt.execute();
                        if (pstmt != null) {
                            pstmt.close();
                        }
                        result = 0;
                        break;
                    } catch (Exception e) {
                        logger.warn("(" + (i + 1) + ") check with failure. sleep ("
                                + getRecheckInterval() + ") for next round check.");
                        try {
                            TimeUnit.MILLISECONDS.sleep(getRecheckInterval());
                        } catch (InterruptedException e1) {
                            logger.warn("interrupted when waiting for next round rechecking.");
                        }
                        continue;
                    } finally {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                logger.warn("failed to close checking connection:\n", e);
                            }
                        }
                    }
                }
                return result;
            }
        });

        try {
            Integer result = future.get(getDetectingRequestTimeout(), TimeUnit.MILLISECONDS);
            if (result == -1) {
                doSwap();
            }
        } catch (InterruptedException e) {
            logger.warn("interrupted when getting query result in FailoverMonitorJob.");
        } catch (ExecutionException e) {
            logger.warn("exception occured when checking failover status in FailoverMonitorJob");
        } catch (TimeoutException e) {
            logger.warn("exceed DetectingRequestTimeout threshold. Switch to standby data source.");
            doSwap();
        }
    }

    private  void doSwap() {
        synchronized(hotSwapInvocation){
            DataSource target = (DataSource) hotSwapInvocation.getTarget();
            if (target == masterDataSource) {
                hotSwapInvocation.swap(standbyDataSource);
                currentDetectorDataSource = standbyDetectorDataSource;
            } else {
                hotSwapInvocation.swap(masterDataSource);
                currentDetectorDataSource = masterDetectorDataSource;
            }
        }
    }

    public String getDetectingSQL() {
        return detectingSQL;
    }

    public void setDetectingSQL(String detectingSQL) {
        this.detectingSQL = detectingSQL;
    }

    public long getDetectingRequestTimeout() {
        return detectingRequestTimeout;
    }

    public void setDetectingRequestTimeout(long detectingRequestTimeout) {
        this.detectingRequestTimeout = detectingRequestTimeout;
    }

    

    public DataSource getMasterDataSource() {
        return masterDataSource;
    }

    public void setMasterDataSource(DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;
    }

    public DataSource getStandbyDataSource() {
        return standbyDataSource;
    }

    public void setStandbyDataSource(DataSource standbyDataSource) {
        this.standbyDataSource = standbyDataSource;
    }

    public void setRecheckInterval(long recheckInterval) {
        this.recheckInterval = recheckInterval;
    }

    public long getRecheckInterval() {
        return recheckInterval;
    }

    public void setRecheckTimes(int recheckTimes) {
        this.recheckTimes = recheckTimes;
    }

    public int getRecheckTimes() {
        return recheckTimes;
    }

    public void setMasterDetectorDataSource(DataSource masterDetectorDataSource) {
        this.masterDetectorDataSource = masterDetectorDataSource;
    }

    public DataSource getMasterDetectorDataSource() {
        return masterDetectorDataSource;
    }

    public void setStandbyDetectorDataSource(DataSource standbyDetectorDataSource) {
        this.standbyDetectorDataSource = standbyDetectorDataSource;
    }

    public DataSource getStandbyDetectorDataSource() {
        return standbyDetectorDataSource;
    }

    public void setCurrentDetectorDataSource(DataSource currentDetectorDataSource) {
        this.currentDetectorDataSource = currentDetectorDataSource;
    }

    public DataSource getCurrentDetectorDataSource() {
        return currentDetectorDataSource;
    }

    
    public HotSwapInvocationHandler getHotSwapInvocation() {
        return hotSwapInvocation;
    }

    
    public void setHotSwapInvocation(HotSwapInvocationHandler hotSwapInvocation) {
        this.hotSwapInvocation = hotSwapInvocation;
    }

}
