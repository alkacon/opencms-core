/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/TestCmsEvents.java,v $
 * Date   : $Date: 2005/10/10 16:11:09 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms events.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $
 */
public class TestCmsEvents extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsEvents(String arg0) {

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
        suite.setName(TestCmsEvents.class.getName());

        suite.addTest(new TestCmsEvents("testBeforeAfterPublishEvent"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test the before and after publish event.<p>
     * 
     * @throws Throwable if the test fails
     */
    public void testBeforeAfterPublishEvent() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing to event before / after publish project");

        String projectName = "PublishEventTest";

        cms.getRequestContext().saveSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = cms.createProject(
                projectName,
                "Unit test project for publish events",
                OpenCms.getDefaultUsers().getGroupUsers(),
                OpenCms.getDefaultUsers().getGroupProjectmanagers(),
                CmsProject.PROJECT_TYPE_NORMAL);
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/");
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        // create and register the event listener
        TestEventListener handler = new TestEventListener();
        OpenCms.addCmsEventListener(handler, new int[] {
            I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        CmsProject current = cms.readProject(projectName);
        cms.getRequestContext().setCurrentProject(current);

        cms.publishProject();

        assertTrue(handler.hasRecievedEvent(I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT));
        assertTrue(handler.hasRecievedEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT));
        // only 2 events should be been recieved
        assertEquals(2, handler.getEvents().size());
    }
}