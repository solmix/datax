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
    private static void copy(ValidatorInfo source,ValidatorInfo target){
        target.id=source.id;
        target.type=source.type;
        target.name=source.name;
        target.errorMessage=source.errorMessage;
        target.serverOnly=source.serverOnly;
        target.clientOnly=source.clientOnly;
        target.validateOnChange=source.validateOnChange;
        target.max=source.max;
        target.min=source.min;
        target.exclusive=source.exclusive;
        target.expression=source.expression;
        target.substring=source.substring;
        target.operator=source.operator;
        target.count=source.count;
        target.clazz=source.clazz;
        target.node=source.node;
        target.lookup=source.lookup;
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
          copy(vi, this);
        }
        @Override
        public String toString(){
            return new StringBuilder().append("Resolver Validator2:").append(refid).toString();
        }

    }
    
    public static class Parser extends BaseXmlNodeParser<ValidatorInfo>{

        @Override
        public ValidatorInfo parse(XMLNode node, XmlParserContext context) {
            String refid = node.getStringAttribute("refid");
            if (refid != null) {
                refid=parseRefid(refid,context);
                ValidatorInfo vi = null;
                try {
                    vi = context.getValidatorInfo(refid);
                } catch (ReferenceNoFoundException e) {
                    ValidatorInfoResolver v = new ValidatorInfoResolver(refid, context);
                    context.getRepositoryService().addReferenceResolver(v);
                    return v;
                }

                return new ValidatorInfo(vi);
            }
            String id = node.getStringAttribute("id");
            if (id != null) {
                id = context.applyCurrentService(id, false);
            }
            ValidatorInfo vi = new ValidatorInfo(node, id);
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
            return vi;
        }
        
    }

    @Override
    public XMLNode getXMLNode() {
        return node;
    }
}
