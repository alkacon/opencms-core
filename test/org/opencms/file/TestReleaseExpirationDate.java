/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestReleaseExpirationDate.java,v $
 * Date   : $Date: 2004/06/01 16:07:46 $
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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;

/**
 * Unit test for the "readFileHeader" method of the CmsObject to test the release and expiration date.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestReleaseExpirationDate extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestReleaseExpirationDate(String arg0) {
        super(arg0);       
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
       
        
        suite.addTest(new TestReleaseExpirationDate("testReadBeforeReleaseDate"));
        suite.addTest(new TestReleaseExpirationDate("testReadInValidTimeRange"));
        suite.addTest(new TestReleaseExpirationDate("testReadAfterExpirationDate"));
        suite.addTest(new TestReleaseExpirationDate("testReadBeforeReleaseDateIgnore"));
        suite.addTest(new TestReleaseExpirationDate("testReadInValidTimeRangeIgnore"));
        suite.addTest(new TestReleaseExpirationDate("testReadAfterExpirationDateIgnore"));
        
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
     * Test readFileHeader of a file before its release date.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readBeforeReleaseDate(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {            
              
        tc.storeResources(cms, resource1);
      
        // preperation, modify the release date
        CmsFile preperationRes = (CmsFile)cms.readFileHeader(resource1, CmsResourceFilter.ALL);
        // set the release date to one hour in the future
        preperationRes .setDateReleased(System.currentTimeMillis() + (60 * 60 *1000));
        cms.writeFileHeader(preperationRes);
        
        // now try to access the resource
        try {
            cms.readFileHeader(resource1, filter);
            if (!filter.includeDeleted()) {
                // the file could be read, despite the release date set in the future
                fail("Resource "+ resource1+ " could be read before release date");
            }               
        } catch (CmsException e) {
            if (filter.includeDeleted()) {
                fail("Resource "+ resource1+ " could not be read");
            }  
        }      
    }

    /**
     * Test readFileHeader of a file after its expirationrelease date.<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readAfterExpirationDate(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {            
              
        tc.storeResources(cms, resource1);
      
        // preperation, modify the expiration date
        CmsFile preperationRes = (CmsFile)cms.readFileHeader(resource1, CmsResourceFilter.ALL);
        // set the expiration date to one hour in the past
        preperationRes .setDateExpired(System.currentTimeMillis() - (60 * 60 *1000));
        cms.writeFileHeader(preperationRes);
        
        // now try to access the resource
        try {
            cms.readFileHeader(resource1, filter);
            if (!filter.includeDeleted()) {
                // the file could be read, despite the expiration date was set to the past
                fail("Resource "+ resource1+ " could be read after the expiration date");
            }               
        } catch (CmsException e) {
            if (filter.includeDeleted()) {
                fail("Resource "+ resource1+ " could not be read");
            }  
        }      
    }
  
    /**
     * Test readFileHeader of a file in its valid time range<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @param filter the filter to use
     * @throws Throwable if something goes wrong
     */
    public static void readInValidTimeRange(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsResourceFilter filter) throws Throwable {            
              
        tc.storeResources(cms, resource1);
      
        // preperation, modify the expiration date
        CmsFile preperationRes = (CmsFile)cms.readFileHeader(resource1, CmsResourceFilter.ALL);
        // set the release date to one hour in the future
        preperationRes .setDateReleased(System.currentTimeMillis()- (60 * 60 *1000));
        // set the expiration date to one hour in the past
        preperationRes .setDateExpired(System.currentTimeMillis() + (60 * 60 *1000));
        cms.writeFileHeader(preperationRes);
        
        // now try to access the resource
        try {
            cms.readFileHeader(resource1, filter);            
        } catch (CmsException e) {
                fail("Resource "+ resource1+ " could not be read");
        }      
    }
    
    /**
     * Test readFileHeader of a file before its release date.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadBeforeReleaseDate() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date");
        readBeforeReleaseDate(this, cms, "/folder1/page1.html", CmsResourceFilter.DEFAULT);
    }
    
    /**
     * Test readFileHeader of a file after its expiration date.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadAfterExpirationDate() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date");
        readAfterExpirationDate(this, cms, "/folder1/page2.html", CmsResourceFilter.DEFAULT);
    }

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadInValidTimeRange() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range");
        readInValidTimeRange(this, cms, "/folder1/page3.html", CmsResourceFilter.DEFAULT);
    }
    
    /**
     * Test readFileHeader of a file before its release date.<p>
     * The valid time range will be ignored.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadBeforeReleaseDateIgnore() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file before the release date, ignoring valid timerange");
        readBeforeReleaseDate(this, cms, "/folder1/page1.html", CmsResourceFilter.ALL);
    }
    
    /**
     * Test readFileHeader of a file after its expiration date.<p>
     * The valid time range will be ignored.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadAfterExpirationDateIgnore() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file after the expiration date, ignoring valid timerange");
        readAfterExpirationDate(this, cms, "/folder1/page2.html", CmsResourceFilter.ALL);
    }

    /**
     * Test readFileHeader of a file in its valid time range.<p>
     * The valid time range will be ignored.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReadInValidTimeRangeIgnore() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing readFileHeader of a file in its valid time range, ignoring valid timerange");
        readInValidTimeRange(this, cms, "/folder1/page3.html", CmsResourceFilter.ALL);
    }
    
}
