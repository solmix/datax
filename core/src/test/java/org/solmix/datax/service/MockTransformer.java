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
package org.solmix.datax.service;

import java.util.Map;

import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.transformer.TransformerAdaptor;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月2日
 */

public class MockTransformer extends TransformerAdaptor
{
    @Override
    public Object transformRequest(Object values,DSRequest request) {
       Map<String,Object> value=(Map<String,Object>)values;
       value.put("text", value.get("text")+"-transformRequest");
       request.setRawValues(value);
        return value;
    }

    @Override
    public Object transformResponse(Object value,DSResponse response,DSRequest req) {
        String tt=value.toString();
         tt=tt+"-transformResponse";
        return tt;
    }

}
