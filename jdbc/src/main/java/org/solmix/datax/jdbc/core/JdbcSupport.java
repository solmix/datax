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

package org.solmix.datax.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.datax.jdbc.SQLRuntimeException;
import org.solmix.datax.jdbc.helper.DataSourceHelper;
import org.solmix.datax.jdbc.helper.JdbcHelper;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月5日
 */

public class JdbcSupport
{

    private static final Logger LOG = LoggerFactory.getLogger(JdbcSupport.class);

    private DataSource dataSource;

    public JdbcSupport()
    {

    }

    public JdbcSupport(DataSource datasource)
    {
        this.dataSource = datasource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public int update(final String sql) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL update [" + sql + "]");
        }
       return execute(new StatementCallback<Integer>() {

            @Override
            public Integer doInStatement(Statement stmt) throws SQLException {
                int rows = stmt.executeUpdate(sql);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SQL update affected " + rows + " rows");
                }
                return rows;
            }
        });
    }
    public void execute(final String sql) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL statement [" + sql + "]");
        }
        execute(new StatementCallback<Object>() {

            @Override
            public Object doInStatement(Statement stmt) throws SQLException {
                stmt.execute(sql);
                return null;
            }
        });
    }
    
    public <T> T execute(StatementCallback<T> action){
        
        Connection conn= DataSourceHelper.getConnection(getDataSource());
        Statement stmt = null;
        try {
            Connection conToUse = conn;
            stmt = conToUse.createStatement();
            Statement stmtToUse = stmt;
            T result = action.doInStatement(stmtToUse);
            return result;
        }catch (SQLException ex) {
            JdbcHelper.closeStatement(stmt);
            stmt = null;
            DataSourceHelper.releaseConnection(conn, getDataSource());
            conn = null;
            throw new SQLRuntimeException(ex);
        }
        finally {
            JdbcHelper.closeStatement(stmt);
            DataSourceHelper.releaseConnection(conn, getDataSource());
      }
        
    }
    

}
