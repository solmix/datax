package org.solmix.datax.builder;

import java.util.HashMap;
import java.util.Map;

public class StringMap
{
    
    public static Builder newBuilder(){
        return new Builder();
    }
    
    public static class Builder{
        private Map<String,Object> values = new HashMap<String,Object>();
        
        public Builder set(String key,Object value){
            values.put(key, value);
            return this;
        }
        
        public Map<String,Object> build(){
            return values;
        }
    }

}
