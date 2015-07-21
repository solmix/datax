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
import org.solmix.datax.model.OperationInfo;

/**
 * 代表一次DataService(DS)请求。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public interface DSRequest extends Pageable
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

    void freeResources();

    /**
     * 
     * @param FreeResourcesHandler handler
     */
    void registerFreeResourcesHandler(FreeResourcesHandler handler);

    /**
     * 全称 XXX.XXX.fetch
     * 
     * @return
     */
    String getOperationId();

    /**
     * 全称 XXX.XXX.fetch 相对：fetch
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

}
