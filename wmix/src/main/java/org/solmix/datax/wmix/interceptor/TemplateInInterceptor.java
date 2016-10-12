
package org.solmix.datax.wmix.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.pager.PageControl;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.ServletUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.RequestContext;
import org.solmix.datax.attachment.OldValues;
import org.solmix.datax.attachment.OldValuesBean;
import org.solmix.datax.export.ExportConfig;
import org.solmix.datax.wmix.Constants;
import org.solmix.datax.wmix.type.DSProtocol;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.runtime.Container;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmix.wmix.mapper.MapperService;
import org.solmix.wmix.mapper.support.SimpleMapper;
import org.solmix.wmix.parser.ParameterParser;

public class TemplateInInterceptor extends AbstractInInterceptor
{

    private String mapperType = SimpleMapper.TYPE;

    private MapperService mapperService;

    public TemplateInInterceptor(Container container)
    {
        super(Phase.UNMARSHAL);
        Assert.assertNotNull(container);
        mapperService=container.getExtension(MapperService.class);
    }

    public TemplateInInterceptor(String phase, String type)
    {
        super(phase);
        this.mapperType = type;
    }

    @Override
    public void handleMessage(WmixMessage message) throws Fault {
        HttpServletRequest request = (HttpServletRequest) message.get(WmixMessage.HTTP_REQUEST);
        Object protocol = request.getParameter(Constants.PROTOCOL);
        DSProtocol pro = null;

        if (protocol != null) {
            pro = DSProtocol.fromValue(protocol.toString());
        }
        if (pro == null || DSProtocol.GETPARAMS == pro || DSProtocol.POSTPARAMS == pro) {
            handleParameters(message, request);
        } else if (DSProtocol.POSTXML == pro || DSProtocol.POSTMESSAGE == pro) {
            handlePostMessage(message);
        }
    }

    protected void handleParameters(WmixMessage message, HttpServletRequest request) {
        final Exchange exchange = message.getExchange();
        final DataServiceManager manager = exchange.get(DataServiceManager.class);
        ParameterParser parameterParser = exchange.get(ParameterParser.class);
        DSRequest dsr = manager.createDSRequest();
        String url = ServletUtils.getResourcePath(request);
        if(mapperService!=null){
           String action = mapperService.map(mapperType, url);
           if(action!=null){
               dsr.setOperationId(action);
           }
        }
        Map<String, Object> values = new HashMap<String, Object>();

        for (String key : parameterParser.keySet()) {
            if ("_appID".equals(key)) {
                dsr.setApplicationId(parameterParser.getString(key));
            } else if ("_startRow".equals(key)) {
            	PageControl page = PageControl.fromRows(parameterParser.getInt("_startRow"), parameterParser.getInt("_endRow"));
                dsr.addAttachment(PageControl.class, page);
            } else if ("_exportResults".equals(key)) {
                if (parameterParser.getBoolean("_exportResults")) {
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
            } else if ("_action".equals(key)) {
                dsr.setOperationId(parameterParser.getString("_action"));
            } else if (key.startsWith("_")) {
                dsr.setAttribute(key, parameterParser.getString(key));
            } else {
                values.put(key, parameterParser.getString(key));
            }
        }
        dsr.setRawValues(values);
        RequestContext requestContext = wrappedRequestcontext(exchange);
        dsr.setRequestContext(requestContext);
        message.setContent(Object.class, dsr);
    }

    @Override
    protected void postToSchema(DataTypeMap data, DataServiceManager manager, WmixMessage message, Exchange exchange,
        ParameterParser parameterParser) {
        List<?> operations = data.getList("operations");
        RequestContext requestContext = wrappedRequestcontext(exchange);
        if (operations != null) {
            if (operations.size() == 1) {
                DSRequest request = manager.createDSRequest();
                prepareRequest(request, new DataTypeMap((Map) operations.get(0)), true);
                request.setRequestContext(requestContext);
                for (String key : parameterParser.keySet()) {
                    if (!Constants.PAYLOAD_NAME.equals(key) && !Constants.SECOND_PAYLOAD_NAME.equals(key)) {
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
        	PageControl page = PageControl.fromRows(Integer.valueOf(start.toString()), Integer.valueOf(end.toString()));
            request.addAttachment(PageControl.class, page);
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
}
