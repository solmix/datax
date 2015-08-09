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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.Reflection;
import org.solmix.commons.util.StringUtils;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月7日
 */

public class HotSwapInvocationHandler implements InvocationHandler
{
    private static final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = new HashSet<String>(8);
    private static final Logger LOG = LoggerFactory.getLogger(HotSwapInvocationHandler.class);
    private Object mainDataSource;

    private Object standbyDataSource;

    private Object target;

    private boolean passiveFailover;
    static {
        DATA_ACCESS_RESOURCE_FAILURE_CODES.add("08"); // Connection exception
        DATA_ACCESS_RESOURCE_FAILURE_CODES.add("53"); // PostgreSQL: insufficient resources (e.g. disk full)
        DATA_ACCESS_RESOURCE_FAILURE_CODES.add("54"); // PostgreSQL: program limit exceeded (e.g. statement too complex)
        DATA_ACCESS_RESOURCE_FAILURE_CODES.add("57"); // DB2: out-of-memory exception / database not started
        DATA_ACCESS_RESOURCE_FAILURE_CODES.add("58"); // DB2: unexpected system error
    }
    /**
     * @param target
     */
    public HotSwapInvocationHandler(Object target)
    {
        this.target=target;
    }

    public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
        Assert.assertNotNull(newTarget, "Target object must not be null");
        Object old = this.getTarget();
        setTarget(newTarget);
        return old;
  }
    

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Assert.assertNotNull(target, "target");
        if (passiveFailover && StringUtils.isEquals(method.getName(), "getConnection")) {
            try {
                return invokeMethod(proxy, method, args);
            } catch (SQLException ex) {
                String sqlState = ex.getSQLState();
                if (sqlState == null) {
                    SQLException nestedEx = ex.getNextException();
                    if (nestedEx != null) {
                        sqlState = nestedEx.getSQLState();
                    }
                }
                if (DATA_ACCESS_RESOURCE_FAILURE_CODES.contains(sqlState)) {
                    synchronized (this) {
                        if (target == mainDataSource) {
                            LOG.warn("hot swap from '" + target + "' to '" + standbyDataSource + "'.");
                            target = standbyDataSource;
                        } else {
                            LOG.warn("hot swap from '" + target + "' to '" + mainDataSource + "'.");
                            target = mainDataSource;
                        }
                    }
                }
            }
        }
        if (Reflection.isEqualsMethod(method)) {
            return equals(args[0]);
        }
        if (Reflection.isHashCodeMethod(method)) {
            return hashCode();
        }

        return invokeMethod(proxy, method, args);
    }
    
    private Object invokeMethod(Object proxy, Method method, Object[] args) throws Throwable{
        Object retVal = null;
        try {
            retVal = Reflection.invokeMethod(target, method, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        } catch (IllegalArgumentException ex) {
            throw new InvocationTargetException(ex, "Invocation configuration seems to be invalid: tried calling method [" + method + "] on target ["
                + target + "]");
        }
        Class<?> returnType = method.getReturnType();
        if (retVal != null && retVal == target  && returnType.isInstance(proxy)) {
            retVal = proxy;
        } else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new IllegalArgumentException("Null return value from advice does not match primitive return type for: " + method);
        }
        return retVal;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    
    public boolean isPassiveFailover() {
        return passiveFailover;
    }

    
    public void setPassiveFailover(boolean passiveFailover) {
        this.passiveFailover = passiveFailover;
    }

    
    public Object getMainDataSource() {
        return mainDataSource;
    }

    
    public void setMainDataSource(Object mainDataSource) {
        this.mainDataSource = mainDataSource;
    }

    
    public Object getStandbyDataSource() {
        return standbyDataSource;
    }

    
    public void setStandbyDataSource(Object standbyDataSource) {
        this.standbyDataSource = standbyDataSource;
    }
    
}
