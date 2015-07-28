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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.commons.util.ObjectUtils.NullObject;
import org.solmix.commons.util.Reflection;
import org.solmix.datax.DSRequest;
import org.solmix.datax.RequestContext;
import org.solmix.datax.annotation.Param;
import org.solmix.datax.annotation.ParamType;
import org.solmix.datax.model.LookupType;
import org.solmix.runtime.Container;
import org.solmix.runtime.bean.ConfiguredBeanProvider;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;
import org.solmix.runtime.resource.support.ResourceManagerImpl;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月26日
 */

public class InvokerObject
{
    private static final  Logger LOG = LoggerFactory.getLogger(InvokerObject.class);

    private final Class<?> clazz;

    private final LookupType lookup;

    private final Container container;

    Method method;

    Object serviceInstance;

    final String methodName;

    private final String serviceName;
    
    private final RequestContext requestContext;
    private final DSRequest request;

    public InvokerObject(Container container,DSRequest req, LookupType lookup, Class<?> clazz, String serviceName, String methodName)
    {
        this.container = container;
        this.request=req;
        this.requestContext=req.getRequestContext();
        this.lookup = lookup;
        this.clazz = clazz;
        this.serviceName = serviceName;
        this.methodName = methodName;
    }

    /**
     * @param methodName
     * @return
     */
    public Method getServiceMethod() {
        if (method == null) {
            method = findMethod(clazz, methodName);
        }
        return method;
    }

    public static Method findMethod(Class<?> serverObjectClass, String methodName) {
        Method methods[] = serverObjectClass.getMethods();
        List<Method> candidateMethods = new ArrayList<Method>(4);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (methodName.equals(method.getName()))
                candidateMethods.add(method);
        }

