/**
 * Copyright 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.datax.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2016年5月8日
 */

public class   ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;


    /**
     * Create a new ArgPreparedStatementSetter for the given arguments.
     * @param args the arguments to set
     */
    public ArgumentPreparedStatementSetter(Object[] args) {
          this.args = args;
    }


    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
          if (this.args != null) {
                for (int i = 0; i < this.args.length; i++) {
                      Object arg = this.args[i];
                      doSetValue(ps, i + 1, arg);
                }
          }
    }

    /**
     * Set the value for prepared statements specified parameter index using the passed in value.
     * This method can be overridden by sub-classes if needed.
     * @param ps the PreparedStatement
     * @param parameterPosition index of the parameter position
     * @param argValue the value to set
     * @throws SQLException
     */
    protected void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
          if (argValue instanceof SqlParameterValue) {
                SqlParameterValue paramValue = (SqlParameterValue) argValue;
                StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue, paramValue.getValue());
          }
          else {
                StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN, argValue);
          }
    }


}
