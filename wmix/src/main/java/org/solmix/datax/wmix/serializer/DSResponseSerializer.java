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
package org.solmix.datax.wmix.serializer;

import java.io.IOException;
import java.util.List;

import org.solmix.datax.DSResponse;
import org.solmix.datax.attachment.Pageable;
import org.solmix.datax.wmix.Constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月19日
 */

public class DSResponseSerializer extends JsonSerializer<ResultObject>
{

    @Override
    public void serialize(ResultObject response, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if(response.isDSResponse()){
            jgen.writeStartObject();
            jgen.writeFieldName("response");
            serialize((DSResponse)response.getReal(),jgen,provider);
            jgen.writeEndObject();
        }else if(response.isDSResponseList()){
            List<DSResponse> res = (List<DSResponse>)response.getReal();
            jgen.writeStartObject();
            jgen.writeArrayFieldStart("responses");
            for(DSResponse re:res){
                serialize(re,jgen,provider);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
            
        }else{
            jgen.writeObject(response.getReal());
        }
       
    }
    
    public void serialize(DSResponse response, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
       
        jgen.writeStartObject();
        jgen.writeNumberField("status", response.getStatus().value());
        jgen.writeBooleanField("isDSResponse", true);
        if(response.getAffectedRows()!=null){
            jgen.writeNumberField("affectedRows", response.getAffectedRows());
        }
        Object invalidate = response.getAttribute(Constants.INVALIDATE_CACHE);
        Pageable page = response.getAttachment(Pageable.class);
        if(invalidate!=null){
            jgen.writeBooleanField("invalidateCache", Boolean.valueOf(invalidate.toString()));
        }
        if(page!=null){
            jgen.writeNumberField("startRow", page.getStartRow());
            jgen.writeNumberField("endRow", page.getEndRow());
            jgen.writeNumberField("totalRows", page.getTotalRow());
        }
        Object o =response.getRawData();
        if(o!=null){
            jgen.writeObjectField("data", o);
        }
        Object[] errors = response.getErrors();
        if(errors!=null&&errors.length>0){
            jgen.writeObjectField("errors",errors);
        }
        jgen.writeEndObject();
       
    }
    @Override
    public Class<ResultObject> handledType() { return ResultObject.class; }
}
