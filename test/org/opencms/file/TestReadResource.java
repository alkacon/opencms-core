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
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "readFileHeader" method of the CmsObject to test the release and expiration date.<p>
 *
 */
public class TestReadResource extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestReadResource(String arg0) {

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
        suite.setName(TestReadResource.class.getName());

        suite.addTest(new TestReadResource("testReadBeforeReleaseDate"));
        suite.addTest(new TestReadResource("testReadInValidTimeRange"));
        suite.addTest(new TestReadResource("testReadAfterExpirationDate"));
        suite.addTest(new TestReadResource("testReadBeforeReleaseDateIgnore"));
        suite.addTest(new TestReadResource("testReadInValidTimeRangeIgnore"));
        suite.addTest(new TestReadResource("testReadAfterExpirationDateIgnore"));
        suite.addTest(new TestReadResource("testReadWithResourceID"));
        suite.addTest(new TestReadResource("testReadWithWrongResourceID"));
        suite.addTest(new TestReadResource("testReadFileWithResourceID"));

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
     * Test readResource of a file before its release date.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readBeforeReleaseDate(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsResourceFilter filter) throws Throwable {

        tc.storeResources(cms, resource1);

        // preperation, modify the release date
        CmsFile preperationRes = cms.readFile(resource1, CmsResourceFilter.ALL);
        // set the release date to one hour in the future
        preperationRes.setDateReleased(System.currentTimeMillis() + (60 * 60 * 1000));

        cms.lockResource(resource1);
        cms.writeFile(preperationRes);
        cms.unlockResource(resource1);

        // now try to access the resource
        try {
            cms.readResource(resource1, filter);
            if (!filter.includeDeleted()) {
                // the file could be read, despite the release date set in the future
                fail("Resource " + resource1 + " could be read before release date");
            }
        } catch (CmsException e) {
            if (filter.includeDeleted()) {
                fail("Resource " + resource1 + " could not be read");
            }
        }
    }

