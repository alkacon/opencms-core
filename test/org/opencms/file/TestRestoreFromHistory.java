/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestRestoreFromHistory.java,v $
 * Date   : $Date: 2004/08/11 10:50:02 $
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
import org.opencms.test.OpenCmsTestCase;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the history restore method.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.1 $
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
       echo("Testing create resource");
       
       String resourcename = "/test-restore1.txt";        
              
       String contentStr1 = "Hello this is content version 1";
       String contentStr2 = "Hello this is content version 2";     
       
       // create the resource with content version 1
       cms.createResource(resourcename, CmsResourceTypePlain.C_RESOURCE_TYPE_ID, contentStr1.getBytes(), null);

       // check the content
       assertContent(cms, resourcename, contentStr1.getBytes());

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
       
       // check the content - must be version 2
       assertContent(cms, resourcename, contentStr2.getBytes());
       
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
   }
}
