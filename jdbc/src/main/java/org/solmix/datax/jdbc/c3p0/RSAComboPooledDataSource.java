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

    private String publicKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCC5rAsApe+inUuzq8oqSN/HsPaGOEaQ6rHdrHgKtHr7kvFPBBkSMUFXdGJGwvT3h7eRfH0q2Yr24OrNfnYZQH3tN7UxbFtmB/ULbBjCzhCr9iIZZ/NJjNlJ682PCIrlEQ9ytv3akw607piAsq7dZJZmCebuSV08dosygy/ybyipQIDAQAB";
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
    public void setJdbcUrl( String jdbcUrl )
    { 
        try {
           byte[] decodedata= RSAUtils.decryptByPublicKey( Base64Utils.decode(jdbcUrl), publicKey);
            super.setJdbcUrl(new String(decodedata));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    @Override
    public void setUser( String jdbcUrl )
    { 
        try {
            byte[] decodedata= RSAUtils.decryptByPublicKey(Base64Utils.decode(jdbcUrl), publicKey);
             super.setUser(new String(decodedata));
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    @Override
    public void setPassword( String jdbcUrl )
    { 
        try {
            byte[] decodedata= RSAUtils.decryptByPublicKey(Base64Utils.decode(jdbcUrl), publicKey);
             super.setPassword(new String(decodedata));
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    
    
    

}
