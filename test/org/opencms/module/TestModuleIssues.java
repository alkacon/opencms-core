/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/TestModuleIssues.java,v $
 * Date   : $Date: 2004/07/27 11:15:54 $
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
 
package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for issues found in the new module mechanism.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class TestModuleIssues extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestModuleIssues(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestModuleIssues("testAdditionalSystemFolder"));
        
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
     * Issue: Additional "system" folder created in current site after module import.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAdditionalSystemFolder() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing for additional 'system' folder after module import");
              
        String moduleName = "org.opencms.test.modules.test3";        
        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/" + moduleName + ".zip");
        OpenCms.getImportExportManager().importData(cms, moduleFile, null, new CmsShellReport());
        
        // basic check if the module was imported correctly
        if (! OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }
        
        cms.getRequestContext().setSiteRoot("/");
        boolean found = true;
        try {
            cms.readFolder("/sites/default/system/");
        } catch (CmsVfsResourceNotFoundException e) {
            // this is the expected result
            found = false;
        }
        
        if (found) {
            fail("Additional 'system' folder was created!");
        }
    }  
}
