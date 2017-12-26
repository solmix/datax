package org.solmix.datax.wmix;

import org.solmix.datax.wmix.interceptor.UploadInterceptor;
import org.solmix.wmix.mapper.MapperService;

public class UploadEndpoint extends DataxEndpoint
{

    private static final long serialVersionUID = -1633004647563444571L;
    
    @Override
    protected void prepareInInterceptors(){
        UploadInterceptor in = new UploadInterceptor();
        MapperService mapperService=container.getExtension(MapperService.class);
        in.setMapperService(mapperService);
        getInInterceptors().add(in);
    }
    
}
