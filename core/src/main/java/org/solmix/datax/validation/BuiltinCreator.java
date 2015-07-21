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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.model.ValidatorInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月20日
 */

public class BuiltinCreator implements ValidationCreator
{

    List<ValidatorInfo> validators;
    
    protected String name;
    public BuiltinCreator(String name,ValidatorInfo info){
        this.name=name;
        validators= DataUtils.makeListIfSingle(info);
    }
    
    public BuiltinCreator(String name,List<ValidatorInfo> validators){
        this.name=name;
        this.validators= validators;
    }
    @Override
    public String getName() {
        return name;
    }

 
    @Override
    public Object create(Object value, ValidationContext vcontext) throws ValidationException {
        if(validators==null ||value ==null){
            return value;
        }
        if ( value instanceof List< ? > )
        {
           List<?> valueList = (List<?>) value;
           List<Object> res = new ArrayList<Object>();
           for ( Object o : valueList )
           {
              res.add(validateValue( o, vcontext ));
           }
           return res;
        } else{
           return validateValue( value, vcontext );
        }
    }

    private Object validateValue(Object value, ValidationContext vcontext) {
        String fieldName = vcontext.getFieldName();
        Map< String ,Object > currentRecord = vcontext.getCurrentRecord();
        ErrorReport errors = vcontext.validateField( currentRecord, fieldName, validators, vcontext, value );
        if ( errors != null )
        {
           Object error = errors.get( fieldName );
           if ( error != null )
               vcontext.addError( error );
        }
        if ( vcontext.resultingValueIsSet() )
        {
           value = vcontext.getResultingValue();
           vcontext.clearResultingValue();
        }
        return value;
    }

    /**
     * @return
     */
    public List<ValidatorInfo> getValidatorInfos() {
        return validators;
    }

}
