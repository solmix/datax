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
package org.solmix.datax.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.solmix.datax.jdbc.core.JdbcSupport;
import org.solmix.runtime.Container;
import org.solmix.runtime.support.spring.SpringContainerFactory;
import org.springframework.context.ApplicationContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月11日
 */

public abstract class AbstractSQLTest
{
    public static final String CREATE_TABLE_USER="CREATE TABLE IF NOT EXISTS AUTH_USERS (USER_ID integer not null,USER_NAME varchar(20),PASSWORD varchar(100),TRUENAME varchar(20) ,primary key (USER_ID))";
    public static final String CREATE_TABLE_ROLE="CREATE TABLE IF NOT EXISTS  AUTH_ROLES(ROLE_ID      integer not null, ROLE_NAME varchar(20) , ROLE_TITLE  varchar(20) , primary key (ROLE_ID))";
    public static final String CREATE_TABLE_HA="CREATE TABLE IF NOT EXISTS  HA(timeflag TIMESTAMP)";

    public static final String CREATE_TABLE_SEQ="create sequence AUTH_SEQ_ID start with 1700  increment by 1 cache 200";
    public static final String   TRUNCATE_TABLE_USER   = "TRUNCATE TABLE AUTH_USERS";
    public static final String   TRUNCATE_TABLE_ROLE   = "TRUNCATE TABLE AUTH_ROLES";
    public static final String   TRUNCATE_TABLE_HA   = "TRUNCATE TABLE HA";
    
    protected Container container;
    protected SqlSessionFactory sessionFactory;
    protected ApplicationContext applicationContext;
    protected  JdbcSupport jdbc1m;
    protected  JdbcSupport jdbc1s;
    protected  JdbcSupport jdbc2m;
    protected  JdbcSupport jdbc2s;
    public AbstractSQLTest(String[] location){
        SpringContainerFactory factory  = new SpringContainerFactory();
        container= factory.createContainer(location,true);
        applicationContext=container.getExtension(ApplicationContext.class);
        sessionFactory=container.getExtension(SqlSessionFactory.class);
        
        Assert.assertNotNull(applicationContext);
        Assert.assertNotNull(sessionFactory);
        
        
        jdbc1m= new JdbcSupport((DataSource) applicationContext.getBean("partition1_main"));
        jdbc1s= new JdbcSupport((DataSource) applicationContext.getBean("partition1_standby"));
        jdbc2m= new JdbcSupport((DataSource) applicationContext.getBean("partition2_main"));
        jdbc2s= new JdbcSupport((DataSource) applicationContext.getBean("partition2_standby"));
        
        jdbc1m.execute(CREATE_TABLE_USER);
        jdbc1m.execute(CREATE_TABLE_ROLE);
        jdbc1m.execute(CREATE_TABLE_HA);
        jdbc1m.execute(CREATE_TABLE_SEQ);
        
        jdbc1s.execute(CREATE_TABLE_USER);
        jdbc1s.execute(CREATE_TABLE_ROLE);
        jdbc1s.execute(CREATE_TABLE_HA);
        jdbc1s.execute(CREATE_TABLE_SEQ);
        
        jdbc2m.execute(CREATE_TABLE_USER);
        jdbc2m.execute(CREATE_TABLE_ROLE);
        jdbc2m.execute(CREATE_TABLE_HA);
        jdbc2m.execute(CREATE_TABLE_SEQ);
        
        jdbc2s.execute(CREATE_TABLE_USER);
        jdbc2s.execute(CREATE_TABLE_ROLE);
        jdbc2s.execute(CREATE_TABLE_HA);
        jdbc2s.execute(CREATE_TABLE_SEQ);
        
    }
    @BeforeClass
    public static void setupDb(){
      
    }
    
    @Before
    public  void setup(){
        jdbc1m.execute(TRUNCATE_TABLE_USER);
        jdbc1m.execute(TRUNCATE_TABLE_ROLE);
        jdbc1m.execute(TRUNCATE_TABLE_HA);
        
        jdbc1s.execute(TRUNCATE_TABLE_USER);
        jdbc1s.execute(TRUNCATE_TABLE_ROLE);
        jdbc1s.execute(TRUNCATE_TABLE_HA);
        
        jdbc2m.execute(TRUNCATE_TABLE_USER);
        jdbc2m.execute(TRUNCATE_TABLE_ROLE);
        jdbc2m.execute(TRUNCATE_TABLE_HA);
        
        jdbc2s.execute(TRUNCATE_TABLE_USER);
        jdbc2s.execute(TRUNCATE_TABLE_ROLE);
        jdbc2s.execute(TRUNCATE_TABLE_HA);
    }
}
