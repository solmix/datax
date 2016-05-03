package org.solmix.datax.builder;

import java.util.HashMap;
import java.util.Map;

public class StringMap extends HashMap<String, Object>
{

    private static final long serialVersionUID = 6441989943571225328L;

    public static Builder newBuilder(){
        return new Builder();
    }
    
    public static class Builder{
        private StringMap values = new StringMap();
        
        public Builder set(String key,Object value){
            values.put(key, value);
            return this;
        }
        
        public Map<String,Object> build(){
            return values;
        }
        public StringMap stringMap(){
            return values;
        }
    }
    
    public static StringMap stringMap(Object ...pairs ){
        StringMap values = new StringMap();
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i = i + 2) {
                values.put(pairs[i].toString(), pairs[i + 1]);
            }
        }
        return values;
    }

}
