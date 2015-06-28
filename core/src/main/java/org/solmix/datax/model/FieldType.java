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
package org.solmix.datax.model;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月28日
 */

public enum FieldType
{
    TEXT("text"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    FLOAT("float"),
    DATE("date"),
    TIME("time"),
    DATETIME("datetime"),
    ENUM("enum"),
    INT_ENUM("intEnum"),
    SEQUENCE("sequence"),
    LINK("link"),
    IMAGE("image"),
    BINARY("binary"),
    IMAGE_FILE("imageFile"),
    ANY("any"),
    MODIFIER("modifier"),
    MODIFIER_TIMESTAMP("modifierTimestamp"),
    CREATOR_TIMESTAMP("creatorTimestamp"),
    CREATOR("creator"),
    PASSWORD("password"),
    CUSTOM("custom"),
    NTEXT("ntext"),
    UNKNOWN("unknown");
    private final String value;

    FieldType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FieldType fromValue(String v) {
        for (FieldType c: FieldType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
