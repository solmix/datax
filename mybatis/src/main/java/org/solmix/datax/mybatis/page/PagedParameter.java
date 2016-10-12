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

package org.solmix.datax.mybatis.page;

import org.apache.ibatis.session.SqlSession;
import org.solmix.commons.pager.PageControl;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.jdbc.dialect.SQLDialect;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月11日
 */

public class PagedParameter
{

     final DSRequest request;

     final DSResponse response;

     final Object values;

     final SQLDialect sqlDialect;

    
     final SqlSession sqlSession;
    
     final PageControl page;

    public PagedParameter(DSRequest req, DSResponse res, Object values,
        SQLDialect defaultDialect,SqlSession session, PageControl page)
    {
        this.request = req;
        this.response = res;
        this.values = values;
        this.sqlDialect = defaultDialect;
        this.sqlSession=session;
        this.page=page;
    }

    public DSRequest getRequest() {
        return request;
    }

    public DSResponse getResponse() {
        return response;
    }

    public Object getValues() {
        return values;
    }

    public SQLDialect getSqlDialect() {
        return sqlDialect;
    }
    
    public SqlSession getSqlSession() {
        return sqlSession;
    }

}
