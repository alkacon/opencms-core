/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestUndoChanges.java,v $
 * Date   : $Date: 2004/05/29 09:30:21 $
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
 
package org.opencms.file;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.test.OpenCmsTestResourceStorage;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "undoChanges" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestUndoChanges extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestUndoChanges(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestUndoChanges("testUndoChanges"));
        
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
     * Test the touch method to touch a single resource.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void undoChanges(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {            
              
        // create a global storage and store the resource
        tc.createStorage(cms, "undoChanges");
        tc.switchStorage("undoChanges");
        tc.storeResources(cms, resource1);
        tc.switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        // now do a touch on the resource
        TestTouch.touchResource(tc, cms, resource1);
        
        // change a property
        CmsProperty property1 = new CmsProperty("Title", "undoChanges", null);  
        TestProperty.writeProperty(tc, cms, resource1, property1);
                      
        // now undo everything
        cms.undoChanges(resource1, false);
        
        tc.switchStorage("undoChanges");
        
        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
    }

    /**
     * Test the undoChanges method to touch a single resource.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testUndoChanges() throws Throwable {
        
        CmsObject cms = getCmsObject();        
        echo("Testing undoChanges on a file");
        undoChanges(this, cms, "/release/installation.html");
    }
}
