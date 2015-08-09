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
package org.solmix.datax.router.rule;

import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.datax.router.RequestToken;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月8日
 */

public abstract class AbstractRequestTokenRule extends AbstractTypeRule<RequestToken, List<String>>
{
    public static final String RESOURCES_SEPARATOR = ",";
    private List<String>       resourceIds                         = new ArrayList<String>();
    public static final int OPERATON = 1;
    public static final int DATASERVICE = 2;
    public static final int NAMESPACE = 3;
    public AbstractRequestTokenRule(String pattern, String action)
    {
        super(pattern, action);
    }

    @Override
    public synchronized List<String> action() {
        if (DataUtils.isNullOrEmpty(resourceIds)) {
            List<String> ids = new ArrayList<String>();
            for (String id : StringUtils.split(getAction(), RESOURCES_SEPARATOR)) {
                ids.add(StringUtils.trimToEmpty(id));
            }
            setResourceIds(ids);
        }
        return resourceIds;
    }

    
    public List<String> getResourceIds() {
        return resourceIds;
    }

    
    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
    
    protected String toOperation(RequestToken token){
        String action =token.getAction();
        if(StringUtils.isEmpty(action)){
            return null;
        }
        return action;
    }
    
    protected String toDataservice(RequestToken token){
        String action =token.getAction();
        if(StringUtils.isEmpty(action)){
            return null;
        }
        return action.substring(0, action.lastIndexOf("."));
    }
    
    protected String toNamespace(RequestToken token){
        String action =token.getAction();
        if(StringUtils.isEmpty(action)){
            return null;
        }
        String ds=action.substring(0, action.lastIndexOf("."));
        return ds.substring(0, ds.lastIndexOf("."));
    }
    
}
