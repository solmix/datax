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

import javax.servlet.http.HttpServletResponse;

import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Service;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.wmix.exchange.WmixMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月25日
 */

public class TemplateOutFaultInterceptor extends PhaseInterceptorSupport<Message>
{

    /**
     * @param phase
     */
    public TemplateOutFaultInterceptor()
    {
        super(Phase.MARSHAL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = exchange.get(Endpoint.class);
        final Service service = endpoint.getService();

        Object content_type = message.get(Message.CONTENT_TYPE);
        Object encoding = exchange.get(Message.ENCODING);
        final HttpServletResponse response = (HttpServletResponse) message.get(WmixMessage.HTTP_RESPONSE);
        if (content_type != null) {
            response.setContentType(content_type.toString());
        }
        if (encoding != null) {
            response.setCharacterEncoding(encoding.toString());
        }

        try {
            response.sendRedirect("500.html");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
