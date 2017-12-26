package org.solmix.datax.wmix.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.solmix.datax.DSResponse;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.wmix.exchange.WmixMessage;


public class ImageOutInterceptor extends PhaseInterceptorSupport<Message>
{

    public ImageOutInterceptor()
    {
        super(Phase.MARSHAL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        DSResponse response = (DSResponse) message.getContent(Object.class);
        
        InputStream input = (InputStream)response.getRawData();
        final HttpServletResponse httpResponse = (HttpServletResponse) message.get(WmixMessage.HTTP_RESPONSE);
        
        try {
            OutputStream out=  httpResponse.getOutputStream();
            IOUtils.copy(input, out);
        } catch (IOException e) {
            throw new Fault(e);
        }
    }

}
