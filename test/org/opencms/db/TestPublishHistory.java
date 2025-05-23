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

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms publish history.<p>
 */
public class TestPublishHistory extends OpenCmsTestCase implements I_CmsEventListener {

    /** Internal shared variable to test the right publish history. */
    private static CmsResourceState m_test;

    /** Name of resource to test. */
    private static final String RESOURCENAME = "/folder1/testfile.txt";

    /** Name of resource to test after renaming. */
    private static final String RESOURCENAME_MOVED = "/folder1/testfile_moved.txt";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPublishHistory(String arg0) {

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
        suite.setName(TestPublishHistory.class.getName());
        suite.addTest(new TestPublishHistory("testCleanupPublishHistory1"));
        suite.addTest(new TestPublishHistory("testCleanupPublishHistory2"));
        suite.addTest(new TestPublishHistory("testPublishNewFile"));
        suite.addTest(new TestPublishHistory("testPublishChangedFile"));
        suite.addTest(new TestPublishHistory("testPublishMovedFile"));
        suite.addTest(new TestPublishHistory("testPublishDeletedFile"));

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
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                try {
                    CmsObject cms = getCmsObject();
                    // event data contains a list of the published resources
                    CmsUUID publishHistoryId = new CmsUUID(
                        (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                    List publishedResources = cms.readPublishedResources(publishHistoryId);
                    CmsPublishedResource pubRes = null;
                    if (m_test.isNew()) {
                        assertEquals(1, publishedResources.size());
                        pubRes = (CmsPublishedResource)publishedResources.get(0);
                        assertEquals("/sites/default" + RESOURCENAME, pubRes.getRootPath());
                        assertEquals(CmsResource.STATE_NEW, pubRes.getState());
                        assertFalse(pubRes.isMoved());
                    } else if (m_test.isChanged()) {
                        assertEquals(1, publishedResources.size());
                        pubRes = (CmsPublishedResource)publishedResources.get(0);
                        assertEquals("/sites/default" + RESOURCENAME, pubRes.getRootPath());
                        assertEquals(CmsResource.STATE_CHANGED, pubRes.getState());
                        assertFalse(pubRes.isMoved());
                    } else if (m_test == CmsPublishedResource.STATE_MOVED_SOURCE) {
                        assertEquals(2, publishedResources.size());
                        pubRes = (CmsPublishedResource)publishedResources.get(0);
                        boolean moved = false;
                        if (pubRes.getRootPath().endsWith(RESOURCENAME)) {
                            assertEquals("/sites/default" + RESOURCENAME, pubRes.getRootPath());
                            assertEquals(CmsResource.STATE_DELETED, pubRes.getState());
                            assertEquals(CmsPublishedResource.STATE_MOVED_SOURCE, pubRes.getMovedState());
                            assertTrue(pubRes.isMoved());
                            moved = false;
                        } else if (pubRes.getRootPath().endsWith(RESOURCENAME_MOVED)) {
                            assertEquals("/sites/default" + RESOURCENAME_MOVED, pubRes.getRootPath());
                            assertEquals(CmsResource.STATE_NEW, pubRes.getState());
                            assertEquals(CmsPublishedResource.STATE_MOVED_DESTINATION, pubRes.getMovedState());
                            assertTrue(pubRes.isMoved());
                            moved = true;
                        } else {
                            fail("unexpected publish resource " + pubRes.getRootPath());
                        }
                        pubRes = (CmsPublishedResource)publishedResources.get(1);
                        if (moved && pubRes.getRootPath().endsWith(RESOURCENAME)) {
                            assertEquals("/sites/default" + RESOURCENAME, pubRes.getRootPath());
                            assertEquals(CmsResource.STATE_DELETED, pubRes.getState());
                            assertTrue(pubRes.isMoved());
                        } else if (!moved && pubRes.getRootPath().endsWith(RESOURCENAME_MOVED)) {
                            assertEquals("/sites/default" + RESOURCENAME_MOVED, pubRes.getRootPath());
                            assertEquals(CmsResource.STATE_NEW, pubRes.getState());
                            assertTrue(pubRes.isMoved());
                        } else {
                            fail("unexpected publish resource " + pubRes.getRootPath());
                        }
                    } else if (m_test.isDeleted()) {
                        assertEquals(1, publishedResources.size());
                        pubRes = (CmsPublishedResource)publishedResources.get(0);
                        assertEquals("/sites/default" + RESOURCENAME_MOVED, pubRes.getRootPath());
                        assertEquals(CmsResource.STATE_DELETED, pubRes.getState());
                        assertFalse(pubRes.isMoved());
                    } else {
                        fail("should never happen!");
                    }
                } catch (CmsException e) {
                    fail(e.getMessage());
                }
                break;
            default:
                fail("should never happen!");
        }
    }

    /**
     * Tests publish history cleanup.
     * @throws Exception if something goes wrong
     */
    public void testCleanupPublishHistory1() throws Exception {

        String prefix = "tph1_";
        OpenCms.addCmsEventListener(evt -> {
            if (evt.getType() == I_CmsEventListener.EVENT_PUBLISH_PROJECT) {
                System.out.println(evt.getData().get(I_CmsEventListener.KEY_PUBLISHID));
            }
        });
        CmsObject cms = getCmsObject();
        CmsUUID[] historyIds = new CmsUUID[12];
        for (int i = 0; i < 12; i++) {
            String path = "/" + prefix + i + ".txt";
            CmsResource res = cms.createResource(path, 1);
            CmsUUID historyId = OpenCms.getPublishManager().publishResource(cms, path);
            historyIds[i] = historyId;
            OpenCms.getPublishManager().waitWhileRunning();
        }
        Thread.sleep(1000);
        // publish history size is configured as 10, auto publish resource cleanup is on,
        // we have published 12 times, check that the cleanup worked
        assertEquals(0, cms.readPublishedResources(historyIds[0]).size());
        assertEquals(0, cms.readPublishedResources(historyIds[1]).size());
        assertEquals(1, cms.readPublishedResources(historyIds[2]).size());
        assertEquals(1, cms.readPublishedResources(historyIds[3]).size());
    }

    /**
     * Tests publish history cleanup.
     * @throws Exception if something goes wrong
     */
    public void testCleanupPublishHistory2() throws Exception {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<CmsUUID> fakeHistoryIds = Arrays.asList(new CmsUUID(), new CmsUUID(), new CmsUUID());
        try {
            conn = OpenCms.getSqlManager().getConnection(OpenCms.getSqlManager().getDefaultDbPoolName());
            stmt = conn.prepareStatement("INSERT INTO CMS_PUBLISH_HISTORY VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            for (CmsUUID id : fakeHistoryIds) {
                stmt.setString(1, id.toString());
                stmt.setInt(2, 0);
                stmt.setString(3, new CmsUUID().toString());
                stmt.setString(4, new CmsUUID().toString());
                stmt.setString(5, "/" + Math.random());
                stmt.setInt(6, 0);
                stmt.setInt(7, 1);
                stmt.setInt(8, 1);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            // noop

        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    // noop
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // noop
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // noop
                }
            }

        }

        CmsObject cms = getCmsObject();
        for (CmsUUID id : fakeHistoryIds) {
            assertEquals(1, cms.readPublishedResources(id).size());
        }
        OpenCms.getPublishManager().cleanupPublishHistory(cms, Arrays.asList(fakeHistoryIds.get(0)));
        assertEquals(1, cms.readPublishedResources(fakeHistoryIds.get(0)).size());
        assertEquals(0, cms.readPublishedResources(fakeHistoryIds.get(1)).size());
        assertEquals(0, cms.readPublishedResources(fakeHistoryIds.get(1)).size());

    }

