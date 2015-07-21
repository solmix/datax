/*
 * Copyright 2012 The Solmix Project
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

package org.solmix.datax.application.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.application.Application;
import org.solmix.datax.application.ApplicationManager;
import org.solmix.datax.application.ApplicationSecurity;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 110035 2011-11-12
 */

public class DefaultApplicationManager implements ApplicationManager
{

    private Container container;

    private final ConcurrentHashMap<String, Application> providers = new ConcurrentHashMap<String, Application>(4);

    public DefaultApplicationManager()
    {
        this(null);
    }

    public DefaultApplicationManager(final Container container)
    {
        setApplicationManager(container);
    }

    /**
     * @param sc
     */
    private void setApplicationManager(final Container container) {
        this.container = container;
        if (container != null) {
            container.setExtension(this, ApplicationManager.class);
        }
    }

    private DataTypeMap getApplicationProperties() {
        DataServiceManager dsm = container.getExtension(DataServiceManager.class);
        Map<String, Object> props = dsm.getProperties();
        if(props==null){
            props= new HashMap<String, Object>();
        }
        DataTypeMap dtm = new DataTypeMap(props);
        return dtm.getSubtree("application");
    }

    /**
     * find the application by id,if the id is <code>null</code>,used as default "builtinApplication". {@inheritDoc}
     * 
     * @see org.solmix.api.application.ApplicationManager#findByID(java.lang.String)
     */
    @Override
    public Application findByID(String appID) {
        if (appID == null)
            appID = BUILT_IN_APPLICATION;
        Application application = providers.get(BUILT_IN_APPLICATION);
        if (application == null) {
            application = container.getExtensionLoader(Application.class).getExtension(appID);
            application.init(getApplicationProperties());
            application.setApplicationSecurity(findApplicationSecurity());
            providers.putIfAbsent(appID, application);
        }
        return application;
    }

    protected ApplicationSecurity findApplicationSecurity() {
        if (container != null) {
            return container.getExtension(ApplicationSecurity.class);
        }
        return null;
    }

}
