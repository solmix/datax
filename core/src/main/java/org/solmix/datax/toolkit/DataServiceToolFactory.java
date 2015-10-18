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
package org.solmix.datax.toolkit;

import javax.annotation.Resource;

import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataxSession;
import org.solmix.datax.support.DataxSessionImpl;
import org.solmx.service.toolkit.ToolFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月28日
 */

public class DataServiceToolFactory implements ToolFactory
{

    @Resource
    private DataServiceManager dataServiceManager;
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmx.service.toolkit.ToolFactory#createTool()
     */
    @Override
    public Object createTool() throws Exception {
        return this;
    }
    
    public Object fetch(String statement ,String parameter){
        DataxSession ds = new DataxSessionImpl(dataServiceManager);
        return ds.fetchOne(statement, parameter, Object.class);
    }

    public Object fetch(String statement ){
        DataxSession ds = new DataxSessionImpl(dataServiceManager);
        return ds.fetchOne(statement, null, Object.class);
    }

}