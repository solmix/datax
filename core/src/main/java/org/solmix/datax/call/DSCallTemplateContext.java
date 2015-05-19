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

package org.solmix.datax.call;

import java.util.Map;
import java.util.Set;

import org.solmix.datax.DATAX;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.RequestContext;
import org.solmix.runtime.Container;
import org.solmx.service.template.support.MappedTemplateContext;
import org.solmx.service.toolkit.ToolkitService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年5月19日
 */

public class DSCallTemplateContext extends MappedTemplateContext
{

    public DSCallTemplateContext(ToolkitService toolkit, DSRequest request, DSResponse response)
    {
        initToolkit(toolkit);
        initRequestContext(request.getRequestContext());
        initResponse(request, response);
    }

    protected void initResponse(DSRequest request, DSResponse response) {
        put(DATAX.VM_DSREQUEST, request);
        put(DATAX.VM_DSRESPONSE, response);
        if (request.getDSCall() != null) {
            put(DATAX.VM_DSC, request.getDSCall());
        }
        put(DATAX.VM_DS, request.getDataService());
        put(DATAX.VM_VALUES, request.getRawValues());

        put(DATAX.VM_DATA, response.getRawData());
    }

    protected void initContainer(Container container) {
        put(DATAX.VM_CONTAINER, container);
    }

    protected void initRequestContext(RequestContext requestContext) {
        if (requestContext != null) {
            Set<String> keys = requestContext.keySet();
            for (String key : keys) {
                if (!key.startsWith("java.") && !key.startsWith("javax.")) {
                    put(key, requestContext.get(key));
                }
            }
            put(DATAX.VM_REQUESTCONTEXT, requestContext);
        }
    }

    protected void initToolkit(ToolkitService toolkit) {
        if (toolkit != null) {
            Map<String, Object> tools = toolkit.getTools();
            if (tools != null) {
                for (String key : tools.keySet()) {
                    put(key, tools.get(key));
                }
            }
        }
    }

    public DSCallTemplateContext(Container container, DSRequest request, DSResponse response)
    {
        initContainer(container);
        initToolkit(container.getExtension(ToolkitService.class));
        initRequestContext(request.getRequestContext());
        initResponse(request, response);
    }
}
