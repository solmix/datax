package org.solmix.datax.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.solmix.commons.Version;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.Reflection;


public class SqlRowSetResultSetExtractor implements ResultSetExtractor<SqlRowSet> {

	private static final CachedRowSetFactory cachedRowSetFactory;

	static {
		if (Version.getMajorJavaVersion() >= Version.JAVA_17) {
			// using JDBC 4.1 RowSetProvider
			cachedRowSetFactory = new StandardCachedRowSetFactory();
		}
		else {
			// JDBC 4.1 API not available - fall back to Sun CachedRowSetImpl
			cachedRowSetFactory = new SunCachedRowSetFactory();
		}
	}


	@Override
	public SqlRowSet extractData(ResultSet rs) throws SQLException {
		return createSqlRowSet(rs);
	}

	/**
	 * Create a SqlRowSet that wraps the given ResultSet,
	 * representing its data in a disconnected fashion.
	 * <p>This implementation creates a Spring ResultSetWrappingSqlRowSet
	 * instance that wraps a standard JDBC CachedRowSet instance.
	 * Can be overridden to use a different implementation.
	 * @param rs the original ResultSet (connected)
	 * @return the disconnected SqlRowSet
	 * @throws SQLException if thrown by JDBC methods
	 * @see #newCachedRowSet
	 * @see org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet
	 */
	protected SqlRowSet createSqlRowSet(ResultSet rs) throws SQLException {
		CachedRowSet rowSet = newCachedRowSet();
		rowSet.populate(rs);
		return new ResultSetWrappingSqlRowSet(rowSet);
	}

	/**
	 * Create a new CachedRowSet instance, to be populated by
	 * the {@code createSqlRowSet} implementation.
	 * <p>The default implementation uses JDBC 4.1's RowSetProvider
	 * when running on JDK 7 or higher, falling back to Sun's
	 * {@code com.sun.rowset.CachedRowSetImpl} class on older JDKs.
	 * @return a new CachedRowSet instance
	 * @throws SQLException if thrown by JDBC methods
	 * @see #createSqlRowSet
	 */
	protected CachedRowSet newCachedRowSet() throws SQLException {
		return cachedRowSetFactory.createCachedRowSet();
	}


	/**
	 * Internal strategy interface for the creation of CachedRowSet instances.
	 */
	private interface CachedRowSetFactory {

		CachedRowSet createCachedRowSet() throws SQLException;
	}


	/**
	 * Inner class to avoid a hard dependency on JDBC 4.1 RowSetProvider class.
	 */
	private static class StandardCachedRowSetFactory implements CachedRowSetFactory {

		private final RowSetFactory rowSetFactory;

		public StandardCachedRowSetFactory() {
			try {
				this.rowSetFactory = RowSetProvider.newFactory();
			}
			catch (SQLException ex) {
				throw new IllegalStateException("Cannot create RowSetFactory through RowSetProvider", ex);
			}
		}

		@Override
		public CachedRowSet createCachedRowSet() throws SQLException {
			return this.rowSetFactory.createCachedRowSet();
		}
	}


	/**
	 * Inner class to avoid a hard dependency on Sun's CachedRowSetImpl class.
	 */
	private static class SunCachedRowSetFactory implements CachedRowSetFactory {

		private static final Class<?> implementationClass;

		static {
			try {
				implementationClass = ClassLoaderUtils.loadClass("com.sun.rowset.CachedRowSetImpl",
						SqlRowSetResultSetExtractor.class);
			}
			catch (Throwable ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public CachedRowSet createCachedRowSet() throws SQLException {
			try {
				return (CachedRowSet) implementationClass.newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

}