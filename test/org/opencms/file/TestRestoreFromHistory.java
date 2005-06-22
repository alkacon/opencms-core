/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestRestoreFromHistory.java,v $
 * Date   : $Date: 2005/06/22 10:38:24 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the history restore method.<p>
 * 
 * @author Carsten Weinholz 
 * @version $Revision: 1.11 $
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
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestRestoreFromHistory.class.getName());
        
        suite.addTest(new TestRestoreFromHistory("testRestoreResource"));       
        suite.addTest(new TestRestoreFromHistory("testRestoreDeletedResource"));
        suite.addTest(new TestRestoreFromHistory("testHistoryOverflow"));
        
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
     * Tests the history overflow function.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testHistoryOverflow() throws Throwable {
        
        final int C_MAX_VERSIONS = 10;
        
        CmsObject cms = getCmsObject();
        echo("Testing history overflow");
        
        String resourcename ="/test-overflow1.txt";
        String contentStr = "1";
        
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);        
        this.storeResources(cms, resourcename);
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();
        
        int version;
        for (version = 1; version < 20; version++) {
            
            cms.lockResource(resourcename);
            
            // check that there is the appropriate number of backup files
            List allFiles = cms.readAllBackupFileHeaders(resourcename);
            if (version <= C_MAX_VERSIONS) {
                if (allFiles.size() != version) {
                    fail("Number of backup files found = " + allFiles.size() + " != " + version + " expected");
                }
            } else {
                if (allFiles.size() != C_MAX_VERSIONS) {
                    fail("Number of backup files found = " + allFiles.size() + " != " + C_MAX_VERSIONS + " expected");
                }
            }
            
            // now check the previous version if available
            if (version > 1) {
                CmsBackupResource backup = (CmsBackupResource)allFiles.get(1);
                cms.restoreResourceBackup(resourcename, backup.getTagId());
            
                // check the content - must be version-1
                assertContent(cms, resourcename, Integer.toString(version-1).getBytes());
            }
            
            // change to content of the file to next version and publish it again
            contentStr = Integer.toString(version+1);
            CmsFile update = cms.readFile(resourcename);
            update.setContents(contentStr.getBytes());
            cms.writeFile(update);
            this.storeResources(cms, resourcename);
            cms.unlockProject(cms.getRequestContext().currentProject().getId());
            cms.publishProject();
        }
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
        
        
        CmsProperty sProp1 = new CmsProperty("StructureProp", "Structure property value version 1", null, true);
        CmsProperty rProp1 = new CmsProperty("ResourceProp", null, "Resource property value version 1", true);
        List props1 = new ArrayList();
        props1.add(sProp1);
        props1.add(rProp1);
        
        CmsProperty sProp2 = new CmsProperty("StructureProp", "Structure property value version 2", null, true);
        CmsProperty rProp2 = new CmsProperty("ResourceProp", null, "Resource property value version 2", true);
        List props2 = new ArrayList();
        props2.add(sProp2);
        props2.add(rProp2);
        
        // create the resource with content version 1
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), contentStr1.getBytes(), null);
        this.storeResources(cms, resourcename);
        
        // set the properties
        cms.writePropertyObject(resourcename, sProp1);
        cms.writePropertyObject(resourcename, rProp1);
        
        // check the content
        assertContent(cms, resourcename, contentStr1.getBytes());
        assertPropertyNew(cms, resourcename, props1);
        
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
        
        // store current resource contents
        this.storeResources(cms, resourcename);
        
        // change to content of the file to version 2 and publish it again
        cms.lockResource(resourcename);
        CmsFile update = cms.readFile(resourcename);
        update.setContents(contentStr2.getBytes());
        cms.writeFile(update);
        
        // change the properties
        cms.writePropertyObject(resourcename, sProp2);
        cms.writePropertyObject(resourcename, rProp2);
        
        // check the content - must be version 2
        assertContent(cms, resourcename, contentStr2.getBytes());
        
        // check the properties - must be version 2
        assertPropertyChanged(cms, resourcename, props2);

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
        
        // store current resource contents
        this.storeResources(cms, resourcename);
        
        // now restore the first version
        cms.lockResource(resourcename);
        cms.restoreResourceBackup(resourcename, backup.getTagId());
        
        // check the content - must be version 1
        assertContent(cms, resourcename, contentStr1.getBytes());
        
        // check the properties - must be version 1
        assertPropertyChanged(cms, resourcename, props1);
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
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);
        
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
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), null, null);
        
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
