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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.RequestContext;
import org.solmix.datax.attachment.OldValues;
import org.solmix.datax.attachment.OldValuesBean;
import org.solmix.datax.attachment.Pageable;
import org.solmix.datax.attachment.PagedBean;
import org.solmix.datax.call.DSCallFactory;
import org.solmix.datax.call.support.DefaultDSCallFactory;
import org.solmix.datax.export.ExportConfig;
import org.solmix.exchange.Exchange;
import org.solmix.wmix.exchange.WmixMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月16日
 */

public class SgtInInterceptor extends AbstractInInterceptor
{

    private DSCallFactory dscFactory = new DefaultDSCallFactory();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void postToSchema(DataTypeMap data, DataServiceManager manager, WmixMessage message, Exchange exchange) {

        List<?> operations = data.getList("operations");
        RequestContext requestContext = wrappedRequestcontext(exchange);
        if (operations != null) {
            if (operations.size() == 1) {
                DSRequest request = manager.createDSRequest();
                prepareRequest(request, new DataTypeMap((Map) operations.get(0)), true);
                request.setRequestContext(requestContext);
                message.setContent(Object.class, request);
            } else if (operations.size() > 1) {
                List<DSRequest> ml = new ArrayList<DSRequest>();
                for (Object operation : operations) {
                    DSRequest request = manager.createDSRequest();
                    DataTypeMap op = new DataTypeMap((Map) operation);
                    prepareRequest(request, op, false);
                    request.setRequestContext(requestContext);
                    ml.add(request);
                }
                message.setContent(List.class, ml);
            }

        }

    }

    private void prepareRequest(DSRequest request, DataTypeMap operation, boolean joinTransaction) {
        request.setCanJoinTransaction(joinTransaction);
        String action = operation.getString("action");
        Assert.assertNotNull(action, "operation action must be not null");
        request.setOperationId(action);

        request.setRawValues(operation.get("values"));
        request.setApplicationId(operation.getString("appID"));

        Object start = operation.get("startRow");
        Object end = operation.get("endRow");
        if (start != null && end != null) {
            PagedBean page = new PagedBean();
            page.setStartRow(Integer.valueOf(start.toString()));
            page.setEndRow(Integer.valueOf(end.toString()));
            request.addAttachment(Pageable.class, page);
        }
        Object oldValues = operation.get("oldValues");
        if (oldValues != null) {
            request.addAttachment(OldValues.class, new OldValuesBean(oldValues));
        }
        Object exportResults = operation.get("exportResults");
        if (DataUtils.asBoolean(exportResults)) {
            prepareExport(request, operation);
        }

    }

    private void prepareExport(DSRequest request, DataTypeMap operation) {
        ExportConfig export = new ExportConfig();
        export.setExportAs(operation.getString("exportAs"));
        export.setExportDatesAsFormattedString(operation.getBoolean("exportDatesAsFormattedString"));
        export.setExportDelimiter(operation.getString("exportDelimiter"));
        export.setExportDisplay(operation.getString("exportDisplay"));
        export.setExportFilename(operation.getString("exportFilename"));
        String exportFields = operation.getString("exportFields");
        if (exportFields != null) {
            export.setExportFields(Arrays.asList(exportFields.split(",")));
        }
        export.setExportFooter(operation.getString("exportFooter"));
        export.setExportHeader(operation.getString("exportHeader"));
        export.setLineBreakStyle(operation.getString("lineBreakStyle"));

        request.addAttachment(ExportConfig.class, export);
    }

    public DSCallFactory getDscFactory() {
        return dscFactory;
    }

    public void setDscFactory(DSCallFactory dscFactory) {
        this.dscFactory = dscFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleParameters(WmixMessage message) {
        final Exchange exchange = message.getExchange();
        final DataServiceManager manager = exchange.get(DataServiceManager.class);
        HttpServletRequest request=(HttpServletRequest)  message.get(WmixMessage.HTTP_REQUEST);
        Enumeration<String> e = request.getParameterNames();
        if (e == null)
            return;
        DSRequest dsr = manager.createDSRequest();
        Map<String,Object> values = new HashMap<String, Object>();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if("_appID".equals(key)){
                dsr.setApplicationId(request.getParameter(key));
            }else if("_startRow".equals(key)){
                PagedBean page = new PagedBean();
                page.setStartRow(Integer.valueOf(request.getParameter("_startRow")));
                page.setEndRow(Integer.valueOf(request.getParameter("_endRow")));
                dsr.addAttachment(Pageable.class, page);
            }else if("_exportResults".equals(key)){
                if(DataUtils.asBoolean(request.getParameter("_exportResults"))){
                    ExportConfig export = new ExportConfig();
                    export.setExportAs(request.getParameter("_exportAs"));
                    export.setExportDatesAsFormattedString(DataUtils.asBoolean(request.getParameter("_exportDatesAsFormattedString")));
                    export.setExportDelimiter(request.getParameter("_exportDelimiter"));
                    export.setExportDisplay(request.getParameter("_exportDisplay"));
                    export.setExportFilename(request.getParameter("_exportFilename"));
                    String exportFields = request.getParameter("_exportFields");
                    if (exportFields != null) {
                        export.setExportFields(Arrays.asList(exportFields.split(",")));
                    }
                    export.setExportFooter(request.getParameter("_exportFooter"));
                    export.setExportHeader(request.getParameter("_exportHeader"));
                    export.setLineBreakStyle(request.getParameter("_lineBreakStyle"));

                    dsr.addAttachment(ExportConfig.class, export);
                }
            }else if("_action".equals(key)){
                dsr.setOperationId(request.getParameter("_action"));
            }else if(key.startsWith("_")){
                dsr.setAttribute(key, request.getParameter(key));
            }else{
                values.put(key, request.getParameter(key));
            }
        }
        dsr.setRawValues(values);
        RequestContext requestContext = wrappedRequestcontext(exchange);
        dsr.setRequestContext(requestContext);
        message.setContent(Object.class, dsr);
    }
}
