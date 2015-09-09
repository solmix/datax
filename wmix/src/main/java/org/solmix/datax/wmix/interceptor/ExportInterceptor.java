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

import static org.solmix.commons.util.DataUtils.asBoolean;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.ServletUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;
import org.solmix.datax.export.ExportConfig;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.runtime.Container;
import org.solmix.service.export.ExportAs;
import org.solmix.service.export.ExportException;
import org.solmix.service.export.ExportManager;
import org.solmix.service.export.ExportService;
import org.solmix.service.export.support.MappedExportContext;
import org.solmix.wmix.exchange.WmixMessage;

/**
 * 将执行结果作为数据集导出
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月1日
 */

public class ExportInterceptor extends PhaseInterceptorSupport<Message>
{

    public ExportInterceptor()
    {
        super(Phase.DECODE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final Exchange exchange = message.getExchange();
        Message inMsg = exchange.getIn();
        DSRequest request = (DSRequest) inMsg.getContent(Object.class);

        DSResponse response = (DSResponse) message.getContent(Object.class);
        ExportConfig export = request.getAttachment(ExportConfig.class);
        Assert.assertNotNull(export, "ExportConfig is null");

        Container container = exchange.getContainer();
        ExportManager exportManager = container.getExtension(ExportManager.class);
        Assert.assertNotNull(exportManager, "ExportManager is null");

        final HttpServletResponse httpResponse = (HttpServletResponse) message.get(WmixMessage.HTTP_RESPONSE);

        DataService ds = response.getDataService() == null ? request.getDataService() : response.getDataService();
        DataServiceInfo info = ds.getDataServiceInfo();
        Map<String, String> fieldMap = new HashMap<String, String>();
        List<String> fieldNames = export.getExportFields();
        if (DataUtils.isNullOrEmpty(fieldNames)) {
            List<FieldInfo> fields = info.getFields();
            fieldNames = new ArrayList<String>();
            for (FieldInfo field : fields) {
                fieldNames.add(field.getName());
            }
        }
        List<String> efields = export.getExportFields();
        List<String> finalFields = new ArrayList<String>();

        ExportAs exportAs = ExportAs.fromValue(export.getExportAs());
        String separatorChar = export.getExportTitleSeparatorChar();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldTitle = null;
            FieldInfo field = info.getField(fieldName);
            if (field != null && !asBoolean(field.getHidden()) && (field.getCanExport()==null|| asBoolean(field.getCanExport()))) {
                fieldTitle = field.getTitle();
                if (fieldTitle == null) {
                    fieldTitle = fieldName;
                }
                if (exportAs == ExportAs.XML) {
                    if (separatorChar == null)
                        separatorChar = "";
                    fieldTitle = fieldTitle.replaceAll("[$&<>() ]", separatorChar);
                }
                fieldMap.put(fieldName, fieldTitle);
                finalFields.add(fieldName);
            }
        }
        if(DataUtils.isNullOrEmpty(efields)){
            efields=finalFields;
        }

        int lineBreakStyleId = 4;
        if (export.getLineBreakStyle() != null) {
            String lineBreakStyle = export.getLineBreakStyle().toLowerCase();
            lineBreakStyleId = lineBreakStyle.equals("mac") ? 1 : ((int) (lineBreakStyle.equals("unix") ? 2
                : ((int) (lineBreakStyle.equals("dos") ? 3 : 4))));
        }

        String delimiter = export.getExportDelimiter();
        if (delimiter == null || delimiter == "")
            delimiter = ",";
        
        MappedExportContext conf = new MappedExportContext();
        conf.put(ExportService.LINE_BREAK_STYLE, lineBreakStyleId);
        conf.put(ExportService.EXPORT_DELIMITER, delimiter);
        conf.put(ExportService.ORDER, efields);

        String exportHeader = export.getExportHeader();
        if (exportHeader != null) {
            conf.put(ExportService.EXPORT_HEADER_STRING, exportHeader);
        }
        String exportFooter = export.getExportFooter();
        if (exportFooter != null) {
            conf.put(ExportService.EXPORT_FOOTER_STRING, exportFooter);
        }
        ExportService exportService = exportManager.getExportService(exportAs, conf);
        try {
            OutputStream out = message.getContent(OutputStream.class);
            BufferedOutputStream bufferedOS = new BufferedOutputStream(out);
            String fileNameEncoding = ServletUtils.encodeParameter("filename", export.getExportFilename());
            if ("download".equals(export.getExportDisplay())) {
                httpResponse.addHeader("content-disposition", "attachment;" + fileNameEncoding);
                String contentType = null;
                switch (exportAs) {
                    case XML: // '\003'
                        contentType = "unknown";
                        break;

                    case JSON: // '\002'
                        contentType = "application/json";
                        break;

                    case XLS: // '\004'
                        contentType = "application/vnd.ms-excel";
                        break;

                    case OOXML: // '\005'
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        break;

                    case CSV: // '\001'
                        contentType = "text/comma-separated-values";
                        conf.put(ExportService.ENCODING, "gbk");
                        break;

                    default:
                        contentType = "text/csv";
                        break;
                }
                httpResponse.setContentType(contentType);
            } else {
                httpResponse.addHeader("content-disposition", (new StringBuilder()).append("inline; ").append(fileNameEncoding).toString());
            }
            List<Map<Object, Object>> data = response.getRecordList();
            exportService.exportResultSet(data, fieldMap, bufferedOS);
            bufferedOS.flush();
            out.flush();
        } catch (IOException e) {
            throw new ExportException("export error", e);
        }
    }

}
