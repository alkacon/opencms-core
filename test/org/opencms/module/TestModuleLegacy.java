/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/Attic/TestModuleLegacy.java,v $
 * Date   : $Date: 2004/11/25 13:04:33 $
 * Version: $Revision: 1.6 $
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
 
package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.genericsql.CmsDbAccess;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms legacy module.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 */
public class TestModuleLegacy extends OpenCmsTestCase {
    
    /** The DB pool url. */
    public static final String m_pool = "opencms:default";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestModuleLegacy(String arg0) {
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
        suite.setName(TestModuleLegacy.class.getName());
                
        suite.addTest(new TestModuleLegacy("testLegacyImport"));
        suite.addTest(new TestModuleLegacy("testLegacyInitialization"));
        suite.addTest(new TestModuleLegacy("testLegacyMasterIO"));
        
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
     * Tests the legacy module import.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLegacyImport() throws Throwable {
        CmsObject cms = getCmsObject();
        echo("Testing legacy module import");
        
        // check that master tables are not already available
        CmsDbAccess masterDbAccess = new CmsDbAccess(m_pool);
        if (masterDbAccess.checkTables()) {
            fail("Master tables already created ?!");
        } else {
            echo ("Master tables not detected - ok");
        }
        
        String moduleName = "org.opencms.legacy.compatibility";
        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/" + moduleName + ".zip");
        OpenCms.getImportExportManager().importData(cms, moduleFile, null, new CmsShellReport());
        
        // basic check if the module was imported correctly
        if (! OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }
    }

    /**
     * Tests the legacy module initialization during startup.<p>
     * If not already available, master module tables will be created
     * in the database.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLegacyInitialization() throws Throwable {

        echo("Testing legacy module initialization");
        
        restart();
        
        // start cms
        getCmsObject();
        
        CmsDbAccess masterDbAccess = new CmsDbAccess(m_pool);
        if (!masterDbAccess.checkTables()) {
            fail ("Master tables not created!");
        }
    }
    
    /**
     * Tests wrinting and reading the master table.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testLegacyMasterIO()  throws Throwable {

        echo("Testing legacy module i/o");

        CmsObject cms = getCmsObject();
        
        CmsDbAccess masterDbAccess = new CmsDbAccess(m_pool);
        CmsMasterDataSet dataset1 = new CmsMasterDataSet(), dataset2 = new CmsMasterDataSet();
        CmsMasterContent content = new CmsMasterContent(cms) {
            public int getSubId() {
                return 4711;
            }            
        };

        dataset1.m_title = "Master Test Content";
        masterDbAccess.insert(cms, content, dataset1);        
        masterDbAccess.read(cms, content, dataset2, dataset1.m_masterId);
        
        assertEquals(dataset1.m_title, dataset2.m_title);        
    }    
}