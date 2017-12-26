package org.solmix.datax.wmix;

import org.solmix.datax.wmix.interceptor.ImageInInterceptor;
import org.solmix.datax.wmix.interceptor.ImageOutInterceptor;
import org.solmix.wmix.mapper.MapperService;


public class ImageEndpoint extends DataxEndpoint
{
    
    private static final long serialVersionUID = -6580931206250178376L;

    @Override
    protected void prepareOutInterceptors(){
        getOutInterceptors().add(new ImageOutInterceptor());
    }
    
    @Override
    protected void prepareInInterceptors(){
        ImageInInterceptor in = new ImageInInterceptor();
        MapperService mapperService=container.getExtension(MapperService.class);
        in.setMapperService(mapperService);
          getInInterceptors().add(in);
      }
}
