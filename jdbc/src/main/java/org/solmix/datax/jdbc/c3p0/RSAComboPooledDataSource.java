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
package org.solmix.datax.jdbc.c3p0;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.solmix.commons.util.Base64Utils;
import org.solmix.commons.util.RSAUtils;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年10月26日
 */

public class RSAComboPooledDataSource extends AbstractComboPooledDataSource
{

    private String privateKey ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAILmsCwCl76KdS7OryipI38ew9oY4RpDqsd2seAq0evuS8U8EGRIxQVd0YkbC9PeHt5F8fSrZivbg6s1+dhlAfe03tTFsW2YH9QtsGMLOEKv2Ihln80mM2UnrzY8IiuURD3K2/dqTDrTumICyrt1klmYJ5u5JXTx2izKDL/JvKKlAgMBAAECgYBRNfOoajdgdB/9WSccT8sA68JQRc0p8T87nmz+iTJRcDa79+angOoSyUDdEdWFrTFzbuuMguXRYc/PYZ5O3WOZObifoHvaGP/hDW7S8+oVuK1IFEnveM+33r2EzceAnpKR4FtfSWq7A4ziEoY1PujpxZWTBDD/dzCdRvwgRK+7QQJBAME+wyCNkBOu5BKYnIloMS9f0KSDDkk/srca/fBUEjSZbZhRUvETRXEoYeOjxuTf2hvFjiGNAhSHx/0OOia13zUCQQCtaQS5diDx7Vh26x6ugn6MZYwUb+Bvt35+HqvjLzD9k5PELDaZQf9MxfWsceny79ttWgWFloyj6XanOGokZvOxAkEAq44RYmPqhV7dARlU1rOV/q28J2BlnWecO+wNhn7MTr/qyK9hx71JB8VG6fWqi+Oi2MbQgD6TmzBTvfcUbutFBQJAL3gUBwDDO/aQxNzP5U1rfts9YUrO0UYVpkiXHPWKH6AKTyUbPRDH5ig6fB4iwJHQKzr9T/hKP4RlKplS1OwpwQJAZVYVCwYuNiWv45fugTZzOD6viDPh/Pbd3cC/BYaw4TdnBXV6KzNQjsghd5vXsgyVbt4kCDn37Cj90KVriuuQqg==";
    public RSAComboPooledDataSource()
    { super(); }

    public RSAComboPooledDataSource( boolean autoregister )
    { super( autoregister ); }

    public RSAComboPooledDataSource(String configName)
    { super( configName );  }


    private static final long serialVersionUID = 1;
    private static final short VERSION = 0x0002;

    private void writeObject( ObjectOutputStream oos ) throws IOException
    {
        oos.writeShort( VERSION );
    }

    private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException
    {
        short version = ois.readShort();
        switch (version)
        {
        case VERSION:
          //ok
            break;
        default:
            throw new IOException("Unsupported Serialized Version: " + version);
        }
    }
   
    @Override
    public void setPassword( String password )
    { 
        try {
            byte[] decodedata= RSAUtils.decryptByPrivateKey(Base64Utils.decode(password), privateKey);
             super.setPassword(new String(decodedata));
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    
    
    

}
