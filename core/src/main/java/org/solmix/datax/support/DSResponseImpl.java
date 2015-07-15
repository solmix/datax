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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.TransformUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class DSResponseImpl  extends PagedBean implements DSResponse
{
    private static final Logger LOG = LoggerFactory.getLogger(DSResponseImpl.class);
    private Status status=Status.UNSET;
    
    private Object[] errors;
    private DataService dataService;
    private Long affectedRows;
    private Object rawData;
    public DSResponseImpl(Status status)
    {
        setStatus(status);
    }
    /**
     * @param dataService
     * @param dsRequestImpl
     */
    public DSResponseImpl(DataService dataService, DSRequest request)
    {
        setDataService(dataService);
    }

    public DSResponseImpl(DSRequest request, Status status)
    {
        DataService ds = request.getDataService();
        setDataService(ds);
        if (status != null)
            setStatus(status);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getDataService()
     */
    @Override
    public DataService getDataService() {
        return dataService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#setDataService(org.solmix.datax.DataService)
     */
    @Override
    public void setDataService(DataService dataService) {
        this.dataService=dataService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getSingleRecord()
     */
    @Override
    public Map<Object, Object> getSingleRecord() {
        if(getDataService()==null){
            throw new java.lang.IllegalStateException("Attempted to call getSingleRecord() on a DSResponse with null DataSource");
        }
        Object singleData = null;
        if (rawData instanceof List<?>) {
            if (((List<?>) rawData).size() > 0)
                singleData = ((List<?>) rawData).get(0);
            else
                singleData = null;
        } else {
            singleData = rawData;
        }
        Map<Object, Object> record = getDataService().getProperties(singleData);
        return record;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<Object, Object>> getRecordList() {
        if (getDataService() == null) {
            throw new java.lang.IllegalStateException("Attempted to call getSingleRecord() on a DSResponse with null DataSource");
        }
        List<Map<Object, Object>> target = new ArrayList<Map<Object, Object>>();
        List<Object> sources = DataUtils.makeListIfSingle(rawData);
        for (Object source : sources) {
            if (source instanceof Map<?, ?>) {
                target.add((Map<Object, Object>) source);
            } else {
                target.add(getDataService().getProperties(source));
            }
        }
        return target;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getStatus()
     */
    @Override
    public Status getStatus() {
        return this.status;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#setStatus(org.solmix.datax.DSResponse.Status)
     */
    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#isSuccess()
     */
    @Override
    public boolean isSuccess() {
        Status _s = this.getStatus();
        return _s == Status.STATUS_SUCCESS;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getRawData()
     */
    @Override
    public Object getRawData() {
        return rawData;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#setRawData(java.lang.Object)
     */
    @Override
    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getSingleResult(java.lang.Class)
     */
    @Override
    public <T> T getSingleResult(Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getResultList(java.lang.Class)
     */
    @Override
    public <T> List<T> getResultList(Class<T> type) {
        List<T> res = new ArrayList<T>();
        if (List.class.isAssignableFrom(rawData.getClass())) {
            for (Object obj : List.class.cast(rawData)) {
                res.add(getResultInternal(type, obj));

            }
        }else {
            try {
                res.add(TransformUtils.transformType(type, rawData));
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getResultInternal(Class<T> type, Object data) {
        if (data == null)
            return null;
        if (type.isInstance(data))
            return (T) data;
        // First, assume that the type is Map.
        if (Map.class.isAssignableFrom(type)) {
            if (data instanceof List<?>) {
                if (((List<?>) data).size() == 0) {
                    return null;
                } else if (((List<?>) data).get(0) instanceof Map<?, ?>) {
                    return (T) ((List<?>) data).get(0);
                }
            } else if (data instanceof Map<?, ?>) {
                return (T) data;
            }
            // Then,assume that the type is List.
        } else if (List.class.isAssignableFrom(type)) {
            if (data instanceof List<?>) {
                return (T) data;
            } else {
                List<Object> re = new ArrayList<Object>();
                re.add(data);
                return (T) re;
            }
        } else if (!type.isPrimitive() && !type.isInterface() && !type.isArray()) {
            try {
                if (Map.class.isAssignableFrom(data.getClass())) {
                    Object instance = type.newInstance();
                    DataUtils.setProperties(Map.class.cast(data), instance, false);
                    return (T) instance;
                } else if (List.class.isAssignableFrom(data.getClass())) {
                    List<Object> datas = List.class.cast(data);
                    int size = datas.size();
                    if (size > 0) {
                        Object one = datas.get(0);
                        T _return = null;
                        if (type.isAssignableFrom(one.getClass())) {
                            _return = type.cast(one);
                        } else if (Map.class.isAssignableFrom(one.getClass())) {
                            _return = type.newInstance();
                            DataUtils.setProperties((Map<?, ?>) one, _return, false);
                        }
                        if (size > 1) {
                            LOG.warn("The data is more than one map or bean, used the first one and drop other " + (datas.size() - 1) + "(s)");
                        }
                        return _return;

                    } else {
                        LOG.warn("The data is List is empty ,return object is null ");
                        return null;
                    }

                } else {
                    return TransformUtils.transformType(type, data);
                }
            } catch (Exception ee) {
                LOG.debug((new StringBuilder()).append("Tried to convert inbound nested Map to: ").append(type.getName()).append(
                    " but DataTools.setProperties() on instantiated class failed").append(" with the following error: ").append(ee.getMessage()).toString());
            }
        }
        throw new IllegalArgumentException((new StringBuilder()).append("Can't convert value of type ").append(data.getClass().getName()).append(
            " to target type ").append(type.getName()).toString());
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getErrors()
     */
    @Override
    public Object[] getErrors() {
        return errors;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#setErrors(java.lang.Object[])
     */
    @Override
    public void setErrors(Object... errors) {
        this.errors=errors;
    }

}