        if (candidateMethods.size() == 0) {
            throw new InvokerException(
                new StringBuilder()
                .append("Couldn't find a public method named: ")
                .append(methodName)
                .append(" on class: ")
                .append(serverObjectClass.getName())
                .toString());
        } else if (candidateMethods.size() > 1) {
            throw new InvokerException((new StringBuilder())
                .append("Class ")
                .append(serverObjectClass.getName())
                .append(" defines multiple")
                .append(" methods named: ")
                .append(methodName)
                .append(" - overloading is not supported - please disambiguate.")
                .toString());

        } else {
            return candidateMethods.get(0);
        }
    }

    public Object invoke() throws InvokerException{
        Method method = getServiceMethod();
        Object instance = getServiceInstance();
        List<Object> methodArgs = new ArrayList<Object>();
        //XXX ${template} 配置调用方法的参数
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        GenericParameterNode[] genericParamTypes = getGenericParameterTypes(method);
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            GenericParameterNode genericParamInfo = genericParamTypes[i];
            Annotation[] parameterannotation=parameterAnnotations[i];
            if (genericParamInfo != null && genericParamInfo.getClassByIndex(0) == paramType)
                genericParamInfo = genericParamInfo.getChildNode();
            boolean discoveried = false;
            try {
                methodArgs.add(lookupValue(paramType,parameterannotation, genericParamInfo));
                discoveried=true;
            } catch (Exception e) {
                if(LOG.isWarnEnabled()){
                    LOG.warn(new StringBuilder()
                    .append("Exception occurred attempting to map arguments: ")
                    .append(e.toString()).append(" in ")
                    .append(e.getStackTrace()[0].toString())
                    .toString());
                }
            }
            if(discoveried){
                continue;
            }
            String errorString = new StringBuilder()
                .append("Unable to assign a  argument to slot #")
                .append(i + 1)
                .append(" taking type: ")
                .append(paramType.getName())
                .append(" of method:")
                .append(method.toString())
                .append( "Can't lookup arguments match this type").toString();
            throw new InvokerException(errorString);
        }
        
        Object args[] = DataUtils.listToArray(methodArgs);
        if(LOG.isTraceEnabled()){
            LOG.trace((new StringBuilder())
                .append("method takes: ")
                .append(parameterTypes.length)
                .append(" args.  I've assembled: ")
                .append( methodArgs.size())
                .append(" args")
                .append(" invoking method:")
                .append(getFormattedMethodSignature(method))
                .append(" with arg types: ").append(
                getFormattedParamTypes(args))
                .toString());
        }
        
        try {
            return method.invoke(instance, args);
        }  catch (Exception e) {
            throw new InvokerException("Invoke Exception", e);
        }

    }
   
    protected Object lookupValue(Class<?> targetType,Annotation[] annotaions, GenericParameterNode node) {
        Param param= findParam(annotaions);
        Object founded = null;
        if(param!=null){
            if (isCollection(targetType)) {
                if(param.type()==ParamType.RESOURCE){
                    throw new InvokerException(new StringBuilder()
                    .append("Annotate @Param(type=ParamType.RESOURCE) can't parse Collection resource:")
                    .append(targetType.getClass()).toString());
                }
                try {
                    // XXX ${template} expression  配置调用方法的参数
                    Object newArgValue;
                    if (param.collectionClass() != NullObject.class) {
                        newArgValue = param.collectionClass().newInstance();
                    } else if (targetType.isInterface() || Modifier.isAbstract(targetType.getModifiers())) {
                        if (Map.class.isAssignableFrom(targetType)) {
                            newArgValue = new LinkedMap();
                        } else if (List.class.isAssignableFrom(targetType)) {
                            newArgValue = new ArrayList<Object>();
                        } else if (Set.class.isAssignableFrom(targetType)) {
                            newArgValue = new HashSet<Object>();
                        } else if (Queue.class.isAssignableFrom(targetType)) {
                            newArgValue = new LinkedList<Object>();
                        } else if (Collection.class.isAssignableFrom(targetType)) {
                            newArgValue = new ArrayList<Object>();
                        }
                    } else {
                        newArgValue = targetType.newInstance();
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            } else {
                ParamType type = param.type();
                switch (type) {
                    case DATA:
                        founded = lookupData(targetType, param,null);
                        break;
                    case RESOURCE:
                        founded = lookupResource(targetType, param);
                        break;
                    default:
                        break;
                }
            }
        }else{
             //不是集合类型
            if(!isCollection(targetType)){
                //现在请求域查找
                if(requestContext!=null){
                    founded = requestContext.get(targetType); 
                }
                 //container中查找
                if(founded==null){
                    founded = container.getExtension(targetType);
                }
            }else{
                throw new InvokerException(new StringBuilder()
                .append("Not annotate @Param, default @Param(type=ParamType.RESOURCE) can't parse Collection resource:")
                .append(targetType.getClass()).toString());
            }
        }
        return founded;
    }
    
    /**
     * @param targetType
     * @param param
     * @return
     */
    private Object lookupResource(Class<?> targetType, Param param) {
        Object founded=null;
        if(ObjectUtils.EMPTY_STRING.equals(param.name())){
            
            //现在请求域查找
            if(requestContext!=null){
                founded = requestContext.get(targetType); 
            }
             //container中查找
            if(founded==null){
                founded = container.getExtension(targetType);
            }
        }else{
            ConfiguredBeanProvider provider = container.getExtension(ConfiguredBeanProvider.class);
            if(provider!=null){
                founded= provider.getBeanOfType(param.name(), targetType);
            }
        }
        return founded;
    }

    /**
     * @param targetType
     * @param param
     * @return
     * @throws Exception 
     */
    private Object lookupData(Class<?> targetType, Param param,Object argValue) {
       if(argValue==null){
           argValue=request.getRawValues();
       }
       if(argValue==null){
           return null;
       }
       Class<?> argType = argValue.getClass();
        if (targetType.isAssignableFrom(argType)){
            return param.javaClass() != NullObject.class ? param.javaClass().cast(argValue) : argType.cast(argValue);
        }else if(Map.class.isAssignableFrom(argType)){
            Object beanInstance = null;
            try {
                beanInstance = Reflection.newInstance(targetType);
                DataUtils.setProperties(Map.class.cast(argType), beanInstance);
                return beanInstance;
            } catch (Exception e) {
                Throwable rte = Reflection.getRealTargetException(e);
                throw new InvokerException(new StringBuilder()
                .append("Failed to convert Map arg to bean of type: ")
                .append(targetType.getName())
                .append(" - because instantiation of ")
                .append(targetType.getName())
                .append(" threw the following Exception: ")
                .append(rte.toString())
                .toString(),rte);
            }
        }
        return null;
    }

    private boolean isCollection(Class<?> targetType) {
        if ((Collection.class.isAssignableFrom(targetType) || Map.class.isAssignableFrom(targetType))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param annotaions
     */
    private Param findParam(Annotation[] annotaions) {
        for(Annotation an:annotaions){
            if(an.annotationType()==Param.class){
                return Param.class.cast(an);
            }
        }
        return null;
    }

    private static GenericParameterNode[] getGenericParameterTypes(Method method) {
        if (method == null)
            return new GenericParameterNode[0];
        Type[] gParams = method.getGenericParameterTypes();
        GenericParameterNode[] nodes = new GenericParameterNode[gParams.length];
        for (int i = 0; i < gParams.length; i++) {
            GenericParameterNode gpn;
            if (gParams[i] instanceof ParameterizedType)
                gpn = buildGenericParameterTree((ParameterizedType) gParams[i]);
            else
                gpn = new GenericParameterNode((Class<?>) gParams[i]);
            nodes[i]=(gpn);
        }
        return nodes;
    }

    private static GenericParameterNode buildGenericParameterTree(ParameterizedType type) {
        GenericParameterNode gpn = new GenericParameterNode();
        Type paramTypes[] = type.getActualTypeArguments();
        if (paramTypes.length == 1 && (paramTypes[0] instanceof Class<?>)) {
            gpn.addClass((Class<?>) paramTypes[0]);
            return gpn;
        }
        gpn.addClass((Class<?>) type.getRawType());
        for (int j = 0; j < paramTypes.length; j++)
            if (paramTypes[j] instanceof Class) {
                if (gpn.getChildNode() == null)
                    gpn.setChildNode(new GenericParameterNode((Class<?>) paramTypes[j]));
                else
                    gpn.getChildNode().addClass((Class<?>) paramTypes[j]);
            } else {
                gpn.setChildNode(buildGenericParameterTree((ParameterizedType) paramTypes[j]));
            }

        return gpn;
    }
    /**
     * @return
     */
    public Object getServiceInstance() {
            switch (lookup) {
                case CONTAINER:
                
                    if(serviceName==null){
                      serviceInstance=  container.getExtension(clazz);
                    }else{
                        ConfiguredBeanProvider provider = container.getExtension(ConfiguredBeanProvider.class);
                        if(provider!=null){
                            serviceInstance= provider.getBeanOfType(serviceName, clazz);
                        }
                        if(serviceInstance==null){
                            serviceInstance=container.getExtensionLoader(clazz).getExtension(serviceName);
                        }
                    }
                    if(serviceInstance!=null){
                        ResourceManagerImpl rm = new ResourceManagerImpl(new RequestContextResourceResolver(requestContext));
                        ResourceInjector injector = new ResourceInjector(rm);
                        injector.inject(serviceInstance);
                    }
                    break;
                case NEW:
                    try {
                        serviceInstance = Reflection.newInstance(clazz);
                    } catch (Exception e) {
                       throw new InvokerException("Instance object",e);
                    }
                    if(serviceInstance!=null){
                        ResourceManager rma= container.getExtension(ResourceManager.class);
                        ResourceManagerImpl rm = new ResourceManagerImpl(rma.getResourceResolvers());
                        rm.addResourceResolver( new RequestContextResourceResolver(requestContext));
                        ResourceInjector injector = new ResourceInjector(rm);
                        injector.inject(serviceInstance);
                    }
                    break;
                default:
                    break;
        }
            
            
        return serviceInstance;
    }
    private static String getFormattedParamTypes(Object params[]) {
        String result = "";
        if (params == null || params.length == 0)
            return "(empty parameter list)";
        for (int ii = 0; ii < params.length; ii++) {
            result = (new StringBuilder()).append(result).append(params[ii] == null ? "null" : params[ii].getClass().getName()).toString();
            if (ii + 1 < params.length)
                result = (new StringBuilder()).append(result).append(", ").toString();
        }

        return result;
    }
    
    private static String getFormattedMethodSignature(Method method) {
        if (method == null)
            return null;
        String result = (new StringBuilder()).append(method.getReturnType().getName()).append(" ").append(method.getDeclaringClass().getName()).append(
            ".").append(method.getName()).append("(").toString();
        Class<?> paramTypes[] = method.getParameterTypes();
        for (int ii = 0; ii < paramTypes.length; ii++) {
            result = (new StringBuilder()).append(result).append(paramTypes[ii].getName()).toString();
            if (ii + 1 < paramTypes.length)
                result = (new StringBuilder()).append(result).append(", ").toString();
        }

        result = (new StringBuilder()).append(result).append(")").toString();
        Class<?> exceptionTypes[] = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            result = (new StringBuilder()).append(result).append(" throws ").toString();
            for (int ii = 0; ii < exceptionTypes.length; ii++) {
                result = (new StringBuilder()).append(result).append(exceptionTypes[ii].getName()).toString();
                if (ii + 1 < exceptionTypes.length)
                    result = (new StringBuilder()).append(result).append(", ").toString();
            }

        }
        return result;
    }
    
    public static class GenericParameterNode
    {

        public Class<?> getClassByIndex(int index) {
            if (index < classes.size())
                return classes.get(index);
            else
                return null;
        }

        public void addClass(Class<?> theClass) {
            classes.add(theClass);
        }

        public GenericParameterNode getChildNode() {
            return childNode;
        }

        public void setChildNode(GenericParameterNode childNode) {
            this.childNode = childNode;
        }

        private final List<Class<?>> classes;

        private GenericParameterNode childNode;

        public GenericParameterNode()
        {
            this(null);
        }

        public GenericParameterNode(Class<?> theClass)
        {
            classes = new ArrayList<Class<?>>();
            if (theClass != null)
                classes.add(theClass);
            childNode = null;
        }
    }
}
