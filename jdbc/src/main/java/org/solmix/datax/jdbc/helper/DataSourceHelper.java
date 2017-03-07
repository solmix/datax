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
import org.solmix.datax.jdbc.GetConnectionException;
import org.solmix.datax.jdbc.support.ConnectionBinder;
import org.solmix.datax.jdbc.support.ConnectionTransaction;
import org.solmix.datax.jdbc.support.ConnectionWrapperedTransaction;
import org.solmix.runtime.transaction.Transaction;
import org.solmix.runtime.transaction.TransactionInfo;
import org.solmix.runtime.transaction.TransactionIsolation;
import org.solmix.runtime.transaction.support.TransactionListener;
import org.solmix.runtime.transaction.support.TxSynchronizer;

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
        ConnectionBinder conHolder = (ConnectionBinder) TxSynchronizer.getResource(dataSource);
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			conHolder.requested();
			if (!conHolder.hasConnection()) {
				LOG.debug("Fetching resumed JDBC Connection from DataSource");
				conHolder.setConnection(dataSource.getConnection());
			}
			return conHolder.getConnection();
		}
		// Else we either got no holder or an empty thread-bound holder here.

		LOG.debug("Fetching JDBC Connection from DataSource");
		Connection con = dataSource.getConnection();

		if (TxSynchronizer.isSynchronizationActive()) {
			LOG.debug("Registering transaction synchronization for JDBC Connection");
			// Use same Connection for further JDBC actions within the transaction.
			// Thread-bound object will get removed by synchronization at transaction completion.
			ConnectionBinder holderToUse = conHolder;
			if (holderToUse == null) {
				holderToUse = new ConnectionBinder(con);
			}
			else {
				holderToUse.setConnection(con);
			}
			holderToUse.requested();
			TxSynchronizer.registerSynchronization(
					new ConnectionListener(holderToUse, dataSource));
			holderToUse.setSynchronizedWithTransaction(true);
			if (holderToUse != conHolder) {
				TxSynchronizer.bindResource(dataSource, holderToUse);
			}
		}

		return con;
        
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
        if (dataSource != null) {
        	ConnectionBinder conHolder = (ConnectionBinder) TxSynchronizer.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// It's the transactional Connection: Don't close it.
				conHolder.released();
				return;
			}
        }
        LOG.debug("Returning JDBC Connection to DataSource");
        doCloseConnection(con, dataSource);
    }
    
    private void doCloseConnection(Connection con, DataSource dataSource) throws SQLException {
		con.close();
		
	}

	private static boolean connectionEquals(ConnectionBinder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				heldCon.equals(passedInCon));
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

	public static TransactionIsolation prepareConnectionForTransaction(
			Connection con, TransactionInfo definition) throws SQLException {
		Assert.isNotNull(con, "No Connection specified");

		// Set read-only flag.
		if (definition != null && definition.isReadOnly()) {
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Setting JDBC Connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (SQLException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from JDBC 4.0
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" SQLException -> ignore, it's just a hint anyway
				LOG.debug("Could not set JDBC Connection read-only", ex);
			}catch (RuntimeException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from Hibernate
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" UnsupportedOperationException -> ignore, it's just a hint anyway
				LOG.debug("Could not set JDBC Connection read-only", ex);
			}
		}

		// Apply specific isolation level, if any.
		TransactionIsolation previousIsolationLevel = null;
		if (definition != null && definition.getTransactionIsolation() != TransactionIsolation.DEFAULT) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Changing isolation level of JDBC Connection [" + con + "] to " +
						definition.getTransactionIsolation());
			}
			TransactionIsolation currentIsolation =TransactionIsolation.fromValue( con.getTransactionIsolation());
			if (currentIsolation != definition.getTransactionIsolation()) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(definition.getTransactionIsolation().value());
			}
		}
		return previousIsolationLevel;
	}


	
	public static void resetConnectionAfterTransaction(Connection con, TransactionIsolation previousIsolationLevel) {
		Assert.isNotNull(con, "No Connection specified");
		try {
			if (previousIsolationLevel != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Resetting isolation level of JDBC Connection [" +con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel.value());
			}
			if (con.isReadOnly()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			LOG.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}
	
	private static class ConnectionListener implements TransactionListener{
		private final ConnectionBinder connectionHolder;

		private final DataSource dataSource;

		private boolean holderActive = true;

		public ConnectionListener(ConnectionBinder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
		}

	

		@Override
		public void suspend() {
			if (this.holderActive) {
				TxSynchronizer.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					// Release Connection on suspend if the application doesn't keep
					// a handle to it anymore. We will fetch a fresh Connection if the
					// application accesses the ConnectionHolder again after resume,
					// assuming that it will participate in the same transaction.
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TxSynchronizer.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			// Release Connection early if the holder is not open anymore
			// (that is, not used by another resource like a Hibernate Session
			// that has its own cleanup via transaction synchronization),
			// to avoid issues with strict JTA implementations that expect
			// the close call before transaction completion.
			if (!this.connectionHolder.isOpen()) {
				TxSynchronizer.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			// If we haven't closed the Connection in beforeCompletion,
			// close it now. The holder might have been used for other
			// cleanup in the meantime, for example by a Hibernate Session.
			if (this.holderActive) {
				// The thread-bound ConnectionHolder might not be available anymore,
				// since afterCompletion might get called from a different thread.
				TxSynchronizer.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					// Reset the ConnectionHolder: It might remain bound to the thread.
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
		@Override
		public void flush() {
		}

		@Override
		public void beforeCommit(boolean readOnly) {
		}

		@Override
		public void afterCommit() {
		}
	}
}
