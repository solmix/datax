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

import org.solmix.commons.util.DataUtils;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.FieldInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月25日
 */

public class DataServiceInfoSerializer extends JsonSerializer<DataServiceInfo>
{

    /**
     * {@inheritDoc}
     * 
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(DataServiceInfo value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString("isc.AdvanceDataSource.create(");
        jgen.writeStartObject();
        jgen.writeStringField("ID", value.getId());
        List<FieldInfo> fields =value.getFields();
        if(DataUtils.isNotNullAndEmpty(fields)){
            jgen.writeFieldName("fields");
            jgen.writeStartArray();
            for(FieldInfo f:fields){
                jgen.writeStartObject();
                jgen.writeStringField("name", f.getName());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();
        }
        jgen.writeEndObject();
        jgen.writeString(");\r\n");
    }
    @Override
    public Class<DataServiceInfo> handledType() { return DataServiceInfo.class; }
}
