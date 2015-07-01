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

import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.model.DataServiceInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月26日
 */

public class DataServiceInfoResolver implements ReferenceResolver
{

    private final XMLNode node;

    private final XmlParserContext context;

    public DataServiceInfoResolver(XMLNode node, XmlParserContext context)
    {
        this.node = node;
        this.context = context;
    }

    @Override
    public void resolve() {
        DataServiceInfo dsi= context.parseNode(XmlNodeParserProvider.SERVICE, node, DataServiceInfo.class);
        if(dsi!=null){
            context.getRepositoryService().addDataService(dsi);
        }
    }
    @Override
    public String toString(){
        String id = node.getStringAttribute("id");
         id = context.applyCurrentNamespace(id, true);
        return new StringBuilder().append("Resolver DataService:").append(id).toString();
    }

}
