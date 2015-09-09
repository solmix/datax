/*
 * Copyright 2015 The Solmix Project
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
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年5月19日
 */
@Immutable
public class ForwardInfo implements XMLSource
{

    protected String name;

    protected String path;

    protected String script;
    
    protected String contentType;
    
    private Map<String, ParamInfo> params;

    private XMLNode node;
    
    ForwardInfo(){
        
    }
    ForwardInfo(XMLNode node){
        this.node=node;
    }
    @Override
    public XMLNode getXMLNode() {
        return node;
    }
    public String getName() {
        return name;
    }
    
    public String getContentType() {
        return contentType;
    }
    public String getPath() {
        return path;
    }

    
    public String getScript() {
        return script;
    }

    
    public Map<String, ParamInfo> getParams() {
        return params;
    }
    public static class Parser extends BaseXmlNodeParser<ForwardInfo>
    {

        @Override
        public ForwardInfo parse(XMLNode node, XmlParserContext context) {

            String name = node.getStringAttribute("name");
            String method = node.getStringAttribute("path");
            String script = node.getStringAttribute("script");
            String contentType = node.getStringAttribute("content-type");
            ForwardInfo ti = new ForwardInfo(node);
            ti.name=name;
            ti.path=method;
            ti.script=script;
            ti.contentType=contentType;
            
            List<XMLNode> nodes= node.evalNodes("param");
            Map<String, ParamInfo> params = new LinkedHashMap<String, ParamInfo>(nodes.size());
            for(XMLNode n:nodes){
                ParamInfo  param= context.parseNode(XmlNodeParserProvider.PARAM,n, ParamInfo.class);
                
                params.put(param.getKey(), param);
            }
            ti.params=params;
            return ti;
        }
        
    }
    
}
