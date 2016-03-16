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

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.ReferenceNoFoundException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.solmix.datax.validation.Validator;


/**
 * &lt;validator type="xxx" max=".." min=".." /&gt;
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月28日
 */
@Immutable
public class ValidatorInfo implements XMLSource
{
    public static final String v = null;
    protected String id;
    protected static int COUNT=0;
    protected String name;
    protected String type;
    protected XMLNode node;
    protected String errorMessage;
    protected Boolean serverOnly;
    protected Boolean clientOnly;
    protected Boolean validateOnChange;
    protected Boolean exclusive;
    protected Double max;
    protected Double min;
    protected Double precision;
    protected String mask;
    protected String expression;
    protected String substring;
    protected String operator;
    protected Long count;
    protected Class<? extends Validator> clazz;
    protected LookupType lookup;
    
    protected Map<String,Object> properties;
    
    ValidatorInfo(){
    }
   public ValidatorInfo(String type){
        this.type=type;
    }
    public ValidatorInfo(XMLNode node,String id){
        this.node=node;
        if(id==null){
            id=ValidatorInfo.class.getName()+"#"+(++COUNT);
        }
        this.id=id;
    }
    
    @Override
    public String toString(){
        return new StringBuilder().append("type:").append(type).append(id==null?"":" id:"+id).toString();
    }
    public Boolean getClientOnly() {
        return clientOnly;
    }
    public Object getProperty(String key) {
        Object res= null;
        if(properties!=null){
            res= properties.get(key);
        }
        if(res==null&&getXMLNode()!=null){
            res = getXMLNode().getStringAttribute(key);
        }
        return res;
    }
    
    public String getName() {
        return name;
    }
    
    public Class<? extends Validator> getClazz() {
        return clazz;
    }
    
    public LookupType getLookup() {
        return lookup;
    }
    /**
     * @param vi
     */
    public ValidatorInfo(ValidatorInfo vi)
    {
        copy(vi,this);
    }
    
    public ValidatorInfo(String string, Map<String, Object> properties)
    {
        this(string);
        this.properties=properties;
    }
    private static void copy(ValidatorInfo target,ValidatorInfo source){
        target.id=source.id;
        if(target.type==null)
        target.type=source.type;
        if(target.name==null)
        target.name=source.name;
        if(target.errorMessage==null)
        target.errorMessage=source.errorMessage;
        if(target.serverOnly==null)
        target.serverOnly=source.serverOnly;
        if(target.clientOnly==null)
        target.clientOnly=source.clientOnly;
        if(target.validateOnChange==null)
        target.validateOnChange=source.validateOnChange;
        if(target.max==null)
        target.max=source.max;
        if(target.min==null)
        target.min=source.min;
        if(target.exclusive==null)
        target.exclusive=source.exclusive;
        if(target.expression==null)
        target.expression=source.expression;
        if(target.substring==null)
        target.substring=source.substring;
        if(target.operator==null)
        target.operator=source.operator;
        if(target.count==null)
        target.count=source.count;
        if(target.clazz==null)
        target.clazz=source.clazz;
        if(target.lookup==null)
        target.lookup=source.lookup;
        if(target.properties==null)
        target.properties=source.properties;
        target.node=source.node;
    }
    /**
     * @return
     */
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Boolean getServerOnly() {
        return serverOnly;
    }
    
    public Boolean getValidateOnChange() {
        return validateOnChange;
    }
    
    public Boolean getExclusive() {
        return exclusive;
    }
    
    public Double getMax() {
        return max;
    }
    
    public Double getMin() {
        return min;
    }
    
    public Double getPrecision() {
        return precision;
    }
    
    public String getMask() {
        return mask;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public String getSubstring() {
        return substring;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public Long getCount() {
        return count;
    }

    public static class ValidatorInfoResolver extends ValidatorInfo implements ReferenceResolver{

        private final String refid;
        private XmlParserContext context;
        public ValidatorInfoResolver(String refid,XmlParserContext context){
            this.refid=refid;
            this.context=context;
        }
        
        @Override
        public void resolve() {
          ValidatorInfo vi= context.getValidatorInfo(refid);
          copy(this,vi);
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
    public static class Parser extends BaseXmlNodeParser<ValidatorInfo>{

        private boolean applyCurrentService;
        public Parser(){
            this(true);
        }
       
        public Parser(boolean applyCurrentService)
        {
            this.applyCurrentService=applyCurrentService;
        }
        @Override
        public ValidatorInfo parse(XMLNode node, XmlParserContext context) {
            String refid = node.getStringAttribute("refid");
            String id = node.getStringAttribute("id");
            if ( id != null) {
                if(this.applyCurrentService){
                    id = context.applyCurrentService(id, false);
                }else{
                    id=context.applyCurrentNamespace(id, false);
                }
                
            }
            ValidatorInfo vi = new ValidatorInfo(node, id);
            ValidatorInfo refvi = null;
            if (refid != null) {
                refid=parseRefid(refid,context);
                try {
                    refvi = context.getValidatorInfo(refid);
                    vi = new ValidatorInfo(node, id);
                } catch (ReferenceNoFoundException e) {
                    ValidatorInfoResolver v = new ValidatorInfoResolver(refid, context);
                    context.getRepositoryService().addReferenceResolver(v);
                    vi=v;
                }catch (Exception e) {
                    throw new BuilderException("Can't found validator ref:"+context.applyCurrentService(refid, true),e);
                }
            }else{
                vi = new ValidatorInfo(node, id);
            }
           
            Class<? extends Validator> clzz = super.paseClass(node, Validator.class);
            Long count = node.getLongAttribute("count");
            String errorMessage = node.getStringAttribute("errorMessage");
            Boolean exclusive = node.getBooleanAttribute("exclusive");
            String expression = node.getStringAttribute("expression");
            String mask = node.getStringAttribute("mask");
            Double max = node.getDoubleAttribute("max");
            Double min = node.getDoubleAttribute("min");
            String operator = node.getStringAttribute("operator");
            Double precision = node.getDoubleAttribute("precision");
            Boolean serverOnly = node.getBooleanAttribute("serverOnly");
            Boolean  clientOnly = node.getBooleanAttribute("clientOnly");
            Boolean validateOnChange= node.getBooleanAttribute("validateOnChange");
            String substring = node.getStringAttribute("substring");
            String type = node.getStringAttribute("type");
           String strlookup= node.getStringAttribute("lookup");
           LookupType lookup;
           if(strlookup==null){
               lookup=LookupType.NEW;
           }else{
               lookup=LookupType.fromValue(strlookup);
           }
            if (id != null) {
                // 如果设置了ID，可以被引用
                context.getRepositoryService().addValidatorInfo(vi);
            }
            String name = node.getStringAttribute("name");
            vi.name = name;
            vi.clazz = clzz;
            vi.count = count;
            vi.errorMessage = errorMessage;
            vi.exclusive = exclusive;
            vi.expression = expression;
            vi.mask = mask;
            vi.max = max;
            vi.min = min;
            vi.operator = operator;
            vi.precision = precision;
            vi.serverOnly = serverOnly;
            vi.clientOnly=clientOnly;
            vi.substring = substring;
            vi.type = type;
            vi.lookup=lookup;
            vi.validateOnChange=validateOnChange;
            if(refid!=null&&refvi!=null){
                copy(vi, refvi);
            }
            return vi;
        }
        
    }

    @Override
    public XMLNode getXMLNode() {
        return node;
    }
}
