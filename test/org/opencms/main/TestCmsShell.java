/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/TestCmsShell.java,v $
 * Date   : $Date: 2005/06/21 11:05:17 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;

/** 
 * Test cases for the OpenCms shell.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.5 $
 * 
 * @since 5.0
 */
public class TestCmsShell extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsShell(String arg0) {
        super(arg0);
    }

    /**
     * Tests the Junit OpenCms VFS test setup using the "base" test class.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSetup() throws Throwable {
        
        CmsObject cms;        
        
        // setup OpenCms using the base test class
        cms = setupOpenCms("simpletest", "/sites/default/");        
        // check the returned CmsObject
        assertEquals(cms.getRequestContext().currentUser(), cms.readUser("Admin"));
        assertEquals(cms.getRequestContext().currentProject(), cms.readProject("Offline"));
        assertEquals(cms.getRequestContext().getSiteRoot(), "/sites/default");
        
        // check the CmsObject initialization
        cms = getCmsObject();        
        // check the returned CmsObject
        assertEquals(cms.getRequestContext().currentUser(), cms.readUser("Admin"));
        assertEquals(cms.getRequestContext().currentProject(), cms.readProject("Offline"));
        assertEquals(cms.getRequestContext().getSiteRoot(), "/sites/default");
                
        // remove OpenCms
        removeOpenCms();
    }
    
    /**
     * Tests the CmsShell and setup procedure.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsShell() throws Throwable {
        
        // create a new database first
        setupDatabase();
        
        // create a shell instance
        CmsShell shell = new CmsShell(
            getTestDataPath("WEB-INF" + File.separator),
            null,
            null, 
            "${user}@${project}>", null);
        
        // open the test script 
        File script;
        FileInputStream stream;
        
        // start the shell with the base script
        script = new File(getTestDataPath("scripts/script_base.txt"));
        stream = new FileInputStream(script);        
        shell.start(stream);
        
        // add the default folders by script
        script = new File(getTestDataPath("scripts/script_default_folders.txt"));
        stream = new FileInputStream(script);        
        shell.start(stream); 
        
        // log in the Admin user and switch to the setup project
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("_setupProject"));
        
        // import the "simpletest" files
        importResources(cms, "simpletest", "/sites/default/");
        
        // publish the current project by script
        script = new File(getTestDataPath("scripts/script_publish.txt"));
        stream = new FileInputStream(script);        
        shell.start(stream);                
        
        // get the name of the folder for the backup configuration files
        File configBackupDir = new File(getTestDataPath("WEB-INF/config/backup/"));
        
        // exit the shell
        shell.exit();
        
        // remove the database
        removeDatabase();
        
        // remove the backup configuration files
        CmsFileUtil.purgeDirectory(configBackupDir);
    }
}
