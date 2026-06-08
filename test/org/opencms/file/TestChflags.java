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

import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit test for the "chflags" method of the CmsObject.<p>
 *
 * @since 6.0 alpha 2
 */
public class TestChflags extends OpenCmsTestRunner {

    /**
     * Tests setting the "internal" flag on a resource.<p>
     *
     * @param tc the OpenCmsTestRunner
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    public static void addFlagInternal(OpenCmsTestRunner tc, CmsObject cms) throws Throwable {

        String resource1 = "/index.html";

        CmsResource resource = cms.readResource(resource1, CmsResourceFilter.ALL);
        tc.storeResources(cms, resource1);

        int existingFlags = resource.getFlags();
        int flags = existingFlags;
        long timestamp = System.currentTimeMillis();

        // the "internal" flag is not set
        assertFalse(resource.isInternal());

        // add the "internal" flag
        flags += CmsResource.FLAG_INTERNAL;

        // change the flag
        cms.lockResource(resource1);
        cms.chflags(resource1, flags);
        cms.unlockResource(resource1);

        // check the status of the changed file
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHFLAGS);
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        tc.assertState(cms, resource1, CmsResource.STATE_CHANGED);
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        tc.assertFlags(cms, resource1, CmsResource.FLAG_INTERNAL);
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests setting the "internal" flag on a resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testAddFlagInternal() throws Throwable {

        final CmsObject cms = getCmsObject();

        echo("Tests setting the \"internal\" flag on a resource");
        addFlagInternal(this, cms);
    }

}
