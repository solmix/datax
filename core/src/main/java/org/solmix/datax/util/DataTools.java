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
package org.solmix.datax.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.pager.PageControl;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.support.DSResponseImpl;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class DataTools
{

    /**
     * @param operationType
     * @return
     */
    public static boolean isModificationOperation(OperationType operationType) {
        if (isAdd(operationType) || isRemove(operationType) || isUpdate(operationType))
            return true;
        else
            return false;
    }

    public static boolean isFetch(OperationType operationType) {
        return operationType == OperationType.FETCH;

    }

    public static boolean isAdd(OperationType operationType) {
        return operationType == OperationType.ADD;

    }

    public static boolean isFilter(OperationType operationType) {
        return operationType == OperationType.FETCH;

    }

    public static boolean isCustom(OperationType operationType) {
        return operationType == OperationType.CUSTOM;
    }

    public static boolean isRemove(OperationType operationType) {
        return operationType == OperationType.REMOVE;
    }

    public static boolean isUpdate(OperationType operationType) {
        return operationType == OperationType.UPDATE;
    }


    public static boolean isBinary(FieldInfo field) {
        if (field == null)
            return false;
        FieldType type = field.getType();
        return type != null && (type == FieldType.BINARY || type == FieldType.IMAGE || type == FieldType.IMAGE_FILE || "clob".equals(type.value()));
    }

    /**
     * @param type
     * @return
     */
    public static boolean typeIsBoolean(String type) {
        return "boolean".equals(type) || "false".equals(type) || "true".equals(type);
    }

    public static boolean typeIsNumeric(String type) {
        return "number".equals(type) || "float".equals(type) || "decimal".equals(type) || "double".equals(type) || "int".equals(type)
            || "intEnum".equals(type) || "integer".equals(type) || "sequence".equals(type);
    }

    /**
     * @param dataSourceName
     * @param opType
     * @return
     */
    public static String autoCreateOperationID(String dataSourceName, OperationType opType) {
        if (dataSourceName != null && opType != null)
            return dataSourceName + "_" + opType.value();
        return null;
    }

    /**
     * @param _type
     * @return
     */
    public static boolean isBinaryType(FieldType type) {
        if (type == null)
            return false;
        if (type == FieldType.BINARY || type == FieldType.IMAGE || type == FieldType.IMAGE_FILE || "clob".equalsIgnoreCase(type.value()))
            return true;
        else
            return false;
    }

    /**
     * @param req
     * @return
     */
    public static boolean isModificationRequest(DSRequest req) {
        if(req==null){
            return false;
        }
        return isModificationOperation(req.getOperationInfo().getType());
    }
    
    public static boolean isPaged(DSRequest req){
        if(req==null){
            return false;
        }
        PageControl page=   req.getAttachment(PageControl.class);
        if(page!=null){
            return isPaged(page);
        }
        
        return false;
    }
    
    public static boolean isPaged(PageControl page){
        if(page==null){
            return false;
        }
        if (page.getPageSize()>= 0 
            && page.getPageNum() > 0 ){
            return true;
        }
           
        return false;
    }
    
    public static DSResponse getMergedResponse(Map<DSRequest,DSResponse> responses,MergedType merged){
    	if(merged==null){
            merged=MergedType.SIMPLE;
        }
        switch (merged) {
            case SIMPLE:
                return simpleResponse(responses);
            case WRAPPED:
                return wrappedResponse(responses);
            default:
               throw new UnsupportedOperationException(merged.value());
           
        }
    }
    
    private static DSResponse simpleResponse(Map<DSRequest,DSResponse> results) {
    	List<DSRequest> canReturn = new ArrayList<DSRequest>();
        for (DSRequest req : results.keySet()) {
            if (DataUtils.asBoolean(req.getOperationInfo().getOneway())) {
                continue;
            } else {
                canReturn.add(req);
            }
        }
        Status st=Status.STATUS_SUCCESS;
        for(DSResponse res:results.values()){
        	if(res.getStatus()!=Status.STATUS_SUCCESS){
        		st=Status.STATUS_TRANSACTION_FAILED;
        	}
        }
        DSResponse res = null;
        boolean onlyOneRequest = canReturn.size()==1;
        if (onlyOneRequest) {
            res =results.get(canReturn.get(0));
        } else {
            res = new DSResponseImpl(st);
            Map<String, Object> mergedData = new LinkedHashMap<String, Object>();
            List<Object> errors = new ArrayList<Object>();
            for (DSRequest req : canReturn) {
                DSResponse resp = results.get(req);
                mergedData.put(req.getOperationId(), resp.getRawData());
                Object[] error = resp.getErrors();
                if (error != null && error.length > 0) {
                    errors.addAll(Arrays.asList(error));
                }
                res.setRawData(mergedData);
                if (errors.size() > 0) {
                    res.setErrors(errors.toArray());
                }
            }
        }
        return res;

    }
    
    private static DSResponse wrappedResponse(Map<DSRequest,DSResponse> results) {
        List<DSResponse> array= new ArrayList<DSResponse>();

        for (DSRequest req : results.keySet()) {
            if(DataUtils.asBoolean(req.getOperationInfo().getOneway())){
                continue;
            }
            DSResponse resp = results.get(req);
            array.add(resp);
        }
        Status st=Status.STATUS_SUCCESS;
        for(DSResponse res:results.values()){
        	if(res.getStatus()!=Status.STATUS_SUCCESS){
        		st=Status.STATUS_TRANSACTION_FAILED;
        	}
        }
        DSResponse res = new DSResponseImpl(st);
        res.setRawData(array);
        return res;
    }
}
