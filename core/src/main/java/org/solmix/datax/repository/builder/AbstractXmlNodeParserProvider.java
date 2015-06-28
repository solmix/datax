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

import java.util.HashMap;
import java.util.Map;

import org.solmix.commons.util.Reflection;
import org.solmix.runtime.Container;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月25日
 * @param <T>
 */

public abstract class AbstractXmlNodeParserProvider implements XmlNodeParserProvider
{

   
    private boolean init;
    
    private Map<String,Class<?>> registryClasses= new HashMap<String, Class<?>>();
    
    private Map<String,XmlNodeParser<?>> registry=new HashMap<String, XmlNodeParser<?>>();
    
    Container container;
    public AbstractXmlNodeParserProvider(Container container){
        this.container=container;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> XmlNodeParser<T> getXmlNodeParser(String path, Class<T> clz) {
        ensureInit();
        XmlNodeParser<?> parser=registry.get(path);
        if(parser!=null){
            return XmlNodeParser.class.cast(parser);
        }
        Class<?> parserClass=registryClasses.get(path);
        if(parserClass!=null){
            try {
               Object instance= Reflection.newInstance(parserClass);
               if(instance instanceof XmlNodeParser){
                   XmlNodeParser<?> p=(XmlNodeParser<?>)instance;
                   configParser(p);
                   registry.put(path, p);
                   return XmlNodeParser.class.cast(p);
               }
            } catch (Exception e) {
                throw new BuilderException("Can't instance xmlNodeParser for path:"+path, e);
            }
        }
        return null;
    }
    
    /**
     * @param p
     */
    private void configParser(XmlNodeParser<?> p) {
       if(this.container!=null){
           ResourceManager resourceManager = container.getExtension(ResourceManager.class);
           if (resourceManager != null) {
               ResourceInjector injector = new ResourceInjector(resourceManager);
               injector.inject(p);
               injector.construct(p);
           }
       }
        
    }

    protected synchronized void ensureInit(){
        if(!init){
            config();
            init=true;
        }
    }
    
    /**
     * 子类可以通过该方法实现{@link XmlNodeParser}的配置
     */
    protected abstract void config();
    
    protected <T> void bind(String path,Class<? extends XmlNodeParser<T>> parser){
        registryClasses.put(path, parser);
    }

}
