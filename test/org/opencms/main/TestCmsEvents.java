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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit tests for OpenCms events.<p>
 */
public class TestCmsEvents extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Test the before and after publish event.<p>
     *
     * @throws Throwable if the test fails
     */
    @Test
    public void testBeforeAfterPublishEvent() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing to event before / after publish project");

        String projectName = "PublishEventTest";

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");
            CmsProject project = cms.createProject(
                projectName,
                "Unit test project for publish events",
                OpenCms.getDefaultUsers().getGroupUsers(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_NORMAL);
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/");
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }

        // create and register the event listener
        CmsTestEventListener handler = new CmsTestEventListener();
        OpenCms.addCmsEventListener(
            handler,
            new int[] {I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT, I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        CmsProject current = cms.readProject(projectName);
        cms.getRequestContext().setCurrentProject(current);

        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertTrue(handler.hasRecievedEvent(I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT));
        assertTrue(handler.hasRecievedEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT));
        // only 2 events should be been recieved
        assertEquals(2, handler.getEvents().size());
    }
}
