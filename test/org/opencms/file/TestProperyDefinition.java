/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestProperyDefinition.java,v $
 * Date   : $Date: 2004/08/10 15:42:43 $
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

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;

/**
 * Unit test for the "createPropertyDefinition", "readPropertyDefiniton" and
 * "readAllPropertyDefintions" methods of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestProperyDefinition extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestProperyDefinition(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestProperyDefinition.class.getName());
                
        suite.addTest(new TestProperyDefinition("testCreatePropertyDefinition"));
        
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
     * Test the createPropertyDefintion method.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param propertyDefiniton1 the property definition to create
     * @throws Throwable if something goes wrong
     */
    public static void createPropertyDefinition(OpenCmsTestCase tc, CmsObject cms, String propertyDefiniton1) throws Throwable {            
       
        // get all propertydefintions
        List allPropertydefintions = cms.readAllPropertydefinitions();
        // create a property defintion with a dummy id value (the real id is created by db)
        CmsPropertydefinition prop = new CmsPropertydefinition(0, propertyDefiniton1, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);

        cms.createPropertydefinition(propertyDefiniton1);
        
        // check if the propertsdefintion was written
        tc.assertPropertydefinitionExist(cms, prop);
        // check if all other properties are still idenitcal
        tc.assertPropertydefinitions(cms, allPropertydefintions , prop);
    }
    
    
    /**
     * Test the createPropertyDefintion method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreatePropertyDefinition() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing createPropetyDefinition and readPropertyDefiniton");
        createPropertyDefinition(this, cms, "NewPropertyDefinition");   
    }  
    
  
}
