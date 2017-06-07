/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

/**
 * @since 6.0.0
 */
public class TestCmsSetupBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSetupBean(String arg0) {

        super(arg0);
    }

    /**
     * Tests the method saveProperties.<p>
     *
     * @throws IOException if something goes wrong
     */
    public void testSaveProperties() throws IOException {

        CmsSetupBean bean = new CmsSetupBean();
        bean.init("", null, null);

        String testPropPath = "org/opencms/configuration/opencms-test.properties";
        URL url = this.getClass().getClassLoader().getResource(testPropPath);
        String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
        File input = new File(decodedPath);
        System.out.println("URL: '" + url + "'");
        System.out.println("URL path decoded: '" + decodedPath + "'");
        System.out.println("File: '" + input + "'");
        // make sure the test properties file is found
        assertTrue("Test property file '" + input.getAbsolutePath() + "' not found", input.exists());

        String inputFile = input.getAbsolutePath();
        String outputFile = input.getParent() + "/output.properties";

        System.out.println("Reading properties from " + inputFile);
        CmsParameterConfiguration oldProperties = new CmsParameterConfiguration(inputFile);

        System.out.println("Writing properties to " + outputFile);
        bean.copyFile(inputFile, outputFile);
        if (!bean.getErrors().isEmpty()) {
            for (String message : bean.getErrors()) {
                System.out.println(message);
            }
            assertTrue("There shouldn't be any errors copying the properties files", !bean.getErrors().isEmpty());
        }
        bean.saveProperties(oldProperties, outputFile, false);
        if (!bean.getErrors().isEmpty()) {
            for (String message : bean.getErrors()) {
                System.out.println(message);
            }
            assertTrue("There shouldn't be any errors saving the properties files", !bean.getErrors().isEmpty());
        }
        System.out.println("Checking properties from " + outputFile);
        CmsParameterConfiguration newProperties = new CmsParameterConfiguration(outputFile);

        for (String key : oldProperties.keySet()) {
            Object obj = oldProperties.getObject(key);

            String oldValue = "", newValue = "";
            if (obj instanceof List) {
                StringBuffer buf;

                List<?> l1 = (List<?>)obj;
                buf = new StringBuffer();
                for (Object o1 : l1) {
                    buf.append("[" + o1 + "]");
                }
                oldValue = buf.toString();

                buf = new StringBuffer();
                List<?> l2 = (List<?>)newProperties.getObject(key);
                for (Object o2 : l2) {
                    buf.append("[" + o2 + "]");
                }
                newValue = buf.toString();

            } else {
                oldValue = (String)obj;
                newValue = newProperties.get(key);
            }
            System.out.println("key  : " + key);
            System.out.println("read : " + oldValue);
            System.out.println("wrote: " + newValue);
            System.out.println("---");
            assertEquals(oldValue, newValue);
        }

        /*
        // clean up - remove generated file
        File output = new File(outputFile);
        output.delete();
        */
    }
}
