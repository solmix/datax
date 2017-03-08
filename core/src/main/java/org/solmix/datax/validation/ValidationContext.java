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

package org.solmix.datax.validation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DATAX;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DataService;
import org.solmix.datax.RequestContext;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.i18n.ResourceBundleManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月18日
 */

public class ValidationContext 
{


    public static final String DATE_FORMAT = "dateFormat";

    public static final String TEMPLATE_FIELD="field";

    public static final String TEMPLATE_DATASERVICE = "dataService";

    public static final String TEMPLATE_RECORD = "record";
    public static ValidationContext instance() {
        ValidationContext instance = new ValidationContext();
        return instance;
    }

    private ValidationEventFactory factory;


    private RequestContext requestContext;
    
    private DSRequest dsRequest;

    Map<String, Object> errors;

    String fieldName;

    String path="";

    protected Map<String, Object> templateContext;

    Map<String, Object> currentRecord;

    private DataService currentDataService;
    

    private DefaultValidatorService validatorService;

    private boolean propertiesOnly;
    
    boolean valueIsSet;
    
    private Object resultingValue;
    
    private ResourceBundleManager bundleManger;

    private Locale locale;

    private Container container;
    public void setResultingValue(Object resultingValue) {
        this.valueIsSet = true;
        this.resultingValue = resultingValue;
    }
    /**
     * <code>ResultingValue</code> is cached the current <code>Tfield</code> value which has validated.<br>
     * 
     * @return
     */
    public Object getResultingValue() {
        return resultingValue;
    }
    /**
     * <code>ResultingValue</code> is cached the current <code>Tfield</code> value which has validated.<br>
     * After we get the value or before set another <code>ResultingValue</code>,we must clear it.
     */
    public void clearResultingValue() {
        this.resultingValue = null;
        valueIsSet = false;
    }
    
    /**
     * <code>ResultingValue</code> is cached the current <code>Tfield</code> value which has validated.<br>
     * This method indicate the value is set or not.
     * 
     * @return
     */
    public boolean resultingValueIsSet() {
        return valueIsSet;
    }
    /**
     * @return
     */
    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }

    
    public DSRequest getDSRequest() {
        return dsRequest;
    }
    
    public void setDSRequest(DSRequest dsRequest) {
        this.dsRequest = dsRequest;
    }
    /**
     * @param currentDataSource the currentDataSource to set
     */
    public void setCurrentDataService(DataService currentDataService) {
        this.currentDataService = currentDataService;
    }

    public DataService getCurrentDataService() {
        return currentDataService;
    }

    /**
     * @param requestContext
     */
    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;

    }


    public void removePathSegment() {
        path = path.substring(0, path.lastIndexOf("/"));
        fieldName = path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    public void addPath(String segment) {
        path = (new StringBuilder()).append(path).append("/").append(segment).toString();
        fieldName = segment;
    }

    /**
     * @param instance
     */
    public void setValidationEventFactory(ValidationEventFactory instance) {
        this.factory = instance;
    }

    public void addError(Object newErrors) {
        addError(fieldName, newErrors);
    }

    public void addError(String fieldName, Object newErrors) {
        if (newErrors == null)
            return;
        if (errors == null)
            errors = new HashMap<String, Object>();
        LoggerFactory.getLogger(DATAX.VALIDATION_LOGNAME).debug(
            (new StringBuilder()).append("Adding validation errors at path '").append(path).append("': ").append(newErrors).toString());
        String recordPath = getCurrentRecordPath(fieldName);
        ErrorReport report = (ErrorReport) errors.get(recordPath);
        if (report == null) {
            report = new ErrorReport();
            report.put("recordPath", recordPath);
            errors.put(recordPath, report);
        }
        DataUtils.putCombinedList(report, fieldName, newErrors);
    }

    public Map<String, Object> getCurrentRecord() {
        return currentRecord;
    }

    /**
     * @param currentRecord the currentRecord to set
     */
    public void setCurrentRecord(Map<String, Object> currentRecord) {
        this.currentRecord = currentRecord;
    }

    public String getCurrentRecordPath(String fieldName) {
        String recordPath = path;
        if (fieldName != null && recordPath.endsWith((new StringBuilder()).append("/").append(fieldName).toString()))
            recordPath = recordPath.substring(0, recordPath.lastIndexOf("/"));
        return recordPath;
    }

    /**
     * @return
     */
    public ValidationEventFactory getValidationEventFactory() {
        return factory;
    }
    public Map<String, Object> getTemplateContext() {
        return templateContext;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    public void addToTemplateContext(String key, Object value) {
        if (templateContext == null)
            initTemplateContext();
        templateContext.put(key, value);

    }

    public void addToTemplateContext(Map<String, Object> keyValues) {
        if (templateContext == null)
            initTemplateContext();
        templateContext = DataUtils.mapMerge(keyValues, templateContext);

    }

    /**
    * 
    */
    private void initTemplateContext() {
        templateContext = new HashMap<String, Object>();
        templateContext.put("DataUtils", new DataUtils());

    }

    /**
     * 如果为真，那么验证时只管是否设置了key，而不管value是否为空。
     * 
     * @return
     */
    public boolean isPropertiesOnly() {
        return propertiesOnly;
    }
    
    public void setPropertiesOnly(boolean b) {
        this.propertiesOnly = b;

    }

    /**
     * @param instance
     */
    public void setValidatorService(DefaultValidatorService instance) {
        this.validatorService=instance;
        
    }

    public ErrorMessage localizedErrorMessage(ErrorMessage errorMessage) {
        ResourceBundle resourceBundle = (ResourceBundle) templateContext.get("RESOURCE_BUNDLE");
        if (resourceBundle == null) {
            resourceBundle = bundleManger.getResourceBundle(locale);
            if (resourceBundle != null) {
                templateContext.put("RESOURCE_BUNDLE", resourceBundle);
            }
        }
        if (resourceBundle != null) {
            String emsg = errorMessage.getErrorString();
            if (emsg.startsWith("%")) {
                emsg = emsg.substring(1);
                emsg = resourceBundle.getString(emsg);
                if (errorMessage.getArgments() != null) {
                    emsg = MessageFormat.format(emsg, errorMessage.getArgments());
                }
                errorMessage.setErrorString(emsg);
            }
        }
        return errorMessage;
    }
    public String getFieldName() {
        return fieldName;
    }
    
    public ErrorReport validateField(Map<String, Object> record, String fieldName, List<ValidatorInfo> validators,
        ValidationContext vcontext, Object value) {
        if(validatorService!=null){
            return validatorService.validateField(record, fieldName, validators, vcontext, value,null);
        }else{
            return null;
        }
    }
    /**
     * @param type
     * @param vcontext
     * @return
     */
    public BuiltinCreator getBuiltinCreator(FieldType type, ValidationContext vcontext) {
        ValidatorInfo validator = validatorService.getValidatorInfo(type);
        if (validator != null)
            return new BuiltinCreator(type.value(), validator);
        else
            return null;
    }
    /**
     * @param resourceBundleManager
     */
    public void setResourceBundleManager(ResourceBundleManager resourceBundleManager) {
       this.bundleManger=resourceBundleManager;
        
    }
    /**
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale=locale;
    }
    /**
     * @param container
     */
    public void setContainer(Container container) {
        this.container=container;
    }
    
    public Container getContainer() {
        return container;
    }

}
