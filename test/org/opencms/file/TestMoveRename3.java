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

package org.opencms.file;

import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for move/delete/publish operations.<p>
 */
public class TestMoveRename3 extends OpenCmsTestCase {

    /**
     * Container for structure entries.<p>
     */
    class CmsVfsStructureEntry {

        /** Parent id. */
        private final String m_parentId;

        /** Resource id. */
        private final String m_resourceId;

        /** Resource path. */
        private final String m_resourcePath;

        /** Structure id. */
        private final String m_structureId;

        /**
         * Constructor for a structure entry.<p>
         *
         * Immutable object.<p>
         *
         * @param structureId the structure id
         * @param parentId the parent structure id
         * @param resourceId the resource id
         * @param resourcePath the resource path
         */
        public CmsVfsStructureEntry(String structureId, String parentId, String resourceId, String resourcePath) {

            m_structureId = structureId;
            m_parentId = parentId;
            m_resourceId = resourceId;
            m_resourcePath = resourcePath;
        }

        /**
         * Returns the parent Id.<p>
         *
         * @return the parent Id
         */
        public String getParentId() {

            return m_parentId;
        }

        /**
         * Returns the resource Id.<p>
         *
         * @return the resource Id
         */
        public String getResourceId() {

            return m_resourceId;
        }

        /**
         * Returns the resource Path.<p>
         *
         * @return the resource Path
         */
        public String getResourcePath() {

            return m_resourcePath;
        }

        /**
         * Returns the structure Id.<p>
         *
         * @return the structure Id
         */
        public String getStructureId() {

            return m_structureId;
        }
    }

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestMoveRename3(String arg0) {

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
        suite.setName(TestMoveRename3.class.getName());

        suite.addTest(new TestMoveRename3("testDeleteUndeleteMovedFile"));
        suite.addTest(new TestMoveRename3("testDeleteUndeletePublishMovedFile"));
        suite.addTest(new TestMoveRename3("testMovedFileParent"));
        suite.addTest(new TestMoveRename3("testRenameToExistingFolder"));
        suite.addTest(new TestMoveRename3("testRenameToInvalidName"));
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
     * Test delete and undelete of a moved file.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testDeleteUndeleteMovedFile() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing delete undelete of a moved file");
        String folderOne = "/folderOneA";
        String folderTwo = "/folderTwoA";
        String testFile = "testA.txt";
        // set the test to new file
        //        m_test = CmsPublishedResource.STATE_MOVED_SOURCE;
        cms.createResource(folderOne, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderOne + "/" + testFile, CmsResourceTypePlain.getStaticTypeId());

        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(folderTwo, CmsResourceTypeFolder.getStaticTypeId());

        cms.lockResource(folderOne + "/" + testFile);
        cms.moveResource(folderOne + "/" + testFile, folderTwo + "/" + testFile);
        cms.unlockResource(folderTwo + "/" + testFile);

        cms.lockResource(folderOne);
        cms.deleteResource(folderOne, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderOne);
        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();

        storeResources(cms, folderTwo + "/" + testFile);

        cms.lockResource(folderTwo + "/" + testFile);
        cms.deleteResource(folderTwo + "/" + testFile, CmsResourceDeleteMode.valueOf(1));

        cms.undeleteResource(folderTwo + "/" + testFile, false);

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableDateLastModifiedTest();
        assertFilter(cms, cms.readResource(folderTwo + "/" + testFile), filter);

    }

