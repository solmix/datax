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
package org.solmix.datax.validation;

import java.util.Hashtable;
import java.util.Map;

import org.solmix.datax.DATAX;
import org.solmix.runtime.event.BaseEvent;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月18日
 */

public  class ValidationEvent extends BaseEvent
{
    public enum Status
    {
       HANDLED( 0 ) , NO_HANDLED( 1 );

       private final int value;

       Status( int value )
       {
          this.value = value;
       }

       public int value()
       {
          return value;
       }
    }

    public enum Level
    {
       DEBUG( 0 ) , WARNING( 1 ) , ERROR( 2 ) , ;

       private final int value;

       Level( int value )
       {
          this.value = value;
       }

       public int value()
       {
          return value;
       }
    }

    public enum OutType
    {
       UN_SET( 0 ) , SERVER( 1 ) , CLIENT( 2 ) , ;

       private final int value;

       OutType( int value )
       {
          this.value = value;
       }

       public int value()
       {
          return value;
       }
    }
    public static final String ERROR_MESSAGE = "errorMessage";

    public static final String OUT_TYPE = "outType";

    public static final String EXCEPTION = "exception";

    public static final String STATUS = "status";

    public static final String NAME = "name";

    public static final String LEVEL = "level";
    public ValidationEvent( Level severity, String msg )
    {
       this( severity, msg, null, null, null );

    }

    public ValidationEvent( Level severity, ErrorMessage msg )
    {
       this( severity, msg, null, null );
    }

    public ValidationEvent( Level severity, String msg, Throwable e, ValidationEventLocator locator, String sugest )
    {
        this(severity,OutType.UN_SET,Status.HANDLED,new ErrorMessage( msg, sugest ),e);
    }

    public ValidationEvent( Level severity, ErrorMessage msg, Throwable e )
    {
       this( severity, msg, e, null );
    }

    public ValidationEvent( Level severity, ErrorMessage msg, Throwable e, ValidationEventLocator locator )
    {
        this(severity,OutType.UN_SET,Status.HANDLED,msg,e);
    }

    public ValidationEvent( Level severity, String msg, Throwable e )
    {
       this( severity, msg, e, null );
    }

    public ValidationEvent( Level severity, String msg, Throwable e, ValidationEventLocator locator )
    {
       this( severity, msg, e, locator, null );
    }
    /**
     * @param topic
     * @param properties
     */
    public ValidationEvent(Level level,OutType outType,Status value,ErrorMessage errorMessage,Throwable exception)
    {
        super(getValidationTopic(level), setup(level,outType,value,errorMessage,exception));
    }
    
    private static Map<String, ?> setup(Level level, OutType outType, Status value, ErrorMessage errorMessage, Throwable exception) {
        Map<String, Object> properties = new Hashtable<String, Object>();
        if (errorMessage != null)
            properties.put(ERROR_MESSAGE, errorMessage);
        if (outType!= null)
            properties.put(OUT_TYPE, outType);
        if (exception != null)
            properties.put(EXCEPTION, exception);
        if (value != null)
            properties.put(STATUS,value);
        if (level != null)
            properties.put(LEVEL, level);
        return properties;
    }

    /**
     * @return
     */
    private static String getValidationTopic(Level level) {
        String topic = DATAX.VALIDATION_TOPIC_PREFIX;
        switch (level) {
            case DEBUG:
                topic = topic + "DEBUG";
                break;
            case WARNING:
                topic = topic + "WARNING";
                break;
            case ERROR:
                topic = topic + "ERROR";
                break;
            default:
                topic = topic + "DEFAULT";

        }
        return topic;
    }

}
