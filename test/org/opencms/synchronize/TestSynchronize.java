/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/synchronize/TestSynchronize.java,v $
 * Date   : $Date: 2004/07/27 14:40:23 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test cases for the VFS/RFS synchronization.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.6
 * @version $Revision: 1.1 $
 */
public class TestSynchronize extends OpenCmsTestCase {

    /** The XML string of an empty Xml page. */
    protected static final String C_NO_CONTENT_XML_PAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE page SYSTEM \"http://www.opencms.org/dtd/6.0/xmlpage.dtd\">\n"
        + "<page>\n"
        + "\t<elements>\n"
        + "\t\t<element name=\"body\" language=\"en\">\n"
        + "\t\t\t<links/>\n"
        + "\t\t\t<content><![CDATA[Test]]></content>\n"
        + "\t\t</element>\n"
        + "\t</elements>\n"
        + "</page>\n";

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

        TestSuite suite = new TestSuite();

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
            syncSettings.setDestinationPathInRfs(getTestDataPath() + "sync/");
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

                String name = resource.getName().toLowerCase();
                if (name.endsWith(".txt") || name.endsWith(".jsp")) {
                    // txt and jsp files get overwritten with some dummy content
                    modifyResourceInRfs(cms, resource, null);
                } else if (name.endsWith(".html")) {
                    // html pages get overwritten with an empty XML page
                    modifyResourceInRfs(cms, resource, C_NO_CONTENT_XML_PAGE);
                }
            }

            // synchronize everything back to the VFS
            new CmsSynchronize(cms, new CmsShellReport());

            // assert if the synchronization worked fine
            for (int i = 0, n = tree.size(); i < n; i++) {
                CmsResource vfsResource = (CmsResource)tree.get(i);
                String name = vfsResource.getName().toLowerCase();
                
                if (name.endsWith(".txt") || name.endsWith(".jsp") || name.endsWith(".html")) {
                    // assert the resource state
                    assertState(cms, cms.getSitePath(vfsResource), I_CmsConstants.C_STATE_CHANGED);
                    
                    // assert the modification date
                    String path = syncSettings.getDestinationPathInRfs() + CmsResource.getFolderPath(cms.getSitePath(vfsResource));
                    File rfsResource = new File(path, name);
                    
                    // TODO file.lastModified() doesn't return always the same timestamp, +/- a few millisecs
                    assertDateLastModifiedAfter(cms, cms.getSitePath(vfsResource), rfsResource.lastModified());                    
                } else {
                    assertState(cms, cms.getSitePath(vfsResource), I_CmsConstants.C_STATE_UNCHANGED);
                }
            }

        } finally {
            
            // remove the test data
            echo("Purging directory " + OpenCms.getSystemInfo().getSynchronizeSettings().getDestinationPathInRfs());
            CmsStaticExportManager.purgeDirectory(new File(getTestDataPath() + "sync/"));
        }

    }

    /**
     * Modifies a resource synchronized from the VFS to the RFS so that it is
     * synchronized back to the VFS as a modified resource.<p>
     * 
     * @param cms the current user's Cms object
     * @param resource the VFS resource to be modified in the RFS
     * @param content some file content, or null
     * @see #C_NO_CONTENT_XML_PAGE
     */
    protected void modifyResourceInRfs(CmsObject cms, CmsResource resource, String content) {

        PrintStream stream = null;
        FileOutputStream out = null;
        String path = null;

        try {
            path = getTestDataPath() + "sync" + cms.getSitePath(resource);
            out = new FileOutputStream(path);
            stream = new PrintStream(out);

            if (content != null) {
                stream.println(content);
            } else {
                stream.println("resource:" + cms.getSitePath(resource));
            }

            echo("\nModified " + path);
        } catch (Exception e) {
            echo("Error modifying " + path);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable t) {
                echo("Error closing I/O streams of " + path);
            }
        }
    }

}