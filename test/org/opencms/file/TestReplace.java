/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestReplace.java,v $
 * Date   : $Date: 2004/08/20 11:44:42 $
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

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLock;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for replace operation.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class TestReplace extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestReplace(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestReplace.class.getName());
                
        suite.addTest(new TestReplace("testReplaceResourceContent"));
        
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
     * Tests the "replace resource" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReplaceResourceContent() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing replacement of file content");
        
        String path = "/types/text.txt";
        String contentStr = "Hello this is the new content";
        
        long timestamp = System.currentTimeMillis();
        
        storeResources(cms, path);
        cms.lockResource(path);
        cms.replaceResource(path, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, contentStr.getBytes(), null);        
        
        // project must be current project
        assertProject(cms, path, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, path, I_CmsConstants.C_STATE_CHANGED);
        // date lastmodified must be new
        assertDateLastModifiedAfter(cms, path, timestamp);
        // user lastmodified must be current user
        assertUserLastModified(cms, path, cms.getRequestContext().currentUser());
        // assert lock state
        assertLock(cms, path, CmsLock.C_TYPE_EXCLUSIVE);
        // assert new content
        assertContent(cms, path, contentStr.getBytes());
        // now check the rest of the attributes
        assertFilter(cms, path, OpenCmsTestResourceFilter.FILTER_REPLACERESOURCE);       
    }    
}
