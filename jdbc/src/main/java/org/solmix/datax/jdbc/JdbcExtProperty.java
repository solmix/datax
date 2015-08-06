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
package org.solmix.datax.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.solmix.datax.model.OperationInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月5日
 */

public class JdbcExtProperty
{
    public static final String CUSTOM_VALUE_FIELDS_NODE="customValueFields";
    public static final String EXCLUDE_CRITERIA_FIELDS_NODE="excludeCriteriaFields";
    public static final String CUSTOM_FIELDS_NODE="customFields";
    public static final String QUALIFY_COLUMN_NAMES = "qualifyColumnNames";
    public static final String CUSTOM_CRITERIA_FIELDS_NODE = "customCriteriaFields";
    public static final String OUTPUTS_NODE = "outputs";
    public static final String SORT_BY_FIELDS = "sortByFields";
    public static final String CONSTRAINTS = "constraints";
    
    public static List<String> getExtensionFields(OperationInfo oi,String extension) {
        List<String> extensions = null;
        String cuscf = oi.getExtension(extension);
        if (cuscf != null) {
            extensions = new ArrayList<String>();
            for (String str : cuscf.split(","))
                extensions.add(str.trim());
        }
        return extensions;
    }

}
