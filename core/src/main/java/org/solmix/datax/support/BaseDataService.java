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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.commons.util.TransformUtils;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceNoFoundException;
import org.solmix.datax.DataxException;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallFactory;
import org.solmix.datax.call.DSCallUtils;
import org.solmix.datax.model.BatchOperations;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.LookupType;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.model.ParamInfo;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.datax.model.TransformerInfo;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.transformer.TemplateTransformer;
import org.solmix.datax.transformer.Transformer;
import org.solmix.datax.transformer.TransformerException;
import org.solmix.datax.util.DataTools;
import org.solmix.datax.validation.BuiltinCreator;
import org.solmix.datax.validation.DefaultValidatorService;
import org.solmix.datax.validation.ErrorMessage;
import org.solmix.datax.validation.ValidationContext;
import org.solmix.datax.validation.ValidationCreator;
import org.solmix.datax.validation.ValidationEvent;
import org.solmix.datax.validation.ValidationEvent.Level;
import org.solmix.datax.validation.ValidationEventFactory;
import org.solmix.datax.validation.ValidationException;
import org.solmix.runtime.Container;
import org.solmix.runtime.bean.ConfiguredBeanProvider;
import org.solmix.runtime.event.EventService;
import org.solmix.runtime.event.TimeMonitorEvent;
import org.solmix.runtime.i18n.ResourceBundleManager;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;
import org.solmix.runtime.resource.support.ResourceManagerImpl;
import org.w3c.dom.Element;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月3日
 */

public class BaseDataService implements DataService
{
    private static final Logger LOG = LoggerFactory.getLogger(BaseDataService.class);
    private static final Logger VALIDATION =LoggerFactory.getLogger(DATAX.VALIDATION_LOGNAME);
    private Container container;
    private DataTypeMap properties;
    private DataServiceInfo info;
    
    private DSCallFactory dsCallFactory;
    
    private  EventService eventService;
    
    public BaseDataService(DataServiceInfo info,Container container,DataTypeMap prop)
    {
        Assert.assertNotNull(info);
        setContainer(container);
        this.info=info;
        this.properties=prop;
        init();
    }
    
    protected void setContainer(Container container) {
        Assert.assertNotNull(container);
        this.container=container;
    }
    
    protected void init(){
        if(LOG.isTraceEnabled()){
            LOG.trace((new StringBuilder()).append("Creating instance of DataSource '").append(info.getId()).append("'").toString());
        }
    }
    
    /**
     * 释放执行请求时缓存
     */
    @Override
    public void freeResources() {

    }
    
    @Override
    public String getId() {
        return info.getId();
    }

    @Override
    public String getServerType() {
        return BaseDataServiceFactory.BASE;
    }

    @Override
    public DSResponse execute(DSRequest req) throws DSCallException {
        if (req == null) {
            return null;
        }
        req.registerFreeResourcesHandler(this);
        if (req.getDataService() == null && req.getDataServiceId() == null) {
            req.setDataService(this);
        }
       
        OperationInfo oi = info.getOperationInfo(req.getOperationId());
        if (oi == null) {
            throw new OperationNoFoundException("Not found operation：" + req.getOperationId() + " in datasource:" + getId());
        }
        if (oi.getBatch() != null) {
                return executeBatch(req,oi);
        }
        //附加的参数
        Map<String,ParamInfo> params = oi.getParams();
        if (params!= null && !params.isEmpty()) {
            prepareParams(req,params);
        }
        //转换请求
        List<TransformerInfo> trans = oi.getTransformers();
        List<Transformer> transformers = prepareTransformer(oi,req,trans);
        if (transformers != null && !transformers.isEmpty()) {
            transformRequest(req, transformers);
        }
       //验证
        OperationType type = oi.getType();
        DSResponse response = null;
        //自定义的不自动验证
        if (type != OperationType.CUSTOM  && oi.getInvoker() == null) {
            response = validateDSRequest(req);
            if (response != null) {
                return transformResponse(response, transformers);
            }
        }

        req.setRequestStarted(true);
        // 配置了invoker优先处理
        if ((!req.isInvoked()) && oi.getInvoker() != null) {
            response = DMIDataService.execute(container, req, req.getDSCall());
            return transformResponse(response, transformers);
        } else {
            response = executeDefault(req, type);
            return transformResponse(response, transformers);
        }
    }
   
