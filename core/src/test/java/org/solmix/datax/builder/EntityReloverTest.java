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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Test;
import org.solmix.commons.xml.XMLNode;
import org.solmix.commons.xml.XMLParser;
import org.solmix.datax.DATAX;
import org.solmix.datax.repository.builder.xml.DataServiceEntityResolver;


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
        XMLParser parser = new XMLParser(is, true, null,new DataServiceEntityResolver(),DATAX.NS);
        XMLNode node =parser.evalNode("datax/configuration");
        assertNotNull(node);
        assertEquals("http://www.solmix.org/schema/dataservice-1.0.0", node.getNode().getNamespaceURI());
    }

    @Test
    public void testPattern() {
         Pattern NAMESPACE_PATTERN = Pattern.compile("^([a-zA-Z]\\w+[.])*[a-zA-Z]\\w+");
         assertTrue(NAMESPACE_PATTERN.matcher("com.example").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com2.example.ssd").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com.exampl2e.ssd2").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com.exampl2e.ssd2.exampl2e.ssd2").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.2example.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.2ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.$ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.ss$d").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.exa$mple.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.exa mple.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.").matches());
    }
    
    @Test
    public void testRefPattern() {
         Pattern NAMESPACE_PATTERN = Pattern.compile("^([#a-zA-Z]\\w+[.])*[#]{0,2}[a-zA-Z]\\w+");
         assertTrue(NAMESPACE_PATTERN.matcher("#com.example").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("##com.example").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("##example").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com2.example.ssd").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com.exampl2e.ssd2").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("#ssd2").matches());
         assertTrue(NAMESPACE_PATTERN.matcher("com.exampl2e.ssd2.exampl2e.ssd2").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.2example.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.2ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.$ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.ss$d").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.exa$mple.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.exa mple.ssd").matches());
         assertFalse(NAMESPACE_PATTERN.matcher("com.example.").matches());
    }
}
