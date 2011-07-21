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

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Test cases for the parameter configuration.<p>
 */
public class TestParameterConfiguration extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestParameterConfiguration(String arg0) {

        super(arg0);
    }

    /**
     * Test reading the parameter configuration.<p>
     * 
     * @throws Exception
     */
    public void testReadParameterConfiguration() throws Exception {

        String testPropPath = "org/opencms/configuration/opencms-test.properties";
        URL url = this.getClass().getClassLoader().getResource(testPropPath);
        File file = new File(url.getPath());
        System.out.println("URL: " + url);
        System.out.println("File: " + file);
        // make sure the test properties file is found
        assertTrue("Test property file '" + file.getAbsolutePath() + "' not found", file.exists());

        CmsParameterConfiguration cmsProp = new CmsParameterConfiguration(file.getAbsolutePath());
        assertEquals("C:\\dev\\workspace\\opencms-core\\test\\data", cmsProp.getString("test.path.one"));

        // test some of the more advanced features
        assertEquals(4, cmsProp.getList("test.list").size());
        assertEquals(3, cmsProp.getList("test.otherlist").size());
        assertEquals("comma, escaped with \\ backslash", cmsProp.getString("test.escaping"));
        assertEquals("this is a long long long long long long line!", cmsProp.getString("test.multiline"));

        // test compatibility with Collection Extended Properties
        ExtendedProperties extProp = new ExtendedProperties(file.getAbsolutePath());
        assertEquals(extProp.size(), cmsProp.size());
        for (String key : cmsProp.getParameters()) {
            Object value = cmsProp.getObject(key);
            assertTrue("Key '" + key + "' not found in CmsConfiguration", extProp.containsKey(key));
            assertTrue("Objects for '" + key + "' not equal", value.equals(extProp.getProperty(key)));
        }
    }

    /**
     * Test merging the parameter configuration.<p>
     * 
     * @throws Exception
     */
    public void testMergeParameterConfiguration() throws Exception {

        CmsParameterConfiguration config1 = new CmsParameterConfiguration();
        String p = "testParam";
        config1.addParameter(p, "1");
        config1.addParameter(p, "2");
        config1.addParameter(p, "3");
        config1.addParameter("x", "y");

        CmsParameterConfiguration config2 = new CmsParameterConfiguration();
        config2.addParameter(p, "a");
        config2.addParameter(p, "b");
        config2.addParameter(p, "c");
        config2.addParameter("v", "w");

        config1.merge(config2);

        assertEquals("1,2,3,a,b,c", config1.getString(p));
        assertEquals(6, config1.getList(p).size());
        assertEquals("y", config1.getString("x"));
        assertEquals("w", config1.getString("v"));
    }

    /**
     * Tests escaping and unescaping values in the parameter configuration.<p>
     * 
     * @throws Exception
     */
    public void testEscapeUnescapeParameterConfiguration() throws Exception {

        CmsParameterConfiguration config = new CmsParameterConfiguration();

        config.addParameter("test1", "test, eins");
        assertEquals("test, eins", config.getString("test1"));

        config.addParameter("test2", "test \\\\ zwei");
        assertEquals("test \\\\ zwei", config.getString("test2"));

        config.addParameter("test3", "test \\= drei");
        assertEquals("test \\= drei", config.getString("test3"));

    }
}
