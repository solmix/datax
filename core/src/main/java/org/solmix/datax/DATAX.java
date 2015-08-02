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

package org.solmix.datax;

import java.util.regex.Pattern;

import org.solmix.datax.call.DSCall;
import org.solmix.runtime.Container;

/**
 * Datax常量
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月25日
 */

public final class DATAX
{

    /**
     * 使用XSD验证XML时,java默认的<code>xpath</code>取Node时需要命名空间，默认命名空间为ds.
     */
    public static final String NS = "ds:";

    public final static Pattern NAMESPACE_PATTERN = Pattern.compile("^([a-zA-Z]\\w+[.])*[a-zA-Z]\\w+");

    public final static Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z]\\w+");

    public final static Pattern REF_ID_PATTERN = Pattern.compile("^([#a-zA-Z]\\w+[.])*[#a-zA-Z]\\w+");

    /**
     * 命名空间占位符 etc: <code>
     * namespace=com.example
     * ##ds.validator=com.example.ds.validator
     * </code>
     */
    public final static String NS_AREA = "##";

    /**
     * Dataserivce空间占位符 etc: <code>
     * serviceid=com.example.ds
     * #validator=com.example.ds.validator
     * </code>
     */
    public final static String DS_AREA = "#";

    public static final String LOG_CONTEXT = "ds";

    public static final String VALIDATION_LOGNAME = "org.solmix.datax.VALIDATION";

    public static final String VALIDATION_TOPIC_PREFIX = "org/solmix/datax/validation/";;
    
    /**
     * Velocity 中{@link Container} 的名称
     */
    public static final String VM_CONTAINER = "container";
    
    /**
     * Velocity 中{@link RequestContext} 的名称
     */
    public static final String VM_REQUESTCONTEXT = "requestContext";
    
    /**
     * Velocity 中{@link DSCall} 的名称
     */
    public static final String VM_DSC = "dsc";
    
    /**
     * Velocity 中{@link DSRequest} 的名称
     */
    public static final String VM_DSREQUEST = "dsrequest";

    /**
     * Velocity 中{@link DataService} 的名称
     */
    public static final String VM_DS = "ds";

    public static final String VM_VALUES = "values";

    public static final String VM_OLD_VALUES = "oldValues";
    

    /**
     * Velocity 表达式生成的临时结果标示
     */
    public static final String VT_TMP_NAME = "_d_vm_result";
    
}
