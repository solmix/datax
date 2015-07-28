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
package org.solmix.datax.annotation;

import org.solmix.datax.model.ValueEnum;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月1日
 */

public enum ParamType implements ValueEnum
{
    DATA("new"),
    RESOURCE("request");
    
    private final String value;
    ParamType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ParamType fromValue(String v) {
        for (ParamType c: ParamType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
