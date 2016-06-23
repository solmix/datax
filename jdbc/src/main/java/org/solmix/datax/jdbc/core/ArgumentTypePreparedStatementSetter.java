package org.solmix.datax.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.Types;
import java.util.Collection;


/**
 * Simple adapter for {@link PreparedStatementSetter} that applies
 * given arrays of arguments and JDBC argument types.
 *
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final Object[] args;

	private final int[] argTypes;


	/**
	 * Create a new ArgTypePreparedStatementSetter for the given arguments.
	 * @param args the arguments to set
	 * @param argTypes the corresponding SQL types of the arguments
	 */
	public ArgumentTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
				(args != null && args.length != argTypes.length)) {
			throw new JdbcException("args and argTypes parameters must match");
		}
		this.args = args;
		this.argTypes = argTypes;
	}


	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		int parameterPosition = 1;
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				if (arg instanceof Collection && this.argTypes[i] != Types.ARRAY) {
					Collection<?> entries = (Collection<?>) arg;
					for (Object entry : entries) {
						if (entry instanceof Object[]) {
							Object[] valueArray = ((Object[]) entry);
							for (Object argValue : valueArray) {
								doSetValue(ps, parameterPosition, this.argTypes[i], argValue);
								parameterPosition++;
							}
						}
						else {
							doSetValue(ps, parameterPosition, this.argTypes[i], entry);
							parameterPosition++;
						}
					}
				}
				else {
					doSetValue(ps, parameterPosition, this.argTypes[i], arg);
					parameterPosition++;
				}
			}
		}
	}

	/**
	 * Set the value for the prepared statement's specified parameter position using the passed in
	 * value and type. This method can be overridden by sub-classes if needed.
	 * @param ps the PreparedStatement
	 * @param parameterPosition index of the parameter position
	 * @param argType the argument type
	 * @param argValue the argument value
	 * @throws SQLException
	 */
	protected void doSetValue(PreparedStatement ps, int parameterPosition, int argType, Object argValue)
			throws SQLException {

		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, argValue);
	}

	@Override
	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
