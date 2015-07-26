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

package org.solmix.datax.application.support;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;
import org.solmix.datax.application.Application;
import org.solmix.datax.application.ApplicationManager;
import org.solmix.datax.application.ApplicationSecurity;
import org.solmix.datax.support.DSResponseImpl;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月18日
 */
@Extension(name=ApplicationManager.BUILT_IN_APPLICATION)
public class BuiltInApplication implements Application
{

    private final static Logger LOG = LoggerFactory.getLogger(BuiltInApplication.class.getName());

    public static final String P_AUTHENTICATION_ENABLED = "authenticationEnabled";

    public static final String P_AUTHORIZATION_ENABLED = "authorizationEnabled";

    protected boolean authorizationEnabled;

    protected boolean authenticationEnabled;

    private String[] fmkDefinedOperations;

    private ApplicationSecurity security;

    private DataTypeMap appConfig;

    @Override
    public void init(Map<String, Object> config) {
        DataTypeMap dtm = DataTypeMap.typeof(config);
        this.appConfig = dtm;
        authorizationEnabled = appConfig.getBoolean(P_AUTHORIZATION_ENABLED, false);
        authenticationEnabled = appConfig.getBoolean(P_AUTHENTICATION_ENABLED, false);
        String operations = appConfig.getString("definedOperations", "*");
        fmkDefinedOperations = operations.split(",");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.application.Application#execute(org.solmix.datax.DSRequest,
     *      org.solmix.datax.RequestContext)
     */
    @Override
    public DSResponse execute(DSRequest request, RequestContext context) throws DSCallException {
         DSResponse result =null;
         String operationid =request.getOperationId();
         result= new DSResponseImpl(request.getDataService(), request);
         if(this.authenticationEnabled){
             if(this.security==null){
                throw new java.lang.IllegalStateException("BuildIn application configured enable authentication,but the runtime ENV not have applicationSecurity instance");
             }else{
                if( !security.isAuthenticated()){
                    result.setStatus(Status.STATUS_LOGIN_REQUIRED);
                    return result;
                }
             }
         }
         //log
        if (LOG.isDebugEnabled()) {
            MDC.put(DATAX.LOG_CONTEXT, request.getOperationId());
        }
        try{
            if(!isPermitted(request, context)){
                LOG.warn((new StringBuilder()).append("User does not qualify for any userTypes that are allowed to perform this operation ('").append(
                    operationid).append("')").toString());
                result.setStatus(Status.STATUS_AUTHORIZATION_FAILURE);
            }else{
                DataService ds =request.getDataService();
                result=ds.execute(request);
            }
            if (result != null && result.getStatus() == Status.UNSET) {
                result.setStatus(Status.STATUS_SUCCESS);
            }
        }finally{
            if(LOG.isDebugEnabled()){
                MDC.remove(DATAX.LOG_CONTEXT);
            }
        }
         
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.application.Application#setApplicationSecurity(org.solmix.datax.application.ApplicationSecurity)
     */
    @Override
    public void setApplicationSecurity(ApplicationSecurity security) {
        this.security=security;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.application.Application#isPermitted(org.solmix.datax.DSRequest,
     *      org.solmix.datax.RequestContext)
     */
    @Override
    public boolean isPermitted(DSRequest request, RequestContext context) {
        if(this.security!=null){
            return security.isPermitted(request, context);
        }
        return true;
    }

}
