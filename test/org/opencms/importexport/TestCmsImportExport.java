/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/TestCmsImportExport.java,v $
 * Date   : $Date: 2004/11/10 16:35:19 $
 * Version: $Revision: 1.2 $
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
 
package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comment for <code>TestCmsImportExport</code>.<p>
 */
public class TestCmsImportExport extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsImportExport(String arg0) {
        super(arg0);
    } 

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestCmsImportExport.class.getName());
                
        suite.addTest(new TestCmsImportExport("testUserImport"));
        suite.addTest(new TestCmsImportExport("testImportResourceTranslator"));
        
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
     * Tests if the user passwords are imported correctly.<p>
     * The password digests are hex-128 (legacy) encoded, and
     * should be converted to base-64. 
     * 
     * @throws Exception if something goes wrong
     */
    public void testUserImport() throws Exception {
        
        CmsObject cms = getCmsObject();
        I_CmsPasswordHandler passwordHandler = new CmsDefaultPasswordHandler();
        CmsUser user;

        // check if passwords will be converted
        if ("com.opencms.legacy.CmsLegacyPasswordHandler".equals(OpenCms.getPasswordHandler().getClass().getName())) {
            echo("Skipping test since digest encoding is set to legacy");
            return;
        }
            
        // check the admin user
        user = cms.readUser("Admin");
        assertEquals(user.getPassword(), passwordHandler.digest("admin", "MD5", "UTF-8"));

        // check the guest user
        user = cms.readUser("Guest");
        assertEquals(user.getPassword(), passwordHandler.digest("", "MD5", "UTF-8"));
        
        // check the test1 user
        user = cms.readUser("test1");
        assertEquals(user.getPassword(), passwordHandler.digest("test1", "MD5", "UTF-8"));

        // check the test2 user
        user = cms.readUser("test2");
        assertEquals(user.getPassword(), passwordHandler.digest("test2", "MD5", "UTF-8"));
    }
    
    /**
     * Tests the resource translation during import.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportResourceTranslator() throws Exception {
        
        CmsObject cms = getCmsObject();        
        cms.getRequestContext().setSiteRoot("/sites/default");
        
        String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
        OpenCms.getImportExportManager().importData(cms, importFile, "/", new CmsShellReport());        
                
    }
}
