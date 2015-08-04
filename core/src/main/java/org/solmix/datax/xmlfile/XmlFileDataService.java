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
package org.solmix.datax.xmlfile;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.DOMUtils;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.support.BaseDataService;
import org.solmix.datax.support.DSResponseImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.resource.InputStreamResource;
import org.solmix.runtime.resource.ResourceManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月4日
 */

public class XmlFileDataService extends BaseDataService
{

    public XmlFileDataService(DataServiceInfo info, Container container, DataTypeMap prop)
    {
        super(info, container, prop);
    }

    @Override
    public String getServerType() {
        return XmlFileDataServiceFactory.BASE;
    }
    
    @Override
    protected DSResponse executeFetch(DSRequest req) throws DSCallException{
       OperationInfo oi=req.getOperationInfo();
       Object dataUrl=oi.getProperty("dataUrl");
       
       Object recordXPath=oi.getProperty("recordXPath");
       DSResponseImpl res = new DSResponseImpl(this, req);
       Object value = getDataFromFile(dataUrl);
       if(recordXPath!=null){
           JXPathContext context = JXPathContext.newContext(value);
           res.setRawData(context.getValue(recordXPath.toString()));
       }else{
           res.setRawData(value);
       }
       return res;
    }

    /**
     * @param dataUrl
     * @return
     * @throws DSCallException 
     */
    private Object getDataFromFile(Object dataUrl) throws DSCallException {
        if(dataUrl==null){
            return null;
        }
        InputStream is = null;
        try {
            ResourceManager rm=  getContainer().getExtension(ResourceManager.class);
            if(rm!=null){
                InputStreamResource isr=  rm.getResourceAsStream(dataUrl.toString());
                is=isr.getInputStream();
               return DOMUtils.getValue(is);
            }
           
        } catch (Exception e) {
            throw new DSCallException("Parse xml failed:", e);
        } finally {
            if (is != null)
                IOUtils.closeQuietly(is);
        }
        return null;
    }

}
