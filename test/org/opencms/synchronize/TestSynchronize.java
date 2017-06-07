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

package org.opencms.synchronize;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test cases for the VFS/RFS synchronization.<p>
 *
 * @since 6.0.0
 */
public class TestSynchronize extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSynchronize(String arg0) {

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
        suite.setName(TestSynchronize.class.getName());

        suite.addTest(new TestSynchronize("testSynchronize"));
        suite.addTest(new TestSynchronize("testLoadSaveSynchronizeSettings"));
        suite.addTest(new TestSynchronize("testSynchronizeSeveralFolders"));

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
     * Tests loading and saving the user synchronize settings.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLoadSaveSynchronizeSettings() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing loading and saving the synchronization settings of a user");

        CmsUserSettings userSettings = new CmsUserSettings(cms);
        // default sync settings are null
        assertNull(userSettings.getSynchronizeSettings());

        String source = "/folder1/";
        String dest = getTestDataPath("");
        if (dest.endsWith(File.separator)) {
            dest = dest.substring(0, dest.length() - 1);
        }

        CmsSynchronizeSettings syncSettings = new CmsSynchronizeSettings();
        syncSettings.setEnabled(true);
        ArrayList sourceList = new ArrayList();
        sourceList.add(source);
        syncSettings.setSourceListInVfs(sourceList);
        syncSettings.setDestinationPathInRfs(dest);

        // store the settings
        userSettings.setSynchronizeSettings(syncSettings);
        userSettings.save(cms);

        // login another user
        cms.loginUser("test1", "test1");

        userSettings = new CmsUserSettings(cms);
        // default sync settings are null for this user
        assertNull(userSettings.getSynchronizeSettings());

        // login another user
        cms.loginUser(OpenCms.getDefaultUsers().getUserAdmin(), "admin");

        userSettings = new CmsUserSettings(cms);
        syncSettings = userSettings.getSynchronizeSettings();
        assertNotNull(syncSettings);

