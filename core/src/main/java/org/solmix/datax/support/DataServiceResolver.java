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

import java.io.IOException;

import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataxSession;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.runtime.resource.InputStreamResource;
import org.solmix.runtime.resource.ResourceResolver;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月2日
 */

public class DataServiceResolver implements ResourceResolver
{

    private DataServiceManager dataServiceManager;
    /**
     * @param defaultDataServiceManager
     */
    public DataServiceResolver(DataServiceManager dataServiceManager)
    {
        this.dataServiceManager=dataServiceManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        
        if(DataServiceInfo.class.isAssignableFrom(resourceType)){
            return (T) dataServiceManager.getRepositoryService().getDataService(resourceName);
        }else if(DataService.class.isAssignableFrom(resourceType)){
            return  (T)dataServiceManager.getDataService(resourceName);
        }else if(DataxSession.class==resourceType){
            return (T) new DataxSessionImpl(dataServiceManager);
        }
        return null;
    }

    @Override
    public InputStreamResource getAsStream(String location) {
        return null;
    }

    @Override
    public InputStreamResource[] getAsStreams(String locationPattern) throws IOException {
        return null;
    }

}
