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

import java.util.Map;

import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月31日
 */

public class MethodArgInfo implements XMLSource
{
    protected XMLNode node;
    
    protected String value;
    
    
    protected String expression;
    
    protected int order;
    /**
     * @param node2
     */
    public MethodArgInfo(XMLNode node)
    {
       this.node=node;
    }

    @Override
    public XMLNode getXMLNode() {
        return node;
    }
    
    public String getValue() {
        return value;
    }
 
    
    public String getExpression() {
        return expression;
    }
    
    public int getOrder() {
        return order;
    }
    
    public static class Parser extends BaseXmlNodeParser<MethodArgInfo>
    {

        @Override
        public MethodArgInfo parse(XMLNode node, XmlParserContext context) {
            String value = node.getStringAttribute("value");
            Integer order = node.getIntAttribute("order");
            String expression= node.getStringAttribute("expression");
            if(order!=null&&order<0){
                throw new BuilderException(node.getPath()+" method arg's order must >0");
            }
            if(order==null){
                order=-1;
            }
            MethodArgInfo ti = new MethodArgInfo(node);
            ti.value = value;
            ti.expression=expression;
            ti.order=order;
            return ti;

        }
    }
}
