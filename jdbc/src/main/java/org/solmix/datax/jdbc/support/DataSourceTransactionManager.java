package org.solmix.datax.jdbc.support;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.jdbc.helper.DataSourceHelper;
import org.solmix.runtime.transaction.CreateTransactionException;
import org.solmix.runtime.transaction.TransactionException;
import org.solmix.runtime.transaction.TransactionInfo;
import org.solmix.runtime.transaction.TransactionIsolation;
import org.solmix.runtime.transaction.TransactionResourceProvider;
import org.solmix.runtime.transaction.support.AbstractTransactionManager;
import org.solmix.runtime.transaction.support.DefaultTransactionState;
import org.solmix.runtime.transaction.support.TxSynchronizer;

public class DataSourceTransactionManager extends AbstractTransactionManager implements TransactionResourceProvider{

	private static final Logger LOG = LoggerFactory.getLogger(DataSourceTransactionManager.class);
	private DataSource dataSource;
	
	public DataSourceTransactionManager(){
		setNestedTransactionAllowed(true);
	}
	
	public DataSourceTransactionManager(DataSource dataSource){
		this();
		Assert.isNotNull(dataSource,"dataSource must be not null");
		setDataSource(dataSource);
	}
	

	@Override
	protected Object doGetTransaction() throws TransactionException {
		DataSourceTransaction txObject = new DataSourceTransaction();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());
		ConnectionBinder conHolder =
			(ConnectionBinder) TxSynchronizer.getResource(this.dataSource);
		txObject.setConnectionHolder(conHolder, false);
		return txObject;
	}
	
	@Override
	protected void doBegin(Object transaction, TransactionInfo definition) {
		DataSourceTransaction txObject = (DataSourceTransaction) transaction;
		Connection con = null;
		try {
			if (txObject.getConnectionBinder() == null ||
					txObject.getConnectionBinder().isSynchronizedWithTransaction()) {
				Connection newCon = this.dataSource.getConnection();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
				}
				txObject.setConnectionHolder(new ConnectionBinder(newCon), true);
			}

			txObject.getConnectionBinder().setSynchronizedWithTransaction(true);
			con = txObject.getConnectionBinder().getConnection();

			TransactionIsolation previousIsolationLevel = DataSourceHelper.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousTransactionIsolation(previousIsolationLevel);

			// Switch to manual commit if necessary. This is very expensive in some JDBC drivers,
			// so we don't want to do it unnecessarily (for example if we've explicitly
			// configured the connection pool to set it already).
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Switching JDBC Connection [" + con + "] to manual commit");
				}
				con.setAutoCommit(false);
			}
			txObject.getConnectionBinder().setTransactionActive(true);

			int timeout = determineTimeout(definition);
			if (timeout != TransactionInfo.TIMEOUT_DEFAULT) {
				txObject.getConnectionBinder().setTimeoutInSeconds(timeout);
			}

			// Bind the session holder to the thread.
			if (txObject.isNewConnectionHolder()) {
				TxSynchronizer.bindResource(getDataSource(), txObject.getConnectionBinder());
			}
		}catch (Throwable ex) {
			DataSourceHelper.releaseConnection(con, this.dataSource);
			throw new CreateTransactionException("Could not open JDBC Connection for transaction", ex);
		}
	}
	
	@Override
	protected Object doSuspend(Object transaction) {
		DataSourceTransaction txObject = (DataSourceTransaction) transaction;
		txObject.setConnectionBinder(null);
		ConnectionBinder conHolder = (ConnectionBinder)TxSynchronizer.unbindResource(this.dataSource);
		return conHolder;
	}
	
	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		ConnectionBinder conHolder = (ConnectionBinder) suspendedResources;
		TxSynchronizer.bindResource(this.dataSource, conHolder);
	}

	
	@Override
	protected void doCommit(DefaultTransactionState status) {
		DataSourceTransaction txObject = (DataSourceTransaction) status.getTransaction();
		Connection con = txObject.getConnectionBinder().getConnection();
		if (status.isDebug()) {
			LOG.debug("Committing JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.commit();
		}
		catch (SQLException ex) {
			throw new TransactionException("Could not commit JDBC transaction", ex);
		}
	}
	
	@Override
	protected void doRollback(DefaultTransactionState status) {
		DataSourceTransaction txObject = (DataSourceTransaction) status.getTransaction();
		Connection con = txObject.getConnectionBinder().getConnection();
		if (status.isDebug()) {
			LOG.debug("Rolling back JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.rollback();
		}
		catch (SQLException ex) {
			throw new TransactionException("Could not roll back JDBC transaction", ex);
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionState status) {
		DataSourceTransaction txObject = (DataSourceTransaction) status.getTransaction();
		if (status.isDebug()) {
			LOG.debug("Setting JDBC transaction [" + txObject.getConnectionBinder().getConnection() +
					"] rollback-only");
		}
		txObject.setRollbackOnly();
	}
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransaction txObject = (DataSourceTransaction) transaction;

		// Remove the connection holder from the thread, if exposed.
		if (txObject.isNewConnectionHolder()) {
			TxSynchronizer.unbindResource(this.dataSource);
		}

		// Reset connection.
		Connection con = txObject.getConnectionBinder().getConnection();
		try {
			if (txObject.isMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}
			DataSourceHelper.resetConnectionAfterTransaction(con, txObject.getPreviousTransactionIsolation());
		}
		catch (Throwable ex) {
			LOG.debug("Could not reset JDBC Connection after transaction", ex);
		}

		if (txObject.isNewConnectionHolder()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Releasing JDBC Connection [" + con + "] after transaction");
			}
			DataSourceHelper.releaseConnection(con, this.dataSource);
		}

		txObject.getConnectionBinder().clear();
	}

	@Override
	public Object getResourceFactory() {
		return getDataSource();
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
