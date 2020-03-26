/*
 * Copyright 2015 The Solmix Project
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.pager.PageControl;
import org.solmix.commons.pager.PageList;
import org.solmix.commons.util.ArrayUtils;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataxSession;
import org.solmix.datax.annotation.Argument;
import org.solmix.datax.annotation.Operation;
import org.solmix.runtime.helper.ProxyHelper;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月23日
 */

public class DataServiceProxy<T> implements InvocationHandler
{
    private Class<T> interfaceClass;
    private ClassLoader classLoader;
    private DataxSession session;
    private final Map<Method, String> methodToOp = new HashMap<Method, String>(16, 0.75f);
    
    DataServiceProxy(Class<T> clz,DataxSession session){
        interfaceClass=clz;
        this.session=session;
        classLoader=clz.getClassLoader();
        prepareDataService();
    }
   
    private void prepareDataService() {
        org.solmix.datax.annotation.DataService ds = interfaceClass.getAnnotation(org.solmix.datax.annotation.DataService.class);
        String id =StringUtils.trimToNull(ds.value());
        if(id==null){
            id=interfaceClass.getName();
        }
       Method[] methods= interfaceClass.getMethods();
       for(Method method:methods){
           Operation operation=  method.getAnnotation(Operation.class);
           String operationId;
           if(operation==null){
               operationId=new StringBuilder().append(id).append(".").append(method.getName()).toString();
           }else{
               String configedid=operation.value();
               configedid= StringUtils.trimToNull(configedid);
               if(configedid==null){
                   operationId=new StringBuilder().append(id).append(".").append(method.getName()).toString();
               }else if(configedid.startsWith("#")){
                   operationId=new StringBuilder().append(id).append(".").append(configedid.substring(1)).toString();
               }else{
                   operationId=configedid;
               }
           }
           if(methodToOp.containsValue(operationId)){
               throw new IllegalArgumentException("Double method with operation id:"+operationId);
           }
           methodToOp.put(method, operationId);
       }
        
    }

    @SuppressWarnings("unchecked")
    public T newInstance(){
        return (T) ProxyHelper.getProxy(classLoader, new Class[]{interfaceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Reflection.isEqualsMethod(method)) {
            return equals(args[0]);
        }
        if (Reflection.isHashCodeMethod(method)) {
            return hashCode();
        }
        
       String operationId = methodToOp.get(method);
       DSRequest request=session.getDataServiceManager().createDSRequest();
       request.setOperationId(operationId);
       Class<?>[] types= method.getParameterTypes();
       if(!ArrayUtils.isEmptyArray(args)){
    	   int pindex=-1;
    	   for(int j=0;j<types.length;j++){
    		   if(PageControl.class.isAssignableFrom(types[j]) ){
    			   pindex=j;
    			   break;
    		   }
    	   }
    	   Operation operation=  method.getAnnotation(Operation.class);
    	   Class<?> argType=null;
    	   if(operation!=null){
    		   argType=operation.argType();
    	   }
    	   if(pindex==-1){
	           if(args.length==1){
	        	 //method只有一个输入参数
	        	   request.setRawValues(args[0]);
	           }else{
	        	 //多个输入参数转化为一个
	        	   parseMultiArgs(method,args,pindex,argType,request);
	              
	           }
	       }else{
	    	   if(args.length==1){
	    		   request.addAttachment(PageControl.class, (PageControl)args[0]);
	    	   }else if(args.length==2){
	    		   for(int i=0;i<2;i++){
	    			   if(i==pindex){
	    				   request.addAttachment(PageControl.class, (PageControl)args[i]);
	    			   }else{
	    				   request.setRawValues(args[i]);
	    			   }
	    		   }
	    	   }else{
	    		   parseMultiArgs(method,args,pindex,argType,request);
	    	   }
	       }
       }
      Class<?> returnType= method.getReturnType();
      DSResponse response =session.execute(request);
      if(returnType == Void.TYPE){
          return null;
      }else if(returnType.isArray()){
           Class<?> realy=returnType.getComponentType();
          List<?> list= response.getResultList(realy);
          Object array= Array.newInstance(realy, list.size());
          for(int i=0;i<list.size();i++){
              Array.set(array, i, list.get(i));
          }
          return array;
       }else if(PageList.class.isAssignableFrom(returnType)){
           List<?> list= response.getSingleResult(List.class);
           int total=0;
           PageControl pc = response.getAttachment(PageControl.class);
           if(pc!=null){
        	   total=pc.getTotalSize();
           }else if(list!=null){
        	   total=list.size();
           }
           @SuppressWarnings({ "rawtypes", "unchecked" })
           PageList page  = new PageList(list,total);
           return page;
       }else{
		   Object result = response.getSingleResult(returnType);
		   if(returnType.isPrimitive()){
			if(returnType==Boolean.TYPE){
				return false;
			}else if(returnType == Integer.TYPE){
				return 0;
			}else if(returnType == Long.TYPE){
				return 0l;
			}else if(returnType == Float.TYPE){
				return 0f;
			}else if(returnType == Double.TYPE){
				return 0d;
			}else if(returnType == Byte.TYPE){
				return 0;
			}else{
				throw new IllegalArgumentException("Not support type"+returnType);
			}
		   }else{
			   return result;
		   }
		   
       }
    }

	private void parseMultiArgs(Method method, 
								Object[] args, 
								int pindex,
								Class<?> argType, 
								DSRequest request) throws Exception {
		Map<String, Object> merged = new LinkedHashMap<String, Object>();
		Annotation[][] annos = method.getParameterAnnotations();
		if (pindex == -1) {
			for (int i = 0; i < annos.length; i++) {
				Annotation[] parameterAnnons = annos[i];
				Argument arg = findArgument(parameterAnnons);
				if (arg == null) {
					throw new IllegalArgumentException(
							"Method have multi argument should annotate @Argument");
				} else {
					merged.put(arg.value(), args[i]);
				}
			}
		} else {
			for (int i = 0; i < annos.length; i++) {
				if (i == pindex) {
					request.addAttachment(PageControl.class,
							(PageControl) args[i]);
				} else {
					Annotation[] parameterAnnons = annos[i];
					Argument arg = findArgument(parameterAnnons);
					if (arg == null) {
						throw new IllegalArgumentException(
								"Method have multi argument should annotate @Argument");
					} else {
						merged.put(arg.value(), args[i]);
					}
				}

			}
		}
		if (argType == null || argType == void.class) {
			request.setRawValues(merged);
		} else {
			Object instance = Reflection.newInstance(argType);
			DataUtils.setProperties(merged, instance);
			request.setRawValues(instance);
		}
	}
    
    private Argument findArgument(Annotation[] annos){
    	 for(int i=0;i<annos.length;i++){
    		 Annotation  an=annos[i];
    		 if(an instanceof Argument){
    			 return (Argument)an;
    		 }
    	 }
    	 return null;
    }

}
