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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for {@link org.opencms.file.CmsResourceFilter}.<p>
 */
public class TestResourceFilter extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestResourceFilter(String arg0) {

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
        suite.setName(TestResourceFilter.class.getName());

        suite.addTest(new TestResourceFilter("testAddRequireFolder"));

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
     * Tests the given filter if it only returns folders.<p>
     *
     * @param folderFilter the filter that is excpected to only let through folders.
     *
     * @throws CmsException if access to test resources from VFS fails.
     *
     */
    public void assertFilterFolderOnly(CmsResourceFilter folderFilter) throws CmsException {

        CmsObject cms = getCmsObject();
        List folders = cms.readResources("/", folderFilter);
        assertNotNull(folders);
        assertTrue("Zero folders in test system found. ", folders.size() > 0);

        int resourceTypeFolder = CmsResourceTypeFolder.RESOURCE_TYPE_ID;
        CmsResource resource;
        Iterator itResources = folders.iterator();
        while (itResources.hasNext()) {
            resource = (CmsResource)itResources.next();
            assertTrue(
                "Filter let a resource of type "
                    + OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName()
                    + " pass: "
                    + resource.getRootPath(),
                resourceTypeFolder == resource.getTypeId());
        }

    }

    /**
     * Tests filters that should only allow folders (obtained by
     * {@link CmsResourceFilter#addRequireFolder()}).<p>
     *
     * @throws Exception if the test fails
     */
    public void testAddRequireFolder() throws Exception {

        CmsResourceFilter filterFolder = CmsResourceFilter.ALL.addRequireFolder();

        echo("Testing if only folders pass the resource filter CmsResourceFilter.ALL.addRequireFolder()");
        assertFilterFolderOnly(filterFolder);

        filterFolder = CmsResourceFilter.ALL.addRequireFolder().addExcludeState(CmsResource.STATE_DELETED);

        echo(
            "Testing if only folders pass the resource filter CmsResourceFilter.ALL.addRequireFolder().addExcludeState(CmsResourceState.STATE_DELETED)");
        assertFilterFolderOnly(filterFolder);
    }
}