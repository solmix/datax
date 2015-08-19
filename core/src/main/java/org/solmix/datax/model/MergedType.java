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
package org.solmix.datax.model;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年8月19日
 */

public enum MergedType implements ValueEnum
{
    /**
     * 简单合并
     */
    SIMPLE( "simple" ) ,
    /**
     * 包装
     */
    WRAPPED( "wrapped" ),
    /**
     * 将结果合并为Map，key为operationId，value为DSResonse
     * <b>NOTE:不适用于DataService配置</b>
     */
    MAPED( "maped" ),
    /**
     * 
     */
    ARRAY( "array" ),
    ;

   private String value;

   MergedType( String value )
   {
      this.value = value;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.solmix.api.types.ValueEnum#getValue()
    */
   @Override
   public String value()
   {
      return value;
   }
   public static MergedType fromValue(String v) {
       for (MergedType c : MergedType.values()) {
           if (c.value.equals(v)) {
               return c;
           }
       }
       throw new IllegalArgumentException(v);
   }
}
