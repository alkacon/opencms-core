/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/Attic/CmsShellTest.java,v $
 * Date   : $Date: 2004/05/25 11:26:45 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.io.FileInputStream;

/** 
 * Test cases for the OpenCms shell.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.0
 */
public class CmsShellTest extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public CmsShellTest(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests the CmsShell.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsShell() throws Throwable {
        
        // create a new database first
        setupDatabase();
        
        // create a shell instance
        CmsShell shell = new CmsShell(
            getTestDataPath() + "WEB-INF" + File.separator,
            "${user}@${project}>", 
            null);
        
        // open the test script 
        File script = new File(getTestDataPath() + "scripts/script1.txt");
        FileInputStream stream = new FileInputStream(script);
        
        // start the shell with the script
        shell.start(stream);
        
        // log in the Admin user as a test
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser("Admin", "admin");
        
        // get the name of the folder for the backup configuration files
        File configBackupDir = OpenCmsCore.getInstance().getConfigurationManager().getBackupFolder();
        
        // exit the shell
        shell.exit();
        
        // remove the database
        removeDatabase();
        
        // remove the backup configuration files
        CmsStaticExportManager.purgeDirectory(configBackupDir);
    }
}
