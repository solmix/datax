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
package org.solmix.datax.wmix.context;

import java.util.Set;

import org.solmix.datax.RequestContext;
import org.solmix.exchange.Exchange;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月18日
 */

public class ExchangeRequestContext implements RequestContext
{
    private  Exchange exchange;

    public ExchangeRequestContext(Exchange exchange)
    {
        this.exchange=exchange;
    }


    @Override
    public void put(String key, Object value) {
        exchange.put(key, value);
    }

   
    @Override
    public Object get(String key) {
        return exchange.get(key);
    }

   
    @Override
    public void remove(String key) {
        exchange.remove(key);
    }

    
    @Override
    public boolean containsKey(String key) {
        return exchange.containsKey(key);
    }

   
    @Override
    public Set<String> keySet() {
        return exchange.keySet();
    }

    
    @Override
    public <T> T get(Class<T> key) {
        return exchange.get(key);
    }

    
    @Override
    public <T> void put(Class<T> key, T value) {
        exchange.put(key, value);
    }

}