    /**
     * 根据配置准备Transformers
     */
    private List<Transformer> prepareTransformer(OperationInfo oi, DSRequest req,List<TransformerInfo> trans) {
        List<Transformer> transformers=null;
        if(trans!=null){
            transformers= new ArrayList<Transformer>();
            for(TransformerInfo tran:trans){
                Transformer transformer=null;
               LookupType type= tran.getLookup();
               String serviceName=tran.getName();
               Class<? extends Transformer> serviceClass= tran.getClazz();
               if(type==LookupType.CONTAINER){
                   if(serviceName==null){
                       if(serviceClass==null||serviceClass==Transformer.class){
                           //通常有多个validator
                           throw new ValidationException("transformer must specify name to determine validator service ");
                       }else{
                           transformer=container.getExtension(serviceClass);
                       }
                       
                   }else{
                       ConfiguredBeanProvider provider = container.getExtension(ConfiguredBeanProvider.class);
                       if(provider!=null){
                           transformer= provider.getBeanOfType(serviceName, serviceClass==null?Transformer.class:serviceClass);
                       }
                   }
               } else if(serviceClass!=null&&type==LookupType.NEW){
                   try {
                       transformer = Reflection.newInstance(serviceClass);
                   } catch (Exception e) {
                      throw new ValidationException("Instance object",e);
                   }
               }
               if(transformer!=null){
                   ResourceManager rma= container.getExtension(ResourceManager.class);
                   ResourceManagerImpl rm = new ResourceManagerImpl(rma.getResourceResolvers());
                   rm.addResourceResolver( new RequestContextResourceResolver(req.getRequestContext()));
                   rm.addResourceResolver( new DSRequestResolver(req));
                   ResourceInjector injector = new ResourceInjector(rm);
                   injector.inject(transformer);
                   transformers.add(transformer);
                  
               }
            }
        }
        customTransformer(transformers,req,oi);
        return transformers;
    }

    
    protected void customTransformer(List<Transformer> transformers, DSRequest req, OperationInfo oi) {
        if(transformers==null){
            transformers= new ArrayList<Transformer>();
        }
        if (oi.getProperty("template") != null) {
            transformers.add(new TemplateTransformer(oi.getProperty("template").toString(), false));
        } else if (oi.getProperty("template-file") != null) {
            transformers.add(new TemplateTransformer(oi.getProperty("template-file").toString(), true));
        }
    }

    protected void transformRequest(DSRequest req, List<Transformer> trans) {
        for(Transformer transformer :trans){
            try {
                Object transformedValues= transformer.transformRequest(req.getRawValues(),req);
                req.setRawValues(transformedValues);
            } catch (Exception e) {
                throw new TransformerException("Transformer DSRequest",e);
            }
        }
    }

    protected DSResponse transformResponse(DSResponse response, List<Transformer> transformers) {
        if (transformers==null||transformers.size()==0) {
            return response;
        }
        for(Transformer transformer :transformers){
            try {
               Object transformedObject= transformer.transformResponse(response.getRawData(),response);
               response.setRawData(transformedObject);
            } catch (Exception e) {
                throw new TransformerException("Transformer DSRequest",e);
            }
        }
        return response;
    }

