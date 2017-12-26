package org.solmix.datax.wmix;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.wmix.interceptor.AntDInInterceptor;
import org.solmix.datax.wmix.interceptor.AntDOutInterceptor;
import org.solmix.datax.wmix.interceptor.OutFaultInterceptor;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Service;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.interceptor.support.MessageSenderInterceptor;
import org.solmix.exchange.model.ArgumentInfo;
import org.solmix.exchange.processor.InFaultChainProcessor;
import org.solmix.exchange.processor.OutFaultChainProcessor;
import org.solmix.runtime.Container;
import org.solmix.wmix.exchange.AbstractWmixEndpoint;
import org.solmix.wmix.exchange.WmixMessage;
import org.solmix.wmix.mapper.MapperService;

public class AntDEndpoint extends AbstractWmixEndpoint implements Endpoint{

	private static final long serialVersionUID = 7096409697024891764L;

	private static final Logger LOG = LoggerFactory.getLogger(AntDEndpoint.class);


    private DataxServiceFactory serviceFactory;
    
    private ArgumentInfo argumentInfo;
    
    private DataServiceManager dataServiceManager;
    
    public AntDEndpoint(){
        serviceFactory= new DataxServiceFactory();
    }
    @Override
    protected void prepareInterceptors() {
        setInFaultProcessor(new InFaultChainProcessor(container, getPhasePolicy()));
        setOutFaultProcessor(new OutFaultChainProcessor(container,  getPhasePolicy()));
        getOutInterceptors().add(new MessageSenderInterceptor());
        getOutFaultInterceptors().add(new MessageSenderInterceptor());
        prepareInInterceptors();
        prepareOutInterceptors();
        prepareOutFaultInterceptors();
    }
    
    protected void prepareOutFaultInterceptors(){
        getOutFaultInterceptors().add(new OutFaultInterceptor());
    }
    protected void prepareOutInterceptors(){
        getOutInterceptors().add(new AntDOutInterceptor());
    }
    
    protected void prepareInInterceptors(){
    	AntDInInterceptor in = new AntDInInterceptor();
    	MapperService mapperService=container.getExtension(MapperService.class);
    	in.setMapperService(mapperService);
        getInInterceptors().add(in);
    }
    
    @Override
    protected void setContainer(Container container) {
        super.setContainer(container);
        argumentInfo = new ArgumentInfo();
        argumentInfo.setTypeClass(Map.class);
        dataServiceManager=container.getExtension(DataServiceManager.class);
        Assert.assertNotNull(dataServiceManager,"NO found DataServiceManager");
        DataProcessor dataProcessor = container.getExtension(DataProcessor.class);
        serviceFactory.setDataProcessor(dataProcessor);
    }

    @Override
    public void service(WmixMessage message) throws Exception {
        message.put(ArgumentInfo.class, argumentInfo);
        message.getExchange().put(DataServiceManager.class, dataServiceManager);
        message.getExchange().put(Transporter.class, getTransporter());
        getTransporter().invoke(message);
    }

   
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Service createService() {
        
        return serviceFactory.create();
    }
}
