package org.solmix.datax.call.criteria;

import java.util.HashMap;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 110045  2013-3-28
 */
public class Parameters extends HashMap<String, Object>
{

    private static final long serialVersionUID = 1L;

   public Parameters(String key,Object value){
       put(key,value);
   }
   public Parameters add(String key,Object value){
       this.put(key,value);
       return this;
   }
}
