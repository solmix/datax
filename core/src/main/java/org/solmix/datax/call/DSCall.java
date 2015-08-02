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
package org.solmix.datax.call;

import java.util.List;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataxException;
import org.solmix.datax.FreeResourcesHandler;
import org.solmix.datax.model.TransactionPolicy;


/**
 * 代表一连串的DSRequest。
 * 一次Call可能包含多个DSRequest。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public interface DSCall extends FreeResourcesHandler
{

    /**
     * 调用事物策略
     * @return
     */
    TransactionPolicy getTransactionPolicy();

    /**
     * 调用队列中是否已包含修改请求。
     * 
     * @param req
     * @return
     */
    boolean queueIncludesUpdates(DSRequest req);

    /**
     * @param transactionPolicy
     * @throws SlxException
     */
    void setTransactionPolicy(TransactionPolicy transactionPolicy) throws TransactionException;

    void registerCallback(DSCallCompleteCallback callback);
    
    /**
     * 执行带事物的请求
     * @param request
     * @return
     */
    DSResponse execute(DSRequest request) throws DSCallException;


    /**
     * @return
     */
    List<DSRequest> getRequests();

    /**
     * @param req
     * @return
     */
    DSResponse getResponse(DSRequest req);

    /**
     * 执行DSCall自带的Request,执行结果通过{@link #getResponse(DSRequest)}获取
     */
    void execute();

    /**
     * @param key
     */
    void removeAttribute(Object key);

    /**
     * @param key
     * @param value
     */
    void setAttribute(Object key, Object value);

    /**
     * @param key
     * @return
     */
    Object getAttribute(Object key);

 
    /**
     * 结束DSCall事物  并 返回DSCall已经执行了的所有结果的一个合并集。
     * 
     * @return
     */
    DSResponse getMergedResponse()throws DSCallException;
    
}