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
package org.solmix.datax.wmix.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.ValidatorInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月25日
 */

public class DataServiceInfoSerializer extends JsonSerializer<DataServiceInfo>
{

    /**
     * {@inheritDoc}
     * 
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(DataServiceInfo value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("ID", value.getId());
        jgen.writeStringField("recordXPath", "/data");
        jgen.writeStringField("dataFormat", "json");
        jgen.writeBooleanField("clientOnly", false);
        List<FieldInfo> fields =value.getFields();
        if(DataUtils.isNotNullAndEmpty(fields)){
            jgen.writeFieldName("fields");
            jgen.writeStartArray();
            
            for(FieldInfo f:fields){
                jgen.writeObject(convertFiledToMap(f));
                
            }
            jgen.writeEndArray();
        }
        Collection<OperationInfo> operations =value.getOperations();
        if(operations!=null){
            Iterator<OperationInfo> it=  operations.iterator();
            jgen.writeFieldName("operationBindings");
            jgen.writeStartArray();
            while(it.hasNext()){
                OperationInfo oi = it.next();
                jgen.writeStartObject();
                jgen.writeStringField("operationType", oi.getType().value()); 
                jgen.writeStringField("operationId", oi.getId());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();
        }
        jgen.writeEndObject();
    }
    
    private Object convertFiledToMap(FieldInfo f) {
        Map<String, Object> fm = new HashMap<String, Object>();
        fm.put("name", f.getName());
        if (f.getHidden()!=null) {
            fm.put("hidden", f.getHidden());
        }
        if (f.getTitle() != null)
            fm.put("title", f.getTitle());
        if (f.getType() != null)
            fm.put("type", f.getType().value());
        if (f.getRequired() != null)
            fm.put("required", f.getRequired());
        if (f.getCanEdit() != null)
            fm.put("canEdit", f.getCanEdit());
        if (f.getCanExport() != null)
            fm.put("canExport", f.getCanExport());
        if (f.getCanFilter() != null)
            fm.put("canFilter", f.getCanFilter());
        if (f.getExportTitle() != null)
            fm.put("exportTitle", f.getExportTitle());
        if (f.getMaxFileSize() != null)
            fm.put("maxFileSize", f.getMaxFileSize());
        if (f.getDateFormat() != null)
            fm.put("dateFormat", f.getDateFormat());
        if (f.getPrimaryKey()!=null)
            fm.put("primaryKey", f.getPrimaryKey());
        if (f.getForeignKey() != null)
            fm.put("foreignKey", f.getForeignKey());
        if (f.getRootValue() != null)
            fm.put("rootValue", f.getRootValue());
        
        // valuemap
        if (f.getValueMap() != null) {
            fm.put("valueMap", f.getValueMap());
        }
        // validators
        if (f.getValidators() != null) {
            List<Object> vs = getValidatorsMap(f.getValidators());
            if (vs.size() > 0)
                fm.put("validators", vs);
        }
        return fm;
    }

    /**
     * @param validators
     * @return
     */
    private List<Object> getValidatorsMap(List<ValidatorInfo> validators) {
        List<Object> _return = new ArrayList<Object>();
        for (ValidatorInfo v : validators) {
            if (DataUtils.asBoolean(v.getClientOnly())) {
                Map<String, Object> vm = new HashMap<String, Object>();
                _return.add(vm);
                vm.put("clientOnly", v.getClientOnly());
                if (v.getType() != null)
                    vm.put("type", v.getType());
                if (v.getErrorMessage() != null)
                    vm.put("errorMessage", v.getErrorMessage());
                if (v.getMax() != null)
                    vm.put("max", v.getMax());
                if (v.getMin() != null)
                    vm.put("min", v.getMin());
                if (v.getExclusive()!=null)
                    vm.put("exclusive", v.getExclusive());
                if (v.getMask() != null)
                    vm.put("mask", v.getMask());
                if (v.getPrecision() != null)
                    vm.put("precision", v.getPrecision());
                if (v.getExpression() != null)
                    vm.put("expression", v.getExpression());
                if (v.getSubstring() != null)
                    vm.put("substring", v.getSubstring());
                if (v.getOperator() != null)
                    vm.put("operator", v.getOperator());
                if (v.getCount() != null)
                    vm.put("count", v.getCount());
            }
        }
        return _return;
    }


    @Override
    public Class<DataServiceInfo> handledType() { return DataServiceInfo.class; }
}
