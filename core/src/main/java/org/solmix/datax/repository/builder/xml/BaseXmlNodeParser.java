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

import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.DATAX;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.XmlNodeParser;
import org.solmix.datax.repository.builder.XmlParserContext;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月25日
 */

public abstract class BaseXmlNodeParser<T> implements XmlNodeParser<T>
{
    /**验证ID，有效返回true，无效返回false*/
    protected boolean  validateId(String id){
       return DATAX.ID_PATTERN.matcher(id).matches();
    }
    
    /**
     * 同一个service下的reference用一个#占位
     * 同一个name space下的reference用两个##占位
     * @param id
     * @param context
     * @return
     */
    public static String parseRefid(String id,XmlParserContext context){
        if(id.startsWith(DATAX.NS_AREA)){
            return context.applyCurrentNamespace(id.substring(2), false);
        }else if(id.startsWith(DATAX.DS_AREA)){
            return context.applyCurrentService(id.substring(1), false);
        }else{
            return id;
        }
    }
    
    public <E> Class<? extends E> paseClass(XMLNode node,Class<E> clz){
        String strclz =node.getStringAttribute("class");
        if(strclz==null){
            return null;
        }
        try {
            Class<? extends E> loadclz = ClassLoaderUtils.loadClass(strclz, clz, clz);
            return loadclz;
        } catch (ClassNotFoundException e) {
            throw new BuilderException("Can't load class.", e);
        }
        
    }

    public <E> Class<?> paseClass(XMLNode node,String name){
        String strclz =node.getStringAttribute(name);
        if(strclz==null){
            return null;
        }
        try {
            Class<?> loadclz = ClassLoaderUtils.loadClass(strclz,BaseXmlNodeParser.class);
            return loadclz;
        } catch (ClassNotFoundException e) {
            throw new BuilderException("Can't load class.", e);
        }
        
    }
    public <E> Class<?> paseClass(XMLNode node){
       return paseClass(node,"class");
    }
}
