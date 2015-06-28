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
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.ReferenceNoFoundException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * &lt;validator type="xxx" max=".." min=".." /&gt;
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月28日
 */

public class ValidatorInfo
{
    private static final Logger LOG = LoggerFactory.getLogger(ValidatorInfo.class);
    public static final String v = null;
    private String id;
    private static int COUNT=0;
    
    private String containerRef;
    private String type;
    private XMLNode node;
    private String errorMessage;
    
    private Boolean serverOnly;
    private Boolean validateOnChange;
    
    private Boolean exclusive;
    private Double max;
    private Double min;
    
    private Double precision;
    private String mask;
    private String expression;
    private String substring;
    private String operator;
    private Long count;
    
    ValidatorInfo(){
    }
    public ValidatorInfo(XMLNode node,String id){
        this.node=node;
        if(id==null){
            id=ValidatorInfo.class.getName()+"#"+(++COUNT);
        }
    }
    /**
     * @param vi
     */
    public ValidatorInfo(ValidatorInfo vi)
    {
        copy(vi,this);
    }
    private static void copy(ValidatorInfo source,ValidatorInfo target){
        target.id=source.id;
        target.type=source.type;
        target.containerRef=source.containerRef;
        target.errorMessage=source.errorMessage;
        target.serverOnly=source.serverOnly;
        target.validateOnChange=source.validateOnChange;
        target.max=source.max;
        target.min=source.min;
        target.exclusive=source.exclusive;
        target.expression=source.expression;
        target.substring=source.substring;
        target.operator=source.operator;
        target.count=source.count;
    }
    /**
     * @return
     */
    public String getId() {
        return id;
    }
    
    
    public String getContainerRef() {
        return containerRef;
    }
    
    public String getType() {
        return type;
    }
    
    public XMLNode getNode() {
        return node;
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
        
    }
    
    public static class Parser extends BaseXmlNodeParser<ValidatorInfo>{

        @Override
        public ValidatorInfo parse(XMLNode node, XmlParserContext context) {
            String containerRef= node.getStringAttribute("container-ref");
            String refid= node.getStringAttribute("refid");
           if(refid!=null&&containerRef!=null){
               LOG.warn("Validator configure both container-ref:{} and refid:{},ignore container-ref",containerRef,refid);
           }
           if(refid!=null){
               ValidatorInfo vi=null;
               try {
                 vi=  context.getValidatorInfo(refid);
            } catch (ReferenceNoFoundException e) {
                ValidatorInfoResolver v = new ValidatorInfoResolver(refid,context);
                context.getRepositoryService().addReferenceResolver(v);
                return v;
            }
               
               return new ValidatorInfo(vi);
           }
           String id= node.getStringAttribute("id");
           if(id!=null){
               id= context.applyCurrentNamespace(id, false);
           }
           ValidatorInfo vi = new ValidatorInfo(node,id);
           if(id!=null){
               //如果设置了ID，可以被引用
               context.getRepositoryService().addValidatorInfo(vi);
           }
           vi.containerRef=containerRef;
            return vi;
        }
        
    }

   

}
