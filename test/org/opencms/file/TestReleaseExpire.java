/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestReleaseExpire.java,v $
 * Date   : $Date: 2005/10/10 16:11:08 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "setDateExpired" and "setDateReleased" method of the CmsObject.<p>
 * 
 * @author Jan Baudisch
 * @version $Revision: 1.2 $
 */
public class TestReleaseExpire extends OpenCmsTestCase {

    private int m_msecPerDay = 1000 * 60 * 60 * 12;

    private String m_resourceName = "/index.html";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestReleaseExpire(String arg0) {

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
        suite.setName(TestReleaseExpire.class.getName());

        suite.addTest(new TestReleaseExpire("testSetDateReleased"));
        suite.addTest(new TestReleaseExpire("testSetDateExpired"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test to set relase date on a resource.<p>
     * 
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT
     * @throws Throwable if something goes wrong
     */
    public void testSetDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set expire date");

        long yesterday = System.currentTimeMillis() - m_msecPerDay;
        cms.lockResource(m_resourceName);
        cms.setDateExpired(m_resourceName, yesterday, false);
        cms.unlockResource(m_resourceName);

        CmsResource resource = testOutsideTimeRange(cms);
        assertEquals(resource.getDateExpired(), yesterday);
    }

    /**
     * Test to set relase date on a resource.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSetDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set relase date");

        long tomorrow = System.currentTimeMillis() + m_msecPerDay;
        cms.lockResource(m_resourceName);
        cms.setDateReleased(m_resourceName, tomorrow, false);
        cms.unlockResource(m_resourceName);

        CmsResource resource = testOutsideTimeRange(cms);
        assertEquals(resource.getDateReleased(), tomorrow);
    }

    private CmsResource testOutsideTimeRange(CmsObject cms) throws CmsException {

        boolean coughtException = false;
        // should throw exception  
        try {
            cms.readResource(m_resourceName, CmsResourceFilter.DEFAULT);
        } catch (CmsVfsResourceNotFoundException e) {
            coughtException = true;
        }
        if (!coughtException) {
            fail("Read invisible resource with filter CmsResourceFilter.ONLY_VISIBLE");
        }

        try {
            return cms.readResource(m_resourceName, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            fail("Unable to read invisible resource with filter CmsResourceFilter.ALL");
        }
        return null;
    }
}
