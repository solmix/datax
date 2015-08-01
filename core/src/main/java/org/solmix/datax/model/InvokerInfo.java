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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月29日
 */
@Immutable
public class InvokerInfo implements XMLSource
{
    protected Class<?> clazz;
    protected XMLNode node;
    protected String name;
    
    protected String methodName;
    
    protected LookupType lookup;
    
    protected Map<Integer,MethodArgInfo> methodArgs;
    
    public InvokerInfo(){
        
    }
    
    InvokerInfo(XMLNode node){
        this.node=node;
    }
    @Override
    public XMLNode getXMLNode() {
        return node;
    }
    
    
    public Class<?> getClazz() {
        return clazz;
    }
    
    public String getName() {
        return name;
    }
    
    
    
    public Map<Integer, MethodArgInfo> getMethodArgs() {
        return methodArgs;
    }

    public LookupType getLookup() {
        return lookup;
    }

    public String getMethodName() {
        return methodName;
    }

    public static class Parser extends BaseXmlNodeParser<InvokerInfo>
    {

        @Override
        public InvokerInfo parse(XMLNode node, XmlParserContext context) {
            String name = node.getStringAttribute("name");
            String method = node.getStringAttribute("method");
            InvokerInfo ti = new InvokerInfo(node);
            String strlookup= node.getStringAttribute("lookup");
            LookupType lookup;
            if(strlookup==null){
                lookup=LookupType.NEW;
            }else{
                lookup=LookupType.fromValue(strlookup);
            }
            Class<?> clzz = super.paseClass(node);
            Map<Integer,MethodArgInfo> args = parseMethodArgs(node.evalNodes("method-arg"),context);
            ti.name = name;
            ti.methodName=method;
            ti.methodArgs=args;
            ti.lookup=lookup;
            ti.clazz = clzz;
            return ti;

        }

        private Map<Integer, MethodArgInfo> parseMethodArgs(List<XMLNode> nodes, XmlParserContext context) {
            if (nodes == null || nodes.size() == 0) {
                return null;
            }
            Map<Integer, MethodArgInfo> operations = new LinkedHashMap<Integer, MethodArgInfo>(nodes.size());
            for (int i=0;i<nodes.size();i++) {
                XMLNode n= nodes.get(i);
                MethodArgInfo oi = context.parseNode(XmlNodeParserProvider.METHOD_ARG, n, MethodArgInfo.class);
                if(oi.getOrder()==-1){
                    oi.order=i;
                }
                if (operations.containsKey(oi.getOrder())) {
                    throw new BuilderException("Have duplicate Order id:" + oi.getOrder());
                }
                operations.put(oi.getOrder(), oi);
            }
            return operations;
        }
    }
}
