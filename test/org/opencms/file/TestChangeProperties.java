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

import org.opencms.test.OpenCmsTestRunner;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit tests for the <code>{@link CmsObject#changeResourcesInFolderWithProperty(String, String, String, String, boolean)}</code>
 * method.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestChangeProperties extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tries to change the "Description" property of the two files
     * "/sites/default/folder1/index.html" and
     * "/sites/default/folder2/index.html" with the site-root "".
     *
     * The test fails, if the <code>recursive</code> parameter of
     * <code>changeResourcesInFolderWithProperty()</code> changes the
     * semantics of the method call.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    @Test
    @Order(2)
    public void testChangeResourcesFullPath() throws Throwable {

        CmsObject cms = getCmsObject();

        String resource1 = cms.getRequestContext().getSiteRoot() + "/folder2/subfolder21/index.html";
        String resource2 = cms.getRequestContext().getSiteRoot() + "/folder2/subfolder22/index.html";

        cms.getRequestContext().setSiteRoot("");

        cms.lockResource(resource1);
        cms.lockResource(resource2);
        assertLock(cms, resource1);
        assertLock(cms, resource2);

        System.out.println(
            "Changing property of \"" + resource1 + "\" in \"" + cms.getRequestContext().getSiteRoot() + "\"");

        List l1 = cms.changeResourcesInFolderWithProperty(
            resource1,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page in subfolder21",
            "Changed Value",
            true);

        System.out.println(
            "Changing property of \"" + resource2 + "\" in \"" + cms.getRequestContext().getSiteRoot() + "\"");

        List l2 = cms.changeResourcesInFolderWithProperty(
            resource2,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page in subfolder22",
            "Changed value",
            false);

        assertEquals(l1.size(), l2.size());
    }

    /**
     * Tries to change the "Description" property of the two files
     * "/folder1/index.html" and "/folder2/index.html" with the site-root
     * "/sites/default".
     *
     * The test fails, if the <code>recursive</code> parameter of
     * <code>changeResourcesInFolderWithProperty()</code> changes the
     * semantics of the method call.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    @Test
    @Order(1)
    public void testChangeResourcesRelativePath() throws Throwable {

        CmsObject cms = getCmsObject();

        String resource1 = "/folder1/subfolder11/index.html";
        String resource2 = "/folder1/subfolder12/index.html";
        cms.lockResource(resource1);
        cms.lockResource(resource2);
        assertLock(cms, resource1);
        assertLock(cms, resource2);

        System.out.println(
            "Changing property of \"" + resource1 + "\" in \"" + cms.getRequestContext().getSiteRoot() + "\"");

        List l1 = cms.changeResourcesInFolderWithProperty(
            resource1,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page of subfolder11",
            "Changed Value",
            true);

        System.out.println(
            "Changing property of \"" + resource2 + "\" in \"" + cms.getRequestContext().getSiteRoot() + "\"");

        List l2 = cms.changeResourcesInFolderWithProperty(
            resource2,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index in subfolder12",
            "Changed value",
            false);

        assertEquals(l1.size(), l2.size());
    }
}
