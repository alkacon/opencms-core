/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestMoveRename.java,v $
 * Date   : $Date: 2004/06/29 14:38:56 $
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
 
package org.opencms.file;

import org.opencms.lock.CmsLock;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for move/reanme operation.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class TestMoveRename extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestMoveRename(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestMoveRename("testMoveSingleResource"));
        
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
     * Tests the "move single resource" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testMoveSingleResource() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing move of a file");
        
        String source = "/folder1/index.html";
        String destination = "/folder1/index_move.html";

        storeResources(cms, source);        
        
        cms.lockResource(source);
        cms.moveResource(source, destination);     
        
        // source resource:
        
        // project must be current project
        assertProject(cms, source, cms.getRequestContext().currentProject());
        // state must be "deelted"
        assertState(cms, source, I_CmsConstants.C_STATE_DELETED);
        // assert lock state
        assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        // "internal" property must have been added
        assertPropertyNew(cms, source, new CmsProperty(I_CmsConstants.C_PROPERTY_INTERNAL, 
            String.valueOf(cms.getRequestContext().currentProject().getId()), null));
        // one sibling must have been added
        assertSiblingCountIncremented(cms, source, 1);
        // now assert the filter for the rest of the attributes        
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_MOVE_SOURCE);   
        
        // destination resource
        
        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, destination, I_CmsConstants.C_STATE_NEW);
        // assert lock state
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        // set filter mapping
        setMapping(destination, source);
        // one sibling must have been added
        assertSiblingCountIncremented(cms, destination, 1);        
        // now assert the filter for the rest of the attributes        
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);       
    }  
}
