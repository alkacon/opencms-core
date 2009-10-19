/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/containerpage/Attic/TestCmsXmlContainerPage.java,v $
 * Date   : $Date: 2009/10/19 11:09:30 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML container pages.<p>
 *
 * @author Michael Moossen
 *  
 * @version $Revision: 1.1.2.1 $
 */
public class TestCmsXmlContainerPage extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContainerPage(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsXmlContainerPage.class.getName());

        suite.addTest(new TestCmsXmlContainerPage("testUnmarshall"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("cntpagesystemtest", "/");
                importData("cntpagetest", "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests unmarshalling a container page.
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshall() throws Exception {

        CmsObject cms = getCmsObject();
        CmsFile file = cms.readFile("containerpage/index.html");
        CmsXmlContainerPage cntPage = CmsXmlContainerPageFactory.unmarshal(cms, file);
        List<Locale> locales = cntPage.getLocales();
        assertEquals(1, locales.size());
        assertEquals(Locale.ENGLISH, locales.get(0));
    }
}