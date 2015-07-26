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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.ReferenceNoFoundException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.solmix.datax.transformer.Transformer;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月29日
 */
@Immutable
public class TransformerInfo implements XMLSource
{

    private static final Logger LOG = LoggerFactory.getLogger(TransformerInfo.class);

    protected XMLNode node;

    protected String id;
    protected String name;
    protected Class<? extends Transformer> clazz;
    protected LookupType lookup;
    private static int COUNT = 0;

    TransformerInfo(){}
    public TransformerInfo(XMLNode node, String id)
    {
        this.node = node;
        if (id == null) {
            id = ValidatorInfo.class.getName() + "#" + (++COUNT);
        }
        this.id = id;
    }

    public TransformerInfo(TransformerInfo vi)
    {
        copy(vi,this);
    }
    @Override
    public XMLNode getXMLNode() {
        return node;
    }
    private static void copy(TransformerInfo source,TransformerInfo target){
        target.id=source.id;
        target.name=source.name;
        target.node=source.node;
        target.lookup=source.lookup;
        target.node=source.node;
    }
    
    public Object getProperty(String key) {
        if(getXMLNode()!=null){
            return getXMLNode().getStringAttribute(key);
        }
        return null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    
    public Class<? extends Transformer> getClazz() {
        return clazz;
    }
    
    public LookupType getLookup() {
        return lookup;
    }

    public static class TransformerInfoResolver extends TransformerInfo implements ReferenceResolver{

        private final String refid;
        private XmlParserContext context;
        public TransformerInfoResolver(String refid,XmlParserContext context){
            this.refid=refid;
            this.context=context;
        }
        
        @Override
        public void resolve() {
            TransformerInfo vi= context.getTransformerInfo(refid);
          copy(vi, this);
        }
        @Override
        public String toString(){
            return new StringBuilder().append("Resolver Validator2:").append(refid).toString();
        }

    }
    public static class Parser extends BaseXmlNodeParser<TransformerInfo>
    {

        @Override
        public TransformerInfo parse(XMLNode node, XmlParserContext context) {

            String containerRef = node.getStringAttribute("container-ref");
            String refid = node.getStringAttribute("refid");
            if (refid != null && containerRef != null) {
                LOG.warn("Validator2 configure both container-ref:{} and refid:{},ignore container-ref", containerRef, refid);
            }
            if (refid != null) {
                TransformerInfo vi = null;
                refid=parseRefid(refid, context);
                try {
                    vi = context.getTransformerInfo(refid);
                } catch (ReferenceNoFoundException e) {
                    TransformerInfoResolver v = new TransformerInfoResolver(refid, context);
                    context.getRepositoryService().addReferenceResolver(v);
                    return v;
                }

                return new TransformerInfo(vi);
            }
            String id = node.getStringAttribute("id");
            if (id != null) {
                id = context.applyCurrentService(id, false);
            }
            TransformerInfo ti = new TransformerInfo(node, id);
            Class<? extends Transformer> clzz = super.paseClass(node, Transformer.class);
           

            if (id != null) {
                // 如果设置了ID，可以被引用
                context.getRepositoryService().addTransformerInfo(ti);
            }
            String name = node.getStringAttribute("name");
            String strlookup= node.getStringAttribute("lookup");
            LookupType lookup;
            if(strlookup==null){
                lookup=LookupType.NEW;
            }else{
                lookup=LookupType.fromValue(strlookup);
            }
            ti.name = name;
            ti.clazz = clzz;
            ti.lookup=lookup;
            return ti;

        }
    }
}
