/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/synchronize/TestSynchronize.java,v $
 * Date   : $Date: 2005/02/17 12:46:01 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test cases for the VFS/RFS synchronization.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.6
 * @version $Revision: 1.7 $
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
     * Tests the synchronize function.<p>
     * 
     * Synchronizes everything below "/" into the RFS, modifies .txt, .jsp and .html
     * files in the RFS, and synchronizes everything back into the VFS.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSynchronize() throws Throwable {

        try {
            CmsObject cms = getCmsObject();
            echo("Testing synchronization of files and folders");

            String source = "/";
            storeResources(cms, source);

            // save what gets synchronized
            CmsSynchronizeSettings syncSettings = OpenCms.getSystemInfo().getSynchronizeSettings();
            syncSettings.setDestinationPathInRfs(getTestDataPath("") + "sync" + File.separator);
            syncSettings.setSourcePathInVfs(source);

            echo("Synchronizing "
                + OpenCms.getSystemInfo().getSynchronizeSettings().getSourcePathInVfs()
                + " with "
                + OpenCms.getSystemInfo().getSynchronizeSettings().getDestinationPathInRfs());

            // synchronize everything to the RFS
            new CmsSynchronize(cms, new CmsShellReport());

            // modify resources in the RFS
            List tree = getSubtree(cms, source);
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource resource = (CmsResource)tree.get(i);

                int type = resource.getTypeId();
                if (((type == CmsResourceTypePlain.C_RESOURCE_TYPE_ID)) 
                || (type == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)
                || (type == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID)) {
                    // modify date last modified on resource
                    touchResourceInRfs(cms, resource, syncSettings);
                }
            }

            // sleep 4 seconds to avoid issues with file system timing
            Thread.sleep(4000);
            
            // synchronize everything back to the VFS
            new CmsSynchronize(cms, new CmsShellReport());

            // assert if the synchronization worked fine
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource vfsResource = (CmsResource)tree.get(i);
                int type = vfsResource.getTypeId();
                String vfsname = cms.getSitePath(vfsResource);
                
                System.out.println("( " + i + " / " + (n-1) + " ) Checking " + vfsname);
                if (((type == CmsResourceTypePlain.C_RESOURCE_TYPE_ID)) 
                || (type == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID) 
                || (type == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID)) {
                    // assert the resource state
                    assertState(cms, vfsname, I_CmsConstants.C_STATE_CHANGED);                    
                    // assert the modification date
                    File rfsResource = new File(getRfsPath(cms, vfsResource, syncSettings));
                    assertDateLastModifiedAfter(cms, vfsname, rfsResource.lastModified());                    
                } else {
                    assertState(cms, vfsname, I_CmsConstants.C_STATE_UNCHANGED);
                }
            }

        } finally {
            
            // remove the test data
            echo("Purging directory " + OpenCms.getSystemInfo().getSynchronizeSettings().getDestinationPathInRfs());
            CmsFileUtil.purgeDirectory(new File(getTestDataPath("sync")));
        }

    }

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
     */
    private void touchResourceInRfs(CmsObject cms, CmsResource resource, CmsSynchronizeSettings syncSettings) {

        // touch file 2 seconds in the future
        String path = getRfsPath(cms, resource, syncSettings);
        System.out.println("Touching: " + path);
        File file = new File(path);
        file.setLastModified(file.lastModified() + 2000);
    }
}