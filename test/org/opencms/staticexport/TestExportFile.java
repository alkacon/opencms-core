/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/staticexport/TestExportFile.java,v $
 * Date   : $Date: 2005/03/17 10:32:10 $
 * Version: $Revision: 1.8 $
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
 
package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.io.FileInputStream;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.8 $
 * 
 * @since 5.1
 */
public class TestExportFile extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestExportFile(String arg0) {
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
        suite.setName(TestExportFile.class.getName());        
        
        suite.addTest(new TestExportFile("testStaticexportFile"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, null, true);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    }
    
    /**
     * Tests the file export.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testStaticexportFile() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing file export");
        
        // set the export mode to export immediately after publishing resources
        OpenCms.getStaticExportManager().setHandler("org.opencms.staticexport.CmsAfterPublishStaticExportHandler");
        
        String resourcename = "/file1.txt";
        String content = "this is a test content";
        
        // create a file in the root directory
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content.getBytes(), null);
        cms.unlockResource(resourcename);
        
        // read and check the content
        this.assertContent(cms, resourcename, content.getBytes()); 
        
        // now publish (and export) the resource
        cms.publishProject();
        
        // now read the exported file in the file system and check its content
        File f = new File(getTestDataPath("export/sites/default" + resourcename));
        assertTrue(f.exists());
        
        // check the exported content
        byte exportContent[] = new byte[(int)f.length()];
        FileInputStream fileStream = new FileInputStream(f);
        fileStream.read(exportContent);
        
        this.assertContent(cms, resourcename, exportContent);
        
    }
}
