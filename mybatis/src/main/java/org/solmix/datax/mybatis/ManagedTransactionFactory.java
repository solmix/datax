package org.solmix.datax.mybatis;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

public class ManagedTransactionFactory implements TransactionFactory {

	@Override
	public void setProperties(Properties props) {

	}

	@Override
	public Transaction newTransaction(Connection conn) {
		throw new UnsupportedOperationException("New managed transactions require a DataSource");
	}

	@Override
	public Transaction newTransaction(DataSource dataSource,
			TransactionIsolationLevel level, boolean autoCommit) {
		return  new ManagedTransaction(dataSource);
	}

}
