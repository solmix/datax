/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.datax;


/**
 * 运行异常。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public class DataxRuntimeException extends RuntimeException
{

    private static final long serialVersionUID = 4014907223048817109L;

    public DataxRuntimeException()
    {

    }

    /**
     * @param string
     * @param e
     */
    public DataxRuntimeException(String string, Throwable e)
    {
        super(string, e);
    }

    public DataxRuntimeException(String string)
    {
        super(string);
    }

    /**
     * @param e
     */
    public DataxRuntimeException(Throwable e)
    {
        super(e);
    }

}
