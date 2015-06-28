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
import java.util.List;

import org.junit.Assert;
import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.VariablesParser;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.IncludeNoFoundException;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月17日
 */
@Immutable
public class DataServiceInfo
{
    private final String id;
    
    private  List<FieldInfo> fields;
    
    public DataServiceInfo(String id){
        Assert.assertNotNull(id);
        this.id=id;
    }
    
    public List<FieldInfo> getFields() {
        return fields;
    }


    public String getId() {
        return id;
    }

public static class Parser extends BaseXmlNodeParser<DataServiceInfo>{

    
    @Override
    public DataServiceInfo parse(XMLNode node,XmlParserContext context){
        String id= node.getStringAttribute("id");
        if(!validateId(id)){
            throw new BuilderException("Invalid id at:"+node.getPath());
        }
        id=context.applyCurrentNamespace(id, false);
        List<FieldInfo> fields = parseFields(node.evalNode("fields"),context);
        DataServiceInfo dsi = new DataServiceInfo(id);
        dsi.fields=fields;
        return dsi;
    }
    
    public List<FieldInfo> parseFields(XMLNode node,XmlParserContext context){
        //处理Include
        include(node.getNode(), context);
        //供别的服务Include.
        context.getRepositoryService().addInclude(context.getCurrentNamespace(),node);
        List<XMLNode> nodes = node.evalNodes("field");
        List<FieldInfo> fields = new ArrayList<FieldInfo>(nodes.size());
        for(XMLNode n:nodes){
            fields.add(context.parseNode(n, FieldInfo.class));
        }
        return fields;
        
    }
    protected void parseInclude(XMLNode xml,XmlParserContext context){
        Node node=xml.getNode();
        include(node, context);
    }
    
    private void include(Node source,XmlParserContext context){
        if (source.getNodeName().equals("include")) {
            Node toInclude = findInculdeNode(getStringAttribute(source, "refid"),context);
            include(toInclude,context);
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
            for (int i=0; i<children.getLength(); i++) {
                include(children.item(i),context);
            }
          }
    }
    /**
     * @param stringAttribute
     * @param context
     * @return
     */
    private Node findInculdeNode(String id, XmlParserContext context) {
        id=VariablesParser.parse(id, context.getDataServiceManagerProperties());
        id=context.applyCurrentNamespace(id, true);
        try{
            XMLNode include= context.getIncludeXMLNode(id);
            return include.getNode();
        }catch(IllegalArgumentException e){
            throw new IncludeNoFoundException("Can't found Include with id:"+id,e);
        }
    }

    private String getStringAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
      }
   }

}
