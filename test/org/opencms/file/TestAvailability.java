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

package org.opencms.file;

import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "setDateExpired" and "setDateReleased" method of the CmsObject.<p>
 */
public class TestAvailability extends OpenCmsTestCase {

    private static final long MSECS_PER_DAY = 1000 * 60 * 60 * 12;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestAvailability(String arg0) {

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
        suite.setName(TestAvailability.class.getName());

        suite.addTest(new TestAvailability("testDateReleased"));
        suite.addTest(new TestAvailability("testDateExpired"));
        suite.addTest(new TestAvailability("testFolderDateReleased"));
        suite.addTest(new TestAvailability("testFolderDateExpired"));
        suite.addTest(new TestAvailability("testSubFolderDateReleased"));
        suite.addTest(new TestAvailability("testSubFolderDateExpired"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test to set release date on a resource.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set expire date");

        String resourceName = "/index.html";

        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(resourceName);
        cms.setDateExpired(resourceName, yesterday, false);
        cms.unlockResource(resourceName);

        testOutsideTimeRange(cms, resourceName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(resourceName);
        cms.undoChanges(resourceName, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resourceName);
    }

    /**
     * Test to set release date on a resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set release date");

        String resourceName = "/index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(resourceName);
        cms.setDateReleased(resourceName, tomorrow, false);
        cms.unlockResource(resourceName);

        testOutsideTimeRange(cms, resourceName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(resourceName);
        cms.undoChanges(resourceName, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resourceName);
    }

    /**
     * Test to set expired date on a folder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFolderDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing expire date in a folder");

        String folderName = "/folder1/";
        String resName = folderName + "index.html";
        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateExpired(folderName, yesterday, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Test to set release date on a folder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFolderDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing release date in a folder");

        String folderName = "/folder1/";
        String resName = folderName + "index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateReleased(folderName, tomorrow, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Test to set expired date on a subfolder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSubFolderDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing expire date in a folder");

        String folderName = "/folder1";
        String folderName2 = "/subfolder11";
        String resName = "/index.html";
        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateExpired(folderName, yesterday, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, folderName + resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, folderName + folderName2 + resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Test to set release date on a subfolder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSubFolderDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing release date in a subfolder");

        String folderName = "/folder1";
        String folderName2 = "/subfolder11";
        String resName = "/index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateReleased(folderName, tomorrow, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, folderName + resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, folderName + folderName2 + resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    private void testOutsideTimeRange(CmsObject cms, String resourceName, long released, long expired)
    throws CmsException {

        try {
            // should throw exception
            cms.readResource(resourceName, CmsResourceFilter.DEFAULT);
            fail("Read outside-of-time-range resource with filter CmsResourceFilter.DEFAULT");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        CmsResource resource;
        try {
            resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            fail("Unable to read outside-of-time-range resource with filter CmsResourceFilter.ALL");
            return;
        }
        assertEquals(released, resource.getDateReleased());
        assertEquals(expired, resource.getDateExpired());
        assertEquals(cms.getRequestContext().getCurrentProject().getUuid(), resource.getProjectLastModified());
    }
}
