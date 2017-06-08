package org.solmix.datax.call;

import java.util.Map;
import java.util.Set;

import org.solmix.datax.DATAX;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.RequestContext;
import org.solmix.runtime.Container;
import org.solmix.service.template.support.MappedTemplateContext;
import org.solmix.service.toolkit.ToolkitService;


public class DSCallTemplateContext extends MappedTemplateContext
{

    public DSCallTemplateContext(ToolkitService toolkit, DSRequest request)
    {
        this(toolkit,request,null);
    }
    public DSCallTemplateContext(ToolkitService toolkit, DSRequest request, DSResponse response)
    {
        initToolkit(toolkit);
        initRequestContext(request.getRequestContext());
        initResponse(request, response);
    }

    protected void initResponse(DSRequest request, DSResponse response) {
        put(DATAX.VM_DSREQUEST, request);
        
        put(DATAX.VM_DS, request.getDataService());
        put(DATAX.VM_VALUES, request.getRawValues());
        if(response!=null){
            put(DATAX.VM_DSRESPONSE, response);
            put(DATAX.VM_DATA, response.getRawData());
        }
        
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
