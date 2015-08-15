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

package org.solmix.datax;

import java.util.List;
import java.util.Map;

import org.solmix.datax.application.Application;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.model.OperationInfo;

/**
 * 代表一次DataService(DS)请求。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public interface DSRequest 
{

    DSResponse execute() throws DSCallException;

    DSCall getDSCall();

    DataService getDataService();

    String getDataServiceId();

    void setDataService(DataService service);

    void setDSCall(DSCall call);

    /**
     * 指明请求是否已经被验证.
     * 
     * @return
     */
    boolean isValidated();
    
    
    /**
     * 指明当前请求是否已经调用了Invoker。
     * 当使用自定义Invoker时，如果在Invoke Method中重用DSrequest并对DSRequest执行调用时
     * 避免进入死循环。
     * 
     * @return
     */
    boolean isInvoked();
    
    /**
     * 设置当前请求是否已经调用了Invoker。
     * 当使用自定义Invoker时，如果在Invoke Method中重用DSrequest并对DSRequest执行调用时
     * 避免进入死循环。
     * 
     */
    void setInvoked(boolean invoked);

    void freeResources();

    /**
     * 
     * @param FreeResourcesHandler handler
     */
    void registerFreeResourcesHandler(FreeResourcesHandler handler);

    /**
     * 全称 xxx.xxx.fetch
     * 
     * @return
     */
    String getOperationId();

    /**
     * 全称 xxx.xxx.fetch 相对：fetch
     * 
     * @param operationId
     */
    void setOperationId(String operationId);

    /**
     * @return
     */
    RequestContext getRequestContext();

    /**
     * @param b
     */
    void setRequestStarted(boolean started);

    /**
     * @param b
     */
    void setValidated(boolean validate);

    /**
     * 获取本次请求的Operation描述信息。
     * 
     * @return
     */
    OperationInfo getOperationInfo();

    /**
     * @return
     */
    Application getApplication();

    Map<String, Object> getValues();

    List<?> getValueSets();

    Map<String, Object> getOldValues();

    List<?> getOldValueSets();

    /**
     * @param rawValues
     */
    void setRawValues(Object rawValues);

    /**
     * @return
     */
    Object getRawValues();

    /**
     * 代表请求是否允许加入事物。
     * 
     * @return
     */
    Boolean isCanJoinTransaction();

    /**
     * 设置请求是否能够加入事物。
     * 
     * @param canJoinTransaction
     */
    void setCanJoinTransaction(Boolean canJoinTransaction);

    /**
     * 设置是否为事物中的一部分。
     * 
     * @param partsOfTransaction
     */
    void setPartsOfTransaction(boolean partsOfTransaction);

    /**
     * 为真，代表该请求在事物中,否则未加入事物。
     * 
     * @return
     */
    boolean isPartsOfTransaction();

    /**
     * @return
     */
    boolean isRequestStarted();

    /**
     * @return
     */
    String getApplicationId();

    /**
     * @param appId
     */
    void setApplicationId(String appId);

    /**
     * @param rc
     */
    void setRequestContext(RequestContext rc);
    
     Object getAttribute(String name);
     
     /**
     * @param name name==null removed
     * @param value
     */
    void setAttribute(String name,Object value);

    /**
     * @return
     */
    Object getRawOldValues();

    /**
     * @param rawOldValues
     */
    void setRawOldValues(Object rawOldValues);

    <T>void addAttachment(Class<T> classKey,T instance);
    
    <T> T getAttachment(Class<T> classKey);
}
