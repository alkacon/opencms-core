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

import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit tests for resource availability operations.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestExists extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests the availability of a file that exists and with proper permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testExistsForExistingFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the availability of a file that exists and with proper permissions");
        String filename = "index.html";

        assertEquals(true, cms.existsResource(filename));
    }

    /**
     * Tests the availability of a file that exists but with not enough permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(3)
    public void testExistsForUnauthorizedFile() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing the availability of a file that exists but with not enough permissions");

        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        cms.createUser("testuser", "test", "A test user", null);
        CmsUser testUser = cms.readUser("testuser");

        String resName = "index.html";

        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "-r-w-v-c-i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser.getName(), "-r-w-v-c-i");
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.loginUser("testuser", "test");
        assertEquals(false, cms.existsResource(resName));
    }

    /**
     * Tests the availability of a file that does not exist.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testExistsForUnexistingFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the availability of a file that does not exist");
        String filename = "xxx.yyy";

        assertEquals(false, cms.existsResource(filename));
    }
}
