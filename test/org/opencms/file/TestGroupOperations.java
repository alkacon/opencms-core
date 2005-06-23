/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestGroupOperations.java,v $
 * Date   : $Date: 2005/06/23 11:11:44 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.file;

import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for basic group operations without test import.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.5 $
 */
public class TestGroupOperations extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestGroupOperations(String arg0) {
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
        suite.setName(TestGroupOperations.class.getName());

        suite.addTest(new TestGroupOperations("testGetUsersOfGroup"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, null, false);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    }     
    
    /**
     * Tests the "getUsersOfGroup" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetUsersOfGroup() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing testGetUsersOfGroup");
        
        List users = cms.getUsersOfGroup("Guests");
        assertEquals("Export", ((CmsUser)users.get(0)).getName());
        assertEquals("Guest", ((CmsUser)users.get(1)).getName());
    }
}