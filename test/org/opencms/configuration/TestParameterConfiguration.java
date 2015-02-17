/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.configuration;

import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Test cases for the parameter configuration.<p>
 */
public class TestParameterConfiguration extends OpenCmsTestCase {

    /**
     * Tests escaping and unescaping values in the parameter configuration.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testEscapeUnescapeParameterConfiguration() throws Exception {

        CmsParameterConfiguration config = new CmsParameterConfiguration();

        config.add("test1", "test, eins");
        assertEquals("test, eins", config.get("test1"));

        config.add("test2", "test \\\\ zwei");
        assertEquals("test \\\\ zwei", config.get("test2"));

        config.add("test3", "test \\= drei");
        assertEquals("test \\= drei", config.get("test3"));

    }

    /**
     * Test merging the parameter configuration.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testMergeParameterConfiguration() throws Exception {

        CmsParameterConfiguration config1 = new CmsParameterConfiguration();
        String p = "testParam";
        config1.add(p, "1");
        config1.add(p, "2");
        config1.add(p, "3");
        config1.add("x", "y");

        CmsParameterConfiguration config2 = new CmsParameterConfiguration();
        config2.add(p, "a");
        config2.add(p, "b");
        config2.add(p, "c");
        config2.add("v", "w");

        config1.putAll(config2);

        assertEquals("1,2,3,a,b,c", config1.get(p));
        assertEquals(6, config1.getList(p).size());
        assertEquals("y", config1.get("x"));
        assertEquals("w", config1.get("v"));
    }

    /**
     * Test reading the parameter configuration.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testReadParameterConfiguration() throws Exception {

        String testPropPath = "org/opencms/configuration/opencms-test.properties";
        URL url = this.getClass().getClassLoader().getResource(testPropPath);
        String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
        File file = new File(decodedPath);
        System.out.println("URL: '" + url + "'");
        System.out.println("URL path decoded: '" + decodedPath + "'");
        System.out.println("File: '" + file + "'");
        // make sure the test properties file is found
        assertTrue("Test property file '" + file.getAbsolutePath() + "' not found", file.exists());

        CmsParameterConfiguration cmsProp = new CmsParameterConfiguration(file.getAbsolutePath());
        assertEquals("C:\\dev\\workspace\\opencms-core\\test\\data", cmsProp.get("test.path.one"));

        // test some of the more advanced features
        assertEquals(4, cmsProp.getList("test.list").size());
        assertEquals(3, cmsProp.getList("test.otherlist").size());
        assertEquals("comma, escaped with \\ backslash", cmsProp.get("test.escaping"));
        assertEquals("this is a long long long long long long line!", cmsProp.get("test.multiline"));

        // test compatibility with Collection Extended Properties
        ExtendedProperties extProp = new ExtendedProperties(file.getAbsolutePath());
        assertEquals(extProp.size(), cmsProp.size());
        for (String key : cmsProp.keySet()) {
            Object value = cmsProp.getObject(key);
            assertTrue("Key '" + key + "' not found in CmsConfiguration", extProp.containsKey(key));
            assertEquals("Objects for " + key + " not equal", extProp.getProperty(key), value);
        }
    }
}
