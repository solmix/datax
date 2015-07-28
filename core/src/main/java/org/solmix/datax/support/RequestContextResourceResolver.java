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

import org.solmix.datax.RequestContext;
import org.solmix.runtime.resource.support.ResourceResolverAdaptor;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月26日
 */

public class RequestContextResourceResolver extends ResourceResolverAdaptor
{
    private final RequestContext context;
    public RequestContextResourceResolver(RequestContext context){
        this.context=context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if(resourceType==RequestContext.class){
            return (T) context;
        }else if(context!=null){
            return context.get(resourceType);
        }
        return null;
    }
}
