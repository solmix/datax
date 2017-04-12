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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
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

public class DSResponseImpl implements DSResponse
{

    private static final Logger LOG = LoggerFactory.getLogger(DSResponseImpl.class);

    private Status status = Status.UNSET;

    private Object[] errors;

    private DataService dataService;

    private Integer affectedRows;

    private Object rawData;
    
    private Map<String ,Object> attributes;
    
    private Map<Class<?> ,Object> attachments;
    
    private String forward;
    
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
        if(request!=null){
            setDataService(request.getDataService());
        }
       
        if (status != null)
            setStatus(status);
    }

    /**
     * @param request
     */
    public DSResponseImpl(DSRequest request)
    {
        this(request,null);
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
    @Override
    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows=affectedRows;
        
    }

 
    @Override
    public Integer getAffectedRows() {
        return affectedRows;
        
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getSingleRecord()
     */
    @Override
    public Map<Object, Object> getSingleRecord() {
        if(status!=Status.STATUS_SUCCESS){
            return null;
        }
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
        if(status!=Status.STATUS_SUCCESS){
            return null;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> T getSingleResult(Class<T> type) {
        if(rawData==null){
            return null;
        }
        if(Object.class == type){
            if(Collection.class.isAssignableFrom(rawData.getClass())){
                Collection<Object> coll = (Collection<Object>)rawData;
                if(coll.size()==0){
                	return null;
                }else if(coll.size()==1){
                    return getResultInternal(type,coll.iterator().next());
                }else{
                    throw new IllegalArgumentException("To Many result");
                }
            }else if(Map.class.isAssignableFrom(rawData.getClass())){
                Map coll = (Map)rawData;
                if(coll.size()==1){
                    Object firstKey = coll.keySet().iterator().next();
                    return getResultInternal(type,coll.get(firstKey));
                }else{
                    throw new IllegalArgumentException("To Many result");
                }
            }else{
                return (T) rawData;
            }
        }else{
            return getResultInternal(type, rawData);
        }
       
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DSResponse#getResultList(java.lang.Class)
     */
    @Override
    public <T> List<T> getResultList(Class<T> type) {
        if (status != Status.STATUS_SUCCESS) {
            return null;
        }
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
        if(status!=Status.STATUS_SUCCESS){
            return null;
        }
        if (type.isInstance(data)){
            return (T) data;
        }
        // First, assume that the type is Map.
        if (Map.class.isAssignableFrom(type)) {
            if (data instanceof List<?>) {
                if (((List<?>) data).size() == 0) {
                    return null;
                } else if (((List<?>) data).get(0) instanceof Map<?, ?>) {
                    if(type == DataTypeMap.class){
                        return (T)new DataTypeMap((Map) ((List<?>) data).get(0));
                    }else{
                        return (T) ((List<?>) data).get(0);
                    }
                    
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
                        	if(LOG.isWarnEnabled())
                        		LOG.warn("The data is more than one map or bean, used the first one and drop other {}(s)" , (datas.size() - 1));
                        }
                        return _return;

                    } else {
                        if(!(type == Void.class)){
                        	if(LOG.isDebugEnabled())
                        		LOG.debug("The response data is List but is empty ,which can't covert to type:{},return object is null ",type.getClass().getName());    
                        }
                        return null;
                    }

                } else {
                    
                    return TransformUtils.transformType(type, data);
                }
            } catch (Exception ee) {
                LOG.debug((new StringBuilder()).append("Tried to convert inbound nested Map to: ").append(type.getName()).append(
                    " but DataTools.setProperties() on instantiated class failed").append(" with the following error: ").append(ee.getMessage()).toString());
            }
        }else{
            try {
                return TransformUtils.transformType(type, data);
            } catch (Exception e) {
                throw new IllegalArgumentException((new StringBuilder()).append("Can't convert value of type ").append(data.getClass().getName()).append(
                    " to target type ").append(type.getName()).toString());
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
    @Override
    public Object getAttribute(String name) {
        if(attributes!=null){
           return attributes.get(name);
        }
        return null;
    }

    
    @Override
    public void setAttribute(String name, Object value) {
        if(name==null){
            return;
        }
        if(attributes==null){
                attributes=new HashMap<String, Object>();
        }
        if(value==null){
            attributes.remove(name);
        }else{
            attributes.put(name, value);
        }
    }
    
    @Override
    public <T> void addAttachment(Class<T> classKey, T instance) {
        if(instance==null||classKey==null){
            return;
        }
        if(attachments==null){
            attachments=new HashMap<Class<?>, Object>();
        }
        attachments.put(classKey, instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttachment(Class<T> classKey) {
        if (attachments != null) {
            return (T) attachments.get(classKey);
        }
        return null;
    }
   
    @Override
    public String getForward() {
        return forward;
    }
    
    @Override
    public void setForward(String forward) {
      this.forward=forward;
        
    }
}
