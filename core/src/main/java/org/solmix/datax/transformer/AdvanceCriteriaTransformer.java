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

package org.solmix.datax.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.datax.DSRequest;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月29日
 */

public class AdvanceCriteriaTransformer extends TransformerAdaptor
{

    public static final String _CONSTRUCTOR = "_constructor";

    public static final String ADVANCED_CRITERIA = "AdvancedCriteria";

    @Override
    public Object transformRequest(Object requestData, DSRequest request) throws Exception {
        return paseAdvanceCriteria(request.getValues());
    }

    public static Map<String, Object> paseAdvanceCriteria(Map<String, Object> advance) {
        if (advance.get(_CONSTRUCTOR) == null || !ADVANCED_CRITERIA.equals(advance.get(_CONSTRUCTOR).toString())) {
            return advance;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        checkAdvanceCriteria(result, advance);
        // _equals
        return result;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void checkAdvanceCriteria(Map<String, Object> result, Map<String, Object> advance) {

        for (String key : advance.keySet()) {
            if (key.equals("criteria") || key.equals("operator") || key.equals("_constructor") || key.equals("value") || key.equals("fieldName")) {
                continue;
            }
            result.put(key, advance.get(key));
        }
        Object fieldName = advance.get("fieldName");
        Object criteria = advance.get("criteria");
        if (fieldName != null) {
            String oper = advance.get("operator").toString();
            Object value = advance.get("value");
            if(value!=null){
                value=value.toString().trim();
            }
            result.put(fieldName.toString() + "_" + oper, value);
        } else if (criteria != null) {
            if (criteria instanceof List) {
                List cri = (List) criteria;
                for (Object c : cri) {
                    Map<String, Object> mc = (Map<String, Object>) c;
                    checkAdvanceCriteria(result, mc);
                }
            } else if (criteria instanceof Map) {
                checkAdvanceCriteria(result, (Map<String, Object>) criteria);
            }
        }
    }
}
