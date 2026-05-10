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

package org.opencms.notification;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestRunner;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit test for the "readResponsibleUsers" method of the CmsObject.<p>
 *
 */
public class TestResponsibles extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Sets responsibles to a file and then tests the readResponsibleUsers method of CmsObject .<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testResponsibles() throws Throwable {

        echo("Testing responsibles of resources");

        // create three users, two of them belonging to a group
        final CmsObject cms = getCmsObject();
        final CmsGroup tastycrats = cms.createGroup("tastycrats", "A test group", 0, null);
        final CmsUser fry = cms.createUser("fry", "password", "First test user", null);
        final CmsUser bender = cms.createUser(
            "bender",
            "password",
            "Second test user, belonging to the tastycrats group.",
            null);
        final CmsUser leela = cms.createUser(
            "leela",
            "password",
            "Third test user, belonging to the tastycrats group.",
            null);
        final CmsUser farnsworth = cms.createUser(
            "farnsworth",
            "password",
            "Another test user, which is not responsible.",
            null);
        cms.addUserToGroup("bender", "tastycrats");
        cms.addUserToGroup("leela", "tastycrats");

        // make group and user responsible for the group
        final String resource1 = "/folder1/index.html";
        final CmsPermissionSet permissions = new CmsPermissionSet(
            CmsPermissionSet.PERMISSION_WRITE,
            CmsPermissionSet.PERMISSION_READ);
        cms.lockResource(resource1);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_USER,
            fry.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            tastycrats.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_USER,
            farnsworth.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            0);
        cms.unlockResource(resource1);

        // check, if the three users are indeed responsible for the resource.
        final Set responsibles = cms.readResponsibleUsers(cms.readResource(resource1));
        final Set expectedResponsibles = new HashSet();
        expectedResponsibles.add(fry);
        expectedResponsibles.add(leela);
        expectedResponsibles.add(bender);
        assertEquals(responsibles, expectedResponsibles);
    }
}
