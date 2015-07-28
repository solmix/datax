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

import static org.solmix.commons.util.ObjectUtils.NULL_PLACEHOLDER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.solmix.datax.RequestContext;
/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月27日
 */

public abstract class AbstractRequestContext implements RequestContext
{

    /** 父context，如果指定key在当前context不存在，则会在父context中查找。 */
    private final RequestContext parentContext;

    /** 创建一个context。 */
    public AbstractRequestContext()
    {
        this(null);
    }

    /** 创建一个context，指定parent context。 */
    public AbstractRequestContext(RequestContext parentContext)
    {
        this.parentContext = parentContext;
    }

    /** 取得父context，如不存在则返回<code>null</code>。 */
    public RequestContext getParentContext() {
        return parentContext;
    }

    /** 添加一个值。 */
    public final void put(String key, Object value) {
        if (value == null) {
            remove(key);
        } else {
            internalPut(key, value);
        }
    }

    /** 取得指定值。 */
    public final Object get(String key) {
        Object value = internalGet(key);

        if (value == null && parentContext != null) {
            return parentContext.get(key);
        }

        return decodeValue(value);
    }

    /** 删除一个值。 */
    public final void remove(String key) {
        if (parentContext != null && parentContext.containsKey(key)) {
            internalPut(key, NULL_PLACEHOLDER);
        } else {
            internalRemove(key);
        }
    }

    /** 判断是否包含指定的键。 */
    public final boolean containsKey(String key) {
        boolean containsKey = internalContainsKey(key);

        if (!containsKey && parentContext != null) {
            return parentContext.containsKey(key);
        }

        return containsKey;
    }

    /** 取得所有key的集合。 */
    public final Set<String> keySet() {
        Set<String> internalKeySet = internalKeySet();
        Set<String> parentKeySet = parentContext == null ? null : parentContext.keySet();

        if (parentKeySet == null || parentKeySet.isEmpty()) {
            return internalKeySet;
        }

        Set<String> newSet = new HashSet<String>();

        newSet.addAll(parentKeySet);
        newSet.addAll(internalKeySet);

        return newSet;
    }

    /** 取得所有key的集合。 */
    protected abstract Set<String> internalKeySet();

    /** 取得指定值。 */
    protected abstract Object internalGet(String key);

    /** 删除一个值。 */
    protected abstract void internalRemove(String key);

    /** 判断是否包含指定的键。 */
    protected abstract boolean internalContainsKey(String key);

    /** 添加一个值。 */
    protected abstract void internalPut(String key, Object value);

    /** 解码context的值。如果为<code>NULL_PLACEHOLDER</code>，则返回<code>null</code>。 */
    private Object decodeValue(Object value) {
        return value == NULL_PLACEHOLDER ? null : value;
    }

    @Override
    public String toString() {
        Map<String, Object> mb;

        if (parentContext == null) {
            mb = getMapBuilder();
        } else {
            mb = new HashMap<String, Object>();

            mb.put("parentContext", parentContext);
            mb.put("thisContext", getMapBuilder());
        }

        return new StringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }

    private Map<String, Object> getMapBuilder() {
        Map<String, Object> mb = new HashMap<String, Object>();

        for (String key : internalKeySet()) {
            mb.put(key, get(key));
        }
        return mb;
    }

}
