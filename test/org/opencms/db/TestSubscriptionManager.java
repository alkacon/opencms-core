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

package org.opencms.db;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms subscription manager.<p>
 */
public class TestSubscriptionManager extends OpenCmsTestCase {

    /** Time to wait for a database operation to finish. */
    private static final long WAIT_FOR_DB_MILLIS = 300;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSubscriptionManager(String arg0) {

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
        suite.setName(TestSubscriptionManager.class.getName());

        suite.addTest(new TestSubscriptionManager("testVisitResources"));
        suite.addTest(new TestSubscriptionManager("testSubscribeResources"));
        suite.addTest(new TestSubscriptionManager("testReadSubscribedResources"));

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
     * Test reading subscribed resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadSubscribedResources() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsGroup group = cms.readGroup("Users");
        echo("Testing reading subscribed resources");

        CmsSubscriptionManager subMan = OpenCms.getSubscriptionManager();
        subMan.subscribeResourceFor(cms, user, "/folder2/index.html");
        List<CmsResource> subscribedUserResources = subMan.readAllSubscribedResources(cms, user);

        assertEquals(1, subscribedUserResources.size());
        assertEquals("/folder2/index.html", cms.getSitePath(subscribedUserResources.get(0)));

        subMan.subscribeResourceFor(cms, group, "/folder2/index.html");

        CmsSubscriptionFilter filter = new CmsSubscriptionFilter();
        filter.addGroup(group);
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(1, subscribedUserResources.size());
        filter.setUser(user);
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(1, subscribedUserResources.size());

        subMan.subscribeResourceFor(cms, user, "/folder2/page1.html");

        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(2, subscribedUserResources.size());
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        subMan.markResourceAsVisitedBy(cms, "/folder2/page1.html", user);
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        filter.setMode(CmsSubscriptionReadMode.VISITED);
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(1, subscribedUserResources.size());

        filter.setMode(CmsSubscriptionReadMode.UNVISITED);
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(1, subscribedUserResources.size());

        long beforeChangeTime = System.currentTimeMillis();
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        cms.lockResource("/folder2/page1.html");
        CmsFile file = cms.readFile("/folder2/page1.html");
        cms.writeFile(file);
        cms.unlockResource("/folder2/page1.html");

        OpenCms.getPublishManager().publishResource(cms, "/folder2/page1.html");
        OpenCms.getPublishManager().waitWhileRunning();
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(2, subscribedUserResources.size());

        filter.setFromDate(beforeChangeTime);
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(1, subscribedUserResources.size());

        filter.setFromDate(System.currentTimeMillis());
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(0, subscribedUserResources.size());

        filter.setFromDate(beforeChangeTime);
        cms.lockResource("/folder2/page1.html");
        cms.deleteResource("/folder2/page1.html", CmsResource.DELETE_REMOVE_SIBLINGS);
        OpenCms.getPublishManager().publishResource(cms, "/folder2/page1.html");
        OpenCms.getPublishManager().waitWhileRunning();
        subscribedUserResources = subMan.readSubscribedResources(cms, filter);
        assertEquals(0, subscribedUserResources.size());

        List<I_CmsHistoryResource> subscribedDeletedResources = subMan.readSubscribedDeletedResources(
            cms,
            user,
            false,
            "/folder2/",
            false,
            0);
        assertEquals(1, subscribedDeletedResources.size());

        subMan.unsubscribeAllDeletedResources(cms, Long.MAX_VALUE);
        subscribedDeletedResources = subMan.readSubscribedDeletedResources(cms, user, false, null, false, 0);
        assertEquals(0, subscribedDeletedResources.size());
    }

    /**
     * Test subscription of resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSubscribeResources() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        echo("Testing subscription of resources");

        CmsSubscriptionManager subMan = OpenCms.getSubscriptionManager();
        subMan.subscribeResourceFor(cms, user, "/index.html");
        List<CmsResource> subscribedUserResources = subMan.readAllSubscribedResources(cms, user);

        assertEquals(1, subscribedUserResources.size());
        assertEquals("/index.html", cms.getSitePath(subscribedUserResources.get(0)));

        subMan.unsubscribeResourceFor(cms, user, "/index.html");
        subscribedUserResources = subMan.readAllSubscribedResources(cms, user);
        assertEquals(0, subscribedUserResources.size());
    }

    /**
     * Test subscription of resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testVisitResources() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        echo("Testing visitation of resources");

        CmsSubscriptionManager subMan = OpenCms.getSubscriptionManager();
        subMan.markResourceAsVisitedBy(cms, "/folder1/index.html", user);
        CmsVisitedByFilter filter = new CmsVisitedByFilter(cms);

        List<CmsResource> visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);

        assertEquals(1, visitedUserResources.size());
        assertEquals("/folder1/index.html", cms.getSitePath(visitedUserResources.get(0)));

        // wait between operations to be able to perform time based tests
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        long storedTime = System.currentTimeMillis();
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        subMan.markResourceAsVisitedBy(cms, "/folder1/page1.html", user);
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(2, visitedUserResources.size());

        filter.setFromDate(storedTime);
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(1, visitedUserResources.size());
        assertEquals("/folder1/page1.html", cms.getSitePath(visitedUserResources.get(0)));

        Thread.sleep(WAIT_FOR_DB_MILLIS);
        storedTime = System.currentTimeMillis();
        filter.setFromDate(storedTime);
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(0, visitedUserResources.size());

        subMan.markResourceAsVisitedBy(cms, "/folder1/page2.html", user);
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        subMan.markResourceAsVisitedBy(cms, "/folder1/page3.html", user);
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        subMan.markResourceAsVisitedBy(cms, "/folder1/subfolder11/page1.html", user);
        Thread.sleep(WAIT_FOR_DB_MILLIS);
        subMan.markResourceAsVisitedBy(cms, "/folder1/subfolder11/page2.html", user);

        filter = new CmsVisitedByFilter(cms);
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(6, visitedUserResources.size());

        filter.setToDate(storedTime);
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(2, visitedUserResources.size());

        assertNotSame(Long.valueOf(0L), Long.valueOf(subMan.getDateLastVisitedBy(cms, user, "/folder1/page2.html")));
        assertNotSame(Long.valueOf(0L), Long.valueOf(subMan.getDateLastVisitedBy(cms, user, "/folder1/index.html")));
        assertSame(Long.valueOf(0L), Long.valueOf(subMan.getDateLastVisitedBy(cms, user, "/index.html")));

        filter.setToDate(Long.MAX_VALUE);
        filter.setParentResource(cms.readResource("/folder1/subfolder11/page1.html"));
        visitedUserResources = subMan.readResourcesVisitedBy(cms, filter);
        assertEquals(2, visitedUserResources.size());

    }
}
