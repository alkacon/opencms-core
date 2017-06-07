/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestServletRequest;
import org.opencms.test.OpenCmsTestServletResponse;
import org.opencms.util.CmsMacroResolver;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test the static OpenCms singleton object.<p>
 *
 */
public class TestOpenCmsSingleton extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestOpenCmsSingleton(String arg0) {

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
        suite.setName(TestOpenCmsSingleton.class.getName());

        suite.addTest(new TestOpenCmsSingleton("testInitCmsObject"));
        suite.addTest(new TestOpenCmsSingleton("testInitResource"));
        suite.addTest(new TestOpenCmsSingleton("testLog"));
        suite.addTest(new TestOpenCmsSingleton("testEncoding"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test case for the encoding.<p>
     *
     * @throws Exception if the test fails
     */
    public void testEncoding() throws Exception {

        echo("Testing the encoding settings");

        String systemEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
        String workplaceEncoding = OpenCms.getWorkplaceManager().getEncoding();

        // get content encoding default property from the JSP resource type
        CmsResourceTypeJsp jsp = (CmsResourceTypeJsp)OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeJsp.getJSPTypeId());
        // note: default test configuration is done in opencms-vfs.xml and may be different from standard installation
        List defaultProperties = jsp.getConfiguredDefaultProperties();
        assertEquals("Test configuration has 2 default properties configured for JSP", 2, defaultProperties.size());
        Iterator i = defaultProperties.iterator();
        String jspEncoding = null;
        while (i.hasNext()) {
            CmsProperty property = (CmsProperty)i.next();
            if (CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING.equals(property.getName())) {
                jspEncoding = property.getValue();
                assertEquals(
                    "Test configuration has property value '${opencms.default.encoding}' configured for JSP",
                    2,
                    defaultProperties.size());
                // resolve the macro
                CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
                jspEncoding = CmsMacroResolver.newInstance().setCmsObject(cms).resolveMacros(jspEncoding);
                break;
            }
        }

        assertEquals("ISO-8859-1", systemEncoding);
        assertEquals(systemEncoding, workplaceEncoding);
        assertEquals(systemEncoding, jspEncoding);
    }

    /**
     * Test case for the initCmsObject methods.<p>
     *
     * @throws Exception if the test fails
     */
    public void testInitCmsObject() throws Exception {

        CmsObject cms;

        echo("Testing access to initCmsObject methods");

        // test creation of "Guest" user CmsObject
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        if (!OpenCms.getDefaultUsers().isUserGuest(cms.getRequestContext().getCurrentUser().getName())) {
            fail("'Guest' user could not be properly initialized!");
        }

        // test creation of "Export" user CmsObject
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        if (!OpenCms.getDefaultUsers().isUserExport(cms.getRequestContext().getCurrentUser().getName())) {
            fail("'Export' user could not be properly initialized!");
        }

        // test creation of "Admin" user CmsObject (this must fail)
        boolean gotException = false;
        cms = null;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserAdmin());
        } catch (CmsException e) {
            gotException = true;
        }
        if (!gotException) {
            fail("'Admin' user could be initialized without permission check (with username)!");
        }

        // create "Admin" context info
        CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());

        // test creation of "Admin" user CmsObject with 2nd option (this must also fail)
        gotException = false;
        cms = null;
        try {
            cms = OpenCms.initCmsObject(null, contextInfo);
        } catch (CmsException e) {
            gotException = true;
        }
        if (!gotException) {
            fail("'Admin' user could be initialized without permission check (with context info)!");
        }

        // now test creation of "Admin" user with admin permissions
        // also check if created context is actually the context provided
        String siteRoot = "/sites/default";
        String requestedUri = "/index.html";
        String encoding = "US-ASCII";

        contextInfo.setSiteRoot(siteRoot);
        contextInfo.setRequestedUri(requestedUri);
        contextInfo.setLocale(Locale.CHINESE);
        contextInfo.setEncoding(encoding);
        try {
            cms = OpenCms.initCmsObject(getCmsObject(), contextInfo);
        } catch (CmsException e) {
            fail("'Admin' user creation with valid Admin context didn't work!");
            return;
        }
        if (!OpenCms.getDefaultUsers().isUserAdmin(cms.getRequestContext().getCurrentUser().getName())) {
            fail("'Admin' user could not be properly initialized with valid Admin context!");
        }
        if (cms == getCmsObject()) {
            fail("'Admin' user Object is the same as creating instance, but must be a new Object!");
        }

        if (!cms.getRequestContext().getSiteRoot().equals(siteRoot)) {
            fail("Site root in created context not as expected.");
        }
        if (!cms.getRequestContext().getUri().equals(requestedUri)) {
            fail("Requested uri in created context not as expected.");
        }
        if (!cms.getRequestContext().getEncoding().equals(encoding)) {
            fail("Encoding in created context not as expected.");
        }
        if (!cms.getRequestContext().getLocale().equals(Locale.CHINESE)) {
            fail("Locale in created context not as expected.");
        }
    }

    /**
     * Test case for the logger.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLog() throws Exception {

        // first 4 log levels are uncritical
        CmsLog.getLog(this).trace("This is a 'trace' log message");
        CmsLog.getLog(this).debug("This is a 'debug' log message");
        CmsLog.getLog(this).info("This is a 'info' log message");
        CmsLog.getLog(this).warn("This is a 'warn' log message");

        // is something is written to log level 'error' or 'fatal'
        // a runtime exception must be thrown while unit tests are running
        boolean noException;
        noException = true;
        try {
            CmsLog.getLog(this).error("This is a 'error' log message");
        } catch (RuntimeException e) {
            noException = false;
        }
        if (noException) {
            fail("Writing to 'error' log level did not cause test to fail.");
        }
        noException = true;
        try {
            CmsLog.getLog(this).fatal("This is a 'fatal' log message");
        } catch (RuntimeException e) {
            noException = false;
        }
        if (noException) {
            fail("Writing to 'fatal' log level did not cause test to fail.");
        }
    }

    /**
     * Test case for resource initialization.<p>
     *
     * @throws Exception if the test fails
     */
    public void testInitResource() throws Exception {

        echo("Testing access to initResource method");

        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setSiteRoot("/sites/default/");

        HttpServletRequest req = new OpenCmsTestServletRequest();
        HttpServletResponse res = new OpenCmsTestServletResponse();

        CmsResource resource = OpenCms.initResource(cms, "/folder1/subfolder12/", req, res);
        assertEquals("/sites/default/folder1/subfolder12/index.html", resource.getRootPath());

        CmsProperty defaultFileProperty = new CmsProperty("default-file", "page1.html", null);
        cms.lockResource("/folder1/subfolder12/");
        cms.writePropertyObject("/folder1/subfolder12/", defaultFileProperty);

        CmsResource resource2 = OpenCms.initResource(cms, "/folder1/subfolder12/", req, res);
        assertEquals("/sites/default/folder1/subfolder12/page1.html", resource2.getRootPath());
    }
}
