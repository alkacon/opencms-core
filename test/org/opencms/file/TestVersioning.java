/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestVersioning.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.3 $
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
 
package org.opencms.file;

import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms versioning.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.3 $
 */
public class TestVersioning extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestVersioning(String arg0) {
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
        suite.setName(TestVersioning.class.getName());
        
        suite.addTest(new TestVersioning("testVersioningLimit"));
        
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
     * Test that the versions are properly updated 
     * after reaching the limit of stored versions.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testVersioningLimit() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing versioning limit");
        
        String source = "/index.html";
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // set the versioning settings
        OpenCms.getSystemInfo().setVersionHistorySettings(true, 3);
        
        // make 5 versions
        for (int i = 0; i < 5; i++) {
            if (i<3) {
                assertEquals(i+1, cms.readAllBackupFileHeaders(source).size());
            } else {
                assertEquals(3, cms.readAllBackupFileHeaders(source).size());
            }
            cms.lockResource(source);
            cms.touch(source, System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false);
            cms.unlockResource(source);
            cms.publishResource(source);
        }
    }        
}
