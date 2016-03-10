package org.solmix.datax.wmix;

import org.solmix.datax.wmix.interceptor.UploadInterceptor;

public class UploadEndpoint extends DataxEndpoint
{

    private static final long serialVersionUID = -1633004647563444571L;
    
    @Override
    protected void prepareInInterceptors(){
        getInInterceptors().add(new UploadInterceptor());
    }
    
}