    /**
     * Test the publish history for a changed file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishChangedFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish history for a changed file");

        // set the test to changed file
        m_test = CmsResource.STATE_CHANGED;

        cms.lockResource(RESOURCENAME);
        cms.setDateLastModified(RESOURCENAME, System.currentTimeMillis(), false);
        cms.unlockResource(RESOURCENAME);
        OpenCms.getPublishManager().publishResource(cms, RESOURCENAME);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test the publish history for a deleted file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishDeletedFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish history for a deleted file");

        // set the test to deleted file
        m_test = CmsResource.STATE_DELETED;

        cms.lockResource(RESOURCENAME_MOVED);
        cms.deleteResource(RESOURCENAME_MOVED, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(RESOURCENAME_MOVED);
        OpenCms.getPublishManager().publishResource(cms, RESOURCENAME_MOVED);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test the publish history for a moved file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishMovedFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish history for a moved file");

        // set the test to new file
        m_test = CmsPublishedResource.STATE_MOVED_SOURCE;

        cms.lockResource(RESOURCENAME);
        cms.moveResource(RESOURCENAME, RESOURCENAME_MOVED);
        cms.unlockResource(RESOURCENAME_MOVED);
        OpenCms.getPublishManager().publishResource(cms, RESOURCENAME_MOVED);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test publish history for a new file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishNewFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish history for a new file");

        // THIS is the first test case, so register the event listener here!
        OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        // set the test to new file
        m_test = CmsResource.STATE_NEW;

        cms.createResource(RESOURCENAME, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockResource(RESOURCENAME);
        OpenCms.getPublishManager().publishResource(cms, RESOURCENAME);
        OpenCms.getPublishManager().waitWhileRunning();
    }
}
