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

import org.solmix.commons.util.Assert;
import org.solmix.datax.router.RequestToken;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月9日
 */

public class ResourceRule extends AbstractRequestTokenRule
{

    private int type;

    public ResourceRule(String pattern, String action,int type)
    {
        super(pattern, action);
        this.type=type;
    }

    @Override
    public boolean isPassed(RequestToken param) {
        Assert.isNotNull(param);
        if(getTypePattern()==null){
            return false;
        }
        switch(type){
            case OPERATON:
                return getTypePattern().equals(toOperation(param));
            case DATASERVICE:
                return getTypePattern().equals(toDataservice(param));
            case NAMESPACE:
                return getTypePattern().equals(toNamespace(param));
        }
        return false;
        
    }
    
    @Override
    public String toString() {
        return "Rule [resources=" + getAction() + ", pattern=" + getTypePattern() + "]";
    }
}
