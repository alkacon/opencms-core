/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/TestCmsImportExport.java,v $
 * Date   : $Date: 2004/11/11 16:39:54 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.importexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comment for <code>TestCmsImportExport</code>.<p>
 */
public class TestCmsImportExport extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsImportExport(String arg0) {
        super(arg0);
    } 

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestCmsImportExport.class.getName());
                
        suite.addTest(new TestCmsImportExport("testUserImport"));
        suite.addTest(new TestCmsImportExport("testImportResourceTranslator"));        
        suite.addTest(new TestCmsImportExport("testImportResourceTranslatorMultipleSite"));
        
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
     * Tests if the user passwords are imported correctly.<p>
     * The password digests are hex-128 (legacy) encoded, and
     * should be converted to base-64. 
     * 
     * @throws Exception if something goes wrong
     */
    public void testUserImport() throws Exception {
        
        CmsObject cms = getCmsObject();
        I_CmsPasswordHandler passwordHandler = new CmsDefaultPasswordHandler();
        CmsUser user;

        // check if passwords will be converted
        if ("com.opencms.legacy.CmsLegacyPasswordHandler".equals(OpenCms.getPasswordHandler().getClass().getName())) {
            echo("Skipping test since digest encoding is set to legacy");
            return;
        }
            
        echo("Testing passwords of imported users");
        
        // check the admin user
        user = cms.readUser("Admin");
        assertEquals(user.getPassword(), passwordHandler.digest("admin", "MD5", "UTF-8"));

        // check the guest user
        user = cms.readUser("Guest");
        assertEquals(user.getPassword(), passwordHandler.digest("", "MD5", "UTF-8"));
        
        // check the test1 user
        user = cms.readUser("test1");
        assertEquals(user.getPassword(), passwordHandler.digest("test1", "MD5", "UTF-8"));

        // check the test2 user
        user = cms.readUser("test2");
        assertEquals(user.getPassword(), passwordHandler.digest("test2", "MD5", "UTF-8"));
    }
    
    /**
     * Tests the resource translation during import with multiple sites.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportResourceTranslatorMultipleSite() throws Exception {       
        
        echo("Testing resource translator with multiple sites");
        CmsObject cms = getCmsObject();       
        
        cms.getRequestContext().setSiteRoot("/");
        
        // create a second site
        cms.createResource("/sites/mysite", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        cms.unlockResource("/sites/mysite");      
        
        cms.createResource("/sites/othersite", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        cms.unlockResource("/sites/othersite");
         
        CmsResourceTranslator oldFolderTranslator = OpenCms.getResourceManager().getFolderTranslator();
        
        CmsResourceTranslator folderTranslator = new CmsResourceTranslator(
            new String[] {
                "s#^/sites(.*)#/sites$1#",
                "s#^/system(.*)#/system$1#",
                "s#^/content/bodys(.*)#/system/bodies$1#",
                "s#^/pics(.*)#/system/galleries/pics$1#",
                "s#^/download(.*)#/system/galleries/download$1#",
                "s#^/externallinks(.*)#/system/galleries/externallinks$1#",
                "s#^/htmlgalleries(.*)#/system/galleries/htmlgalleries$1#",
                "s#^/content(.*)#/system$1#",
                "s#^/othertest(.*)#/sites/othersite$1#",                
                "s#^/(.*)#/sites/mysite/$1#",
                }, 
            false);
        
        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(
            folderTranslator, 
            OpenCms.getResourceManager().getFileTranslator());
        
        // update OpenCms context to ensure new translator is used
        cms = getCmsObject();    
        
        // set root sitr
        cms.getRequestContext().setSiteRoot("/");
        
        // import the files
        String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
        OpenCms.getImportExportManager().importData(cms, importFile, "/", new CmsShellReport());        

        // now switch to "mysite"
        cms.getRequestContext().setSiteRoot("/sites/mysite/");

        // check the results of the import
        CmsXmlPage page;
        CmsFile file;
        CmsLinkTable table;
        List links;
        Iterator i;
        List siblings;       
        
        // test "/importtest/index.html"
        file = cms.readFile("/importtest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));
        
        siblings = cms.readSiblings("/importtest/index.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }        
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));
        assertTrue(links.contains("/sites/mysite/importtest/linktest.html"));          
        
        
        
        // test "/importtest/page2.html"
        file = cms.readFile("/importtest/page2.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertEquals(2, links.size());
        assertTrue(links.contains("/system/galleries/pics/_anfang/bg_teaser_test2.jpg"));       
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));
        
        
        
        // test "/importtest/linktest.html" (sibling of "/importtest/index.html")
        file = cms.readFile("/importtest/linktest.html");        
        assertEquals(CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID, file.getTypeId());        
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));
        
        siblings = cms.readSiblings("/importtest/linktest.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }        
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));
        assertTrue(links.contains("/sites/mysite/importtest/linktest.html"));     
        
        
        // now switch to "othersite"
        cms.getRequestContext().setSiteRoot("/sites/othersite/");
        
        // test "/othertest/index.html"
        file = cms.readFile("/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));  
        
        // reset the translation rules
        OpenCms.getResourceManager().setTranslators(
            oldFolderTranslator, 
            OpenCms.getResourceManager().getFileTranslator());        
    }
    
    /**
     * Tests the resource translation during import.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportResourceTranslator() throws Exception {       
        
        echo("Testing resource translator for import");
        CmsObject cms = getCmsObject();        

        cms.getRequestContext().setSiteRoot("/");

        // need to create the "galleries" folder manually
        cms.createResource("/system/galleries", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        cms.unlockResource("/system/galleries");
        
        cms.getRequestContext().setSiteRoot("/sites/default");
        
        // import the files
        String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
        OpenCms.getImportExportManager().importData(cms, importFile, "/", new CmsShellReport());        
                        
        
        // check the results of the import
        CmsXmlPage page;
        CmsFile file;
        CmsLinkTable table;
        List links;
        Iterator i;
        List siblings;
        
        
        
        // test "/importtest/index.html"
        file = cms.readFile("/importtest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));
        
        siblings = cms.readSiblings("/importtest/index.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }        
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/index.html"));
        assertTrue(links.contains("/sites/default/importtest/linktest.html"));          
        
        
        
        // test "/importtest/page2.html"
        file = cms.readFile("/importtest/page2.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertEquals(2, links.size());
        assertTrue(links.contains("/system/galleries/pics/_anfang/bg_teaser_test2.jpg"));       
        assertTrue(links.contains("/sites/default/importtest/index.html"));
        
        
        
        // test "/importtest/linktest.html" (sibling of "/importtest/index.html")
        file = cms.readFile("/importtest/linktest.html");        
        assertEquals(CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID, file.getTypeId());        
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));
        
        siblings = cms.readSiblings("/importtest/linktest.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }        
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/index.html"));
        assertTrue(links.contains("/sites/default/importtest/linktest.html"));          
        
        // test "/othertest/index.html"
        file = cms.readFile("/othertest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);
        
        table = page.getLinkTable("body", OpenCms.getLocaleManager().getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());    
        }                
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));      
        
        // clean up for the next test
        cms.getRequestContext().setSiteRoot("/");        
        cms.lockResource("/sites/default");
        cms.lockResource("/system");       
        cms.deleteResource("/sites/default/importtest", I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        cms.deleteResource("/system/bodies", I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        cms.deleteResource("/system/galleries/pics", I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        cms.unlockResource("/sites/default");
        cms.unlockResource("/system");               
        cms.publishProject();
    }
}
