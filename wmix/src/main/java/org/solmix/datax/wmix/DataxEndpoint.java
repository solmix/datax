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
package org.solmix.datax.wmix;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Service;
import org.solmix.wmix.endpoint.AbstractWmixEndpoint;
import org.solmix.wmix.exchange.WmixMessage;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月13日
 */

public class DataxEndpoint extends AbstractWmixEndpoint implements Endpoint
{

    private static final Logger LOG = LoggerFactory.getLogger(DataxEndpoint.class);
    private static final long serialVersionUID = 7621213021932655937L;

    @Override
    public void service(WmixMessage message) {
       System.out.println("===================");
       try {
        getTransporter().invoke(message);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Service createService() {
        return new DataxService();
    }

}