    /**
     * Test readResource of a file after its expirationrelease date.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readAfterExpirationDate(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsResourceFilter filter) throws Throwable {

        tc.storeResources(cms, resource1);

        // preperation, modify the expiration date
        CmsFile preperationRes = cms.readFile(resource1, CmsResourceFilter.ALL);
        // set the expiration date to one hour in the past
        preperationRes.setDateExpired(System.currentTimeMillis() - (60 * 60 * 1000));

        cms.lockResource(resource1);
        cms.writeFile(preperationRes);
        cms.unlockResource(resource1);

        // now try to access the resource
        try {
            cms.readResource(resource1, filter);
            if (!filter.includeDeleted()) {
                // the file could be read, despite the expiration date was set to the past
                fail("Resource " + resource1 + " could be read after the expiration date");
            }
        } catch (CmsException e) {
            if (filter.includeDeleted()) {
                fail("Resource " + resource1 + " could not be read");
            }
        }
    }

    /**
     * Test readResource of a file in its valid time range.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readInValidTimeRange(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsResourceFilter filter) throws Throwable {

        tc.storeResources(cms, resource1);

        // preperation, modify the expiration date
        CmsFile preperationRes = cms.readFile(resource1, CmsResourceFilter.ALL);
        // set the release date to one hour in the future
        preperationRes.setDateReleased(System.currentTimeMillis() - (60 * 60 * 1000));
        // set the expiration date to one hour in the past
        preperationRes.setDateExpired(System.currentTimeMillis() + (60 * 60 * 1000));

        cms.lockResource(resource1);
        cms.writeFile(preperationRes);
        cms.unlockResource(resource1);

        // now try to access the resource
        try {
            cms.readResource(resource1, filter);
        } catch (CmsException e) {
            fail("Resource " + resource1 + " could not be read");
        }
    }

    /**
     * Test readResource of a file before its release date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadBeforeReleaseDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date");
        readBeforeReleaseDate(this, cms, "/folder1/page1.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file after its expiration date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadAfterExpirationDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date");
        readAfterExpirationDate(this, cms, "/folder1/page2.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadInValidTimeRange() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range");
        readInValidTimeRange(this, cms, "/folder1/page3.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file before its release date.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadBeforeReleaseDateIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date, ignoring valid timerange");
        readBeforeReleaseDate(this, cms, "/folder1/page1.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readFileHeader of a file after its expiration date.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadAfterExpirationDateIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date, ignoring valid timerange");
        readAfterExpirationDate(this, cms, "/folder1/page2.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadInValidTimeRangeIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range, ignoring valid timerange");
        readInValidTimeRange(this, cms, "/folder1/page3.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readResource methods that use the structureId.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadWithResourceID() throws Throwable {

        CmsObject cms = getCmsObject();
        String path = "/folder1/subfolder11/index.html";

        CmsResource resourceByPath = cms.readResource(path);
        CmsUUID strId = resourceByPath.getStructureId();
        CmsResource resourceById = cms.readResource(strId);

        // compare
        assertEquals(resourceByPath.getRootPath(), resourceById.getRootPath());
        assertEquals(resourceByPath.getName(), resourceById.getName());
        assertEquals(resourceByPath.isFile(), resourceById.isFile());
        assertEquals(resourceByPath.isFolder(), resourceById.isFolder());
        assertEquals(resourceByPath.isInternal(), resourceById.isInternal());
        assertEquals(resourceByPath.isLabeled(), resourceById.isLabeled());
        assertEquals(resourceByPath.isTouched(), resourceById.isTouched());
        assertEquals(resourceByPath.getDateCreated(), resourceById.getDateCreated());
        assertEquals(resourceByPath.getDateExpired(), resourceById.getDateExpired());
        assertEquals(resourceByPath.getDateLastModified(), resourceById.getDateLastModified());
        assertEquals(resourceByPath.getDateReleased(), resourceById.getDateReleased());
        assertEquals(resourceByPath.getFlags(), resourceById.getFlags());
        assertEquals(resourceByPath.getLength(), resourceById.getLength());
        assertEquals(resourceByPath.getProjectLastModified(), resourceById.getProjectLastModified());
        assertEquals(resourceByPath.getResourceId(), resourceById.getResourceId());
        assertEquals(resourceByPath.getSiblingCount(), resourceById.getSiblingCount());
        assertEquals(resourceByPath.getState(), resourceById.getState());
        assertEquals(resourceByPath.getStructureId(), resourceById.getStructureId());
        assertEquals(resourceByPath.getTypeId(), resourceById.getTypeId());
        assertEquals(resourceByPath.getUserCreated(), resourceById.getUserCreated());
        assertEquals(resourceByPath.getUserLastModified(), resourceById.getUserLastModified());
        assertEquals(resourceByPath.hashCode(), resourceById.hashCode());
    }

    /**
     * Test readResource whether an incorrect structureID throws an exception.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadWithWrongResourceID() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUUID wrongId = new CmsUUID("a7b5d298-b3ab-11d8-b3e3-514d35713fed");
        try {
            cms.readResource(wrongId);
            fail("The Id is correct");
        } catch (Exception e) {
            // expected
        }
    }

    /**
     * Test readFile with the structure id.<p>
     *
     * @throws Throwable if something is wrong
     */
    public void testReadFileWithResourceID() throws Throwable {

        String path = "/folder1/subfolder11/index.html";
        CmsObject cms = getCmsObject();
        OpenCms.getPublishManager().waitWhileRunning();
        assertTrue(cms.readFile(path).getState().isUnchanged());
        cms.lockResource(path);
        cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        try {
            cms.readFile(path);
            fail("file could be read");
        } catch (CmsException e) {
            //expected
        }

        cms.readFile(path, CmsResourceFilter.ALL);
        cms.unlockResource(path);
        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();
        try {
            cms.readFile(path, CmsResourceFilter.ALL);
            fail("file could be read");
        } catch (CmsException e) {
            //expected
        }

    }

}
