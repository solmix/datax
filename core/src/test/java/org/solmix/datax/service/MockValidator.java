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
package org.solmix.datax.service;

import java.util.Map;

import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.validation.ErrorMessage;
import org.solmix.datax.validation.ValidationContext;
import org.solmix.datax.validation.ValidationException;
import org.solmix.datax.validation.Validator;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月1日
 */

public class MockValidator implements Validator
{

    @Override
    public ErrorMessage validate(ValidatorInfo validatorInfo, Object value, String fieldName, Map<String, Object> record, ValidationContext context)
        throws ValidationException {
        // TODO Auto-generated method stub
        return null;
    }


}
