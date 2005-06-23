/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/performance/TestFill.java,v $
 * Date   : $Date: 2005/06/23 11:12:02 $
 * Version: $Revision: 1.4 $
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
 
package org.opencms.test.performance;

import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for lock operation.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.4 $
 */
public class TestFill extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestFill(String arg0) {
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
        suite.setName(TestFill.class.getName());
             
        //suite.addTest(new TestFill("testFillResources"));
        suite.addTest(new TestFill("testResWithProps"));
        
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
     * fills the db with app in average: <br>
     *    <ul>
     *        <li>10 folders in 5 subfolders
     *        <li>20 files in each folder, 75% binary / 30% text files,<li>
     *        <li>with 10 properties, 60% individual / 30% shared properties.<li>
     *    </ul>
     * that is a total of app. 10000 files, and 100000 property values. <p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFillResources() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Test filling the db with tons of files");
        long t = System.currentTimeMillis();
        int nFiles = generateContent(cms, "/", 10, 5, 10, 0.6, 20, 0.75);
        t = System.currentTimeMillis() - t;
        echo("" + nFiles + " files have been created in " + t + " msecs");
    }    
    
    /**
     * Tests the <code>{@link CmsObject#readResourcesWithProperty(String)}</code> method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testResWithProps() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing the CmsObject#readResourcesWithProperty(String) method");
        String prop = "NavPos";
        long t = System.currentTimeMillis();
        List l = cms.readResourcesWithProperty(prop);
        t = System.currentTimeMillis() - t;
        echo("There are " + l.size() + " files with prop " + prop);
        echo("readResourcesWithProperty(String) performance was: " + t + " msecs");
    }    
}

