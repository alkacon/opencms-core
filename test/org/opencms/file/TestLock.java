/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestLock.java,v $
 * Date   : $Date: 2004/06/29 14:38:56 $
 * Version: $Revision: 1.4 $
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
import org.opencms.lock.CmsLockException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for lock operation.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 */
public class TestLock extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestLock(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestLock("testLockRequired"));
        suite.addTest(new TestLock("testLockInherit"));
        suite.addTest(new TestLock("testLockForSiblings"));
        suite.addTest(new TestLock("testLockForBaseOperations"));
        
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
     * Tests lock status of a resource for basic operations.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLockForBaseOperations() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing lock state for basic operations");
        
        String source = "/types/text.txt";
        String destination1 = "/types/text_new1.txt";
        String destination2 = "/types/text_new2.txt";
        storeResources(cms, source);
        
        // copy source
        cms.copyResource(source, destination1, I_CmsConstants.C_COPY_AS_NEW);

        // since source was not locked, destination must be locked exclusive
        // and source must still be unlocked
        assertLock(cms, source, CmsLock.C_TYPE_UNLOCKED);
        assertLock(cms, destination1, CmsLock.C_TYPE_EXCLUSIVE);
        
        // copy source again
        cms.lockResource(source);        
        cms.copyResource(source, destination2, I_CmsConstants.C_COPY_AS_NEW);  
        
        // both source and destination must be exlusive locked
        assertLock(cms, source, CmsLock.C_TYPE_EXCLUSIVE);
        assertLock(cms, destination2, CmsLock.C_TYPE_EXCLUSIVE);
                
        // now some move tests
        source = "/types/jsp.jsp";
        destination1 = "/types/jsp_new1.html";

        // lock resource
        cms.lockResource(source);
        cms.moveResource(source, destination1);
        
        // since source was locked, destination must be shared exclusive
        assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        assertLock(cms, destination1, CmsLock.C_TYPE_EXCLUSIVE);
    }    
    
    /**
     * Tests lock status of a resource during sibling creation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLockForSiblings() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing lock state after sibling creation");
        
        String source = "/folder2/index.html";
        String destination1 = "/folder2/index_sib1.html";
        String destination2 = "/folder2/index_sib2.html";
        storeResources(cms, source);
        
        // copy source
        cms.copyResource(source, destination1, I_CmsConstants.C_COPY_AS_SIBLING);

        // since source was not locked, destination must be locked exclusive
        // and source must be locked shared
        assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        assertLock(cms, destination1, CmsLock.C_TYPE_EXCLUSIVE);
        
        // copy source again
        cms.copyResource(source, destination2, I_CmsConstants.C_COPY_AS_SIBLING);  
        
        // since one sibling was already exclusive locked, 
        // new sibling must be shared locked
        assertLock(cms, source, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        assertLock(cms, destination1, CmsLock.C_TYPE_EXCLUSIVE);        
        assertLock(cms, destination2, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        
        // same stuff but in a different order 
        source = "/folder2/page1.html";
        destination1 = "/folder2/page1_sib1.html";
        destination2 = "/folder2/page1_sib2.html";
        
        // this time source is already locked
        cms.lockResource(source);
        cms.createSibling(source, destination1, null);
        
        // since source was locked, destination must be shared exclusive
        assertLock(cms, source, CmsLock.C_TYPE_EXCLUSIVE);
        assertLock(cms, destination1, CmsLock.C_TYPE_SHARED_EXCLUSIVE);    
        
        // create another sibling
        cms.createSibling(destination1, destination2, null);    
        // since one sibling was already exclusive locked, 
        // new sibling must be shared locked
        assertLock(cms, source, CmsLock.C_TYPE_EXCLUSIVE);
        assertLock(cms, destination1, CmsLock.C_TYPE_SHARED_EXCLUSIVE);        
        assertLock(cms, destination2, CmsLock.C_TYPE_SHARED_EXCLUSIVE);    
    }
    
    /**
     * Tests an inherited lock in a resource delete scenario.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLockInherit() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing inherited lock delete scenario");
        
        String source = "/folder1/index.html";
        String folder = "/folder1/";
        storeResources(cms, source);        

        // first delete the resource
        cms.lockResource(source);
        cms.deleteResource(source, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
       
        // now lock the folder
        cms.lockResource(folder);
        
        // make sure the deleted file has an inherited lock
        assertLock(cms, source, CmsLock.C_TYPE_INHERITED);
    }
        
    /**
     * Ensures that a lock is required for all write/control operations.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLockRequired() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing if a lock is required for write/control operations");
        
        String source = "/index.html";
        storeResources(cms, source);        
        long timestamp = System.currentTimeMillis();
        
        // make sure source is not locked
        assertLock(cms, source, CmsLock.C_TYPE_UNLOCKED);                
        
        CmsFile file = cms.readFile(source);
                
        boolean needLock;
        
        needLock = false;
        try {
            cms.touch(source, timestamp, timestamp, timestamp, false);            
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }        
        if (! needLock) {
            fail("Touch operation on resource permitted without a lock on the current user!");
        }         
        
        needLock = false;
        try {
            cms.deleteResource(source, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }        
        if (! needLock) {
            fail("Delete operation on resource permitted without a lock on the current user!");
        } 

        needLock = false;
        try {
            cms.writeFile(file);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Write operation on resource permitted without a lock on the current user!");
        }   

        needLock = false;
        try {
            cms.moveResource(source, "index_dest.html");
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Move operation on resource permitted without a lock on the current user!");
        } 

        needLock = false;
        try {
            cms.writePropertyObject(source, new CmsProperty(I_CmsConstants.C_PROPERTY_TITLE, "New title", null));
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Write property operation on resource permitted without a lock on the current user!");
        }       
        
        needLock = false;
        try {
            List properties = new ArrayList();
            properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_TITLE, "New title 2", null));
            cms.writePropertyObjects(source, properties);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Write property list operation on resource permitted without a lock on the current user!");
        }           

        needLock = false;
        try {
            cms.chflags(source, 1234);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Change flags operation on resource permitted without a lock on the current user!");
        }        

        needLock = false;
        try {
            cms.chtype(source, CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Change type operation on resource permitted without a lock on the current user!");
        }  

        needLock = false;
        try {
            cms.replaceResource(source, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, "Kaputt".getBytes(), null);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Replace operation on resource permitted without a lock on the current user!");
        }     

        needLock = false;
        try {
            cms.changeLastModifiedProjectId(source);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Change last modified in project operation on resource permitted without a lock on the current user!");
        }           

        needLock = false;
        try {
            CmsPermissionSet permissions = new CmsPermissionSet(I_CmsConstants.C_ACCESS_WRITE, I_CmsConstants.C_ACCESS_READ);
            cms.chacc(source, I_CmsPrincipal.C_PRINCIPAL_GROUP, OpenCms.getDefaultUsers().getGroupAdministrators(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), I_CmsConstants.C_ACCESSFLAGS_OVERWRITE);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Change permissions operation on resource permitted without a lock on the current user!");
        }
        
        needLock = false;
        try {
            cms.undeleteResource(source);
        }  catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        } 
        if (! needLock) {
            fail("Unlock operation on resource permitted without a lock on the current user!");
        }  
        
        // make sure original resource is unchanged
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
        
        // now perform a delete operation with lock
        cms.lockResource(source);
        cms.deleteResource(source, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        
        // now undelete the resource
        cms.lockResource(source);        
        cms.undeleteResource(source);
        cms.unlockResource(source);
        
        // make sure original resource is still unchanged
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);        
    }      
}
