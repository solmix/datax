package org.solmix.datax.jdbc.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.solmix.runtime.transaction.support.AbstractResourceBinder;

public class ConnectionBinder extends AbstractResourceBinder {
	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";


	private Connection currentConnection;

	private boolean transactionActive = false;

	private Boolean savepointsSupported;

	private int savepointCounter = 0;


	public ConnectionBinder(Connection connection) {
		this.currentConnection = connection;
	}

	
	public ConnectionBinder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}



	/**
	 * Return whether this holder currently has a Connection.
	 */
	public boolean hasConnection() {
		return (this.currentConnection != null);
	}

	/**
	 * Set whether this holder represents an active, JDBC-managed transaction.
	 * @see DataSourceTransactionManager
	 */
	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	/**
	 * Return whether this holder represents an active, JDBC-managed transaction.
	 */
	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * Override the existing Connection handle with the given Connection.
	 * Reset the handle if given {@code null}.
	 * <p>Used for releasing the Connection on suspend (with a {@code null}
	 * argument) and setting a fresh Connection on resume.
	 */
	public void setConnection(Connection connection) {
		this.currentConnection =connection;
	}

	/**
	 * Return the current Connection held by this ConnectionBinder.
	 * <p>This will be the same Connection until {@code released}
	 * gets called on the ConnectionBinder, which will reset the
	 * held Connection, fetching a new Connection on demand.
	 * @see ConnectionHandle#getConnection()
	 * @see #released()
	 */
	public Connection getConnection() {
	
		return this.currentConnection;
	}

	/**
	 * Return whether JDBC 3.0 Savepoints are supported.
	 * Caches the flag for the lifetime of this ConnectionBinder.
	 * @throws SQLException if thrown by the JDBC driver
	 */
	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = new Boolean(getConnection().getMetaData().supportsSavepoints());
		}
		return this.savepointsSupported.booleanValue();
	}

	/**
	 * Create a new JDBC 3.0 Savepoint for the current Connection,
	 * using generated savepoint names that are unique for the Connection.
	 * @return the new Savepoint
	 * @throws SQLException if thrown by the JDBC driver
	 */
	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	/**
	 * Releases the current Connection held by this ConnectionBinder.
	 * <p>This is necessary for ConnectionHandles that expect "Connection borrowing",
	 * where each returned Connection is only temporarily leased and needs to be
	 * returned once the data operation is done, to make the Connection available
	 * for other operations within the same transaction. This is the case with
	 * JDO 2.0 DataStoreConnections, for example.
	 */
	@Override
	public void released() {
		super.released();
		if (!isOpen() && this.currentConnection != null) {
			this.currentConnection = null;
		}
	}


	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}
}
