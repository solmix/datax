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

import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.solmix.commons.util.ObjectUtils.NullObject;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月26日
 */
@Target({LOCAL_VARIABLE, PARAMETER})
@Retention(RUNTIME)
public @interface Param {
    
    /**
     * 服务名
     * @return
     */
    String name() default "";
    
    /**
     * 指明该参数是请求数据还是服务资源。
     * @return
     */
    ParamType type() default ParamType.RESOURCE;
    
    String expression() default "";
    
    Class<?> collectionClass() default NullObject.class;

    /**
     * @return
     */
    Class<?> javaClass() default NullObject.class;
}
