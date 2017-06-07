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

package org.opencms.file.types;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the resource type configuration options.<p>
 *
 */
public class TestConfigurationOptions extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestConfigurationOptions(String arg0) {

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
        suite.setName(TestConfigurationOptions.class.getName());

        suite.addTest(new TestConfigurationOptions("testDefaultPropertyCreation"));
        suite.addTest(new TestConfigurationOptions("testCopyResourcesOnCreation"));

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
     * Test copy resources on resource creation .<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCopyResourcesOnCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'copy resources' on resource creation");

        // create a new type "11", default tests have this configured as "link gallery" folder
        String resourcename = "/newlinkgallery/";

        cms.createResource(resourcename, 11);

        List subResources = cms.readResources(resourcename, CmsResourceFilter.ALL);
        assertTrue(subResources.size() > 15);

        // read some of the newly created copy resources to make sure they exist
        CmsResource res;

        res = cms.readResource(resourcename + "newname.html");
        // must have 1 additional sibling
        assertTrue(res.getSiblingCount() == 2);

        cms.readResource(resourcename + "mytypes");
        res = cms.readResource(resourcename + "mytypes/text.txt");
        // should have no sibling
        assertTrue(res.getSiblingCount() == 1);

        cms.readResource(resourcename + "subfolder11");
        cms.readResource(resourcename + "subfolder11/subsubfolder111");
    }

    /**
     * Test default property creation (from resource type configuration).<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDefaultPropertyCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing default property creation");

        String resourcename = "/folder1/article_test.html";
        byte[] content = new byte[0];

        // resource 27 is article (xml content) with default properties
        cms.createResource(resourcename, OpenCmsTestCase.ARTICLE_TYPEID, content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, OpenCmsTestCase.ARTICLE_TYPEID);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());

        CmsProperty property1, property2;
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

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }
}
