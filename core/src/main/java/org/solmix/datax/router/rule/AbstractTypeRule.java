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
import org.solmix.commons.util.StringUtils;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月7日
 */

public abstract class AbstractTypeRule<F, T> implements RouterRule<F, T>
{
    private String typePatten;

    private String action;

    public AbstractTypeRule(String pattern, String action)
    {
        Assert.isNotNull(StringUtils.trimToNull(pattern));
        Assert.isNotNull(StringUtils.trimToNull(action));
        this.typePatten = pattern;
        this.action = action;
    }

    public void setTypePattern(String leftExpression) {
        this.typePatten = leftExpression;
    }

    public String getTypePattern() {
        return typePatten;
    }

    public void setAction(String rightExpression) {
        this.action = rightExpression;
    }

    
    public String getAction() {
        return action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((typePatten == null) ? 0 : typePatten.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractTypeRule other = (AbstractTypeRule) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (typePatten == null) {
            if (other.typePatten != null)
                return false;
        } else if (!typePatten.equals(other.typePatten))
            return false;
        return true;
    }
}
