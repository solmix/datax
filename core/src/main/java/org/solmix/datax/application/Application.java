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

package org.solmix.datax.application;

import java.util.Map;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.RequestContext;
import org.solmix.runtime.Extension;


/**
 * the instance of this interface may not thread safety.
 * @author administrator
 * @version 0.0.3 2011-11-11
 * @since 0.0.3
 */
@Extension
public interface Application
{

    enum UserType
    {
        /**
         * Defined As a ADMINISTRATOR User.
         */
        ADMIN_USER(2) ,
        /**
         * Defined As a AUTHENTICATION User.
         */
        AUTH_USER(1) ,
        /**
         * Defined As a ANONYMONUS User.
         */
        ANONY_USER(0);

        int value;

        UserType(int i)
        {
            value = i;
        }

        public int value() {
            return this.value;
        }

        public static UserType fromValue(int v) {
            for (UserType c : UserType.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException("" + v);
        }
    }

    public static final String BUILT_IN_APPLICATION = ApplicationManager.BUILT_IN_APPLICATION;


//     DataService getDataService(String serviceId) ;

   
     DSResponse execute(DSRequest request, RequestContext context) throws DSCallException;

    
    void  setApplicationSecurity(ApplicationSecurity security);

    public boolean isPermitted(DSRequest request, RequestContext context);


    /**
     * @param applicationProperties
     */
    void init(Map<String,Object> applicationProperties);

}
