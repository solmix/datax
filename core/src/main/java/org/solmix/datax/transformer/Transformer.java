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

package org.solmix.datax.transformer;

import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月29日
 */

public interface Transformer
{

    /**
     * 转换请求
     * 
     * @param requestData 请求数据和DSRequest.getRawValues()相同
     * @param request DS请求
     * @return
     * @throws Exception
     */
    Object transformRequest(Object requestData, DSRequest request) throws Exception;

    /**
     * 转换结果
     * 
     * @param responseData responseData 结果集和DSResponse.getRawData()相同
     * @param response
     * @return
     * @throws Exception
     */
    Object transformResponse(Object responseData, DSResponse response) throws Exception;

}
