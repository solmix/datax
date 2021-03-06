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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.xml.VariablesParser;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.XmlElement;
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

    protected Map<String,FieldInfo> fields;
    
    protected List<FieldInfo> fieldList;

    protected Map<String, OperationInfo> operations;

    protected Class<?> serviceClass;

    protected LookupType lookup;

    protected String scope;

    protected String serviceName;
    
    protected XMLNode node;
    
    protected String[] requires;
    
    protected String[] requireRoles;
    
   
    protected Boolean rest;

    public DataServiceInfo(String id,String serverType)
    {
        Assert.assertNotNull(id);
        this.id = id;
        this.serverType=serverType;
    }

    public List<FieldInfo> getFields() {
        return fieldList;
    }

    public String getId() {
        return id;
    }
    
    /**
     * 标记为rest api,可通过json或者xml调用
     * @return
     */
    public boolean isRest(){
        return rest==null ||rest;
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
    public Object getProperty(String key) {
        if(getXMLNode()!=null){
            return getXMLNode().getStringAttribute(key);
        }
        return null;
    }
    
    
    public String[] getRequires() {
        return requires;
    }
    
    
    public String[] getRequireRoles() {
        return requireRoles;
    }

    public XMLNode getXMLNode() {
        return node;
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
        if(operations==null){
            return null;
        }
        return operations.values();
    }

    public OperationInfo getOperationInfo(String id) {
        if(operations==null){
            return null;
        }
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
                Map<String,FieldInfo> fields = parseFields(id, node.evalNode("fields"), context);
                List<FieldInfo> fieldList =null;
                if(fields!=null){
                    fieldList= new ArrayList<FieldInfo>(fields.size());
                    for(FieldInfo fi:fields.values()){
                        fieldList.add(fi);
                    }
                }
                
                Map<String, OperationInfo> operations = parseOperations(node.evalNode("operations"), context);
                //BATCH中配置的也放入operaions中，方便索引
                if (operations != null) {
                    List<OperationInfo> tmp = new ArrayList<OperationInfo>();
                    for (OperationInfo oi : operations.values()) {
                        getBatchOperations(oi,tmp,oi.getId());
                    }
                    if(tmp.size()>0){
                        for(OperationInfo toi:tmp){
                            if(operations.containsKey(toi.getId())){
                                throw new BuilderException("duplicate operationid:"+toi.getId());
                            }
                            operations.put(toi.getId(), toi);
                        }
                    }
                }
                
                String description = node.evalString("description");
                Class<?> clazz = paseClass(node, "serviceClass");
                String strlookup = node.getStringAttribute("lookup");
                LookupType lookup = null;
                if (strlookup != null) {
                    lookup = LookupType.fromValue(strlookup);
                }
                String name = node.getStringAttribute("serviceName");
                String scope = node.getStringAttribute("scope",SCOPE_SINGLETON);
                DataServiceInfo dsi = new DataServiceInfo(id,context.getServerType());
                String[] requires = paseStringArray(node, "requires",DATAX.AUTH_SEPARATOR);
                String[] requireRoles=paseStringArray(node,"requireRoles",DATAX.AUTH_SEPARATOR);
                Boolean rest= node.getBooleanAttribute("rest");
                dsi.fields = fields;
                dsi.node=node;
                dsi.fieldList=fieldList;
                dsi.description = description;
                dsi.operations = operations;
                dsi.serviceClass = clazz;
                dsi.serviceName = name;
                dsi.requires=requires;
                dsi.requireRoles=requireRoles;
                dsi.lookup = lookup;
                dsi.scope = scope;
                dsi.rest=rest;
                return dsi;
            } finally {
                context.setCurrentService(orig);
            }

        }
        private void getBatchOperations(OperationInfo oi,List<OperationInfo> tmp,String parentId){
            BatchOperations bos = oi.getBatch();
            if (bos != null && bos.getOperations() != null) {
                int count=1;
                for (OperationInfo o : bos.getOperations()) {
                   if(o.getId()==null){
                       if(o.localId==null){
                           o.id=parentId+"#"+count;
                       }else{
                           o.id=parentId+"#"+o.localId;
                       }
                   }
                    count++;
                    tmp.add(o);
                    BatchOperations boso = o.getBatch();
                    if (boso != null && boso.getOperations() != null) {
                        getBatchOperations(o,tmp,o.id);
                    }
                }
            }
        }

        protected Map<String, OperationInfo> parseOperations(XMLNode node, XmlParserContext context) {
            if(node==null){
                return null;
            }
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

        public Map<String,FieldInfo> parseFields(String serviceId, XMLNode node, XmlParserContext context) {
            if(node==null){
                return null;
            }
            // 处理Include
            include(node.getNode(), context);
            // 供别的服务Include.每个service只有一个Fields，所以使用Service id
            context.getRepositoryService().addInclude(serviceId, node);
            List<XMLNode> nodes = node.evalNodes("field");
            Map<String,FieldInfo> fields = new LinkedHashMap<String, FieldInfo>(nodes.size());
//            List<FieldInfo> fields = new ArrayList<FieldInfo>(nodes.size());
            for (XMLNode n : nodes) {
                FieldInfo fi = context.parseNode(n, FieldInfo.class);
                if (fields.containsValue(fi)) {
                    throw new BuilderException("Have duplicate field id:" + fi.getName());
                }
                fields.put(fi.getName(), fi);
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

    /**
     * @param fieldName
     * @return
     */
    public FieldInfo getField(String fieldName) {
        return fields==null?null: fields.get(fieldName);
    }

    public XmlElement toElement() {
        XmlElement xml = new XmlElement("service");
        xml.addAttribute(new Attribute("id", id));
        if(serviceClass!=null){
            xml.addAttribute(new Attribute("serviceClass", serviceClass.getName()));
        }
        if(hasLength(serviceName)){
            xml.addAttribute(new Attribute("serviceName", serviceName));
        }
        if(hasLength(scope)){
            xml.addAttribute(new Attribute("scope", scope));
        }
        if(lookup!=null){
            xml.addAttribute(new Attribute("lookup", lookup.value()));
        }
        if(rest!=null){
            xml.addAttribute(new Attribute("rest", rest.toString()));
        }
        if(requires!=null){
            xml.addAttribute(new Attribute("requires", StringUtils.toString(requires)));
        }
        if(requireRoles!=null){
            xml.addAttribute(new Attribute("requireRoles",  StringUtils.toString(requireRoles)));
        }
        if(operations!=null&&operations.size()>0){
            XmlElement operationList = new XmlElement("operations");
            for(OperationInfo pi :operations.values()){
                operationList.addElement(pi.toElement());
            }
            xml.addElement(operationList);
        }
        if(fields!=null&&fields.size()>0){
            XmlElement fieldList = new XmlElement("fields");
            for(FieldInfo pi :fields.values()){
                fieldList.addElement(pi.toElement());
            }
            xml.addElement(fieldList);
        }
        return xml;
    }

}
