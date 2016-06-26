/*
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
package org.solmix.datax.jdbc.sql;

import java.util.Iterator;
import java.util.List;

import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.datax.jdbc.JdbcDataService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class SQLTableClause
{
    private List<String> relatedTables;
    
    private final List<JdbcDataService> dataSources;
    
    public SQLTableClause(JdbcDataService ds)
    {
        this(DataUtils.makeList(ds));
    }
    
    public SQLTableClause(List<JdbcDataService> dataSources)
    {
        this.dataSources = dataSources;
    }
    /**
     * @return the relatedTables
     */
    public List<String> getRelatedTables() {
        return relatedTables;
    }

    /**
     * @param relatedTables the relatedTables to set
     */
    public void setRelatedTables(List<String> relatedTables) {
        this.relatedTables = relatedTables;
    }
    
    public String getSQLString() {
        if (dataSources == null) {
           return ObjectUtils.EMPTY_STRING;
        }
        StringBuffer _buf = new StringBuffer();
        Iterator<JdbcDataService> i = dataSources.iterator();
        while (i.hasNext()) {
            JdbcDataService ds = i.next();
            Object shema = ds.getDataServiceInfo().getProperty("sqlSchema");
            if (shema != null) {
                _buf.append(shema).append(ds.getDialect().getQualifiedSchemaSeparator());
            }
            _buf.append(ds.getTable().getName());
            if (DataUtils.isNotNullAndEmpty(relatedTables))
                for (String table : relatedTables)
                    _buf.append("," + table);
            if (i.hasNext())
                _buf.append(", ");
        }
        return _buf.toString();
    }
}
