/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/staticexport/TestSecure.java,v $
 * Date   : $Date: 2005/06/23 11:11:58 $
 * Version: $Revision: 1.7 $
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

package org.opencms.staticexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the XML page that require a running OpenCms system.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 5.5.0
 */
public class TestSecure extends OpenCmsTestCase {

    /** the prefix of the secure server. */
    private static final String SERVER_SECURE = "https://localhost";

    /** the prefix of the normal server. */
    private static final String SERVER_NORMAL = "http://localhost";

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestSecure.class.getName());

        suite.addTest(new TestSecure("testSecureServerConfig"));
        suite.addTest(new TestSecure("testLinkInXmlPage"));
        suite.addTest(new TestSecure("testSecureLinkProcessing"));
        suite.addTest(new TestSecure("testSetupSecondSite"));

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
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSecure(String arg0) {

        super(arg0);
    }

    /**
     * Test if the site configuration succeeded.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testSecureServerConfig() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the configuration");

        // test, if a secure server exists for the current site
        assertTrue(CmsSiteManager.getCurrentSite(cms).hasSecureServer());

        // test, if the secure server is 'https:www.mysite.com'
        assertEquals(SERVER_SECURE, CmsSiteManager.getCurrentSite(cms).getSecureUrl());

    }

    /**
     * Test if links to secure pages are secure.<p>
     * 
     * Description of the issue:
     * links from secure pages to insecure pages must have a normal server prefix.<p>
     * links from normal pages to secure pages must have a secure server prefix.<p>
     * links from secure pages to secure pages have no server prefix.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testSecureLinkProcessing() throws Exception {

        CmsObject cms = getCmsObject();
        CmsProject onlineProject = cms.readProject("Online");
        cms.getRequestContext().setCurrentProject(onlineProject);
        CmsRequestContext requestContext = cms.getRequestContext();
        CmsLinkManager linkManager = OpenCms.getLinkManager();

        requestContext.setUri("/folder1/page1.html");

        // link from normal to secure document need to have secure server prefix
        assertHasSecurePrefix(linkManager.substituteLink(cms, "/folder1/page4.html"));

        // image links must be without server prefix
        assertHasNoPrefix(linkManager.substituteLink(cms, "/folder1/image1.gif"));
        requestContext.setUri("/folder1/page4.html");

        // link from secure to normal document need to have normal server prefix        
        assertHasNormalPrefix(linkManager.substituteLink(cms, "/folder1/page3.html"));

        // image links need to be relative
        assertHasNoPrefix(linkManager.substituteLink(cms, "/folder1/image1.gif"));

        // in offline mode, no server prefixes must be set
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertHasNoPrefix(linkManager.substituteLink(cms, "/folder1/page3.html"));

    }

    /**
     * Test if links in XML-pages are converted to html-links with correct server prefixes.<p>
     * 
     * make a link from one site to another site, and check, if the correct prefixes are set 
     * @throws Exception in case something goes wrong
     */
    public void testLinkInXmlPage() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing security of links");
        CmsProject onlineProject = cms.readProject("Online");
        cms.getRequestContext().setCurrentProject(onlineProject);
        cms.getRequestContext().setSiteRoot("/sites/default");
        String filename = "/folder1/page1.html";

        CmsFile file = cms.readFile(filename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);

        String element = "body";

        String text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertTrue(text.indexOf(SERVER_SECURE) != -1);

    }

    /**
     * Test if links in XML-pages are converted to html-links with correct server prefixes.<p>
     * 
     * make a link from one site to another site, and check, if the correct prefixes are set 
     * @throws Exception in case something goes wrong
     */
    public void testSetupSecondSite() throws Exception {

        CmsObject cms = getCmsObject();
        // the second site sould be correctly initialized
        // what has to be done is to create the site root folder of the testsite
        cms.getRequestContext().setSiteRoot("/");
        cms.createResource("/sites/testsite/", CmsResourceTypeFolder.getStaticTypeId());
        cms.unlockResource("/sites/testsite/");

        // copy one page to the new site
        cms.copyResource("/sites/default/folder1/page1.html", "/sites/testsite/page1.html");
        cms.unlockResource("/sites/testsite/page1.html");

        cms.publishProject();

        CmsProject onlineProject = cms.readProject("Online");
        cms.getRequestContext().setCurrentProject(onlineProject);
        cms.getRequestContext().setSiteRoot("/sites/testsite/");

        // read the page        
        CmsFile file = cms.readFile("page1.html");
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);

        String element = "body";

        // the converted HTML must have links with normal and secure server prefixes of the first site
        String text = page.getStringValue(cms, element, Locale.ENGLISH);
        
        System.out.println(text);
        
        assertTrue(text.indexOf(SERVER_SECURE) != -1);
        assertTrue(text.indexOf(SERVER_NORMAL) != -1);
    }

    private void assertHasSecurePrefix(String link) {

        assertTrue(link.startsWith(SERVER_SECURE));
    }

    private void assertHasNormalPrefix(String link) {

        assertTrue(link.startsWith(SERVER_NORMAL));
    }

    private void assertHasNoPrefix(String link) {

        assertTrue(link.startsWith("/"));
    }

}