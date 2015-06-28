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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.xml.VariablesParser;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月26日
 */

public class FieldInfo
{
    private final String name;
    
    private final FieldType type;
    
    private String title;
    
    private Boolean hidden;
    
    private Boolean required;
    
    private Boolean canEdit;
    
    private Boolean canExport;
    
    private String exportTitle;
    
    private String rootValue;
    
    private String dateFormat;
    
    private Integer maxFileSize;
    
    private Boolean primaryKey;
    private String foreignKey;
    
    private Map<String,String> valueMap;
    
    private List<ValidatorInfo> validators;
    
    public FieldInfo(String name,FieldType type){
        Assert.assertNotNull(name);
        this.name=name;
        this.type=type;
    }
    
    
    public String getName() {
        return name;
    }
    
    public FieldType getType() {
        return type;
    }


    
    public String getTitle() {
        return title;
    }


    
    public Boolean getHidden() {
        return hidden;
    }


    
    public Boolean getRequired() {
        return required;
    }


    
    public Boolean getCanEdit() {
        return canEdit;
    }


    
    public Boolean getCanExport() {
        return canExport;
    }


    
    public String getExportTitle() {
        return exportTitle;
    }


    
    public String getRootValue() {
        return rootValue;
    }


    
    public String getDateFormat() {
        return dateFormat;
    }


    
    public Integer getMaxFileSize() {
        return maxFileSize;
    }


    
    public Boolean getPrimaryKey() {
        return primaryKey;
    }


    
    public String getForeignKey() {
        return foreignKey;
    }


    @Override
    public boolean equals(Object other){
        if(other instanceof FieldInfo){
            return ((FieldInfo)other).name.equals(name);
        }
        return false;
        
    }
    public static class Parser extends BaseXmlNodeParser<FieldInfo>{

       
        @Override
        public FieldInfo parse(XMLNode node, XmlParserContext context) {
            String name= node.getStringAttribute("name");
            if(!validateId(name)){
                throw new BuilderException("Invalid Filed name at:"+node.getPath());
            }
            String type= node.getStringAttribute("type");
            Boolean canEdit = node.getBooleanAttribute("canEdit");
            Boolean canExport = node.getBooleanAttribute("canExport");
            String exportTitle= node.getStringAttribute("exportTitle");
            Boolean hidden = node.getBooleanAttribute("hidden");
            Boolean primaryKey = node.getBooleanAttribute("primaryKey");
            Boolean required = node.getBooleanAttribute("required");
            String dateFormat= node.getStringAttribute("dateFormat");
            String foreignKey= node.getStringAttribute("foreignKey");
            String title= node.getStringAttribute("title");
            String rootValue= node.getStringAttribute("rootValue");
            Integer maxFileSize = node.getIntAttribute("maxFileSize");
            FieldType ftype=null;
            if(StringUtils.isEmpty(type)){
                ftype=FieldType.TEXT;
            }else{
                ftype=FieldType.fromValue(type);
            }
            
            Map<String,String> valueMap=parseValueMap(node.evalNode("valueMap"),context);
            List<ValidatorInfo> validators=parseValidators(node.evalNodes("validator"),context);
            FieldInfo fi= new FieldInfo(name,ftype);
            fi.required=required;
            fi.canEdit=canEdit;
            fi.title=title;
            fi.canExport=canExport;
            fi.exportTitle=exportTitle;
            fi.dateFormat=dateFormat;
            fi.foreignKey=foreignKey;
            fi.primaryKey=primaryKey;
            fi.hidden=hidden;
            fi.maxFileSize=maxFileSize;
            fi.rootValue=rootValue;
            fi.valueMap=valueMap;
            fi.validators=validators;
            return fi; 
        }

        /**
         * @param evalNodes
         * @param context
         * @return
         */
        private List<ValidatorInfo> parseValidators(List<XMLNode> nodes, XmlParserContext context) {
            if(nodes==null||nodes.size()==0){
                return null;
            }
            List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>(nodes.size());
            for(XMLNode node :nodes){
                validators.add(context.parseNode(node, ValidatorInfo.class));
            }
            return validators;
        }

        /**
         * @param evalNode
         * @param context
         * @return
         */
        private Map<String, String> parseValueMap(XMLNode node, XmlParserContext context) {
           if(node==null){
               return null;
           }
           List<XMLNode> nodes=node.evalNodes("value");
           Map<String,String> valueMap = new LinkedHashMap<String, String>(nodes.size());
           for(XMLNode n:nodes){
               String id=n.getStringAttribute("id");
               String name = n.getStringAttribute("name");
               if(name==null){
                   name=id;
               }else{
                   name = VariablesParser.parse(name, context.getDataServiceManagerProperties());
               }
               valueMap.put(id, name);
           }
           return valueMap;
        }
        
    }
}
