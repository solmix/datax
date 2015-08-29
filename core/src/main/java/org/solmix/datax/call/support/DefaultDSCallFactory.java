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

package org.solmix.datax.call.support;

import javax.annotation.PostConstruct;

import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallFactory;
import org.solmix.datax.call.support.DSCallImpl.STATUS;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.runtime.transaction.TransactionServiceFactory;
import org.solmix.runtime.transaction.support.DefaultTransactionServiceFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月12日
 */

public class DefaultDSCallFactory implements DSCallFactory
{

    private TransactionServiceFactory transactionServiceFactory;

    @Override
    public DSCall createDSCall(TransactionPolicy policy) {
        return new DSCallImpl(STATUS.BEGIN,transactionServiceFactory.createTransactionService(), policy);
    }

    @Override
    public DSCall createDSCall() {
        return new DSCallImpl(transactionServiceFactory.createTransactionService());
    }
    
    @PostConstruct
    public void init(){
        if(transactionServiceFactory==null){
            transactionServiceFactory= new DefaultTransactionServiceFactory();
        }
    }
    
    public TransactionServiceFactory getTransactionServiceFactory() {
        return transactionServiceFactory;
    }

    
    public void setTransactionServiceFactory(TransactionServiceFactory transactionServiceFactory) {
        this.transactionServiceFactory = transactionServiceFactory;
    }

}
