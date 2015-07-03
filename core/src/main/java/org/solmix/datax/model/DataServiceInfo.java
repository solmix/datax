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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.VariablesParser;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.DATAX;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.IncludeNoFoundException;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月17日
 */
@Immutable
public class DataServiceInfo
{
    public static final String SCOPE_SINGLETON="singleton";
    
    public static final String SCOPE_PROPERTY="property";
    
    protected final String id;
    
    protected final String serverType;

    protected String description;

    protected List<FieldInfo> fields;

    protected Map<String, OperationInfo> operations;

    protected Class<?> serviceClass;

    protected LookupType lookup;

    protected String scope;

    protected String serviceName;

    public DataServiceInfo(String id,String serverType)
    {
        Assert.assertNotNull(id);
        this.id = id;
        this.serverType=serverType;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    
    public LookupType getLookup() {
        return lookup;
    }

    
    public String getScope() {
        return scope;
    }
    
    public String getServerType() {
        return serverType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Collection<OperationInfo> getOperations() {
        return operations.values();
    }

    public OperationInfo getOperationInfo(String id) {
        OperationInfo oi = operations.get(id);
        if (oi != null) {
            return oi;
        } else {
            if (id.startsWith(DATAX.DS_AREA)) {
                id = new StringBuilder().append(this.id).append(".").append(id.substring(1)).toString();
            }
            return operations.get(id);
        }
    }

    public static class Parser extends BaseXmlNodeParser<DataServiceInfo>
    {

        @Override
        public DataServiceInfo parse(XMLNode node, XmlParserContext context) {
            String id = node.getStringAttribute("id");
            if (!validateId(id)) {
                throw new BuilderException("Invalid id at:" + node.getPath());
            }
            String orig = context.getCurrentService();
            try {
                context.setCurrentService(id);
                id = context.applyCurrentNamespace(id, false);
                List<FieldInfo> fields = parseFields(id, node.evalNode("fields"), context);
                Map<String, OperationInfo> operations = parseOperations(node.evalNode("operations"), context);
                String description = node.evalString("description");
                Class<?> clazz = paseClass(node, "serviceClass");
                String strlookup = node.getStringAttribute("lookup");
                LookupType lookup;
                if (strlookup == null) {
                    lookup = LookupType.NEW;
                } else {
                    lookup = LookupType.fromValue(strlookup);
                }
                String name = node.getStringAttribute("serviceName");
                String scope = node.getStringAttribute("scope",SCOPE_SINGLETON);
                DataServiceInfo dsi = new DataServiceInfo(id,context.getServerType());
                dsi.fields = fields;
                dsi.description = description;
                dsi.operations = operations;
                dsi.serviceClass = clazz;
                dsi.serviceName = name;
                dsi.lookup = lookup;
                dsi.scope = scope;
                return dsi;
            } finally {
                context.setCurrentService(orig);
            }

        }

        protected Map<String, OperationInfo> parseOperations(XMLNode node, XmlParserContext context) {
            List<XMLNode> nodes = node.evalNodes("fetch|add|remove|update|custom");
            if (nodes == null || nodes.size() == 0) {
                return null;
            }
            Map<String, OperationInfo> operations = new LinkedHashMap<String, OperationInfo>(nodes.size());
            // List<OperationInfo> operations = new ArrayList<OperationInfo>(nodes.size());
            for (XMLNode n : nodes) {
                OperationInfo oi = context.parseNode(XmlNodeParserProvider.OPERATION, n, OperationInfo.class);
                if (operations.containsKey(oi.getId())) {
                    throw new BuilderException("Have duplicate Operation id:" + oi.getId());
                }
                operations.put(oi.getId(), oi);
            }
            return operations;
        }

        public List<FieldInfo> parseFields(String serviceId, XMLNode node, XmlParserContext context) {
            // 处理Include
            include(node.getNode(), context);
            // 供别的服务Include.每个service只有一个Fields，所以使用Service id
            context.getRepositoryService().addInclude(serviceId, node);
            List<XMLNode> nodes = node.evalNodes("field");
            List<FieldInfo> fields = new ArrayList<FieldInfo>(nodes.size());
            for (XMLNode n : nodes) {
                FieldInfo fi = context.parseNode(n, FieldInfo.class);
                if (fields.contains(fi)) {
                    throw new BuilderException("Have duplicate field id:" + fi.getName());
                }
                fields.add(fi);
            }
            return fields;

        }

        protected void parseInclude(XMLNode xml, XmlParserContext context) {
            Node node = xml.getNode();
            include(node, context);
        }

        private void include(Node source, XmlParserContext context) {
            if (source.getNodeName().equals("include")) {
                Node toInclude = findInculdeNode(getStringAttribute(source, "refid"), context);
                include(toInclude, context);
                if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                    toInclude = source.getOwnerDocument().importNode(toInclude, true);
                }
                source.getParentNode().replaceChild(toInclude, source);
                while (toInclude.hasChildNodes()) {
                    toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
                }
                toInclude.getParentNode().removeChild(toInclude);
            } else if (source.getNodeType() == Node.ELEMENT_NODE) {
                NodeList children = source.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    include(children.item(i), context);
                }
            }
        }

        /**
         * @param stringAttribute
         * @param context
         * @return
         */
        private Node findInculdeNode(String id, XmlParserContext context) {
            id = VariablesParser.parse(id, context.getDataServiceManagerProperties());
            id = context.applyCurrentNamespace(id, true);
            try {
                XMLNode include = context.getIncludeXMLNode(id);
                return include.getNode();
            } catch (IllegalArgumentException e) {
                throw new IncludeNoFoundException("Can't found Include with id:" + id, e);
            }
        }

        private String getStringAttribute(Node node, String name) {
            return node.getAttributes().getNamedItem(name).getNodeValue();
        }
    }

}
