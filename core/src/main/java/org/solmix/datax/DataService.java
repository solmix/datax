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

import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.validation.ValidationException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月18日
 */

public interface DataService extends FreeResourcesHandler
{
    /**
     * 服务名称
     * 
     * @return
     */
    String getId();
    
    /**
     * 服务实现类别
     * 
     * @return
     */
    String getServerType();
    
    DSResponse execute(DSRequest req) throws DSCallException;

    /**
     * 根据DataService Field 的配置返回格式化的记录.
     * 
     * @param singleData
     * @return
     */
    Map<Object, Object> getProperties(Object data);
    
    DataServiceInfo getDataServiceInfo();

    /**
     * 自动组装时，可以判断当前是服务是否存在对应字段的值。
     * @param realFieldName
     * @param value
     * @return
     */
//    @Deprecated
    boolean hasRecord(String realFieldName, Object value);

    /**
     * 验证请求.
     * 
     * @param req
     * @return
     */
    List<Object> validateDSRequst(DSRequest req) throws ValidationException;

    /**
     * 请求是否应该加入事物处理中。
     * 
     * @param req
     * @return
     */
    boolean canJoinTransaction(DSRequest req);

    /**
     * 为Velocity提供的接口，允许Velocity将不识别的引用交给DataService做特殊处理
     * 
     * @param data
     * @param reference
     * @return
     */
    Object escapeValue(Object data, String reference);

    /**
     * @param req
     * @param ignoreExistingTransaction 忽略当前策略
     * @return
     */
    boolean canStartTransaction(DSRequest req, boolean ignoreExistingTransaction);
}
