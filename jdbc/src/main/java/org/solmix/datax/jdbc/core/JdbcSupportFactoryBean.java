/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.datax.jdbc.core;

import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.resource.ResourceManager;
import org.solmix.runtime.resource.support.ResourceResolverAdaptor;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月11日
 */

public class JdbcSupportFactoryBean extends ResourceResolverAdaptor implements ContainerAware
{

    private Container container;

    @Override
    public void setContainer(Container container) {
        this.container = container;
        ResourceManager rm = container.getExtension(ResourceManager.class);
        if (rm != null) {
            rm.addResourceResolver(this);
        }
    }
    
    @Override
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if (resourceName == null) {

        }
        return null;
    }

}
