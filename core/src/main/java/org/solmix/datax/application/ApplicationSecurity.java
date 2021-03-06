/*
 *  Copyright 2012 The Solmix Project
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

package org.solmix.datax.application;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;




/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2013-11-14
 */
public interface ApplicationSecurity
{

    /**
     * @param request
     * @param context RequestContext
     * @return
     */
    boolean isPermitted(DSRequest request, RequestContext context);

    /**
     * Returns the currently accessible available to the calling code depending on
     * runtime environment.
     * @return
     */
    boolean isAuthenticated();
    
    /**
     * Return the provider name.
     * @return
     */
    String getName();
    
    
    boolean isExclude(String operationid);

    DSResponse excute(DataService ds, DSRequest request) throws DSCallException ;
}
