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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.TransformUtils;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataService;
import org.solmix.datax.OperationNoFoundException;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.model.ValidatorInfo;
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
import org.solmix.runtime.event.EventService;
import org.solmix.runtime.event.TimeMonitorEvent;
import org.solmix.runtime.i18n.ResourceBundleManager;
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
        // 配置了invoker优先处理
        if (oi.getInvoker() != null) {
            DSResponse response = DMIDataService.execute(req, req.getDSCall(), req.getRequestContext());
            if (response != null) {
                return response;
            }
        }
        if (oi.getBatch() != null) {
            return executeBatch(req);
        }
        OperationType type = oi.getType();
        if (type != OperationType.CUSTOM) {
            DSResponse validationFailure = validateDSRequest(req);
            if (validationFailure != null)
                return validationFailure;
        }
        req.setRequestStarted(true);
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

  
    protected DSResponse executeCustomer(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse notSupported(DSRequest req) {
        OperationInfo oi = info.getOperationInfo(req.getOperationId());
        throw new UnsupportedOperationException(
            new StringBuilder().append("Operation type '")
            .append(oi.getType()).append("' not supported by this DataSource (")
            .append(getServerType()).append(")").toString());
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeAdd(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeUpdate(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeRemove(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeFetch(DSRequest req) {
        return notSupported(req);
    }

    /**
     * @param req
     * @return
     */
    private DSResponse executeBatch(DSRequest req) {
        // TODO Auto-generated method stub
        return null;
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
        validateRecords(req.getValueSets(),vcontext);
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
    protected Object validateRecords(List<?> data, ValidationContext vcontext) {
        if(DataUtils.isNullOrEmpty(data)){
            return null;
        }else{
            long start = System.currentTimeMillis();
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
            return records;
        }
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
            checkAutoConstruct(record, vcontext);
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
    private void checkAutoConstruct(Map<String, Object> record, ValidationContext vcontext) {
        // TODO Auto-generated method stub
        
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
        if (field.getType() == FieldType.ENUM) {
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
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.DataService#hasRecord(java.lang.String, java.lang.Object)
     */
    @Override
    public boolean hasRecord(String realFieldName, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

}
