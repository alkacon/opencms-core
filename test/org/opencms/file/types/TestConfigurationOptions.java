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

package org.opencms.file.types;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit tests for the resource type configuration options.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestConfigurationOptions extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Test copy resources on resource creation .<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testCopyResourcesOnCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'copy resources' on resource creation");

        String resourcename = "/newlinkgallery/";

        cms.createResource(resourcename, 10);

        List subResources = cms.readResources(resourcename, CmsResourceFilter.ALL);
        assertTrue(subResources.size() > 15);

        CmsResource res;

        res = cms.readResource(resourcename + "newname.html");
        assertTrue(res.getSiblingCount() == 2);

        cms.readResource(resourcename + "mytypes");
        res = cms.readResource(resourcename + "mytypes/text.txt");
        assertTrue(res.getSiblingCount() == 1);

        cms.readResource(resourcename + "subfolder11");
        cms.readResource(resourcename + "subfolder11/subsubfolder111");
    }

    /**
     * Test default property creation (from resource type configuration).<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testDefaultPropertyCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing default property creation");

        String resourcename = "/folder1/article_test.html";
        byte[] content = new byte[0];

        cms.createResource(resourcename, ARTICLE_TYPEID, content, null);

        assertResourceType(cms, resourcename, ARTICLE_TYPEID);
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());

        CmsProperty property1;
        CmsProperty property2;
        property1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Test title", null);
        property2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertTrue(property1.isIdentical(property2));

        property1 = new CmsProperty(
            "template-elements",
            "/system/modules/org.opencms.frontend.templateone.form/pages/form.html",
            null);
        property2 = cms.readPropertyObject(resourcename, "template-elements", false);
        assertTrue(property1.isIdentical(property2));

        property1 = new CmsProperty(
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            null,
            "Admin_/folder1/article_test.html_/sites/default/folder1/article_test.html");
        property2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        assertTrue(property1.isIdentical(property2));

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }
}
