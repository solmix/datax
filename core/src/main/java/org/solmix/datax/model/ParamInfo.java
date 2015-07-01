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

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月29日
 */
@Immutable
public class ParamInfo implements XMLSource
{
    protected String key;
    protected String value;
    protected String expression;
    protected Boolean isOverride;
    
    protected XMLNode node;
    
    public ParamInfo(Boolean override, ParamInfo param,XMLNode node)
    {
        this(param.key,param.value,param.expression,override, node);
    }

    public ParamInfo(String key, String value, String expression, Boolean isOverride,XMLNode node)
    {
       this.key=key;
       this.value=value;
       this.expression=expression;
       this.isOverride=isOverride;
       this.node=node;
    }

    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getExpression() {
        return expression;
    }
    public XMLNode getXMLNode() {
        return node;
    }

    public Boolean getIsOverride() {
        return isOverride;
    }
    
    public static class Parser extends BaseXmlNodeParser<ParamInfo>{

        @Override
        public ParamInfo parse(XMLNode node, XmlParserContext context) {
            String key = node.getStringAttribute("key");
            String value = node.getStringAttribute("value");
            String expression= node.getStringAttribute("expression");
            Boolean isOverride = node.getBooleanAttribute("isOverride");
            return new ParamInfo(key,value,expression,isOverride,node);
        }
        
    }

}
