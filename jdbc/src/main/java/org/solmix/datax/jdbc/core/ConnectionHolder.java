package org.solmix.datax.jdbc.core;

import java.sql.Connection;


public interface ConnectionHolder
{
    Connection getConnection();

    /**
     * Release the JDBC Connection that this handle refers to.
     * @param con the JDBC Connection to release
     */
    void releaseConnection(Connection con);
}
