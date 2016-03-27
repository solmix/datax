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

package org.solmix.datax.jdbc.dialect;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.jdbc.helper.JdbcHelper;
import org.solmix.datax.jdbc.support.MetaDataAccessException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月11日
 */

public class SQLDialectFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(SQLDialectFactory.class);

    private final static SQLDialectFactory instance = new SQLDialectFactory();

    private Map<DataSource, SQLDialect> dataSourceCache = new WeakHashMap<DataSource, SQLDialect>(16);

    private final Map<String, SQLDialect> dialectMap = new HashMap<String, SQLDialect>();

    public static SQLDialectFactory getInstance() {
        return instance;
    }

    public SQLDialect getSQLDialect(String dbName) throws Exception {
        Assert.assertNotNull(dbName);
        SQLDialect dialect = this.dialectMap.get(dbName);
        if (dialect != null) {
            return dialect;
        } else {
            dialect = SQLDialect.instance(dbName);
            synchronized (dialectMap) {
                dialectMap.put(dbName, dialect);
            }
        }
        return dialect;
    }

    public SQLDialect getSQLDialect(DataSource dataSource) throws Exception {
        Assert.assertNotNull(dataSource);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking up default SQLDialect for DataSource [" + dataSource + "]");
        }
        synchronized (this.dataSourceCache) {
            SQLDialect dialect=null;
            try {
                dialect = this.dataSourceCache.get(dataSource);
            } catch (Exception e) {
                this.dataSourceCache= new WeakHashMap<DataSource, SQLDialect>(16);
            }
            if (dialect != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SQLDialect found in cache  for DataSource " + dataSource);
                }
                return dialect;
            }
            try {
                String dbName = (String) JdbcHelper.extractDatabaseMetaData(dataSource, "getDatabaseProductName");
                if (dbName != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Database product name cached for DataSource [" + dataSource.getClass().getName() + '@'
                            + Integer.toHexString(dataSource.hashCode()) + "]: name is '" + dbName + "'");
                    }
                    dialect = getSQLDialect(dbName);
                    this.dataSourceCache.put(dataSource, dialect);
                    return dialect;
                }
            } catch (MetaDataAccessException ex) {
                LOG.warn("Error while extracting database product name - falling back to empty error codes", ex);
            }
        }
        return null;
    }
}
