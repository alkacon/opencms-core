/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/CmsRegistryTest.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.file;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.0
 */
public class CmsRegistryTest extends TestCase {

    private static final String C_REGISTRY_PATH = "/opencms/test/com/opencms/file/registry.xml";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public CmsRegistryTest(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests the method getSubNodeValues.<p>
     */
    public void testGetSubNodeValues() {
        try {
            String registryPath = System.getProperty("user.dir") + C_REGISTRY_PATH;
            CmsRegistry registry = new CmsRegistry(registryPath);
            Element systemElement = registry.getSystemElement();
            
            Map test1 = registry.getSubNodeValues(systemElement, "testnode");
            
            // Test
            //  <key0></key0>
            assertEquals (test1.get("key0"), null);
            
            // Test 
            //  <key1>value1</key1>
            assertEquals (test1.get("key1"), "value1");
            
            // Test
            //  <key2>value2-1</key2>
            //  <key2>value2-2</key2>
            assertTrue ("Interface java.util.List expected", test1.get("key2") instanceof java.util.List);
            List l = (List)test1.get("key2");
            assertEquals((String)l.get(0), "value2-1");
            assertEquals((String)l.get(1), "value2-2");
            
            // Test
            //  <key3>value3-1</key3>
            //  <key3>value3-2</key3>
            //  <key3>value3-3</key3>
            assertTrue ("Interface java.util.List expected", test1.get("key3") instanceof java.util.List);
            l = (List)test1.get("key3");
            assertEquals((String)l.get(0), "value3-1");
            assertEquals((String)l.get(1), "value3-2");            
            assertEquals((String)l.get(2), "value3-3"); 
            
            // Test
            //  <key4>
            //      <key4-1>value4-1</key4-1>
            //      <key4-2>value4-2</key4-2>
            //  </key4>
            assertTrue ("Interface java.util.Map expected", test1.get("key4") instanceof java.util.Map);
            Map m = (Map)test1.get("key4");
            assertEquals(m.get("key4-1"), "value4-1");
            assertEquals(m.get("key4-2"), "value4-2");
            
            // Test
            // <key5>
            //    <key5-1>value5-1a</key5-1>
            //    <key5-2>value5-2a</key5-2>
            // </key5>
            // <key5>
            //    <key5-1>value5-1b</key5-1>
            //    <key5-2>value5-2b</key5-2>
            // </key5>
            assertTrue ("Interface java.util.List expected", test1.get("key5") instanceof java.util.List);            
            l = (List)test1.get("key5");
            assertTrue ("Interface java.util.Map expected", l.get(0) instanceof java.util.Map);
            m = (Map)l.get(0);
            assertEquals(m.get("key5-1"), "value5-1a");
            assertEquals(m.get("key5-2"), "value5-2a");
            assertTrue ("Interface java.util.Map expected", l.get(1) instanceof java.util.Map);
            m = (Map)l.get(1);
            assertEquals(m.get("key5-1"), "value5-1b");
            assertEquals(m.get("key5-2"), "value5-2b");
            
            
        } catch (Exception exc) {
            Assert.fail("Exception " + exc.toString());
        }
    }

}
