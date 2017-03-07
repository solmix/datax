package org.solmix.datax.jdbc.support;

import java.sql.Savepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.runtime.transaction.CreateTransactionException;
import org.solmix.runtime.transaction.SavepointSupport;
import org.solmix.runtime.transaction.TransactionException;

public abstract class JdbcTransactionSupport implements SavepointSupport{

	private static final Logger LOG = LoggerFactory.getLogger(JdbcTransactionSupport.class);
	
	private boolean savepointAllowed = false;
	private ConnectionBinder connectionBinder;
	@Override
	public Object createSavepoint() throws TransactionException {
		ConnectionBinder conHolder = getConnectionBinderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new TransactionException(
						"Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
			}
		}
		catch (Throwable ex) {
			throw new TransactionException(
					"Cannot create a nested transaction because your JDBC driver is not a JDBC 3.0 driver", ex);
		}
		try {
			return conHolder.createSavepoint();
		}
		catch (Throwable ex) {
			throw new CreateTransactionException("Could not create JDBC savepoint", ex);
		}
	}
	
	protected ConnectionBinder getConnectionBinderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new TransactionException(
					"Transaction manager does not allow nested transactions");
		}
		if (!hasConnectionBinder()) {
			throw new TransactionException(
					"Cannot create nested transaction if not exposing a JDBC transaction");
		}
		return getConnectionBinder();
	}
	
	public ConnectionBinder getConnectionBinder() {
		return this.connectionBinder;
	}
	
	public void setConnectionBinder(ConnectionBinder connectionHolder) {
		this.connectionBinder = connectionHolder;
	}

	public boolean hasConnectionBinder() {
		return (this.connectionBinder != null);
	}
	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	public boolean isSavepointAllowed() {
		return this.savepointAllowed;
	}

	@Override
	public void rollbackToSavepoint(Object savepoint)
			throws TransactionException {
		try {
			getConnectionBinderForSavepoint().getConnection().rollback((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			throw new TransactionException("Could not roll back to JDBC savepoint", ex);
		}
	}

	@Override
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionBinderForSavepoint().getConnection().releaseSavepoint((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			LOG.debug("Could not explicitly release JDBC savepoint", ex);
		}
	}

}
