/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.datax.wmix.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Service;
import org.solmix.wmix.exchange.AbstractWmixEndpoint;
import org.solmix.wmix.exchange.WmixMessage;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月16日
 */

public class MockEndpoint extends AbstractWmixEndpoint
{

    private static final long serialVersionUID = 3288147415900435552L;

    private static final Logger LOG = LoggerFactory.getLogger(MockEndpoint.class);
    @Override
    public void service(WmixMessage message) {
        
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Service createService() {
        return new MockService();
    }

}
