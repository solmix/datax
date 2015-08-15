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
package org.solmix.datax.jdbc.support;

import java.sql.Connection;

import org.solmix.datax.transaction.Transaction;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月11日
 */

public class ConnectionTransaction implements Transaction
{

    private final Connection conn;

    public ConnectionTransaction(Connection conn){
        this.conn=conn;
    }
   
    @Override
    public Object getTransactionObject() {
        return conn;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#commit()
     */
    @Override
    public void commit() {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#rollback()
     */
    @Override
    public void rollback() {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#reset()
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#isOpen()
     */
    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#released()
     */
    @Override
    public void released() {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.transaction.Transaction#requested()
     */
    @Override
    public void requested() {
        // TODO Auto-generated method stub
        
    }

}
