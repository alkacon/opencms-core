/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestCmsObjectWritePropertyObject.java,v $
 * Date   : $Date: 2004/05/26 16:07:38 $
 * Version: $Revision: 1.3 $
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

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for the "touch" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class TestCmsObjectWritePropertyObject extends OpenCmsTestCase {
        
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsObjectWritePropertyObject(String arg0) {
        super(arg0);
    }
    
    
    /**
     * Tests the touch method in the CmsObject.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsObjectTouch() throws Throwable {
        
        // setup OpenCms
        CmsObject cms = setupOpenCms("simpletest", "/sites/default/");
              
        // now do the test itself
        
        String resource1 = "/release/installation.html";            

        storeResources(cms, resource1);
 
        // do the touch operation
        long timestamp = System.currentTimeMillis();
        
        CmsProperty property1 = new CmsProperty("Title","OpenCms",null);
        CmsProperty property2 = new CmsProperty("Title","OpenCmsNav",null);
        List propertyList = new ArrayList();
        propertyList.add(property1);
        propertyList.add(property2);
        
        cms.writePropertyObject(resource1, property1);
        
        //cms.writePropertyObjects(resource1, propertyList); 
        
        // now evaluate the result
        assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
        // date last modified must be after the test timestamp
        assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        // the property must have the new value
        assertPropertyChanged(cms, resource1, property1);
        
        // remove OpenCms
        removeOpenCms();
    }
    
}
