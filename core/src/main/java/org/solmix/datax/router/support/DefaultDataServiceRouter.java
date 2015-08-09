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

package org.solmix.datax.router.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.LRUCache;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.XMLParser;
import org.solmix.commons.xml.XMLParsingException;
import org.solmix.datax.DATAX;
import org.solmix.datax.DataxRuntimeException;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.xml.DataxEntityResolver;
import org.solmix.datax.router.DataServiceRouter;
import org.solmix.datax.router.RequestToken;
import org.solmix.datax.router.RouterException;
import org.solmix.datax.router.RoutingResult;
import org.solmix.datax.router.rule.ResourceExpressionRule;
import org.solmix.datax.router.rule.ResourceRule;
import org.solmix.datax.router.rule.RouterRule;
import org.solmix.runtime.Container;
import org.solmix.runtime.ProductionAware;
import org.solmix.runtime.resource.InputStreamResource;
import org.solmix.runtime.resource.ResourceManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月7日
 */

public class DefaultDataServiceRouter implements DataServiceRouter<RequestToken>, ProductionAware
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDataServiceRouter.class);
    private static final String DEFAULT_XML_LOCATION = "META-INF/rules/*.xml";
    private List<Set<RouterRule<RequestToken, List<String>>>> rules;

    private LRUCache<RequestToken, RoutingResult> localCache;

    private Boolean enableCache;

    private Integer cacheSize;

    private String configLocation;

    private List<String> resources;

    private boolean productionMode;
    private Container container;

    @Override
    public RoutingResult route(RequestToken param) throws RouterException {
        if (enableCache) {
            synchronized (localCache) {
                if (localCache.containsKey(param)) {
                    RoutingResult result = localCache.get(param);
                    LOG.info("return routing result:{} from cache for fact:{}", result, param);
                    return result;
                }
            }
        }

        RoutingResult result = new RoutingResult();
        result.setResourceIds(new ArrayList<String>());
        if (DataUtils.isNotNullAndEmpty(rules)) {
            RouterRule<RequestToken, List<String>> ruleToUse = null;
            for (Set<RouterRule<RequestToken, List<String>>> ruleSet : rules) {
                for (RouterRule<RequestToken, List<String>> rule : ruleSet) {
                    if (rule.isPassed(param)) {
                        ruleToUse = rule;
                        break;
                    }
                }
                if (ruleToUse != null) {
                    break;
                }
            }
            if (ruleToUse != null) {
                LOG.trace("matched with rule:{} with fact:{}", ruleToUse, param);
                result.getResourceIds().addAll(ruleToUse.action());
            } else {
                LOG.trace("No matched rule found for routing fact:{}", param);
            }
        }
        
        if (enableCache) {
            synchronized (localCache) {
                localCache.put(param, result);
            }
        }
        return result;
    }

    @Resource
    public void setContainer(Container container) {
        this.container = container;
    }


    @PostConstruct
    public void init() {
        // 生产环境默认开启cache.
        if (productionMode && enableCache == null) {
            enableCache = true;
        }
        if (enableCache) {
            cacheSize = cacheSize == null ? 8000 : cacheSize;
            localCache = new LRUCache<RequestToken, RoutingResult>(cacheSize);
        }
        buildRules();
    }

    private void buildRules() {
        Map<String, InputStream> xmlResources = new LinkedHashMap<String, InputStream>();
        // 先加载指定的XML
        xmlResources.putAll(loadDefinitionXmlDataServiceConfig());
        String xmlLocation = configLocation;

        if ( xmlLocation == null) {
            xmlLocation = DEFAULT_XML_LOCATION;
        }
        if (xmlLocation != null) {
            xmlResources.putAll(lookupXmlDataServiceConfig(xmlLocation));
        }
        try {
            rules=loadAllRule(xmlResources);
        } finally {
            for (InputStream input : xmlResources.values()) {
                IOUtils.closeQuietly(input);
            }
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private List<Set<RouterRule<RequestToken, List<String>>>> loadAllRule(Map<String, InputStream> xmlResources) {
        final Set<RouterRule<RequestToken, List<String>>> operationExpressionRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        final Set<RouterRule<RequestToken, List<String>>> operationRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        final Set<RouterRule<RequestToken, List<String>>> dataserviceExpressionRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        final Set<RouterRule<RequestToken, List<String>>> dataserviceRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        final Set<RouterRule<RequestToken, List<String>>> namespaceExpressionRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        final Set<RouterRule<RequestToken, List<String>>> namespaceRules = new HashSet<RouterRule<RequestToken, List<String>>>();
        for (String rulexml : xmlResources.keySet()) {
            try {
                XMLParser parser = new XMLParser(xmlResources.get(rulexml), true, Collections.EMPTY_MAP, new DataxEntityResolver(), DATAX.NS);
                List<XMLNode> nodes = parser.evalNodes("/rules/rule");

                for (XMLNode node : nodes) {
                    XMLNode operation = node.evalNode("operation");
                    XMLNode dataservice = node.evalNode("dataservice");
                    XMLNode namespace = node.evalNode("namespace");
                    XMLNode expression = node.evalNode("expression");
                    XMLNode partations = node.evalNode("partitions");
                    if (partations == null || StringUtils.isEmpty(partations.getStringBody())) {
                        throw new IllegalArgumentException("must specify partitions");
                    }
                    String action = StringUtils.trimToNull(partations.getStringBody());
                    if (operation != null) {
                        if (expression == null || StringUtils.isEmpty(expression.getStringBody())) {
                            operationRules.add(new ResourceRule(
                                    StringUtils.trimToNull(operation.getStringBody()),
                                    action, 
                                    ResourceExpressionRule.OPERATON));
                        } else {
                            operationExpressionRules.add(
                                new ResourceExpressionRule(
                                    StringUtils.trimToNull(operation.getStringBody()),
                                    action, 
                                    StringUtils.trimToNull(expression.getStringBody()), 
                                    ResourceExpressionRule.OPERATON));
                        }

                    } else if (dataservice != null) {
                        if (expression == null || StringUtils.isEmpty(expression.getStringBody())) {
                            dataserviceRules.add(new ResourceRule(
                                StringUtils.trimToNull(dataservice.getStringBody()),
                                action, 
                                ResourceExpressionRule.DATASERVICE));
                        } else {
                            dataserviceExpressionRules.add(
                                new ResourceExpressionRule(
                                    StringUtils.trimToNull(dataservice.getStringBody()),
                                    action, 
                                    StringUtils.trimToNull(expression.getStringBody()), 
                                    ResourceExpressionRule.DATASERVICE));
                        }
                    } else if (namespace != null) {
                        if (expression == null || StringUtils.isEmpty(expression.getStringBody())) {
                            namespaceRules.add(new ResourceRule(
                                StringUtils.trimToNull(namespace.getStringBody()),
                                action, 
                                ResourceExpressionRule.NAMESPACE));
                        } else {
                            namespaceExpressionRules.add(
                                new ResourceExpressionRule(
                                    StringUtils.trimToNull(namespace.getStringBody()),
                                    action, 
                                    StringUtils.trimToNull(expression.getStringBody()), 
                                    ResourceExpressionRule.NAMESPACE));
                        }
                    }
                }

            } catch (XMLParsingException e) {
                throw new BuilderException("Error validate xml file:" + rulexml, e);
            }
        }
        List<Set<RouterRule<RequestToken, List<String>>>> ruleSequences = new ArrayList<Set<RouterRule<RequestToken, List<String>>>>() {
            private static final long serialVersionUID = 1493353938640646578L;
            {
                add(operationExpressionRules);
                add(operationRules);
                add(dataserviceExpressionRules);
                add(dataserviceRules);
                add(namespaceExpressionRules);
                add(namespaceRules);
            }
        };
        return ruleSequences;
    }

    private Map<? extends String, ? extends InputStream> lookupXmlDataServiceConfig(String xmlLocation) {
        ResourceManager rm = container.getExtension(ResourceManager.class);
        Map<String, InputStream> configs = new LinkedHashMap<String, InputStream>();
        try {
            InputStreamResource[] resources = rm.getResourcesAsStream(xmlLocation);
            if (resources != null) {
                for (InputStreamResource resource : resources) {
                    configs.put(resource.getURI().toString(), resource.getInputStream());
                }
            }
        } catch (IOException e) {
            throw new DataxRuntimeException("lookup dataservice configuration file failed.", e);
        }
        return configs;
    }

    private Map<String, InputStream> loadDefinitionXmlDataServiceConfig() {
        if (this.resources == null)
            return Collections.emptyMap();
        ResourceManager rm = container.getExtension(ResourceManager.class);
        Map<String, InputStream> configs = new LinkedHashMap<String, InputStream>();
        try {
            for (String xml : this.resources) {
                InputStreamResource ism = rm.getResourceAsStream(xml);
                if (ism == null) {
                    LOG.warn("Can't found definition xml configuration {}", xml);
                } else {
                    configs.put(ism.getURI().toString(), ism.getInputStream());
                }
            }
        } catch (IOException e) {
            throw new DataxRuntimeException("load definition dataservice configuration file failed.", e);
        }
        return configs;
    }

    public Boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(Boolean enableCache) {
        this.enableCache = enableCache;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configlocation) {
        this.configLocation = configlocation;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public void addResource(String resource) {
        if (this.resources == null) {
            this.resources = new ArrayList<String>();
        }
        resources.add(resource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.ProductionAware#setProduction(boolean)
     */
    @Override
    public void setProduction(boolean productionMode) {
        this.productionMode = productionMode;

    }

}
