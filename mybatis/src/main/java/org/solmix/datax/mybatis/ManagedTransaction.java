package org.solmix.datax.mybatis;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.jdbc.helper.DataSourceHelper;
import org.solmix.datax.jdbc.support.ConnectionBinder;
import org.solmix.runtime.transaction.support.TxSynchronizer;

public class ManagedTransaction implements Transaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedTransaction.class);

	  private final DataSource dataSource;

	  private Connection connection;

	  private boolean isConnectionTransactional;

	  private boolean autoCommit;

	  public ManagedTransaction(DataSource dataSource) {
	    Assert.isNotNull(dataSource, "No DataSource specified");
	    this.dataSource = dataSource;
	  }

	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public Connection getConnection() throws SQLException {
	    if (this.connection == null) {
	      openConnection();
	    }
	    return this.connection;
	  }

	  /**
	   * Gets a connection from Spring transaction manager and discovers if this
	   * {@code Transaction} should manage connection or let it to Spring.
	   * <p>
	   * It also reads autocommit setting because when using Spring Transaction MyBatis
	   * thinks that autocommit is always false and will always call commit/rollback
	   * so we need to no-op that calls.
	   */
	  private void openConnection() throws SQLException {
	    this.connection = DataSourceHelper.getConnection(this.dataSource);
	    this.autoCommit = this.connection.getAutoCommit();
	    this.isConnectionTransactional = DataSourceHelper.isConnectionTransactional(this.connection, this.dataSource);

	    if (LOGGER.isDebugEnabled()) {
	      LOGGER.debug(
	          "JDBC Connection ["
	              + this.connection
	              + "] will"
	              + (this.isConnectionTransactional ? " " : " not ")
	              + "be managed by Spring");
	    }
	  }

	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public void commit() throws SQLException {
	    if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
	      if (LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Committing JDBC Connection [" + this.connection + "]");
	      }
	      this.connection.commit();
	    }
	  }

	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public void rollback() throws SQLException {
	    if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
	      if (LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Rolling back JDBC Connection [" + this.connection + "]");
	      }
	      this.connection.rollback();
	    }
	  }

	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public void close() throws SQLException {
		  DataSourceHelper.releaseConnection(this.connection, this.dataSource);
	  }
	    
	
	  public Integer getTimeout() throws SQLException {
		  ConnectionBinder holder = (ConnectionBinder) TxSynchronizer.getResource(dataSource);
	    if (holder != null && holder.hasTimeout()) {
	      return holder.getTimeToLiveInSeconds();
	    } 
	    return null;
	  }


}
