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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.ArrayUtils;
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
        String id =StringUtils.trimToNull(ds.id());
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
               String configedid=operation.id();
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
       if(!ArrayUtils.isEmptyArray(args)){
           //method只有一个输入参数
           if(args.length==1){
               request.setRawValues(args[0]);
           }
           //多个输入参数转化为一个
           else{
               Map<String,Object> merged= new LinkedHashMap<String, Object>();
              Class<?>[] parameterTypes= method.getParameterTypes();
              for(int i=0;i<parameterTypes.length;i++){
                  Class<?> parameterType=parameterTypes[i];
                  if(!parameterType.isAnnotationPresent(Argument.class)){
                      throw new IllegalArgumentException("Method have multi argument should annotate @Argument");
                  }else{
                      Argument arg=  parameterType.getAnnotation(Argument.class);
                      merged.put(arg.key(), args[i]);
                  }
              }
              request.setRawValues(merged);
           }
       }
      Class<?> returnType= method.getReturnType();
      DSResponse response =session.execute(request);
       if(returnType.isArray()){
           Class<?> realy=returnType.getComponentType();
          List<?> list= response.getResultList(realy);
          Object array= Array.newInstance(realy, list.size());
          for(int i=0;i<list.size();i++){
              Array.set(array, i, list.get(i));
          }
          return array;
       }/*else if(List.class.isAssignableFrom(returnType)){
            boolean b=returnType.getTypeParameters()[0] instanceof ParameterizedType;
           List<?> list= response.getResultList(returnType);
           return list;
       }*/else{
           return response.getSingleResult(returnType);
       }
    }

}
