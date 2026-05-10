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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit test for {@link org.opencms.file.CmsResourceFilter}.<p>
 */
public class TestResourceFilter extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests the given filter if it only returns folders.<p>
     *
     * @param folderFilter the filter that is excpected to only let through folders.
     *
     * @throws CmsException if access to test resources from VFS fails.
     */
    public void assertFilterFolderOnly(CmsResourceFilter folderFilter) throws CmsException {

        CmsObject cms = getCmsObject();
        List folders = cms.readResources("/", folderFilter);
        assertNotNull(folders);
        assertTrue(folders.size() > 0, "Zero folders in test system found. ");

        int resourceTypeFolder = CmsResourceTypeFolder.RESOURCE_TYPE_ID;
        CmsResource resource;
        Iterator itResources = folders.iterator();
        while (itResources.hasNext()) {
            resource = (CmsResource)itResources.next();
            assertTrue(
                resourceTypeFolder == resource.getTypeId(),
                "Filter let a resource of type "
                    + OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName()
                    + " pass: "
                    + resource.getRootPath());
        }
    }

    /**
     * Tests filters that should only allow folders (obtained by
     * {@link CmsResourceFilter#addRequireFolder()}).<p>
     *
     * @throws Exception if the test fails
     */
    @Test
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
