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

package org.opencms.setup;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.TestParameterConfiguration;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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

        String testPropPath = "org/opencms/configuration/";

        File input = new File(this.getClass().getClassLoader().getResource(
            testPropPath + "opencms-test.properties").getPath());
        assertTrue("Checking if test properties file is present", input.exists());
        String inputFile = input.getAbsolutePath();
        String outputFile = input.getParent() + "/output.properties";

        System.out.println("Reading properties from " + inputFile);
        CmsParameterConfiguration oldProperties = new CmsParameterConfiguration(inputFile);

        System.out.println("Writing properties to " + outputFile);
        bean.copyFile(inputFile, outputFile);
        if (!bean.getErrors().isEmpty()){
        	for (String message : bean.getErrors()){
        		System.out.println(message);
        	}
        	assertTrue("There shouldn't be any errors copying the properties files",!bean.getErrors().isEmpty());
        }
        bean.saveProperties(oldProperties, outputFile, false);
        if (!bean.getErrors().isEmpty()){
        	for (String message : bean.getErrors()){
        		System.out.println(message);
        	}
        	assertTrue("There shouldn't be any errors saving the properties files",!bean.getErrors().isEmpty());
        }
        System.out.println("Checking properties from " + outputFile);
        CmsParameterConfiguration newProperties = new CmsParameterConfiguration(outputFile);

        for (String key : oldProperties.keySet()) {
            Object obj = oldProperties.getObject(key);

            String oldValue = "", newValue = "";
            if (obj instanceof List) {
                StringBuffer buf;

                buf = new StringBuffer();
                for (Iterator j = ((List)obj).iterator(); j.hasNext();) {
                    buf.append("[" + (String)j.next() + "]");
                }
                oldValue = buf.toString();

                buf = new StringBuffer();
                for (Iterator j = ((List)newProperties.getObject(key)).iterator(); j.hasNext();) {
                    buf.append("[" + (String)j.next() + "]");
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

        // clean up - remove generated file
        File output = new File(outputFile);
        // output.delete();
    }
}
