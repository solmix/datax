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

import static org.solmix.commons.util.StringUtils.hasLength;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.Element;
import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.datax.repository.builder.BuilderException;
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

  
    @Override
    public XMLNode getXMLNode() {
        return node;
    }
    private static void copy(TransformerInfo target,TransformerInfo source){
        target.id=source.id;
        if(target.name==null)
        target.name=source.name;
        target.node=source.node;
        if(target.lookup==null)
        target.lookup=source.lookup;
        if(target.clazz==null)
        target.clazz=source.clazz;
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
        public TransformerInfoResolver(String id,String refid,XmlParserContext context){
            this.refid=refid;
            this.id=id;
            this.context=context;
        }
        
        @Override
        public void resolve() {
            TransformerInfo vi= context.getTransformerInfo(refid);
          copy(this, vi);
        }
        @Override
        public String toString(){
            return new StringBuilder().append("Resolver Validator2:").append(refid).toString();
        }

    }

    public static class Parsers extends Parser
    {

        public Parsers()
        {
            super(false);
        }
    }
    public static class Parser extends BaseXmlNodeParser<TransformerInfo>
    {
        private boolean applyCurrentService;
        public Parser(){
            this(true);
        }
       
        public Parser(boolean applyCurrentService)
        {
            this.applyCurrentService=applyCurrentService;
        }
        @Override
        public TransformerInfo parse(XMLNode node, XmlParserContext context) {

            String containerRef = node.getStringAttribute("container-ref");
            String refid = node.getStringAttribute("refid");
            String id = node.getStringAttribute("id");
            if ( id != null  ) {
                if(this.applyCurrentService){
                    id = context.applyCurrentService(id, false);
                }else{
                    id=context.applyCurrentNamespace(id, false);
                }
            }
            if (refid != null && containerRef != null) {
                LOG.warn("Validator2 configure both container-ref:{} and refid:{},ignore container-ref", containerRef, refid);
            }
            TransformerInfo ti = null;
            TransformerInfo refti = null;
            if (refid != null) {
                refid=parseRefid(refid, context);
                try {
                    refti = context.getTransformerInfo(refid);
                    ti = new TransformerInfo(node, id);
                } catch (ReferenceNoFoundException e) {
                    TransformerInfoResolver v = new TransformerInfoResolver(id,refid, context);
                    context.getRepositoryService().addReferenceResolver(v);
                    ti=v;
                }catch (Exception e) {
                    throw new BuilderException("Can't found transfomer ref:"+context.applyCurrentService(refid, true),e);
                }
            }else{
                ti = new TransformerInfo(node, id);
            }
           
          
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
            if(refid!=null&&refti!=null){
                copy(ti, refti);
            }
            return ti;

        }
    }
    public Element toElement() {
        XmlElement e = new XmlElement("transformer");
        if(clazz!=null){
            e.addAttribute(new Attribute("class",clazz.getName()));
        }
        if(hasLength(id)){
            e.addAttribute(new Attribute("id",id));
        }
        if(hasLength(name)){
            e.addAttribute(new Attribute("name",name));
        }
        if(lookup!=null){
            e.addAttribute(new Attribute("lookup",lookup.value()));
        }
        return e;
    }
}
