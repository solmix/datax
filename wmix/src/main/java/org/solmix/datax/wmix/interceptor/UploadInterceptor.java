package org.solmix.datax.wmix.interceptor;

import javax.servlet.http.HttpServletRequest;

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

public class UploadInterceptor extends PhaseInterceptorSupport<WmixMessage>
{
    public UploadInterceptor()
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
        if (action.indexOf(".up") != -1) {
            action = action.substring(0, action.indexOf(".up"));
        }
        dsr.setOperationId(action);
        RequestContext requestContext = wrappedRequestcontext(exchange);
        dsr.setRequestContext(requestContext);
        message.setContent(Object.class, dsr);
    }
    
    protected   RequestContext wrappedRequestcontext(Exchange exchange){
        return new ExchangeRequestContext(exchange);
    }
}
