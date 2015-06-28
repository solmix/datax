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
package org.solmix.datax.repository.builder.xml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.StringUtils;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.XMLParser;
import org.solmix.datax.DATAX;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.repository.DefaultRepository;
import org.solmix.datax.repository.builder.AbstractBuilder;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.DataServiceInfoResolver;
import org.solmix.datax.repository.builder.IncludeNoFoundException;
import org.solmix.datax.repository.builder.ReferenceNoFoundException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.XmlNodeParser;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.extension.ExtensionLoader;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月18日
 */

public class XMLDataServiceBuilder extends AbstractBuilder
{

    private XMLParser xmlParser;
    
    private String uri;
    
    private Container container;
    private String defaultServerType;
    private XmlNodeParserProvider xmlNodeParserProvider;
   
    private ExtensionLoader<XmlNodeParserProvider> extensionLoader;
    
    public XMLDataServiceBuilder(InputStream stream, DefaultRepository repositoryService,
        Map<String,Object> variables,String resourceUri,Container container,String defaultServerType){
        this(new XMLParser(stream, true, variables, new DataServiceEntityResolver(),DATAX.NS),repositoryService,resourceUri,container,defaultServerType);
    }
    private XMLDataServiceBuilder(XMLParser parser, DefaultRepository repositoryService,
        String resourceUri,Container container,String defaultServerType){
        super(repositoryService);
        this.container=container;
        this.xmlParser=parser;
        this.uri=resourceUri;
        this.defaultServerType=defaultServerType;
        if(this.container==null){
            this.container=ContainerFactory.getThreadDefaultContainer();
        }
        extensionLoader=this.container.getExtensionLoader(XmlNodeParserProvider.class);
    }
    
    public void build(){
        if(!repository.isResourceLoaded(uri)){
            parseConfigurationElement(xmlParser.evalNode("/datax/configuration"));
            repository.addLoadedResource(uri);
        }
        resolverReference();
        
    }
    /**
     * 
     */
    protected void resolverReference() {
        Collection<ReferenceResolver> references = repository.getReferenceResolvers();
        synchronized (references) {
              Iterator<ReferenceResolver> iter = references.iterator();
              while (iter.hasNext()) {
                    try {
                          iter.next().resolve();
                          iter.remove();
                    } catch (IncludeNoFoundException e) {
                          // Ignore，还是找不到。
                    }catch (ReferenceNoFoundException e) {
                        // Ignore，还是找不到。
                  }
              }
        }
    }
    /**
     * @param evalNodes
     */
    protected void parseConfigurationElement(XMLNode xmlNode) {
        String namespace = xmlNode.getStringAttribute("namespace");
        if (StringUtils.isEmpty(namespace)) {
            throw new BuilderException("dataservice namespace is null");
        } else if (!DATAX.NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new BuilderException("dataservice namespace is valide");
        }
        String serverType = xmlNode.getStringAttribute("serverType");
        if (serverType == null) {
            serverType = defaultServerType;
        }
        xmlNodeParserProvider = extensionLoader.getExtension(serverType);
        if (xmlNodeParserProvider == null) {
            throw new BuilderException("No found XmlNodeParserProvider for serverType: " + serverType);
        }
        XmlParserContext context = new XmlParserContext(repository, xmlNodeParserProvider);

        context.setCurrentNamespace(namespace);
        // parse fields
        fieldsToRepository(xmlNode.evalNodes("fields"), context);
        // parse service element
        List<XMLNode> nodes = xmlNode.evalNodes("service");
        XmlNodeParser<DataServiceInfo> parser = xmlNodeParserProvider.getXmlNodeParser("/datax/configuration/service", DataServiceInfo.class);
        for (XMLNode node : nodes) {
            try {
                DataServiceInfo dsi = parser.parse(node, context);
                repository.addDataService(dsi);
            } catch (IncludeNoFoundException e) {
                DataServiceInfoResolver resolver = new DataServiceInfoResolver(node, context);
                repository.addReferenceResolver(resolver);
            }
        }
    }
    
    protected void fieldsToRepository(List<XMLNode> nodes, XmlParserContext context) {
        for(XMLNode node:nodes){
            String id = node.getStringAttribute("id");
            if(id==null){
                throw new BuilderException("id is null for xml:\n"+node.toString());
            }
            id=context.applyCurrentNamespace(id, false);
            context.getRepositoryService().addInclude(id,node);
        }
        
    }
 
}
