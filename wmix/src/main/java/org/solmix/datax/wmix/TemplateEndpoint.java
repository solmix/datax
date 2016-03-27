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
package org.solmix.datax.wmix;

import org.solmix.datax.wmix.interceptor.TemplateInInterceptor;
import org.solmix.datax.wmix.interceptor.TemplateOutInterceptor;


/**
 * 提供基于模板的数据输出，如Velocity，jsp，freemarker等
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月9日
 */

public class TemplateEndpoint extends DataxEndpoint
{

    private static final long serialVersionUID = -1633004647563444571L;
    
    @Override
    protected void prepareOutInterceptors(){
        getOutInterceptors().add(new TemplateOutInterceptor());
    }
    
    @Override
    protected void prepareInInterceptors(){
        getInInterceptors().add(new TemplateInInterceptor(container));
    }
   /* @Override
    protected void prepareOutFaultInterceptors(){
        getOutFaultInterceptors().add(new TemplateOutFaultInterceptor());
    }*/
}
