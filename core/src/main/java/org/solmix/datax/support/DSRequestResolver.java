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

import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.call.DSCall;
import org.solmix.runtime.resource.support.ResourceResolverAdaptor;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月1日
 */

public class DSRequestResolver extends ResourceResolverAdaptor
{
    private final DSRequest request;
    public DSRequestResolver(DSRequest request){
        this.request=request;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if (resourceType == DSRequest.class) {
            return (T) request;
        } else if (resourceType == DSCall.class) {
            return (T) request.getDSCall();
        } else if (resourceName==null&&resourceType == DataService.class) {
            return (T) request.getDataService();
        }
        return null;
    }
}
