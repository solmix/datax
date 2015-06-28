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
    
    public <T> T parseNode(XMLNode node,Class<T> clz ){
        XmlNodeParser<T> parser = getXmlNodeParser(node,clz);
        return parser.parse(node, this);
    }
    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
          throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
          throw new BuilderException("Wrong namespace. Expected '"
              + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
      }
    
    public String getCurrentNamespace() {
        return currentNamespace;
    }
    public Map<String, Object> getDataServiceManagerProperties(){
       return getRepositoryService().getDataServiceManager().getProperties();
    }
    /**
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
    
}
