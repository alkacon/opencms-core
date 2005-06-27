/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/TestCmsImportExportNonexistentUser.java,v $
 * Date   : $Date: 2005/06/27 23:22:23 $
 * Version: $Revision: 1.11 $
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
 
package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests exporting/import VFS data with nonexistent users.<p>
 * 
 * @author Thomas Weckert  
 * @version $Revision: 1.11 $
 */
public class TestCmsImportExportNonexistentUser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsImportExportNonexistentUser(String arg0) {
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
        suite.setName(TestCmsImportExport.class.getName());
                
        suite.addTest(new TestCmsImportExportNonexistentUser("testImportExportNonexistentUser"));
        
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
     * Tests exporting and import of VFS data with a nonexistent/deleted user.<p>
     * 
     * The username of the deleted user should in the export manifest be replaced 
     * by the name of the Admin user.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportExportNonexistentUser() throws Exception {
        
        String zipExportFilename = null;
        CmsObject cms = getCmsObject();
        
        try {
            String username = "tempuser";
            String password = "password";
            String filename = "/dummy1.txt";
            String contentStr = "This is a comment. I love comments.";
            zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testImportExportNonexistentUser.zip");
            byte[] content = contentStr.getBytes();
            CmsProject offlineProject = cms.getRequestContext().currentProject();
            
            // create a temporary user for this test case
            cms.createUser(username, password, "Temporary user for import/export test case", null);
            // add this user to the project managers user group
            cms.addUserToGroup(username, OpenCms.getDefaultUsers().getGroupProjectmanagers());
            
            // switch to the temporary user, offline project and default site
            cms.loginUser(username, password);
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);
            
            // create a dummy plain text file by the temporary user
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), content, null);
            // publish the dummy plain text file
            cms.unlockResource(filename);
            cms.publishResource(filename);
            
            // switch back to the Admin user, offline project and default site
            cms.loginUser("Admin", "admin");  
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);            
            // delete the temporary user
            cms.deleteUser(username);
            
            // export the dummy plain text file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(cms, vfsExportHandler, new CmsShellReport());
            
            // delete the dummy plain text file
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_REMOVE_SIBLINGS);
            // publish the deleted dummy plain text file
            cms.unlockResource(filename);
            cms.publishResource(filename);
            
            // re-import the exported dummy plain text file
            OpenCms.getImportExportManager().importData(cms, zipExportFilename, "/", new CmsShellReport());
        } catch (Exception e) {
            
            fail(e.toString());
        } finally {
            
            try {
                if (zipExportFilename != null) {
                    File file = new File(zipExportFilename);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Throwable t) {
                // intentionally left blank
            }
            
            cms.getRequestContext().restoreSiteRoot();                        
        }
    }
    
}