    /**
     * Test delete and undelete and publish of a moved file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteUndeletePublishMovedFile() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing delete undelete and publish of a moved file");
        String folderOne = "/folderOneB";
        String folderTwo = "/folderTwoB";
        String testFile = "testB.txt";
        // set the test to new file
        //        m_test = CmsPublishedResource.STATE_MOVED_SOURCE;
        cms.createResource(folderOne, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderOne + "/" + testFile, CmsResourceTypePlain.getStaticTypeId());

        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(folderTwo, CmsResourceTypeFolder.getStaticTypeId());

        cms.lockResource(folderOne + "/" + testFile);
        cms.moveResource(folderOne + "/" + testFile, folderTwo + "/" + testFile);
        cms.unlockResource(folderTwo + "/" + testFile);

        cms.lockResource(folderOne);
        cms.deleteResource(folderOne, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderOne);
        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();

        storeResources(cms, folderTwo + "/" + testFile);

        cms.lockResource(folderTwo + "/" + testFile);
        cms.deleteResource(folderTwo + "/" + testFile, CmsResourceDeleteMode.valueOf(1));
        OpenCms.getPublishManager().publishResource(cms, folderTwo);
        OpenCms.getPublishManager().waitWhileRunning();

    }

    /**
     * Test the publish history for a moved file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMovedFileParent() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing parent for a moved file");
        String folderOne = "/folderOneC";
        String folderTwo = "/folderTwoC";
        String testFile = "testC.txt";
        // set the test to new file
        //        m_test = CmsPublishedResource.STATE_MOVED_SOURCE;
        cms.createResource(folderOne, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderOne + "/" + testFile, CmsResourceTypePlain.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(folderTwo, CmsResourceTypeFolder.getStaticTypeId());

        cms.lockResource(folderOne + "/" + testFile);
        cms.moveResource(folderOne + "/" + testFile, folderTwo + "/" + testFile);
        cms.unlockResource(folderTwo + "/" + testFile);

        cms.lockResource(folderOne);
        cms.deleteResource(folderOne, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderOne);
        OpenCms.getPublishManager().publishResource(cms, folderOne);
        OpenCms.getPublishManager().waitWhileRunning();

        Map strEntries;
        strEntries = readOnlineStructure();
        List ret = new ArrayList();
        // add entries recursively
        while (addBrokenEntries(ret, strEntries)) {
            // noop
        }

        System.out.println(ret.size() + " broken entries found.");
        try {
            OpenCms.getPublishManager().publishResource(cms, folderTwo);
            OpenCms.getPublishManager().waitWhileRunning();
        } catch (Exception e) {
            e.printStackTrace();
        }
        strEntries = readOnlineStructure();
        ret = new ArrayList();
        // add entries recursively
        while (addBrokenEntries(ret, strEntries)) {
            // noop
        }
        System.out.println(ret.size() + " broken entries found.");
    }

    /**
     * Test that renaming a folder fails if a folder with the same name already exists.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testRenameToExistingFolder() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing renaming to an already existing folder");
        String folderOne = "/folderSource";
        String folderTwo = "/folderTarget";
        cms.createResource(folderOne, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderTwo, CmsResourceTypeFolder.getStaticTypeId());
        CmsException exception = null;
        try {
            cms.moveResource(folderOne, folderTwo);
        } catch (CmsException e) {
            exception = e;
        }
        assertNotNull("renaming a folder to an already existing one should fail!", exception);
    }

    /**
     * Test that renaming a folder fails if a folder with the same name already exists.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testRenameToInvalidName() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing renaming to an invalid name");
        String folderOne = "/folderWithValidName";
        String folderTwo = "/folder with spaces in it";
        cms.createResource(folderOne, CmsResourceTypeFolder.getStaticTypeId());
        CmsRuntimeException exception = null;
        try {
            cms.moveResource(folderOne, folderTwo);
        } catch (CmsIllegalArgumentException e) {
            exception = e;
        }
        assertNotNull("Renaming to an invalid name should fail!", exception);
    }

    /**
     * Test all entries in <code>strEntries</code>, and adds the
     * broken entries to the <code>brokenEntries</code> list.<p>
     *
     * @param brokenEntries list, may be empty, to add found broken entries
     * @param strEntries entries to test
     *
     * @return <code>true</code> if at least one broken entry has been found
     */
    private boolean addBrokenEntries(List brokenEntries, Map strEntries) {

        boolean ret = false;
        // remove all entries with parent
        for (Iterator iter = strEntries.values().iterator(); iter.hasNext();) {
            CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)iter.next();
            if (brokenEntries.contains(strEntry)) {
                continue;
            }
            // look for a direct broken entry
            if (strEntries.get(strEntry.getParentId()) == null) {
                if (!strEntry.getResourcePath().equals("/")) {
                    brokenEntries.add(strEntry);
                    ret = true;
                }
            } else if (brokenEntries.contains(strEntries.get(strEntry.getParentId()))) {
                // look for an indirect broken entry
                brokenEntries.add(strEntry);
                ret = true;
            }
        }
        return ret;
    }

    private Map readOnlineStructure() {

        Map strEntries = new HashMap();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = OpenCms.getSqlManager().getConnection(OpenCms.getSqlManager().getDefaultDbPoolName());
            stmt = conn.prepareStatement(
                "SELECT STR.STRUCTURE_ID, STR.PARENT_ID, STR.RESOURCE_ID, STR.RESOURCE_PATH FROM CMS_ONLINE_STRUCTURE STR ORDER BY STR.RESOURCE_PATH");
            res = stmt.executeQuery();

            while (res.next()) {
                CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(
                    res.getString(1),
                    res.getString(2),
                    res.getString(3),
                    res.getString(4));
                strEntries.put(strEntry.getStructureId(), strEntry);
            }
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
        return strEntries;
    }
}