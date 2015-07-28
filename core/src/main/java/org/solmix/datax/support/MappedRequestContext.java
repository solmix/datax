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
package org.solmix.datax.support;

import java.util.Map;
import java.util.Set;

import org.solmix.commons.collections.StringTypeMapper;
import org.solmix.datax.RequestContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月27日
 */

public class MappedRequestContext extends AbstractRequestContext
{

    private final StringTypeMapper map;

    public MappedRequestContext() {
        this(null, null);
    }

    public MappedRequestContext(RequestContext parentContext) {
        this(null, parentContext);
    }

    public MappedRequestContext(Map<String, Object> map) {
        this(map, null);
    }

    public MappedRequestContext(Map<String, Object> map, RequestContext parentContext) {
        super(parentContext);

        if (map == null) {
            map = new StringTypeMapper();
        }
        this.map = new StringTypeMapper(map);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    protected boolean internalContainsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    protected Object internalGet(String key) {
        return map.get(key);
    }

    @Override
    protected void internalPut(String key, Object value) {
        map.put(key, value);
    }

    @Override
    protected Set<String> internalKeySet() {
        return map.keySet();
    }

    @Override
    protected void internalRemove(String key) {
        map.remove(key);
    }

    @Override
    public <T> T get(Class<T> key) {
        return map.get(key);
    }

    @Override
    public <T> void put(Class<T> key, T value) {
       map.put(key, value);
        
    }

}
