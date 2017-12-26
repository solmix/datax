package org.solmix.datax.wmix.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.RequestContext;
import org.solmix.datax.wmix.context.ExchangeRequestContext;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmix.wmix.mapper.MapperService;


public class ImageInInterceptor extends PhaseInterceptorSupport<WmixMessage>
{
    private MapperService mapperService;
    public ImageInInterceptor()
    {
        super(Phase.UNMARSHAL);
    }

    @Override
    public void handleMessage(WmixMessage message) throws Fault {
        final Exchange exchange = message.getExchange();
        
        final DataServiceManager manager = exchange.get(DataServiceManager.class);
        DSRequest dsr = manager.createDSRequest();
     
        
        String reqPath = (String)message.get(Message.REQUEST_URI);
        if (DataUtils.isNullOrEmpty(reqPath)) {
            HttpServletRequest request=(HttpServletRequest)  message.get(WmixMessage.HTTP_REQUEST);
            if(request!=null){
                reqPath = request.getRequestURI();
            }
        }
        
        String action = reqPath.substring(reqPath.lastIndexOf("/") + 1);
        if (action.lastIndexOf(".") != -1) {
            action = action.substring(0, action.lastIndexOf(".") );
        }
        Assert.assertNotNull(action, "operation action must be not null");
        if(mapperService!=null){
            action = mapperService.map("action",action);
        }
        dsr.setOperationId(action);
        RequestContext requestContext = wrappedRequestcontext(exchange);
        dsr.setRequestContext(requestContext);
        message.setContent(Object.class, dsr);
    }
    
    protected   RequestContext wrappedRequestcontext(Exchange exchange){
        return new ExchangeRequestContext(exchange);
    }

       

    
    public MapperService getMapperService() {
        return mapperService;
    }

    
    public void setMapperService(MapperService mapperService) {
        this.mapperService = mapperService;
    }

}
