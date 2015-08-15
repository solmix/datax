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

package org.solmix.datax.jdbc.helper;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallUtils;
import org.solmix.datax.jdbc.GetConnectionException;
import org.solmix.datax.jdbc.support.ConnectionTransaction;
import org.solmix.datax.jdbc.support.ConnectionWrapperedTransaction;
import org.solmix.datax.transaction.Transaction;
import org.solmix.datax.transaction.TransactionService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月12日
 */

public class DataSourceHelper
{

    static final Logger LOG = LoggerFactory.getLogger(DataSourceHelper.class);

    static final DataSourceHelper HELPER;
    static {
        DataSourceHelper theHelper = null;
        try {
            theHelper = new SpringTxDataSourceHelper();
        } catch (Throwable ex) {
            theHelper = new DataSourceHelper();
        }
        HELPER = theHelper;
    }

    public Connection doGetConnectionInternal(DataSource dataSource) throws SQLException {
        Assert.assertNotNull(dataSource, "No DataSource specified");
        Connection conn = null;
        DSCall dsc = DSCallUtils.getDSCall();
        if (dsc != null) {
            TransactionService ts = dsc.getTransactionService();
            if (ts != null) {
                Transaction trans = ts.getResource(dataSource);
                conn = getConnection(trans);
            }
        }
        if (conn == null) {
            LOG.debug("Fetching JDBC Connection from DataSource");
            conn = dataSource.getConnection();
            if (dsc != null) {
                LOG.debug("Registering transaction for JDBC Connection");
                ConnectionTransaction connt = new ConnectionTransaction(conn);
                connt.released();
                dsc.getTransactionService().bindResource(dataSource, connt);
            }
        }
        return conn;
    }

    public void releaseConnectionInternal(Connection con, DataSource dataSource) {
        try {
            doReleaseConnectionInternal(con, dataSource);
        } catch (SQLException ex) {
            LOG.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            LOG.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    public void doReleaseConnectionInternal(Connection con, DataSource dataSource) throws SQLException {
        if (con == null) {
            return;
        }
        if (dataSource != null && DSCallUtils.getDSCall() != null) {
            Transaction transaction = DSCallUtils.getDSCall().getTransactionService().getResource(dataSource);
            if (transaction != null) {
                transaction.released();
                return;
            }

        }
        LOG.debug("Returning JDBC Connection to DataSource");
        con.close();
    }

    public Connection getConnectionInternal(DataSource dataSource) {
        try {
            return doGetConnectionInternal(dataSource);
        } catch (SQLException ex) {
            throw new GetConnectionException("Could not get JDBC Connection", ex);
        }
    }

    public static Connection getConnection(Transaction trans) {
        if (trans instanceof ConnectionTransaction) {
            return (Connection) trans.getTransactionObject();

        } else if (trans instanceof ConnectionWrapperedTransaction) {
            return (Connection) trans.getTransactionObject();
        }
        return null;
    }

    public static void releaseConnection(Connection con, DataSource dataSource) {
        HELPER.releaseConnectionInternal(con, dataSource);
    }

    public static Connection getConnection(DataSource dataSource) {
        return HELPER.getConnectionInternal(dataSource);
    }
}
