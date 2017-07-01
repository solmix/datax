package org.solmix.datax.jdbc.core;

import java.sql.Connection;

import org.springframework.util.Assert;


public class SimpleConnectionHolder implements ConnectionHolder
{
    private final Connection connection;


    /**
     * Create a new SimpleConnectionHandle for the given Connection.
     * @param connection the JDBC Connection
     */
    public SimpleConnectionHolder(Connection connection) {
          Assert.notNull(connection, "Connection must not be null");
          this.connection = connection;
    }

    /**
     * Return the specified Connection as-is.
     */
    @Override
    public Connection getConnection() {
          return this.connection;
    }

    /**
     * This implementation is empty, as we're using a standard
     * Connection handle that does not have to be released.
     */
    @Override
    public void releaseConnection(Connection con) {
    }


    @Override
    public String toString() {
          return "SimpleConnectionHandle: " + this.connection;
    }

}
