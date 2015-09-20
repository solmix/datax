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
package org.solmix.datax.wmix.interceptor;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.solmix.commons.util.ServletUtils;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.export.ExportConfig;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.runtime.io.CachedOutputStream;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmx.service.template.TemplateException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月9日
 */

public class TemplateInterceptor extends PhaseInterceptorSupport<Message>
{

    public TemplateInterceptor()
    {
        super(Phase.DECODE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final Exchange exchange = message.getExchange();

        final HttpServletResponse httpResponse = (HttpServletResponse) message.get(WmixMessage.HTTP_RESPONSE);
        DSResponse response = (DSResponse) message.getContent(Object.class);
        DSRequest request = (DSRequest)exchange.getIn().getContent(Object.class);
        OutputStream out = message.getContent(OutputStream.class);
        Object rawData=response.getRawData();
        String contentType = (String)response.getAttribute(Message.CONTENT_TYPE);
        if(contentType==null){
            contentType=(String)exchange.get(Message.ACCEPT_CONTENT_TYPE);
        }
       
        if(contentType!=null){
            httpResponse.setContentType(contentType);
            //不是输出为text的采用下载方式。
            if(!DATAX.TEMPLATE_CONTENT_TYPE_DEFAULT.equals(contentType)){
                ExportConfig export= request.getAttachment(ExportConfig.class);
                String filename="template";
                if(export!=null){
                    filename=export.getExportFilename();
                }
                String fileNameEncoding = ServletUtils.encodeParameter("filename", filename);
                httpResponse.addHeader("content-disposition", "attachment;" + fileNameEncoding);
            }
        }
        if(rawData instanceof CachedOutputStream){
            CachedOutputStream outData = (CachedOutputStream)rawData;
            try {
                httpResponse.setContentLength((int)outData.size());
                IOUtils.copy(outData.getInputStream(), out);
                out.flush();
            } catch (IOException e) {
               throw new TemplateException("Evalate Template",e);
            }finally{
                try {
                    outData.close();
                } catch (IOException e) {//IGNORE
                }
            }
        }
    }
}
