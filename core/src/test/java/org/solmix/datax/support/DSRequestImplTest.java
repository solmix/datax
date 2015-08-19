/*
 * Copyright 2014 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.datax.support;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DataServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月2日
 */

public class DSRequestImplTest
{
    Container c;

    @Before
    public void setup() {
        c = ContainerFactory.getDefaultContainer(true);
        Assert.assertNotNull(c);
    }
    @Test
    public void test(){
        Map<String,String> a = new HashMap<String, String>();
        a.put("111", "222");
        RBean old= new RBean();
        old.setText("1234");
        DataServiceManager dsm=   c.getExtension(DataServiceManager.class);
        DSRequestImpl req=(DSRequestImpl)dsm.createDSRequest();
        req.setOperationId("com.call.ds.fetch");
        req.setValidated(true);
        req.setRawValues(a);
        DSRequestImpl nreq=req.clone();
        nreq.setValidated(false);
        Map<String,String> mm=(Map<String,String>)nreq.getRawValues();
        mm.put("111", "333");
        nreq.setRawValues(mm);
        assertEquals(req.getOperationId(), nreq.getOperationId());
        try {
            Map<String, Object> map= DataUtils.getProperties(req, false);
            Map<String, Object> nmap= DataUtils.getProperties(nreq, false);
            System.out.println(map);
            System.out.println(nmap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
    }
    
    public static class RBean implements Cloneable{
        private String text;

        
        public String getText() {
            return text;
        }

        
        public void setText(String text) {
            this.text = text;
        }
     /*   
        @Override
        public String toString(){
            return text;
        }*/
        @Override  
        public Object clone() {  
            RBean addr = null;  
            try{  
                addr = (RBean)super.clone();  
            }catch(CloneNotSupportedException e) {  
                e.printStackTrace();  
            }  
            return addr;  
        }  
    }

}
