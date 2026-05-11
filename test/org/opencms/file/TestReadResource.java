/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit test for the "readFileHeader" method of the CmsObject to test the release and expiration date.<p>
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestReadResource extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    @Test
    @Order(10)
    public void testExcludeType() throws Throwable {

        CmsObject cms = getCmsObject();
        cms.createResource("/testExcludeType", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testExcludeType/alpha", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/testExcludeType/beta", CmsResourceTypeBinary.getStaticTypeId());
        List<CmsResource> r1 = cms.readResources(
            "/testExcludeType",
            CmsResourceFilter.IGNORE_EXPIRATION.addExcludeType(OpenCms.getResourceManager().getResourceType("binary")),
            true);

        List<CmsResource> r2 = cms.readResources(
            "/testExcludeType",
            CmsResourceFilter.IGNORE_EXPIRATION.addExcludeType(CmsResourceTypeBinary.getStaticTypeId()),
            true);

        assertEquals(1, r1.size());
        assertEquals(1, r2.size());
        assertEquals(r1.get(0).getRootPath(), r2.get(0).getRootPath());

    }

    /**
     * Test readFileHeader of a file after its expiration date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(3)
    public void testReadAfterExpirationDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date");
        readAfterExpirationDate(cms, "/folder1/page2.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file after its expiration date.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(6)
    public void testReadAfterExpirationDateIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date, ignoring valid timerange");
        readAfterExpirationDate(cms, "/folder1/page2.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readResource of a file before its release date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testReadBeforeReleaseDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date");
        readBeforeReleaseDate(cms, "/folder1/page1.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file before its release date.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(4)
    public void testReadBeforeReleaseDateIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date, ignoring valid timerange");
        readBeforeReleaseDate(cms, "/folder1/page1.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readFile with the structure id.<p>
     *
     * @throws Throwable if something is wrong
     */
    @Test
    @Order(9)
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

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testReadInValidTimeRange() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range");
        readInValidTimeRange(cms, "/folder1/page3.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     * The valid time range will be ignored.
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(5)
    public void testReadInValidTimeRangeIgnore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range, ignoring valid timerange");
        readInValidTimeRange(cms, "/folder1/page3.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readResource methods that use the structureId.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(7)
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
    @Test
    @Order(8)
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
     * Test readResource of a file after its expirationrelease date.<p>
     *
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    private void readAfterExpirationDate(CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {

        storeResources(cms, resource1);

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
     * Test readResource of a file before its release date.<p>
     *
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    private void readBeforeReleaseDate(CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {

        storeResources(cms, resource1);

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
     * Test readResource of a file in its valid time range.<p>
     *
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    private void readInValidTimeRange(CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {

        storeResources(cms, resource1);

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

}
