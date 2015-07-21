/*
 * Copyright 2012 The Solmix Project
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

package org.solmix.datax.validation;

import javax.xml.bind.ValidationEventLocator;

import org.solmix.datax.validation.ValidationEvent.Level;
import org.solmix.datax.validation.ValidationEvent.OutType;
import org.solmix.datax.validation.ValidationEvent.Status;

public class ValidationEventFactory
{

   private ValidationEventFactory()
   {

   }

   static ValidationEventFactory instance =  new ValidationEventFactory();;
   
   public static synchronized ValidationEventFactory instance()
   {
      return instance;
   }
   private Status status;

   private Level level;

   private String name;

   private String type;

   /**
    * @param type the type to set
    */
   public void setType( String type )
   {
      this.type = type;
   }

   private OutType outType;

   /**
    * @return the status
    */
   public Status getStatus()
   {
      return status;
   }

   /**
    * @param status the status to set
    */
   public void setStatus( Status status )
   {
      this.status = status;
   }

   /**
    * @return the level
    */
   public Level getLevel()
   {
      return level;
   }

   /**
    * @param level the level to set
    */
   public void setLevel( Level level )
   {
      this.level = level;
   }

   /**
    * @return the outType
    */
   public OutType getOutType()
   {
      return outType;
   }

   /**
    * @param outType the outType to set
    */
   public void setOutType( OutType outType )
   {
      this.outType = outType;
   }


   /**
    * @param name the name to set
    */
   public void setName( String name )
   {
      this.name = name;
   }

   /**
    * @param errorMssage validation error message
    * @return
    * @throws SlxException
    */
   public ValidationEvent create( String name, String errorMssage )
   {
      return create( name, errorMssage, null );

   }

   public ValidationEvent create( Level level, String errorMssage )
   {
      return create( outType, level, errorMssage );
   }

   public ValidationEvent create( OutType outType, Level level, String errorMssage )
   {
      return create( outType, level, name, errorMssage, null, null, null );
   }

   public ValidationEvent create( String name, String errorMssage, String sugest )
   {
      return create( outType, level, name, errorMssage, null, null, sugest );
   }

   public ValidationEvent create( OutType outType, Level level, String name, String errorMssage, String sugest )
   {
      return create( outType, level, name, errorMssage, null, null, sugest );
   }

   public ValidationEvent create( OutType outType, Level level, String name, String errorMssage, Throwable e )
   {
      return create( outType, level, name, errorMssage, e, null, null );
   }

   public ValidationEvent create( OutType outType, Level level, String name, String errorMssage, Throwable e, ValidationEventLocator locator,
      String sugest )
   {
      return _create( outType, level, name, errorMssage, e, locator, sugest, null );
   }

   public ValidationEvent create( OutType outType, Level level, String name, ErrorMessage erorObject, Throwable e, ValidationEventLocator locator,
      String sugest )
   {
      return _create( outType, level, name, null, e, locator, sugest, erorObject );
   }

   protected ValidationEvent _create( OutType outType, Level level, String name, String errorMssage, Throwable e, ValidationEventLocator locator,
      String sugest, ErrorMessage erorObject )
   {
      ;
      // level
      if ( level == null )
      {
         if ( this.level == null )
         {
            if ( e != null )
               level = Level.ERROR;
            else
               level = Level.DEBUG;
         } else
            level = this.level;
      }
      // error message
      ErrorMessage em=null;
      if ( errorMssage != null && sugest != null )
      {
          em= new ErrorMessage( errorMssage, sugest ) ;
      } else if ( errorMssage != null )
      {
          em= new ErrorMessage( errorMssage ) ;
      }
      if(erorObject==null){
          erorObject=em;
      }
      
      return  new ValidationEvent(level,outType,Status.NO_HANDLED,erorObject,e);
   }
}
