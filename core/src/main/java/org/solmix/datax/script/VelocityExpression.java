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

package org.solmix.datax.script;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;
import org.solmix.datax.support.InvokerException;
import org.solmix.runtime.Container;
import org.solmx.service.toolkit.ToolkitService;
import org.solmx.service.velocity.VelocityEngine;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月31日
 */

public class VelocityExpression
{
    private static final Logger LOG = LoggerFactory.getLogger(VelocityExpression.class);
    private final RuntimeInstance instance;

    private final Container container;

    private final ToolkitService toolkit;

    public VelocityExpression(Container container)
    {

        this.container = container;
        VelocityEngine ve = container.getExtension(VelocityEngine.class);
        Assert.assertNotNull(ve, "VelocityEngine");
        instance = ve.getRuntimeInstance();
        toolkit = container.getExtension(ToolkitService.class);
    }

    public Object evaluateValue(String expression, Map<String, Object> context) {
        VelocityContext vContext = new VelocityContext(context);
        Object result;
        if (expression.startsWith("#")) {
            StringWriter out = new StringWriter();
            instance.evaluate(vContext, out, "VelocityExpression", expression);
            result= out.toString();
        } else {
            try {
                instance.evaluate(vContext, null, "VelocityExpression",
                    new StringBuilder().append("#set($").append(DATAX.VT_TMP_NAME).append(" = ").append(expression).append(")\n").toString());
            } catch (Exception e) {
                throw new InvokerException("Velocity evalute exception:\n", e);
            }
            result= vContext.get(DATAX.VT_TMP_NAME);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("expression["+expression+"] evaluate to value:"+result);
        }
        return result;
    }

    /**
     * 根据Velocity表达式返回值，并允许DataService做特殊处理
     * 
     * @param expression velocity表达式
     * @param context velocity context
     * @param dataService DataService 根据DataService来过滤一些key,将这写key交由DS来处理并返回值。
     * @return
     */
    public Object mergeValue(String expression, Map<String, Object> context, DataService dataService) {
        StringWriter out = new StringWriter();
        VelocityContext vContext = new VelocityContext(context);
        VelocityReferenceInsertionEventHandler handler = new VelocityReferenceInsertionEventHandler(vContext, dataService);
        try {
            instance.evaluate(vContext, out, "VelocityExpression", expression);
        } catch (Exception e) {
            throw new InvokerException("Velocity evalute exception:\n", e);
        }
        if (handler.foundObject != null && handler.foundObject.toString().equals(out.toString()))
            return handler.foundObject;
        else
            return out.toString();
    }

    public Object evaluateValue(String expression, DSRequest request, RequestContext requestContext) {
        return evaluateValue(expression, prepareContext(request, requestContext));
    }

    public Map<String, Object> prepareContext(DSRequest request, RequestContext requestContext) {

        Map<String, Object> vc = new HashMap<String, Object>();
        if (toolkit != null) {
            Map<String, Object> tools = toolkit.getTools();
            vc.putAll(tools);
        }
        vc.put(DATAX.VM_CONTAINER, container);
        if (requestContext != null) {
            Set<String> keys = requestContext.keySet();
            for (String key : keys) {
                if (!key.startsWith("java.") && !key.startsWith("javax.")) {
                    vc.put(key, requestContext.get(key));
                }
            }
            vc.put(DATAX.VM_REQUESTCONTEXT, requestContext);
        }
        vc.put(DATAX.VM_DSREQUEST, request);
        if (request.getDSCall() != null) {
            vc.put(DATAX.VM_DSC, request.getDSCall());
        }
        vc.put(DATAX.VM_DS, request.getDataService());
        vc.put(DATAX.VM_VALUES, request.getRawValues());

        return vc;
    }

}
