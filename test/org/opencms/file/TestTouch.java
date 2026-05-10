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

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit test for the "touch" method of the CmsObject.<p>
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTouch extends OpenCmsTestRunner {

    /**
     * Test the touch method to touch a single resource.<p>
     * @param tc the OpenCms test environment
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResource(OpenCmsTestRunner tc, CmsObject cms, String resource1) throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(resource1);
        cms.setDateLastModified(resource1, timestamp, false);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
    }

    /**
     * Test the touch method to touch a single folder.<p>
     * @param tc the OpenCms test environment
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResources(OpenCmsTestRunner tc, CmsObject cms, String resource1) throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(resource1);
        cms.setDateLastModified(resource1, timestamp, false);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());

        // evaluate all subresources
        List subresources = cms.readResources(resource1, CmsResourceFilter.ALL);

        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }
    }

    /**
     * Test the touch method to touch a complete subtree.<p>
     * @param tc the OpenCms test environment
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResourcesRecursive(OpenCmsTestRunner tc, CmsObject cms, String resource1) throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(resource1);
        cms.setDateLastModified(resource1, timestamp, true);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());

        // evaluate all subresources
        List subresources = cms.readResources(resource1, CmsResourceFilter.ALL);

        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_TOUCH);
            // project must be current project
            tc.assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
            // state must be "changed"
            tc.assertState(cms, resName, tc.getPreCalculatedState(resName));
            // date last modified must be the date set in the tough operation
            tc.assertDateLastModified(cms, resName, timestamp);
            // the user last modified must be the current user
            tc.assertUserLastModified(cms, resName, cms.getRequestContext().getCurrentUser());
        }
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
     * Test the touch method on a file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testTouchFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing touch on file");
        touchResource(this, cms, "/index.html");
    }

    /**
     * Test the touch method on a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testTouchFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing touch on a folder (without recursion)");
        touchResources(this, cms, "/folder1/");
    }

    /**
     * Test the touch method on a folder and recusivly on all resources in the folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(3)
    public void testTouchFolderRecursive() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing touch on a folder (_with_ recursion)");
        touchResourcesRecursive(this, cms, "/folder2/");
    }
}
