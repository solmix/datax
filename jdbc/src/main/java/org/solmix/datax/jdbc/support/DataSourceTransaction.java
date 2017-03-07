package org.solmix.datax.jdbc.support;

import org.solmix.runtime.transaction.TransactionIsolation;


public class DataSourceTransaction extends JdbcTransactionSupport {

	private boolean newConnectionHolder;

	private boolean mustRestoreAutoCommit;

	private TransactionIsolation previousTransactionIsolation;

	public void setConnectionHolder(ConnectionBinder connectionHolder, boolean newConnectionHolder) {
		super.setConnectionBinder(connectionHolder);
		this.newConnectionHolder = newConnectionHolder;
	}

	public boolean isNewConnectionHolder() {
		return this.newConnectionHolder;
	}

	public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
		this.mustRestoreAutoCommit = mustRestoreAutoCommit;
	}

	public boolean isMustRestoreAutoCommit() {
		return this.mustRestoreAutoCommit;
	}

	public void setRollbackOnly() {
		getConnectionBinder().setRollbackOnly();
	}

	public boolean isRollbackOnly() {
		return getConnectionBinder().isRollbackOnly();
	}

	public void setPreviousTransactionIsolation(
			TransactionIsolation previousTransactionIsolation) {
		this.previousTransactionIsolation=previousTransactionIsolation;
		
	}

	public TransactionIsolation getPreviousTransactionIsolation() {
		return previousTransactionIsolation;
	}
	
}