        assertTrue(syncSettings.isEnabled());
        assertEquals(source, syncSettings.getSourceListInVfs().get(0));
        assertEquals(dest, syncSettings.getDestinationPathInRfs());
    }

    /**
     * Tests the synchronize function.<p>
     *
     * Synchronizes everything below "/" into the RFS, modifies .txt, .jsp and .html
     * files in the RFS, and synchronizes everything back into the VFS.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSynchronize() throws Exception {

        String source = "/sites/default/";

        // save what gets synchronized
        CmsSynchronizeSettings syncSettings = new CmsSynchronizeSettings();

        String dest = getTestDataPath("") + "sync1" + File.separator;
        File destFolder = new File(dest);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        syncSettings.setDestinationPathInRfs(dest);
        ArrayList sourceList = new ArrayList();
        sourceList.add(source);
        syncSettings.setSourceListInVfs(sourceList);
        syncSettings.setEnabled(true);

        try {
            CmsObject cms = getCmsObject();
            echo("Testing synchronization of files and folders");

            cms.getRequestContext().setSiteRoot("/");
            storeResources(cms, source);

            echo("Synchronizing "
                + syncSettings.getSourceListInVfs()
                + " with "
                + syncSettings.getDestinationPathInRfs());

            // synchronize everything to the RFS
            new CmsSynchronize(cms, syncSettings, new CmsShellReport(cms.getRequestContext().getLocale()));

            // modify resources in the RFS
            List tree = cms.readResources(source, CmsResourceFilter.ALL);
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource resource = (CmsResource)tree.get(i);

                int type = resource.getTypeId();
                if (((type == CmsResourceTypePlain.getStaticTypeId()))
                    || (CmsResourceTypeJsp.isJspTypeId(type))
                    || (type == CmsResourceTypeXmlPage.getStaticTypeId())) {
                    // modify date last modified on resource
                    touchResourceInRfs(cms, resource, syncSettings);
                }
            }

            // sleep 2 seconds to avoid issues with file system timing
            Thread.sleep(2000);

            // synchronize everything back to the VFS
            new CmsSynchronize(cms, syncSettings, new CmsShellReport(cms.getRequestContext().getLocale()));

            // assert if the synchronization worked fine
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource vfsResource = (CmsResource)tree.get(i);
                int type = vfsResource.getTypeId();
                String vfsname = cms.getSitePath(vfsResource);

                System.out.println("( " + i + " / " + (n - 1) + " ) Checking " + vfsname);
                if (((type == CmsResourceTypePlain.getStaticTypeId()))
                    || (type == CmsResourceTypeJsp.getJSPTypeId())
                    || (type == CmsResourceTypeXmlPage.getStaticTypeId())) {
                    // assert the resource state
                    assertState(cms, vfsname, CmsResource.STATE_CHANGED);
                    // assert the modification date
                    File rfsResource = new File(getRfsPath(cms, vfsResource, syncSettings));
                    assertDateLastModifiedAfter(cms, vfsname, rfsResource.lastModified());
                } else {
                    assertState(cms, vfsname, CmsResource.STATE_UNCHANGED);
                }
            }

        } finally {

            // remove the test data
            echo("Purging directory " + dest);
            CmsFileUtil.purgeDirectory(new File(dest));
        }
    }

    /**
     * Tests the synchronize function with more then one source folder.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSynchronizeSeveralFolders() throws Exception {

        // save what gets synchronized
        CmsSynchronizeSettings syncSettings = new CmsSynchronizeSettings();

        String dest = getTestDataPath("") + "sync2" + File.separator;
        File destFolder = new File(dest);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        syncSettings.setDestinationPathInRfs(dest);
        ArrayList sourceList = new ArrayList();
        sourceList.add("/sites/default/folder1/subfolder11/");
        sourceList.add("/sites/default/folder1/subfolder12/");
        sourceList.add("/sites/default/folder2/subfolder21/");
        syncSettings.setSourceListInVfs(sourceList);
        syncSettings.setEnabled(true);

        try {
            CmsObject cms = getCmsObject();
            echo("Testing synchronization of several folders");

            cms.getRequestContext().setSiteRoot("/");
            storeResources(cms, "/");

            // synchronize everything to the RFS
            new CmsSynchronize(cms, syncSettings, new CmsShellReport(cms.getRequestContext().getLocale()));

            Iterator it = syncSettings.getSourceListInVfs().iterator();
            List tree = new ArrayList();

            // modify resources in the RFS
            while (it.hasNext()) {
                String source = (String)it.next();
                List subTree = cms.readResources(source, CmsResourceFilter.ALL);
                tree.addAll(subTree);
                for (int i = 0, n = subTree.size(); i < n; i++) {
                    CmsResource resource = (CmsResource)subTree.get(i);

                    int type = resource.getTypeId();
                    if (((type == CmsResourceTypePlain.getStaticTypeId()))
                        || (CmsResourceTypeJsp.isJspTypeId(type))
                        || (type == CmsResourceTypeXmlPage.getStaticTypeId())) {
                        // modify date last modified on resource
                        touchResourceInRfs(cms, resource, syncSettings);
                    }
                }
            }

            // sleep 2 seconds to avoid issues with file system timing
            Thread.sleep(2000);

            // synchronize everything back to the VFS
            new CmsSynchronize(cms, syncSettings, new CmsShellReport(cms.getRequestContext().getLocale()));

            // assert if the synchronization worked fine
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource vfsResource = (CmsResource)tree.get(i);
                int type = vfsResource.getTypeId();
                String vfsname = cms.getSitePath(vfsResource);

                System.out.println("( " + i + " / " + (n - 1) + " ) Checking " + vfsname);
                if (((type == CmsResourceTypePlain.getStaticTypeId()))
                    || (type == CmsResourceTypeJsp.getJSPTypeId())
                    || (type == CmsResourceTypeXmlPage.getStaticTypeId())) {
                    // assert the resource state
                    assertState(cms, vfsname, CmsResource.STATE_CHANGED);
                    // assert the modification date
                    File rfsResource = new File(getRfsPath(cms, vfsResource, syncSettings));
                    assertDateLastModifiedAfter(cms, vfsname, rfsResource.lastModified());
                } else {
                    assertState(cms, vfsname, CmsResource.STATE_UNCHANGED);
                }
            }

        } finally {

            // remove the test data
            echo("Purging directory " + dest);
            CmsFileUtil.purgeDirectory(new File(dest));
        }
    }

    /**
     * Returns a rfs path for a given resource to be synchronized.<p>
     *
     * @param cms the cms context
     * @param resource the resource to synchronize
     * @param syncSettings the synchronization settings
     *
     * @return the rfs path
     */
    private String getRfsPath(CmsObject cms, CmsResource resource, CmsSynchronizeSettings syncSettings) {

        String path = syncSettings.getDestinationPathInRfs() + cms.getSitePath(resource);
        return CmsFileUtil.normalizePath(path);
    }

    /**
     * "Touches" the last modification date of a resource the RFS so that it is
     * synchronized back to the VFS as a modified resource.<p>
     *
     * @param cms the current user's Cms object
     * @param resource the VFS resource to be modified in the RFS
     * @param syncSettings the synchronization settings
     */
    private void touchResourceInRfs(CmsObject cms, CmsResource resource, CmsSynchronizeSettings syncSettings) {

        // touch file 2 seconds in the future
        String path = getRfsPath(cms, resource, syncSettings);
        System.out.println("Touching: " + path);
        File file = new File(path);
        file.setLastModified(file.lastModified() + 2000);
    }
}