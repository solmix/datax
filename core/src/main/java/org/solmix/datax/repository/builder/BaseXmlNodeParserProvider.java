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

import org.solmix.datax.model.BatchOperations;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.ForwardInfo;
import org.solmix.datax.model.InvokerInfo;
import org.solmix.datax.model.MethodArgInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.ParamInfo;
import org.solmix.datax.model.TransformerInfo;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月25日
 */
public class BaseXmlNodeParserProvider extends AbstractXmlNodeParserProvider implements XmlNodeParserProvider
{
    public BaseXmlNodeParserProvider(Container container)
    {
        super(container);
    }

    @Override
    protected void config() {
        
       bind(TRANSFORMERS, TransformerInfo.Parsers.class);
       bind(VALIDATORS, ValidatorInfo.Parsers.class);
       bind(SERVICE, DataServiceInfo.Parser.class);
       bind(FIELD, FieldInfo.Parser.class);
              
       
       bind(VALIDATOR, ValidatorInfo.Parser.class);
       bind(OPERATION, OperationInfo.Parser.class);
       bind(PARAM,ParamInfo.Parser.class);
       bind(BATCH,BatchOperations.Parser.class);
       bind(TRANSFORMER,TransformerInfo.Parser.class);
       bind(FORWARD,ForwardInfo.Parser.class);
       bind(INVOKER,InvokerInfo.Parser.class);
       bind(METHOD_ARG,MethodArgInfo.Parser.class);
    }
}
