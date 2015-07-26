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
package org.solmix.datax.session.support;

import java.util.List;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.DataxException;
import org.solmix.datax.DataxRuntimeException;
import org.solmix.datax.Pageable;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.TransactionException;
import org.solmix.datax.call.TransactionFailedException;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.datax.session.DataxSession;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月24日
 */

public class DataxSessionImpl implements DataxSession
{

    private boolean isCall;
    private DSCall call;
    private DataServiceManager dataServiceManager;
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#fetchOne(java.lang.String)
     */
    @Override
    public <T> T fetchOne(String statement) {
       DSRequest req= createDSRequest();
       execute(req);
        return null;
    }

 
    private DSResponse execute(DSRequest req) {
        if (isCall && call != null) {
            try {
                return call.execute(req);
            } catch (TransactionFailedException e) {
                end();
                throw new TransactionException(e.getMessage(), e.getCause());
            } catch (DataxException e) {
                end();
                throw new DataxRuntimeException(e);
            }
        } else {
            try {
                return req.execute();
            } catch (DSCallException e) {
                throw new DataxRuntimeException(e);
            }
        }
    }

    /**
     * @return
     */
    private DSRequest createDSRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#fetchOne(java.lang.String, java.lang.Object)
     */
    @Override
    public <T> T fetchOne(String statement, Object parameter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#fetchList(java.lang.String)
     */
    @Override
    public <E> List<E> fetchList(String statement) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#fetchList(java.lang.String, java.lang.Object)
     */
    @Override
    public <E> List<E> fetchList(String statement, Object parameter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#fetchList(java.lang.String, java.lang.Object, org.solmix.datax.Pageable)
     */
    @Override
    public <E> List<E> fetchList(String statement, Object parameter, Pageable page) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#end()
     */
    @Override
    public void end() {
        isCall=false;
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#execute(java.util.List)
     */
    @Override
    public List<DSResponse> execute(List<DSRequest> requests) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#executeXA(java.util.List, org.solmix.datax.model.TransactionPolicy)
     */
    @Override
    public List<DSResponse> executeXA(List<DSRequest> requests, TransactionPolicy policy) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#getDataServiceManager()
     */
    @Override
    public DataServiceManager getDataServiceManager() {
        return dataServiceManager;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.session.DataxSession#begin(org.solmix.datax.model.TransactionPolicy)
     */
    @Override
    public void begin(TransactionPolicy policy) {
        isCall=true;
        
    }

}
