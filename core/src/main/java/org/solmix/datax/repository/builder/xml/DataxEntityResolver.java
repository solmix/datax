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

package org.solmix.datax.repository.builder.xml;

import java.io.IOException;
import java.net.URL;

import org.solmix.commons.util.ClassLoaderUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月17日
 */

public class DataxEntityResolver implements EntityResolver
{

    public static final String DATASERVICE_XSD = "org/solmix/datax/model/datax-ds-1.0.0.xsd";

    public static final String RULE_XSD =       "org/solmix/datax/rule/datax-rule-1.0.0.xsd";

    public static final String DATASERVICE_ID = "http://www.solmix.org/schema/datax-ds/v1.0.0";

    public static final String RULE_ID =        "http://www.solmix.org/schema/datax-rule/v1.0.0";

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        URL url = null;
        if (DATASERVICE_ID.equals(systemId)) {
            url = ClassLoaderUtils.getResource(DATASERVICE_XSD, DataxEntityResolver.class);
        } else if (RULE_ID.equals(systemId)) {
            url = ClassLoaderUtils.getResource(RULE_XSD, DataxEntityResolver.class);
        }
        if (url == null) {
            throw new IllegalArgumentException("Can't found validate XSD for systemId:" + systemId);
        }
        InputSource in = new InputSource(url.openStream());
        return in;
    }

}