    protected DSRequest prepareParams(DSRequest req, Map<String, ParamInfo> params) throws DSCallException {
        Object values = req.getRawValues();
        // 如果为空，就不做处理
        if (values == null) {
            return req;
        }
        List<?> list = null;
        try {
            if (values instanceof List<?> && values.getClass().isArray()) {
                list = req.getValueSets();
                List<Object> afters = new ArrayList<Object>();
                for (Object o : list) {
                    afters.add(injectParma(o, params));
                }
                req.setRawValues(afters);
            } else {
                req.setRawValues(injectParma(values, params));
            }
        } catch (Exception e) {
            throw new DSCallException("Prepare params error", e);
        }
        return req;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object injectParma(Object values, Map<String, ParamInfo> params) throws Exception {
        Map map;
        boolean isMap=false;
        if(values instanceof Map){
            map=(Map)values;
            isMap=true;
        }else{
            map=TransformUtils.transformType(Map.class, values);
        }
        for(String name:params.keySet()){
            ParamInfo param  = params.get(name);
            if(map.containsKey(name)&&param.isOverride()){
                map.put(name, param.getValue());
            }else{
                map.put(name, param.getValue());
            }
        }
        if(!isMap){
            return DataUtils.setProperties(map, values);
        }else{
            return map;
        }
       
    }

    /**
     * 批量执行一系列请求.
     * 
     * @param req
     * @return
     * @throws DataxException 
     */
    protected DSResponse executeBatch(DSRequest req, OperationInfo oi) throws DSCallException {
        BatchOperations bos = oi.getBatch();
        MergedType  merged = bos.getMergedType();
        TransactionPolicy policy = bos.getTransactionPolicy();
        DSCall old = DSCallUtils.getDSCall();
        try {
            DSCall newdsc = dsCallFactory.createDSCall(policy);
            DSCallUtils.setDSCall(newdsc);
            for (OperationInfo op : bos.getOperations()) {
                DSRequest request = createNewRequest(req, op);
                newdsc.execute(request);
            }
            return newdsc.getMergedResponse(merged);
        } finally {
            DSCallUtils.setDSCall(old);
        }
    }
  
    
    /**
     * 根据OperationInfo重新生成request。
     * 
     * @param req
     * @param op
     * @return
     */
    protected DSRequest createNewRequest(DSRequest req, OperationInfo op) {
       DSRequestImpl newreq= (( DSRequestImpl)req).clone();
       newreq.setOperationId(op.getId());
        return newreq;
    }

    /**
     * 执行带Transformer的请求
     * 
     * @param req
     * @return
     */
    protected DSResponse executeDefault(DSRequest req,OperationType type)throws DSCallException {
        if (DataTools.isFetch(type)) {
            return executeFetch(req);
        } else if (DataTools.isRemove(type)) {
            return executeRemove(req);
        } else if (DataTools.isUpdate(type)) {
            return executeUpdate(req);
        } else if (DataTools.isAdd(type)) {
            return executeAdd(req);
        } else {
            return executeCustomer(req);
        }
    }

    protected DSResponse executeCustomer(DSRequest req)throws DSCallException {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse notSupported(DSRequest req) {
        OperationInfo oi = info.getOperationInfo(req.getOperationId());
        throw new UnsupportedOperationException(
            new StringBuilder().append("Default operation type '")
            .append(oi.getType()).append("' not supported by this DataSource (")
            .append(getServerType()).append(")").toString());
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse executeAdd(DSRequest req) throws DSCallException{
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse executeUpdate(DSRequest req)throws DSCallException {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse executeRemove(DSRequest req)throws DSCallException {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse executeFetch(DSRequest req)throws DSCallException {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    protected DSResponse validateDSRequest(DSRequest req) {
        if (req.isValidated())
            return null;
        req.setValidated(true);
        List<Object> errors = validateDSRequst( req);
        if (errors != null) {
            VALIDATION.info((new StringBuilder()).append("Validation error: ").append(errors).toString());
            DSResponse dsResponse = new DSResponseImpl(this, req);
            dsResponse.setStatus(Status.STATUS_VALIDATION_ERROR);
            dsResponse.setErrors(errors.toArray(new Object[errors.size()]));
            return dsResponse;
        } else {
            return null;
        }
    }

    /**
     * @param baseDataService
     * @param req
     * @return
     */
    @Override
    public List<Object> validateDSRequst(DSRequest req) throws ValidationException{
         if(req.getDataService()==null){
             throw new DataServiceNoFoundException("Not found dataservice:"+req.getDataServiceId());
         }
        List<FieldInfo> fields= req.getDataService().getDataServiceInfo().getFields();
       //如果没有配置Fields,不能验证。
        if(fields==null||fields.isEmpty()){
            return null;
        }
        OperationInfo oi= req.getOperationInfo();
        Boolean validate = oi.getValidate();
        OperationType type =oi.getType();
        if(validate==null){
            if(DataTools.isModificationOperation(type)){
                validate=Boolean.TRUE;
            }else{
                validate=Boolean.FALSE;
            }
        }
        if(!validate){
            return null;
        }
        ValidationContext vcontext = ValidationContext.instance();
        vcontext.setContainer(this.container);
        vcontext.setDSRequest(req);
        vcontext.setDSCall(req.getDSCall());
        vcontext.setRequestContext(req.getRequestContext());
        vcontext.setValidationEventFactory(ValidationEventFactory.instance());
        vcontext.setValidatorService(DefaultValidatorService.getInstance());
        vcontext.setLocale(getLocale());
        vcontext.setResourceBundleManager(getResourceBundleManager());
        //更新的时候，property为null为正常情况。
        if(type==OperationType.UPDATE){
            vcontext.setPropertiesOnly(true);
        }
        Object validated=validateRecords(req.getRawValues(),vcontext);
        if(oi.getUsedValidatedValues()!=null&&oi.getUsedValidatedValues()){
            req.setRawValues(validated);
        }else{
            req.setAttribute(DATAX.AFTER_VALIDATE_VALUES, validated);
        }
        
        Map<String, Object> errors = vcontext.getErrors();
        if (errors != null)
            return new ArrayList<Object>(errors.values());
        else
            return null;
    }
    
    private Locale getLocale(){
       String locale= this.getConfigProperties().getString("locale",null);
       if(locale!=null){
           return new Locale(locale);
       }else{
           return Locale.getDefault();
       }
    }
    
    protected ResourceBundleManager getResourceBundleManager(){
      return container.getExtension(ResourceBundleManager.class);
    }

    /**
     * @param valueSets
     * @param vcontext
     */
    protected Object validateRecords(Object rdata, ValidationContext vcontext) {
        if(rdata==null){
            return null;
        }
        long start = System.currentTimeMillis();
        Object validatedObject;
        if(rdata instanceof List<?>){
            List<?> data=(List<?>)rdata;
            List<Object> records = new ArrayList<Object>();
            for (int i = 0; i < data.size(); i++){
                records.add(validateRecord(data.get(i), vcontext));
            }
            long end = System.currentTimeMillis();
            String __info = new StringBuilder().append("Done validating ")
                .append(data.size()).append(" '").append(getId())
                .append("'s at path '").append(vcontext.getPath())
                .append("': ").append(end - start).append("ms")
                .append( data.size() != 0 ? (new StringBuilder())
                    .append(" (avg ").append((end - start) / data.size())
                    .append(")").toString() : "").toString();
            createAndFireTimeMonitorEvent(end-start,__info);
            validatedObject= records;
        }else{
            validatedObject= validateRecord(rdata, vcontext);
        }
        long end = System.currentTimeMillis();
        String __info = new StringBuilder().append("Done validating ")
            .append("at path '").append(vcontext.getPath())
            .append("': ").append(end - start).append("ms").toString();
        createAndFireTimeMonitorEvent(end-start,__info);
        return validatedObject;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object validateRecord(Object data, ValidationContext vcontext) {
        if (data == null)
            return null;
        if(data instanceof Element){
            throw new UnsupportedOperationException("data is element");
        }else if(data instanceof Map<?, ?>) {
            vcontext.addPath(getId());
            if(VALIDATION.isDebugEnabled()){
                VALIDATION.debug((new StringBuilder()).append("Validating a '").append(getId()).append("' at path '").append(vcontext.getPath()).append("'").toString());
            }
            vcontext.addToTemplateContext(ValidationContext.TEMPLATE_DATASERVICE, this);
            
            Map<String, Object> record = (Map<String, Object>) data;
            for (Object key : record.keySet()) {
                String fieldName = (String) key;
                FieldInfo field = info.getField(fieldName);
                Object value = record.get(fieldName);
                if (field == null){
                    handleExtraValue(record, fieldName, value, vcontext);
                }else{
                   record.put(fieldName, validateFieldValue(record, field, value, vcontext));
                }
            }
            checkStructure(record, vcontext);
            vcontext.removePathSegment();
            return record;
        }else{
            Map  records=null;
            try {
                records=  TransformUtils.transformType(Map.class,data);
            } catch (Exception e) {
                vcontext.addError(new ErrorMessage("validate value can't transform to map ,cause:"+e.getMessage()));
            }
            if(records!=null){
               return validateRecord(records,vcontext);
            }
            return data;
        }
        
    }

    /**
     * @param record
     * @param vcontext
     */
    protected void checkStructure(Map<String, Object> record, ValidationContext vcontext) {
        List<FieldInfo> fields = info.getFields();
        if(fields!=null){
            for(FieldInfo field:fields){
                Object value = record.get(field.getName());
                checkRequired(record, field, value, vcontext);
            }
        }
        
    }

    /**
     * @param record
     * @param field
     * @param value
     * @param vcontext
     */
    protected boolean checkRequired(Map<String, Object> record, FieldInfo field, Object value, ValidationContext vcontext) {
        if (field.getRequired() != null && field.getRequired()
            && ("".equals(value) || value == null && (!vcontext.isPropertiesOnly() || record.containsKey(field.getName())))) {
            vcontext.addError(field.getName(), vcontext.localizedErrorMessage(new ErrorMessage("%validator_requiredField")));
            return false;
        } else {
            return true;
        }

    }

    /**
     * 验证Field的值
     */
    protected  Object validateFieldValue(Map<String, Object> record, FieldInfo field, Object value, ValidationContext vcontext) {
        return validateFieldValue(record,field,value,vcontext,null);
    }
    
    protected  Object validateFieldValue(Map<String, Object> record, FieldInfo field, Object value, ValidationContext vcontext,ValidationCreator creator) {
        String name = field.getName();
        vcontext.addPath(name);
        vcontext.addToTemplateContext(ValidationContext.TEMPLATE_FIELD, field);
        vcontext.addToTemplateContext(ValidationContext.TEMPLATE_RECORD, record);
        if (creator == null) {
            creator = getFieldCreator(field, vcontext);
        }
        if (creator == null && field != null) {
            String vinfo = (new StringBuilder()).append("No such type '").append(field.getType()).append("', not processing field value at ").append(
                vcontext.getPath()).toString();
            vcontext.removePathSegment();
            createAndFireValidationEvent(Level.WARNING, vinfo);
            return value;
        }
        if (VALIDATION.isDebugEnabled()) {
            String vinfo = (new StringBuilder()).append("Validating field at path:")
                .append(vcontext.getPath()).append(" as ")
                .append(getId()).append(".")
                .append( name).append(" type: ")
                .append(creator.getName()).toString();
            createAndFireValidationEvent(Level.DEBUG,vinfo);
        }
        vcontext.setCurrentRecord(record);
        vcontext.setCurrentDataService(this);
        Object res = creator.create(value, vcontext);
        vcontext.removePathSegment();
        return res;
        
    }
    
    protected ValidationCreator getFieldCreator(FieldInfo field, ValidationContext vcontext) {
        String strType = field.getType().value();
        boolean isEnum = false;
        if (field.getType() == FieldType.ENUM||field.getType() == FieldType.INT_ENUM) {
            isEnum = true;
        }
        List<ValidatorInfo> vls = field.getValidators();

        BuiltinCreator creator = vcontext.getBuiltinCreator(field.getType(), vcontext);
        List<ValidatorInfo> typeVs = null;
        if (creator != null) {
            typeVs = creator.getValidatorInfos();
        }
        @SuppressWarnings("unchecked")
        List<ValidatorInfo> allValidators=(List<ValidatorInfo>) DataUtils.combineAsLists(typeVs, vls);
        if (allValidators == null)
            allValidators = new ArrayList<ValidatorInfo>();
        if (isEnum && field.getValueMap() != null) {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("valueMapList", field.getValueMap());
            ValidatorInfo vi = new ValidatorInfo("isOneOf", properties);
            allValidators.add(0, vi);
        }
        if (VALIDATION.isDebugEnabled()) {
            VALIDATION.debug(
                (new StringBuilder()).append("Creating field validator for field ")
                .append(getId()).append(".").append(field.getName())
                .append(", of simple type: ")
                .append(field.getType())
                .append(", with inline validators: ")
                .append(vls).append(", and type validators: ")
                .append(typeVs).toString());
        }
        creator = new BuiltinCreator(strType, allValidators);
        return creator;
    }

    /**
     * 处理没有在DataService中配置Field的值得验证
     */
    protected void handleExtraValue(Map<String, Object> record, String fieldName, Object value, ValidationContext vcontext) {
        if (value != null) {
            VALIDATION.debug(
                (new StringBuilder()).append("Value provided for unknown field: ").append(getId()).append(".").append(fieldName).append(
                    ": value is: ").append(value).toString());
        }
    }

    protected void createAndFireTimeMonitorEvent(long time, String msg) {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        properties.put(TimeMonitorEvent.TOTAL_TIME, time);
        properties.put(TimeMonitorEvent.TIME_UNIT, TimeUnit.MICROSECONDS);
        properties.put(TimeMonitorEvent.MESSAGE, msg);
        TimeMonitorEvent event= new TimeMonitorEvent(properties);
        if(getEventService()!=null){
            getEventService().postEvent(event);
        }
    }
    protected boolean isEventEnable(){
        return getEventService()!=null;
    }
    protected void createAndFireValidationEvent(Level levle, String msg) {
        ValidationEvent event= new ValidationEvent(levle,msg);
        if(getEventService()!=null){
            getEventService().postEvent(event);
        }else{
            VALIDATION.debug(msg);
        }
    }

    public EventService getEventService() {
        return eventService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
   
    @Override
    public DataServiceInfo getDataServiceInfo(){
        return info;
    }

    
    public Container getContainer() {
        return container;
    }

    
    public DataTypeMap getConfigProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataService#getProperties(java.lang.Object)
     */
    @Override
    public Map<Object, Object> getProperties(Object data) {
        boolean dropExtra = DataUtils.asBoolean(info.getProperty("dropExtraFields"));
        return getProperties(data, dropExtra);
    }

    public Map<Object, Object> getProperties(Object obj, boolean dropExtraFields) {
        return getProperties(obj, ((Collection<String>) (null)), dropExtraFields);
    }

    public Map<Object, Object> getProperties(Object obj, Collection<String> propsToKeep) {
        return getProperties(obj, propsToKeep, false);
    }

    public Map<Object, Object> getProperties(Object obj, Collection<String> propsToKeep, boolean dropExtraFields) {
        return getProperties(obj, propsToKeep, dropExtraFields, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<Object, Object> getProperties(Object data, Collection<String> popToKeep, boolean dropExtraFields, ValidationContext validationContext) {
        if (data == null)
            return null;
        if(!dropExtraFields){
            if(Map.class.isAssignableFrom(data.getClass()))
                return Map.class.cast(data);
            else{
                Map obj=null;
                try {
                    obj = DataUtils.getProperties(data);
                } catch (Exception e) {
                    //INGONE;
                }
                return obj;
            }
        }
           
        Map result = new LinkedMap();
       
        Map<Object, Object> source = null;
        Set<String> outProperties = new HashSet<String>();
        if (popToKeep != null)
            outProperties.addAll(popToKeep);
        List<String> prop = new ArrayList<String>();
        //Map<String, String> xpaths = null;--XPATH
        List<FieldInfo> fields =info.getFields();
        if (fields == null)
            return Collections.emptyMap();
        for (FieldInfo field : fields) {
          
            if (dropExtraFields && field.getType() == FieldType.UNKNOWN)
                continue;
            prop.add(field.getName());
            /*if (field.getValueXPath() != null) {
                if (xpaths == null)
                    xpaths = new HashMap<String, String>();
                xpaths.put(field.getName(), field.getValueXPath());
            }--XPATH*/
        }
        if (prop != null)
            outProperties.addAll(prop);
        if (data instanceof Map<?, ?>) {
            source = (Map<Object, Object>) data;
            for (Object key : source.keySet()) {
                if (outProperties.contains(key)) {
                    result.put(key, source.get(key));
                }
            }
        } else {
            try {
                result = DataUtils.getProperties(data, outProperties);
            } catch (Exception e) {
                result = null;
                LOG.warn("transform bean object to map failed .caused by" + e.getMessage());
            }
        }
       /* if (xpaths != null) {
            for (String key : xpaths.keySet()) {
                Object value = result.get(key);
                if (value != null) {
                    JXPathContext context = JXPathContext.newContext(value);
                    result.put(key, context.getValue(xpaths.get(key)));
                }
            }
        }--XPATH*/
        return result;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataService#hasRecord(java.lang.String, java.lang.Object)
     */
    @Override
    public boolean hasRecord(String realFieldName, Object value) {
        // XXX
        return false;
    }
    
    @Override
    public boolean canStartTransaction(DSRequest req, boolean ignoreExistingTransaction) {
        if (req == null)
            return false;
        if (!canJoinTransaction(req))
            return false;
        if (req.getDSCall() == null)
            return false;
        boolean isModification = DataTools.isModificationRequest(req);
        TransactionPolicy policy = req.getDSCall().getTransactionPolicy();
        if (isModification) {
            //不管policy，修改的加入事物。
            if(ignoreExistingTransaction){
                return true;
            }
            if (policy == TransactionPolicy.NONE) {
                return false;
            } else {
                return true;
            }
        } else {
            if(ignoreExistingTransaction){
                return false;
            }
            if (policy == null) {
                return false;
            } else {
                switch (policy) {
                    case NONE:
                        return false;
                    case ALL:
                        return true;
                    case ANY_CHANGE:
                        return req.getDSCall().queueIncludesUpdates(req);
                    case FROM_FIRST_CHANGE:
                        return req.getDSCall().queueIncludesUpdates(req);
                    default:
                        return false;
                }
            }
        }
    }
    

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataService#canJoinTransaction(org.solmix.datax.DSRequest)
     */
    @Override
    public boolean canJoinTransaction(DSRequest req) {
        if (req != null && req.getDSCall() != null) {
            Boolean reqOvrride = req.isCanJoinTransaction();
            if (reqOvrride != null) {
                return reqOvrride.booleanValue();
            }
        }
        Boolean work = autoJoinAtOperationLevel(req);
        if (work == null) {
            // check datasource level
            work = autoJoinAtDataServiceLevel();
            if (work == null) {
                if (req != null && req.getDSCall() != null) {
                    TransactionPolicy policy = req.getDSCall().getTransactionPolicy();
                    if (policy == TransactionPolicy.NONE)
                        return false;
                    if (policy == TransactionPolicy.ALL)
                        return true;
                }
                work = autoJoinAtProviderLevel(req);
                if (work == null)
                    work = autoJoinAtGlobalLevel(req);
            }
        }
        if (work == null)
            return false;
        else
            return work.booleanValue();
    }
    
    /**
     * 全局配置。
     * 
     * @return
     */
    protected Boolean autoJoinAtGlobalLevel(DSRequest req){
       String autoJoin= getConfigProperties().getString("autoJoinTransactions");
       return parseAutoJoinTransactions(req,autoJoin);
    }
    
    protected Boolean parseAutoJoinTransactions(DSRequest req,Object join){
        if (join == null)
            return null;
        String autoJoin=join.toString();
        if (autoJoin.toLowerCase().equals("true") || autoJoin.toLowerCase().equals("ALL"))
            return Boolean.TRUE;
        if (autoJoin.toLowerCase().equals("false") || autoJoin.toLowerCase().equals("NONE"))
            return Boolean.FALSE;
        if (req != null && req.getDSCall() != null) {
            if (autoJoin.equals("FROM_FIRST_CHANGE"))
                return Boolean.valueOf(req.getDSCall().queueIncludesUpdates(req));
            if (autoJoin.equals("ANY_CHANGE"))
                return Boolean.valueOf(req.getDSCall().queueIncludesUpdates(req));
        }
        return null;
    }
    /**
     * DataService实现默认。
     * 
     * @return
     */
    protected Boolean autoJoinAtProviderLevel(DSRequest req) {
        return false;
    }
    
    /**
     * 检查DataService配置
     * @return
     */
    protected Boolean autoJoinAtDataServiceLevel() {
        Object aj= info.getProperty("autoJoinTransactions");
        if(aj==null){
            return null;
        }else{
            return Boolean.valueOf(aj.toString());
        }
    }

    /**
     * 检查operation配置
     */
    protected Boolean autoJoinAtOperationLevel(DSRequest req) {
        OperationInfo oi=   req.getOperationInfo();
        Object aj= oi.getProperty("autoJoinTransactions");
        if(aj==null){
            return null;
        }else{
            return Boolean.valueOf(aj.toString());
        }
    }
    //需要根据DSrequest中配置的服务命名空间和参数规则(rule)决定使用的数据源。
  /*  protected String getTransactionObjectKey(DSRequest req){
        return null;
    }*/
    
   /* protected Transaction getTransactionObject(DSRequest req) {
        if (req == null){
            return null;
        }
        if (req.getDSCall() == null)
            return null;
        else
            return (Transaction) req.getDSCall().getAttribute(getTransactionObjectKey(req));
    }*/

    
    @Override
    public Object escapeValue(Object data, String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDSCallFactory(DSCallFactory factory) {
       this.dsCallFactory=factory;
    }
}
