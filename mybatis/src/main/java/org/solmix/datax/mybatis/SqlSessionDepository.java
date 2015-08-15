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
package org.solmix.datax.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.solmix.datax.jdbc.RoutingRequest;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月11日
 */

public class SqlSessionDepository
{
    private RoutingRequest request;
    private SqlSession sqlSession;
    private boolean usedTransaction;
    
    public RoutingRequest getRequest() {
        return request;
    }
    
    public void setRequest(RoutingRequest request) {
        this.request = request;
    }
    
    public SqlSession getSqlSession() {
        return sqlSession;
    }
    
    public void setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }
    
    public boolean isUsedTransaction() {
        return usedTransaction;
    }
    
    public void setUsedTransaction(boolean usedTransaction) {
        this.usedTransaction = usedTransaction;
    }

}
