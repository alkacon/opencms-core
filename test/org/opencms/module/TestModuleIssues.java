/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/TestModuleIssues.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
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
 
package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for issues found in the new module mechanism.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $
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
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestModuleIssues.class.getName());
                
        suite.addTest(new TestModuleIssues("testAdditionalSystemFolder"));
        
        // important: this must be the last called method since the OpenCms installation is removed from there
        suite.addTest(new TestModuleIssues("testShutdownMethod"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms("simpletest", "/sites/default/");
            }
            
            protected void tearDown() {
                // done in "testShutdownMethod"
                // removeOpenCms();
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
    
    /**
     * Issue: Sthudown method never called on module.<p>
     * 
     * @throws Exception if something goews wrong
     */
    public void testShutdownMethod() throws Exception {
        
        echo("Testing module shutdown method");
              
        String moduleName = "org.opencms.configuration.TestModule1";
        
        // basic check if the module was imported correctly (during configuration)
        if (! OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }
        
        I_CmsModuleAction actionInstance = OpenCms.getModuleManager().getActionInstance(moduleName);
        
        if (actionInstance == null) {
            fail("Module '" + moduleName + "' has no action instance!");            
        }

        if (! (actionInstance instanceof TestModuleActionImpl)) {
            fail("Module '" + moduleName + "' has action class of unexpected type!");                        
        } 
        
        // remove OpenCms installations, must call shutdown
        removeOpenCms();
        
        // check if shutdown flag was set to "true"
        assertTrue(TestModuleActionImpl.m_shutDown);   
        
        // reset flag for next test
        TestModuleActionImpl.m_shutDown = false;
    }
}
