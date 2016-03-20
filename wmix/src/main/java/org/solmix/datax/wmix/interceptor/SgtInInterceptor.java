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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.solmix.datax.wmix.Constants;
import org.solmix.exchange.Exchange;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmix.wmix.parser.ParameterParser;

/**
 * For SmartClient JS Binding interceptor.
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月16日
 */

public class SgtInInterceptor extends AbstractInInterceptor
{

    private DSCallFactory dscFactory = new DefaultDSCallFactory();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void postToSchema(DataTypeMap data, DataServiceManager manager, WmixMessage message, Exchange exchange, ParameterParser parameterParser) {
        List<?> operations = data.getList("operations");
        RequestContext requestContext = wrappedRequestcontext(exchange);
        if (operations != null) {
            if (operations.size() == 1) {
                DSRequest request = manager.createDSRequest();
                prepareRequest(request, new DataTypeMap((Map) operations.get(0)), true);
                request.setRequestContext(requestContext);
                for(String key:parameterParser.keySet()){
                    if(!Constants.PAYLOAD_NAME.equals(key)&&!Constants.SECOND_PAYLOAD_NAME.equals(key)){
                        request.setAttribute(key, parameterParser.get(key));
                    }
                }
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

    @Override
    protected void handleParameters(WmixMessage message) {
        final Exchange exchange = message.getExchange();
        final DataServiceManager manager = exchange.get(DataServiceManager.class);
        ParameterParser parameterParser = exchange.get(ParameterParser.class);
       
        DSRequest dsr = manager.createDSRequest();
        Map<String,Object> values = new HashMap<String, Object>();
        for(String key:parameterParser.keySet()){
            if("_appID".equals(key)){
                dsr.setApplicationId(parameterParser.getString(key));
            }else if("_startRow".equals(key)){
                PagedBean page = new PagedBean();
                page.setStartRow(parameterParser.getInt("_startRow"));
                page.setEndRow(parameterParser.getInt("_endRow"));
                dsr.addAttachment(Pageable.class, page);
            }else if("_exportResults".equals(key)){
                if(parameterParser.getBoolean("_exportResults")){
                    ExportConfig export = new ExportConfig();
                    export.setExportAs(parameterParser.getString("_exportAs"));
                    export.setExportDatesAsFormattedString(parameterParser.getBoolean("_exportDatesAsFormattedString"));
                    export.setExportDelimiter(parameterParser.getString("_exportDelimiter"));
                    export.setExportDisplay(parameterParser.getString("_exportDisplay"));
                    export.setExportFilename(parameterParser.getString("_exportFilename"));
                    String exportFields = parameterParser.getString("_exportFields");
                    if (exportFields != null) {
                        export.setExportFields(Arrays.asList(exportFields.split(",")));
                    }
                    export.setExportFooter(parameterParser.getString("_exportFooter"));
                    export.setExportHeader(parameterParser.getString("_exportHeader"));
                    export.setLineBreakStyle(parameterParser.getString("_lineBreakStyle"));

                    dsr.addAttachment(ExportConfig.class, export);
                }
            }else if("_action".equals(key)){
                dsr.setOperationId(parameterParser.getString("_action"));
            }else if(key.startsWith("_")){
                dsr.setAttribute(key, parameterParser.getString(key));
            }else{
                values.put(key, parameterParser.getString(key));
            }
        }
        dsr.setRawValues(values);
        RequestContext requestContext = wrappedRequestcontext(exchange);
        dsr.setRequestContext(requestContext);
        message.setContent(Object.class, dsr);
    }
}
