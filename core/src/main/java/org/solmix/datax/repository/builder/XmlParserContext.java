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
package org.solmix.datax.repository.builder;

import java.util.Collection;
import java.util.Map;

import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.TransformerInfo;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.repository.DefaultRepository;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月25日
 */

public class XmlParserContext
{
    
    private final DefaultRepository repositoryService;
    private final XmlNodeParserProvider xmlNodeParserProvider;
    private String currentNamespace;
    private String serverType;
    private String currentService;
    
    public XmlParserContext(DefaultRepository repository,XmlNodeParserProvider provider){
        this.repositoryService=repository;
        this.xmlNodeParserProvider=provider;
    }
    
    public DefaultRepository getRepositoryService() {
        return repositoryService;
    }
    
    public XmlNodeParserProvider getXmlNodeParserProvider() {
        return xmlNodeParserProvider;
    }
    public <T> XmlNodeParser<T> getXmlNodeParser(String path,Class<T> clz){
        return xmlNodeParserProvider.getXmlNodeParser(path, clz);
    }
    public <T> XmlNodeParser<T> getXmlNodeParser(XMLNode node,Class<T> clz){
        String path=node.getPath();
        if(!path.startsWith("/")){
            path="/"+path;
        }
        return xmlNodeParserProvider.getXmlNodeParser(path, clz);
    }
    public <T> T parseNode(String path,XMLNode node,Class<T> clz ){
        XmlNodeParser<T> parser = getXmlNodeParser(path,clz);
        return parser.parse(node, this);
    }
    
    public <T> T parseNode(XMLNode node,Class<T> clz ){
        XmlNodeParser<T> parser = getXmlNodeParser(node,clz);
        return parser.parse(node, this);
    }
    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
          throw new BuilderException("The cofiguration element requires a namespace attribute to be specified.");
        }
        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
          throw new BuilderException("Wrong namespace. Expected '"
              + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }
        this.currentNamespace = currentNamespace;
      }
    
    public void setServerType(String serverType) {
        if (serverType == null) {
          throw new BuilderException("The cofiguration element requires a servertype attribute to be specified.");
        }
        if (this.serverType != null && !this.serverType.equals(serverType)) {
          throw new BuilderException("Wrong serverType. Expected '"
              + this.serverType + "' but found '" + serverType + "'.");
        }
        this.serverType = serverType;
      }
    
    public String getCurrentNamespace() {
        return currentNamespace;
    }
    
    public String getServerType() {
        return serverType;
    }
    public String getCurrentService() {
        return currentService;
    }
    
    public void setCurrentService(String currentService) {
        this.currentService = currentService;
    }

    public Map<String, Object> getDataServiceManagerProperties(){
       return getRepositoryService().getDataServiceManager().getProperties();
    }
    
    /**
     * namespace+"."+base
     * @param base
     * @param isReference
     * @return
     */
    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
          if (base.contains(".")){
              return base;
          }
        } else {
          if (base.startsWith(currentNamespace + ".")) {
              return base;
          }
        }
        return currentNamespace + "." + base;
      }
    public String applyCurrentNamespace(String serviceid ,String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
          if (base.contains(".")){
              return base;
          }
        } else {
          if (base.startsWith(currentNamespace + "."+currentService+".")) {
              return base;
          }
        }
        return currentNamespace + "." +serviceid+"."+ base;
    }
    
    /**
     * namespace+"."+serviceid+"."+base.
     * @param base
     * @param isReference
     * @return
     */
    public String applyCurrentService(String base, boolean isReference) {
        return applyCurrentNamespace(currentService,base, isReference);
    }
    
    public Collection<ReferenceResolver> getReferenceResolvers() {
        return repositoryService.getReferenceResolvers();
    }
    
    public void addReferenceResolver(ReferenceResolver resolver){
        repositoryService.addReferenceResolver(resolver);
    }

    /**
     * @param id
     * @return
     */
    public XMLNode getIncludeXMLNode(String id) {
        return  repositoryService.getIncludeXMLNode(id);
    }

    /**
     * @param refid
     * @return
     */
    public ValidatorInfo getValidatorInfo(String refid) {
        try {
          return  repositoryService.getValidatorInfo(refid);
        } catch (IllegalArgumentException e) {
           throw new ReferenceNoFoundException();
        }
    }

    /**
     * 循环取出最终的reference
     * 
     * @param refid
     */
    public OperationInfo getOperationInfo(String refid) {
        try {
            OperationInfo oi=  repositoryService.getOperationInfo(refid);
            if(oi.getRefid()!=null){
                //已经是格式化后的ID。
                return repositoryService.getOperationInfo(oi.getRefid());
            }else{
                return oi;
            }
          } catch (IllegalArgumentException e) {
             throw new ReferenceNoFoundException();
          }
    }

    /**
     * @param refid
     * @return
     */
    public TransformerInfo getTransformerInfo(String refid) {
        try {
            return  repositoryService.getTransformerInfo(refid);
          } catch (IllegalArgumentException e) {
             throw new ReferenceNoFoundException();
          }
    }
    
}
