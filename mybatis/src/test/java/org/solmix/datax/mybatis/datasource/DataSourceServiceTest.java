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
package org.solmix.datax.mybatis.datasource;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.datax.mybatis.MybatisDataServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.support.spring.SpringContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月11日
 */

public class DataSourceServiceTest
{

    @Test
    public void test(){
        SpringContainerFactory factory  = new SpringContainerFactory();
//       Container container= factory.createContainer("META-INF/spring/test-datasources-partation.xml");
       Container container= factory.createContainer("META-INF/spring/test-all.xml");
//       DataSourceService ds=  container.getExtension(DataSourceService.class);
       MybatisDataServiceFactory ds=  container.getExtension(MybatisDataServiceFactory.class);
       Assert.assertNotNull(ds);
    }
}
