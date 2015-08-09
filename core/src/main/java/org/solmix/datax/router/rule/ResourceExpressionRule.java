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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.expr.Expression;
import org.solmix.commons.expr.ExpressionContext;
import org.solmix.commons.expr.ExpressionParseException;
import org.solmix.commons.expr.MappedContext;
import org.solmix.commons.expr.jexl.JexlExpressionFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.datax.router.RequestToken;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月9日
 */

public class ResourceExpressionRule extends AbstractRequestTokenRule
{
    private static final Logger LOG = LoggerFactory.getLogger(ResourceExpressionRule.class);
    
    private static JexlExpressionFactory jexl = new JexlExpressionFactory();
    private int type;
    private String expression;

    public ResourceExpressionRule(String pattern, String action,String expression,int type)
    {
        super(pattern, action);
        this.type=type;
        this.expression=expression;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean isPassed(RequestToken param) {
        Assert.isNotNull(param);
        if(getTypePattern()==null){
            return false;
        }
        boolean matches=false;
        switch(type){
            case OPERATON:
                matches= getTypePattern().equals(toOperation(param));
                break;
            case DATASERVICE:
                matches= getTypePattern().equals(toDataservice(param));
                break;
            case NAMESPACE:
                matches= getTypePattern().equals(toNamespace(param));
                break;
        }
       if(matches){
         Object par=  param.getParameter();
         ExpressionContext context=null;
         if(par instanceof Map<?, ?>){
             context=new MappedContext((Map)par);
         }else if(par instanceof List<?>){
             context=new MappedContext((Map)((List)par).get(0));
         }
        try {
            Expression expre= jexl.createExpression(expression);
            Object o =expre.evaluate(context);
            return DataUtils.asBoolean(o);
        } catch (ExpressionParseException e) {
            LOG.info("failed to evaluate attribute expression:'{}'",expression);
            return false;
        }
       }
       return false;
        
    }
    @Override
    public String toString() {
        return "Rule [resources=" + getAction() + ", pattern=" + getTypePattern() +  ", expression=" + expression+"]";
    }
}
