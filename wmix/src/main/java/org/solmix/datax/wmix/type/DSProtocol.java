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
package org.solmix.datax.wmix.type;

import org.solmix.datax.model.ValueEnum;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月18日
 */

public enum DSProtocol implements ValueEnum
{
    /**
     * 数据附加在dataURL上，例如：
     * http://service.com/search?keyword=foo
     */
    GETPARAMS( "getParams" ) ,
    /**
     * 数据POST到dataURL上，每个POST参数转化为HTTP参数，例如 HTML Form表单提交的数据。
     */
    POSTPARAMS( "postParams" ) ,
    /**
     * 数据序列化为XML格式并放入HTTP包体中，同时设置HTTP Content-Type=text/xml
     */
    POSTXML( "postXML" ) ,

    /**
     * 自定义
     */
    CLIENTCUSTOM( "clientCustom" ) ,

    /**
     * 同POSTXML，只是将序列化的xml通过SOAP封装。
     */
    SOAP( "soap" ) ,
    /**
     *将数据作为文本放入HTTP 包体中
     */
    POSTMESSAGE( "postMessage" );
    private String value;

    DSProtocol( String value )
    {
       this.value = value;
    }

    @Override
    public String value()
    {
       return this.value;
    }
    
    public static DSProtocol fromValue(String v) {
        for (DSProtocol c : DSProtocol.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
