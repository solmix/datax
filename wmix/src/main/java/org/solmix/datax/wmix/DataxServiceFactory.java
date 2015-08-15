package org.solmix.datax.wmix;

import org.solmix.exchange.Service;
import org.solmix.exchange.event.ServiceFactoryEvent;
import org.solmix.exchange.interceptor.support.ServiceInvokerInterceptor;
import org.solmix.exchange.invoker.Invoker;
import org.solmix.exchange.support.AbstractServiceFactory;

public class DataxServiceFactory extends AbstractServiceFactory {

    private Invoker invoker;
    private String address;
    public DataxServiceFactory(String address){
        this.address=address;
    }
	@Override
	public Service create() {
		pulishEvent(ServiceFactoryEvent.START_CREATE);
		initService();
		setupInterceptor();
		if(this.invoker!=null){
		    service.setInvoker(getInvoker());
		}else{
		    service.setInvoker(createInvoker());
		}
		if(getDataFormat()!=null){
		    service.setDataFormat(getDataFormat());
		}
		Service service =getService();
		pulishEvent(ServiceFactoryEvent.END_CREATE,service);
		return service;
	}
	
	protected void initService(){
	    DataxService service = new DataxService(null,address);
	    setService(service);
	}
	
	protected void setupInterceptor(){
	    service.getInInterceptors().add(new ServiceInvokerInterceptor());
	}

    public Invoker getInvoker() {
        return invoker;
    }

    public Invoker createInvoker() {
        return new DataxInvoker();
    }

}
