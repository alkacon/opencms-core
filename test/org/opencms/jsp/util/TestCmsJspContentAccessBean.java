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

package org.opencms.jsp.util;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspContentAccessBean}</code>.<p>
 *
 * @since 7.0.2
 */
public class TestCmsJspContentAccessBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspContentAccessBean(String arg0) {

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
        suite.setName(TestCmsJspContentAccessBean.class.getName());

        suite.addTest(new TestCmsJspContentAccessBean("testContentAccess"));
        suite.addTest(new TestCmsJspContentAccessBean("testIsEditable"));

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
     * Tests general content access for XML content.<p>
     *
     * @throws Exception if the test fails
     */
    public void testContentAccess() throws Exception {

        CmsObject cms = getCmsObject();

        // first read the XML content
        CmsFile file = cms.readFile("/xmlcontent/article_0002.html");
        // need to set URI for macro resolver to work
        cms.getRequestContext().setUri("/xmlcontent/article_0002.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, content);

        // now for some fun with the bean
        assertSame(file, bean.getFile());

        // some simple has / has not checks
        Map<String, Boolean> hasValue = bean.getHasValue();
        assertSame(Boolean.TRUE, hasValue.get("Title"));
        assertSame(Boolean.FALSE, hasValue.get("IdontExistHere"));

        // check which kind of locales we have (should be "en" and "de")
        Map<String, Boolean> hasLocale = bean.getHasLocale();
        assertSame(Boolean.TRUE, hasLocale.get("en"));
        assertSame(Boolean.TRUE, hasLocale.get("de"));
        assertSame(Boolean.FALSE, hasLocale.get("fr"));

        // access the content form the default locale
        Map<String, CmsJspContentAccessValueWrapper> enValue = bean.getValue();
        assertEquals("This is the article 2 sample", String.valueOf(enValue.get("Title")));

        // now access the content from a selected locale
        Map<String, Map<String, CmsJspContentAccessValueWrapper>> localeValue = bean.getLocaleValue();
        Map<String, CmsJspContentAccessValueWrapper> deValue = localeValue.get("de");
        assertEquals("Das ist Artikel 2", String.valueOf(deValue.get("Title")));
        enValue = localeValue.get("en");
        assertEquals("This is the article 2 sample", String.valueOf(enValue.get("Title")));
        Map<String, CmsJspContentAccessValueWrapper> frValue = localeValue.get("fr");
        assertFalse(frValue.get("Title").getExists());

        // check list access to default locale
        Map<String, List<CmsJspContentAccessValueWrapper>> enValues = bean.getValueList();
        assertEquals(2, enValues.get("Teaser").size());
        assertEquals("This is teaser 2 in sample article 2.", String.valueOf(enValues.get("Teaser").get(1)));

        // now check list access to selected locale
        Map<String, Map<String, List<CmsJspContentAccessValueWrapper>>> localeValues = bean.getLocaleValueList();
        Map<String, List<CmsJspContentAccessValueWrapper>> deValues = localeValues.get("de");
        assertEquals(3, deValues.get("Teaser").size());

        // check macro resolving of the Title property
        assertEquals("This is the article 2 sample", String.valueOf(deValues.get("Teaser").get(2).getResolveMacros()));

        enValues = localeValues.get("en");
        assertEquals(2, enValues.get("Teaser").size());
        assertEquals("This is teaser 2 in sample article 2.", String.valueOf(enValues.get("Teaser").get(1)));
        Map<String, List<CmsJspContentAccessValueWrapper>> frValues = localeValues.get("fr");
        assertEquals(0, frValues.get("Title").size());

        // check random access to any object
        CmsJspContentAccessValueWrapper val = enValue.get("i/do/no/exists");
        assertFalse(val.getExists());
        assertTrue(val.getIsEmpty());
        assertTrue(val.getIsEmptyOrWhitespaceOnly());
        assertEquals("", val.toString());
        assertEquals("", val.getPath());
        assertEquals(0, val.getValueList().size());
        assertEquals(0, val.getHasValue().size());
        assertEquals(0, val.getValue().size());

        // create the content access bean with a locale that is not available, so a fall back should be used
        bean = new CmsJspContentAccessBean(cms, Locale.FRENCH, content);

        // check list access to default locale English
        frValues = bean.getValueList();
        assertEquals(2, frValues.get("Teaser").size());
        assertEquals("This is teaser 2 in sample article 2.", String.valueOf(enValues.get("Teaser").get(1)));
    }

    /**
     * Tests for the {@link CmsJspContentAccessBean#getIsEditable()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testIsEditable() throws Exception {

        CmsObject cms = getCmsObject();

        // first read the XML content
        CmsFile file = cms.readFile("/xmlcontent/article_0003.html");
        // need to set URI for macro resolver to work
        cms.getRequestContext().setUri("/xmlcontent/article_0003.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, content);

        CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
        CmsProject offline = cms.getRequestContext().getCurrentProject();

        // make sure the Admin user can edit OFFLINE
        assertTrue("Failed editable check for admin user in offline project", bean.getIsEditable());
        // switch to the online project
        cms.getRequestContext().setCurrentProject(online);
        // make sure the Admin user can edit ONLINE
        assertTrue("Failed editable check for admin user in online project", bean.getIsEditable());

        // create a test user and a test group
        cms.createUser("testuser", "test", "A test user", null);

        CmsUser testUser = cms.readUser("testuser");

        // login as user "test"
        cms.loginUser("testuser", "test");
        assertFalse("Passed editable check for test user in online project", bean.getIsEditable());
        cms.getRequestContext().setCurrentProject(offline);
        assertFalse("Passed editable check for test user in offline project", bean.getIsEditable());

        // change the permissions to allow the test user write access
        CmsObject adminCms = getCmsObject();
        adminCms.lockResource(file);
        adminCms.chacc("/xmlcontent/article_0003.html", I_CmsPrincipal.PRINCIPAL_USER, testUser.getName(), "+r+w+v");
        adminCms.unlockResource(file);

        // add user to required role
        OpenCms.getRoleManager().addUserToRole(adminCms, CmsRole.ELEMENT_AUTHOR, "testuser");

        assertTrue("Failed editable check for test user in offline project", bean.getIsEditable());

        // lock the file with the admin user
        adminCms.lockResource(file);
        assertFalse(
            "Passed editable check with locked resource for test user in offline project",
            bean.getIsEditable());
        adminCms.unlockResource(file);

        // try from the online project
        cms.getRequestContext().setCurrentProject(online);
        // add the user to the default users group, otherwise no projects will be available
        adminCms.addUserToGroup("testuser", OpenCms.getDefaultUsers().getGroupUsers());
        assertTrue("Failed editable check for test user in online project", bean.getIsEditable());
    }
}