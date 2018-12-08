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

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.wmix.serializer.ResultObject;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Service;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.data.ObjectWriter;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.runtime.exception.InvokerException;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmix.wmix.mapper.MapperException;
import org.solmix.wmix.mapper.MapperService;
import org.solmix.wmix.mapper.MapperTypeNotFoundException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月25日
 */

public class OutFaultInterceptor extends PhaseInterceptorSupport<Message>
{

    private MapperService mapperService;

    public OutFaultInterceptor()
    {
        super(Phase.MARSHAL);
    }
    public MapperService getMapperService() {
 		return mapperService;
 	}

 	public void setMapperService(MapperService mapperService) {
 		this.mapperService = mapperService;
 	}
    @Override
    public void handleMessage(Message message) throws Fault {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = exchange.get(Endpoint.class);
        final Service service = endpoint.getService();
        final DataServiceManager dataServiceManager = exchange.get(DataServiceManager.class);
        final DataProcessor dataProcessor = service.getDataProcessor();

        Object content_type = message.get(Message.CONTENT_TYPE);
        Object encoding = exchange.get(Message.ENCODING);
        final HttpServletResponse response = (HttpServletResponse) message.get(WmixMessage.HTTP_RESPONSE);
        if (content_type != null) {
            response.setContentType(content_type.toString());
        }
        if (encoding != null) {
            response.setCharacterEncoding(encoding.toString());
        }

        OutputStream out = message.getContent(OutputStream.class);
        DSResponse res = dataServiceManager.createDsResponse(null);
        Message fault = exchange.getOutFault();
        Exception e = fault.getContent(Exception.class);
        String mssage = handleException(e);
        res.setStatus(Status.STATUS_FAILURE);
        res.setRawData(mssage);
        try {
            ObjectWriter<OutputStream> writer = dataProcessor.createWriter(OutputStream.class);
            writer.write(new ResultObject(res), out);
        } finally {
            if (out != null) {
                IOUtils.closeQuietly(out);
            }
        }

    }

	private String handleException(Exception e) {
		if(e==null) {
			return "";
		}else {
			if(e instanceof Fault){
				if(e.getCause() instanceof InvokerException &&e.getCause().getCause()!=null){
					return mapperString(e.getCause().getCause());
				}else{
					return mapperString(e.getCause());
				}
			}
		}
		return StringUtils.toString(e);
	}
	private String mapperString(java.lang.Throwable e) {
		if(mapperService!=null) {
			String exception = e.getClass().getName();
			try {
				return mapperService.map("exception", exception);
			} catch (MapperTypeNotFoundException e1) {
				return StringUtils.toString(e);
			} catch (MapperException e1) {
				return StringUtils.toString(e);
			}
		}else {
			return StringUtils.toString(e);
		}
	}

}
