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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.RequestContext;
import org.solmix.datax.wmix.context.ExchangeRequestContext;
import org.solmix.datax.wmix.type.DSProtocol;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Service;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.data.ObjectReader;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.exchange.model.ArgumentInfo;
import org.solmix.wmix.exchange.WmixMessage;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月16日
 */

public abstract class AbstractInInterceptor extends PhaseInterceptorSupport<WmixMessage>
{
    public static final String PAYLOAD_NAME = "__payload";

    public static final String DATAFORMAT = "_dataFormat";

    public static final String PROTOCOL = "_protocol";


    public static final String DATASTYLE = "_dataStyle";
    public static final String XML_PREFIX = "<";

    public static final String JSON_PREFIX = "{";
    public AbstractInInterceptor()
    {
        super(Phase.DECODE);
    }

    @Override
    public void handleMessage(WmixMessage message) throws Fault {
        HttpServletRequest request=(HttpServletRequest)  message.get(WmixMessage.HTTP_REQUEST);
        Object protocol =request.getParameter(PROTOCOL);
        DSProtocol pro=null;
        if(protocol!=null){
            pro=DSProtocol.valueOf(protocol.toString());
        }
        if (pro == null||DSProtocol.POSTXML==pro||DSProtocol.POSTMESSAGE==pro){
            handlePostMessage(message);
        }else if(DSProtocol.GETPARAMS==pro||DSProtocol.POSTPARAMS==pro){
            handleParameters(message);
        }
    }

    protected void handleParameters(WmixMessage message) {
        
    }

    /**
     * @param message
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void handlePostMessage(WmixMessage message) {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = exchange.get(Endpoint.class);
        final DataServiceManager dataServiceManager = exchange.get(DataServiceManager.class);
        final Service service = endpoint.getService();
        DataProcessor dataProcessor = service.getDataProcessor();
        ArgumentInfo arg = message.get(ArgumentInfo.class);
        HttpServletRequest request = (HttpServletRequest) message.get(WmixMessage.HTTP_REQUEST);
        String payload = request.getParameter(PAYLOAD_NAME);
        // back support
        if (payload == null) {
            payload = request.getParameter("_transaction");
        }
        Map<String, Object> inputData;
        //从参数中读取数据
        if (payload != null) {
            String content = (String) message.get(Message.CONTENT_TYPE);
            if (content == null) {
                if (payload.startsWith(JSON_PREFIX)) {
                    content = "json";
                } else if (payload.startsWith(XML_PREFIX)) {
                    content = "xml";
                } else {
                    throw new IllegalArgumentException("Not support content-type :" + payload);
                }
            }
            if (content.toLowerCase().indexOf("xml") != -1) {
                arg.setProperty(Message.CONTENT_TYPE, "xml");
            } else {
                arg.setProperty(Message.CONTENT_TYPE, "json");
            }
            StringReader sr = new StringReader(payload);
            ObjectReader<Reader> reader = dataProcessor.createReader(Reader.class);
            inputData = (Map) reader.read(sr, arg);
            // 从HTTP包体中读取数据
        } else {
            arg.setProperty(Message.ENCODING, message.get(Message.ENCODING));
            ObjectReader<InputStream> reader = dataProcessor.createReader(InputStream.class);
            InputStream is =message.getContent(InputStream.class);
            inputData = (Map) reader.read(is, arg);
            if(is!=null){
                org.apache.commons.io.IOUtils.closeQuietly(is);
            }
            
            message.removeContent(InputStream.class);
        }
        postToSchema(new DataTypeMap(inputData), dataServiceManager, message,exchange);
    }
    
    protected   RequestContext wrappedRequestcontext(Exchange exchange){
        return new ExchangeRequestContext(exchange);
    }
    
    protected abstract void postToSchema(DataTypeMap mapdata,DataServiceManager manager, WmixMessage message,Exchange exchange) ;
}
