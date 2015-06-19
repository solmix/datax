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
package org.solmix.datax.builder;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.XMLParser;
import org.solmix.datax.builder.xml.DataServiceEntityResolver;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月17日
 */

public class EntityReloverTest
{

    @Test
    public void test() {
        InputStream is=EntityReloverTest.class.getResourceAsStream("services.xml");
        XMLParser parser = new XMLParser(is, true, null,new DataServiceEntityResolver());
        XMLNode node =parser.evalNode("/ds:datax/ds:service");
       Assert.assertNotNull(node);
       Assert.assertEquals("http://www.solmix.org/schema/dataservice-1.0.0", node.getNode().getNamespaceURI());
    }

}
