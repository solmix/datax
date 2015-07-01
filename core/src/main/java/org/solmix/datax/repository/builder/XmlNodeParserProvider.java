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
package org.solmix.datax.repository.builder;

import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月24日
 */
@Extension
public interface XmlNodeParserProvider
{

    public static final String ROOT = "/datax";

    public static final String CONFIGURATION = ROOT + "/configuration";

    public static final String SERVICE = CONFIGURATION + "/service";

    public static final String FIELDS = SERVICE + "/fields";

    public static final String FIELD = FIELDS + "/field";

    public static final String VALIDATOR = FIELD + "/validator";

    public static final String OPERATIONS = SERVICE + "/operations";

    public static final String OPERATION = OPERATIONS + "/operation";
    
    public static final String PARAM = OPERATION + "/params/param";
    
    public static final String BATCH = OPERATION + "/batch";

    public static final String TRANSFORMER = OPERATION + "/transformer";

    public static final String INVOKER = OPERATION + "/invoker";
    
    <T> XmlNodeParser<T> getXmlNodeParser(String path,Class<T> clz);

}
