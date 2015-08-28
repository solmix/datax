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

import java.util.Map;

import org.solmix.commons.util.TransformUtils;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.attachment.OldValuesBean;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月27日
 */

public class OldValuesTransformer extends TransformerAdaptor
{

    /**
     * 处理情况为：
     * 在修改时，修改前的参数在oldValues中，需改的在values中
     * 需要回传的值为修改成功后的值，即用修改的值覆盖，修改前的。
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object transformResponse(Object responseData, DSResponse response,DSRequest req) throws Exception {
        
        OldValuesBean old=  req.getAttachment(OldValuesBean.class);
        if(old!=null){
            Map oldmap=TransformUtils.transformType( Map.class,old.getOldValues());
            if(oldmap!=null){
                oldmap.putAll(req.getValues());
            }
        }
        return null;
    }

}
