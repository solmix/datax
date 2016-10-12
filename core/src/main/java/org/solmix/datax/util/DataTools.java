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

import org.solmix.commons.pager.PageControl;
import org.solmix.datax.DSRequest;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.OperationType;


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
}
