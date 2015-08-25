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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.NotThreadSafe;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.commons.util.TransformUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;
import org.solmix.datax.annotation.Param;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.model.InvokerInfo;
import org.solmix.datax.model.LookupType;
import org.solmix.datax.model.MethodArgInfo;
import org.solmix.datax.script.VelocityExpression;
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
@NotThreadSafe
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
    private final InvokerInfo invokerInfo;
    
    private VelocityExpression velocity;

    private Map<String, Object> vmContext;

    public InvokerObject(Container container,DSRequest req, InvokerInfo invokerInfo, Class<?> clazz, String serviceName, String methodName)
    {
        this.container = container;
        this.request=req;
        this.requestContext=req.getRequestContext();
        this.invokerInfo=invokerInfo;
        this.lookup = invokerInfo.getLookup();
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
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        GenericParameterNode[] genericParamTypes = getGenericParameterTypes(method);
        Map<Integer,MethodArgInfo> argsInfo=  invokerInfo.getMethodArgs();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            MethodArgInfo argInfo = (argsInfo == null ? null : argsInfo.get(i));
            GenericParameterNode genericParamInfo = genericParamTypes[i];
            Annotation[] parameterannotation=parameterAnnotations[i];
            if (genericParamInfo != null && genericParamInfo.getClassByIndex(0) == paramType)
                genericParamInfo = genericParamInfo.getChildNode();
            try {
                methodArgs.add(lookupValue(paramType,argInfo,parameterannotation, genericParamInfo));
            } catch (Exception e) {
                String errorString = new StringBuilder()
                .append("Unable to assign a  argument to slot #")
                .append(i + 1)
                .append(" taking type: ")
                .append(paramType.getName())
                .append(" of method:")
                .append(method.toString())
                .append(" Can't lookup arguments match this type").toString();
                throw new InvokerException(errorString,e);
            }
            
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
    
    protected Object lookupValue(Class<?> targetType,MethodArgInfo invokerInfo,Annotation[] annotaions, GenericParameterNode node) {
        Param param= findParam(annotaions);
        Object founded = null;
        Object argument=null;
        String expression = invokerInfo == null ? null : invokerInfo.getExpression();
        if(expression==null){
            expression=param==null?null:param.expression();
        }
        if(expression!=null){
            if(velocity==null){
                velocity=new VelocityExpression(container);
                vmContext=velocity.prepareContext(request, requestContext);
            }
            argument=velocity.evaluateValue(expression, vmContext);
        }
        if (invokerInfo != null && invokerInfo.getValue() != null) {
            argument = invokerInfo.getValue();
        }
        //根据表达式和value已经指定了值
        if(argument!=null){
         return adaptValue(targetType, argument, node, 
             param==null?null:param.javaClass(), 
                 param==null?null:param.collectionClass(), 
                     param==null?null:param.javaKeyClass());
        }else{
            //根据需要的类型自动查找
            if (isCollection(targetType)) {
                throw new InvokerException(new StringBuilder()
                .append("Not specify data ,can't parse Collection resource:")
                .append(targetType.getClass()).toString());
            }else{
                //简单对象
                founded = lookupResource(targetType, param);
            }
        }
        return founded;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object adaptValue(Class<?> targetType, Object argument, GenericParameterNode node, Class<?> javaClass, Class<?> javaCollectionClass,
        Class<?> javaKeyClass) {

        if (isCollection(targetType)) {
            try {
                Object newArgValue;
                if (javaCollectionClass != void.class) {
                    newArgValue = javaCollectionClass.newInstance();
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
                    } else
                        newArgValue = argument.getClass().newInstance();
                } else {
                    newArgValue = targetType.newInstance();
                }
                Iterator<Object> i = null;
                Object key = null;
                Class<?> keyClass = Object.class;
                Class<?> valueClass = null;
                Class<?> argType = argument.getClass();
                if (Collection.class.isAssignableFrom(targetType)) {
                    if (argument instanceof Map) {
                        Map map = (Map) argument;
                        if (map.size() == 1) {
                            Iterator mapi = map.entrySet().iterator();
                            java.util.Map.Entry mape = (java.util.Map.Entry) mapi.next();
                            if (mape.getValue() instanceof Collection) {
                                argument = mape.getValue();
                                argType = argument.getClass();
                            } else if (mape.getValue() instanceof Map) {
                                argument = new ArrayList();
                                ((List) argument).add(mape.getValue());
                                argType = argument.getClass();
                            }
                        }
                    }
                    i = ((Collection) argument).iterator();
                    if (node != null)
                        valueClass = node.getClassByIndex(0);
                } else if (Collection.class.isAssignableFrom(argType) && Map.class.isAssignableFrom(targetType)) {
                    if (((Collection) argument).size() == 1) {
                        argument = ((Collection) argument).iterator().next();
                        argType = argument.getClass();
                        i = ((Map) argument).keySet().iterator();
                    }
                } else {
                    i = ((Map) argument).keySet().iterator();
                    if (node != null) {
                        keyClass = node.getClassByIndex(0);
                        valueClass = node.getClassByIndex(1);
                    }
                }
                if (javaClass != null)
                    valueClass = javaClass;
                if (javaKeyClass != null)
                    keyClass = javaKeyClass;
                boolean resetValueClass = false;
                if (valueClass == null)
                    resetValueClass = true;
                while (i.hasNext()) {
                    Object value = null;
                    if (resetValueClass)
                        valueClass = null;
                    if (Collection.class.isAssignableFrom(targetType)) {
                        value = i.next();
                    } else {
                        key = i.next();
                        value = ((Map) argument).get(key);
                    }
                    if (valueClass == null && value != null)
                        valueClass = value.getClass();
                    Object newValue = null;
                    if (value != null) {
                        newValue = adaptValue(valueClass, value, node != null ? node.getChildNode() : null, javaClass, javaCollectionClass,
                            javaKeyClass);
                    }
                    Object newKey = key;
                    if (keyClass != Object.class) {
                        newKey = adaptValue(keyClass, value, node != null ? node.getChildNode() : null, null, null, null);
                    }
                    if (Collection.class.isAssignableFrom(targetType))
                        ((Collection) newArgValue).add(newValue);
                    else
                        ((Map) argument).put(newKey, newValue);
                }
                return newArgValue;
            } catch (Exception e) {
                throw new InvokerException("transform argumnt type:" + targetType.getName() + " to value:" + argument.getClass().getName(), e);
            }

        } else {
            if (targetType.isAssignableFrom(argument.getClass())) {
                return targetType.cast(argument);
            } else {
                try {
                    return TransformUtils.transformType(targetType, argument);
                } catch (Exception e) {
                    throw new InvokerException("transform argument value", e);
                }
            }
        }
    }
   
    
    
  
    private Object lookupResource(Class<?> targetType, Param param) {
        Object founded=null;
        if (param == null || ObjectUtils.EMPTY_STRING.equals(param.name())) {

            if(DSCall.class.isAssignableFrom(targetType)){
                return request.getDSCall();
            } else if(DSRequest.class.isAssignableFrom(targetType)){
                return request;
            } else if(DataService.class.isAssignableFrom(targetType)){
                return request.getDataService();
            } else if(Logger.class.isAssignableFrom(targetType)){
                return LOG;
            } else if(DSCall.class.isAssignableFrom(targetType)){
                return request.getDSCall();
            }else if(RequestContext.class.isAssignableFrom(targetType)){
                return requestContext;
            }else if(Container.class.isAssignableFrom(targetType)){
                return container;
            }
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
                        ResourceManagerImpl rm = new ResourceManagerImpl(new DSRequestResolver(request),new RequestContextResourceResolver(requestContext));
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
                        rm.addResourceResolver( new DSRequestResolver(request));
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
