/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestRestoreFromHistory.java,v $
 * Date   : $Date: 2004/08/17 16:09:25 $
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

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the history restore method.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestRestoreFromHistory extends OpenCmsTestCase {
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestRestoreFromHistory(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestRestoreFromHistory.class.getName());
        
        suite.addTest(new TestRestoreFromHistory("testRestoreResource"));       
        suite.addTest(new TestRestoreFromHistory("testRestoreDeletedResource")); 
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms("simpletest", "/sites/default/");
            }
            
            protected void tearDown() {
                // removeOpenCms();
            }
        };
        
        return wrapper;
    }

    /**
     * Test the restore resource method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRestoreResource() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing restore resource");
        
        String resourcename = "/test-restore1.txt";        
        
        String contentStr1 = "Hello this is content version 1";
        String contentStr2 = "Hello this is content version 2";
        //CmsProperty sProp1 = new CmsProperty("StructureProp", "Structure property value version 1", null, true);
        //CmsProperty rProp1 = new CmsProperty("ResourceProp", null, "Resource property value version 1", true);
        //CmsProperty sProp2 = new CmsProperty("StructureProp", "Structure property value version 2", null, true);
        //CmsProperty rProp2 = new CmsProperty("ResourceProp", null, "Resource property value version 2", true);        
        
        // create the resource with content version 1
        CmsResource res = cms.createResource(resourcename, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, contentStr1.getBytes(), null);
        this.storeResources(cms, resourcename);
        
        // set the properties
        //List properties = new ArrayList();
        //cms.writePropertyObject(resourcename, sProp1);
        //properties.add(sProp1);
        //cms.writePropertyObject(resourcename, rProp1);
        //properties.add(rProp1);
        
        // check the content
        assertContent(cms, resourcename, contentStr1.getBytes());
        //assertPropertyNew(cms, resourcename, sProp1);
        //assertPropertyNew(cms, resourcename, rProp1);
        
        // check that there are no backups available
        List allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (!allFiles.isEmpty()) {
            fail("Unexpected backup files for new created resource found.");
        }
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();    
        
        // check that there is exactly one backup file available
        allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (allFiles.size() != 1) {
            fail("Unexpected number of backup files for published resource found (one expected)");
        }
        
        // change to content of the file to version 2 and publish it again
        cms.lockResource(resourcename);
        CmsFile update = cms.readFile(resourcename);
        update.setContents(contentStr2.getBytes());
        cms.writeFile(update);
        
        // change the properties
        //cms.writePropertyObject(resourcename, sProp2);
        //cms.writePropertyObject(resourcename, rProp2);
        
        // check the content - must be version 2
        assertContent(cms, resourcename, contentStr2.getBytes());
        
        // check the properties - must be version 2
        //this.assertPropertyChanged(cms, resourcename, sProp2);
        //this.assertPropertyChanged(cms, resourcename, rProp2);
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject(); 
        
        // check that there are exactly two backup files available
        allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (allFiles.size() != 2) {
            fail("Unexpected number of backup files for published resource found (two expected)");
        }  
        
        // read the tag id
        CmsBackupResource backup = (CmsBackupResource)allFiles.get(1);
        
        // now restore the first version
        cms.lockResource(resourcename);
        cms.restoreResourceBackup(resourcename, backup.getTagId());
        
        // check the content - must be version 1
        assertContent(cms, resourcename, contentStr1.getBytes());
        
        // check the properties - must be version 1
        //this.assertPropertyChanged(cms, resourcename, sProp1);
        //this.assertPropertyChanged(cms, resourcename, rProp1);
    }
    
    /**
     * Tests the re-creation of already deleted resources.<p>
     * A deleted resource can be restored by creating a new one with the same path 
     * and then restoring its contents from history.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRestoreDeletedResource() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing restoring deleted resources");
        
        String resourcename = "/test-restore3.txt";        
        
        String contentStr = "Hello this is the content";    
        
        // create the resource with content
        cms.createResource(resourcename, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, contentStr.getBytes(), null);
        
        // check the content
        assertContent(cms, resourcename, contentStr.getBytes());
        
        // check that there are no backups available
        List allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (!allFiles.isEmpty()) {
            fail("Unexpected backup files for new created resource found.");
        }
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();    
        
        // check that there is exactly one backup file available
        allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (allFiles.size() != 1) {
            fail("Unexpected number of backup files for published resource found (one expected)");
        }
        
        // now delete and publish the resource
        cms.lockResource(resourcename);
        cms.deleteResource(resourcename, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();
        
        // create a new empty resource
        cms.createResource(resourcename, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        
        // check that there is one backup file available, again
        allFiles = cms.readAllBackupFileHeaders(resourcename);
        if (allFiles.size() != 1) {
            fail("Unexpected number of backup files for published resource found (one expected)");
        }  
        
        // read the tag id
        CmsBackupResource backup = (CmsBackupResource)allFiles.get(0);
        
        // and restore it from history
        cms.restoreResourceBackup(resourcename, backup.getTagId());
        
        // check the content
        assertContent(cms, resourcename, contentStr.getBytes());
    }   
}
