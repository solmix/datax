package org.solmix.datax.wmix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.solmix.commons.util.PackageUtils;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Service;
import org.solmix.exchange.dataformat.DataFormat;
import org.solmix.exchange.interceptor.support.InterceptorProviderAttrSupport;
import org.solmix.exchange.invoker.Invoker;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.ServiceInfo;


public class DataxService extends InterceptorProviderAttrSupport implements Service
{

    private static final long serialVersionUID = -6315846318409396395L;
    private Map<NamedID, Endpoint> endpoints = new HashMap<NamedID, Endpoint>();
    private Invoker invoker;
    private DataFormat dataFormat;
    private NamedID serviceName;
    private String address;
    public DataxService(){
        
    }
    public DataxService(NamedID serviceName,String address){
        this.serviceName=serviceName;
        this.address=address;
    }
    @Override
    public Invoker getInvoker() {
        return invoker;
    }

    @Override
    public NamedID getServiceName() {
        if(this.serviceName!=null){
            return serviceName;
        }
        if(address==null){
            String ns = PackageUtils.getNamespace(PackageUtils.getPackageName(this.getClass()));
            return new NamedID(ns, "service");
        }else{
            return new NamedID(address, "service");
        }
    }

    @Override
    public void setInvoker(Invoker invoker) {
        this.invoker=invoker;
    }

    @Override
    public Map<NamedID, Endpoint> getEndpoints() {
        
        return endpoints;
    }

    @Override
    public EndpointInfo getEndpointInfo(NamedID eid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataFormat getDataFormat() {
        return dataFormat;
    }

    @Override
    public void setDataFormat(DataFormat df) {
        this.dataFormat=df;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public List<ServiceInfo> getServiceInfos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExecutor(Executor executor) {
        //NOT SET
    }

    @Override
    public ServiceInfo getServiceInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
