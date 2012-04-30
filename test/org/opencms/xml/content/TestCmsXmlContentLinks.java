/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML contents with real VFS operations.<p>
 */
public class TestCmsXmlContentLinks extends OpenCmsTestCase {

    /** The link original filename. */
    private static final String FILENAME = "/folder1/image2.gif";

    /** The new link filename. */
    private static final String FILENAME2 = "/folder1/image2.new.gif";

    /** simple xml content schema system id. */
    private static final String SCHEMA_SYSTEM_ID_11 = "http://www.opencms.org/test11.xsd";

    /** nested xml content schema system id. */
    private static final String SCHEMA_SYSTEM_ID_12 = "http://www.opencms.org/test12.xsd";

    /** simple xml content schema system id, with parent invalidation. */
    private static final String SCHEMA_SYSTEM_ID_13 = "http://www.opencms.org/test13.xsd";

    /** simple xml content schema system id, with attachment relation type. */
    private static final String SCHEMA_SYSTEM_ID_14 = "http://www.opencms.org/test14.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentLinks(String arg0) {

        super(arg0);
    }

    /**
     * Compares two link objects.<p>
     * 
     * @param expected the expected link object
     * @param result the resulting link object
     * @param assertStructureId if the structure id should be asserted or not
     */
    public static void assertLink(CmsLink expected, CmsLink result, boolean assertStructureId) {

        // assert attributes
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getType(), result.getType());
        assertEquals(expected.isInternal(), result.isInternal());
        // assert elements
        assertEquals(expected.getTarget(), result.getTarget());
        assertEquals(expected.getQuery(), result.getQuery());
        assertEquals(expected.getAnchor(), result.getAnchor());

        if (assertStructureId) {
            assertEquals(expected.getStructureId(), result.getStructureId());
        }
    }

    /**
     * Returns the unique link for the given html node in the given xml content.<p>
     * 
     * @param cms the cms context
     * @param xmlcontent the xml content
     * @param nodeName the html node name
     * @param linkName the name of the link
     * 
     * @return the link object
     */
    public static CmsLink getHtmlLink(CmsObject cms, CmsXmlContent xmlcontent, String nodeName, String linkName) {

        CmsLinkTable links = ((CmsXmlHtmlValue)xmlcontent.getValue(nodeName, Locale.ENGLISH)).getLinkTable();
        assertEquals(links.size(), 1);
        CmsLink link = links.getLink(linkName);
        assertNotNull(link);
        link.checkConsistency(cms);
        return link;
    }

    /**
     * Returns the link for the given vfs file reference node in the given xml content.<p>
     * 
     * @param cms the cms context
     * @param xmlcontent the xml content
     * @param nodeName the vfs file reference node name
     * 
     * @return the link object
     */
    public static CmsLink getVfsFileRefLink(CmsObject cms, CmsXmlContent xmlcontent, String nodeName) {

        CmsLink link = ((CmsXmlVfsFileValue)xmlcontent.getValue(nodeName, Locale.ENGLISH)).getLink(cms);
        assertNotNull(link);
        return link;
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsXmlContentLinks.class.getName());

        suite.addTest(new TestCmsXmlContentLinks("testSiteLinks"));
        suite.addTest(new TestCmsXmlContentLinks("testUpdatePath"));
        suite.addTest(new TestCmsXmlContentLinks("testUpdateId"));
        suite.addTest(new TestCmsXmlContentLinks("testRemoveNode"));
        suite.addTest(new TestCmsXmlContentLinks("testRemoveParent"));
        suite.addTest(new TestCmsXmlContentLinks("testRelationType"));
        suite.addTest(new TestCmsXmlContentLinks("testInvalidateFalse"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                try {
                    initSchemas();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Initializes all schema definitions.<p>
     * 
     * @throws IOException if something goes wrong
     */
    protected static void initSchemas() throws IOException {

        System.out.println("Initializing schema definitions");

        // unmarshal content definition
        String content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-11.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_11, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-13.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_13, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-12.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_12, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-14.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_14, content.getBytes(CmsEncoder.ENCODING_UTF_8));

    }

    /**
     * Returns a link object prototype for the given type.<p>
     * 
     * @param cms the cms context
     * @param forHtml <code>true</code> for html or <code>false</code> for vfs file refs
     * 
     * @return a link object prototype
     */
    private static CmsLink getExpected(CmsObject cms, boolean forHtml) {

        String name = forHtml ? "link0" : CmsLink.DEFAULT_NAME;
        CmsRelationType type = forHtml ? CmsRelationType.EMBEDDED_IMAGE : CmsRelationType.XML_WEAK;
        String target = cms.getRequestContext().addSiteRoot(FILENAME);
        CmsLink expected = new CmsLink(name, type, target, true);
        expected.checkConsistency(cms);
        return expected;
    }

    /**
     * Updates a link, and checks the result against the given resource.<p>
     * 
     * @param cms the cms context
     * @param link the lin k to update
     * @param resource the resource to check against
     */
    private static void updateLink(CmsObject cms, CmsLink link, CmsResource resource) {

        link.checkConsistency(cms);
        assertEquals(resource.getRootPath(), link.getTarget());
        assertEquals(resource.getStructureId(), link.getStructureId());
    }

    /**
     * Test the option to do not invalidate a broken link node.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testInvalidateFalse() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the option to do not invalidate a broken link node");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-14.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        String name = CmsLink.DEFAULT_NAME;
        CmsRelationType type = CmsRelationType.XML_STRONG;
        String target = cms.getRequestContext().addSiteRoot(FILENAME);
        CmsLink expectedRefLink = new CmsLink(name, type, target, true);
        expectedRefLink.checkConsistency(cms);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink expectedHtmlLink = getExpected(cms, true);
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);

        // delete the link
        cms.lockResource(FILENAME);
        cms.deleteResource(FILENAME, CmsResource.DELETE_PRESERVE_SIBLINGS);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // store the current content, this is important since the original content had no ids
        content = xmlcontent.toString();
        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        expectedRefLink = new CmsLink(name, type, target, true);
        expectedRefLink.checkConsistency(cms);
        refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        expectedHtmlLink = getExpected(cms, true);
        htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);
    }

    /**
     * Test the relation type configuration in xml content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testRelationType() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the relation type configuration in xml content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-14.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        String name = CmsLink.DEFAULT_NAME;
        CmsRelationType type = CmsRelationType.XML_STRONG;
        String target = cms.getRequestContext().addSiteRoot(FILENAME);
        CmsLink expectedRefLink = new CmsLink(name, type, target, true);
        expectedRefLink.checkConsistency(cms);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink expectedHtmlLink = getExpected(cms, true);
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);
    }

    /**
     * Test removing the node of a broken link.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testRemoveNode() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing removing the node of a broken link");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-13.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        CmsLink expectedRefLink = getExpected(cms, false);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink expectedHtmlLink = getExpected(cms, true);
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);

        // change the time window
        cms.lockResource(FILENAME);
        cms.setDateReleased(FILENAME, System.currentTimeMillis() + 1000000, false);
        cms.setDateExpired(FILENAME, System.currentTimeMillis() + 2000000, false);
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline, without link checking.

        // set the flag to prevent link checking
        cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        expectedRefLink = getExpected(cms, false);
        refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, false);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

        // validate, and assert the result
        CmsXmlContentErrorHandler errHandler = xmlcontent.validate(cms);
        assertTrue(errHandler.getErrors().isEmpty());
        assertEquals(errHandler.getWarnings().size(), 1);
        assertTrue(errHandler.getWarnings().containsKey(Locale.ENGLISH));
        Map<String, String> enWarnings = errHandler.getWarnings().get(Locale.ENGLISH);
        assertEquals(enWarnings.size(), 1);
        assertTrue(enWarnings.containsKey("VfsLink[1]"));
        assertTrue(enWarnings.containsValue(org.opencms.xml.content.Messages.get().getBundle().key(
            org.opencms.xml.content.Messages.GUI_XMLCONTENT_CHECK_WARNING_NOT_RELEASED_0)));

        // reset the time to have the 'normal' behaviour again
        cms.getRequestContext().setRequestTime(System.currentTimeMillis());

        // check offline

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get the broken vfs reference
        assertNull(xmlcontent.getValue("VfsLink", Locale.ENGLISH));
        assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertFalse(errHandler.hasErrors());
        assertFalse(errHandler.hasWarnings());

        // check online, always automatically checked, and includes time window check
        CmsProject project = cms.getRequestContext().getCurrentProject();
        try {
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // read the content again
            xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

            // validate the XML structure
            xmlcontent.validateXmlStructure(resolver);

            // get the broken vfs reference
            assertNull(xmlcontent.getValue("VfsLink", Locale.ENGLISH));
            assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

            // get and validate the html link, should be unchanged
            expectedHtmlLink = getExpected(cms, true);
            assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

            // validate, and assert the result
            errHandler = xmlcontent.validate(cms);
            assertFalse(errHandler.hasErrors());
            assertFalse(errHandler.hasWarnings());
        } finally {
            cms.getRequestContext().setCurrentProject(project);
        }

        // delete the link
        cms.lockResource(FILENAME);
        cms.deleteResource(FILENAME, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline, without link checking.

        // set the flag to prevent link checking
        cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        expectedRefLink = getExpected(cms, false);
        refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, false);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertEquals(errHandler.getErrors().size(), 1);
        assertTrue(errHandler.getErrors().containsKey(Locale.ENGLISH));
        Map<String, String> enErrors = errHandler.getErrors().get(Locale.ENGLISH);
        assertEquals(enErrors.size(), 1);
        assertTrue(enErrors.containsKey("VfsLink[1]"));
        assertTrue(enErrors.containsValue(org.opencms.xml.content.Messages.get().getBundle().key(
            org.opencms.xml.content.Messages.GUI_XMLCONTENT_CHECK_ERROR_0)));
        assertTrue(errHandler.getWarnings().isEmpty());

        // reset the time to have the 'normal' behaviour again
        cms.getRequestContext().setRequestTime(System.currentTimeMillis());

        // check offline.

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get the broken vfs reference
        assertNull(xmlcontent.getValue("VfsLink", Locale.ENGLISH));
        assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertFalse(errHandler.hasErrors());
        assertFalse(errHandler.hasWarnings());

        // check online, always automatically checked, and includes time window check
        project = cms.getRequestContext().getCurrentProject();
        try {
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // read the content again
            xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

            // validate the XML structure
            xmlcontent.validateXmlStructure(resolver);

            // get the broken vfs reference
            assertNull(xmlcontent.getValue("VfsLink", Locale.ENGLISH));
            assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

            // get and validate the html link, should be unchanged
            expectedHtmlLink = getExpected(cms, true);
            assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), false);

            // validate, and assert the result
            errHandler = xmlcontent.validate(cms);
            assertFalse(errHandler.hasErrors());
            assertFalse(errHandler.hasWarnings());
        } finally {
            cms.getRequestContext().setCurrentProject(project);
        }

        // recreate the file for the next test case
        cms.createResource(FILENAME, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test removing the parent node of a broken link.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testRemoveParent() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing removing the parent node of a broken link");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-12.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get a valid string
        assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

        // get and validate the vfs reference
        CmsLink expectedRefLink = getExpected(cms, false);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "ALink[1]/VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "ALink[1]/Html", "link0");
        CmsLink expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, htmlLink, true);

        // change the time window
        cms.lockResource(FILENAME);
        cms.setDateReleased(FILENAME, System.currentTimeMillis() + 1000000, false);
        cms.setDateExpired(FILENAME, System.currentTimeMillis() + 2000000, false);
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline, without link checking.

        // set the flag to prevent link checking
        cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get a valid string
        assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

        // get and validate the vfs reference
        expectedRefLink = getExpected(cms, false);
        refLink = getVfsFileRefLink(cms, xmlcontent, "ALink[1]/VfsLink");
        assertLink(expectedRefLink, refLink, false);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "ALink[1]/Html", "link0"), false);

        // validate, and assert the result
        CmsXmlContentErrorHandler errHandler = xmlcontent.validate(cms);
        assertTrue(errHandler.getErrors().isEmpty());
        assertEquals(errHandler.getWarnings().size(), 1);
        assertTrue(errHandler.getWarnings().containsKey(Locale.ENGLISH));
        Map<String, String> enWarnings = errHandler.getWarnings().get(Locale.ENGLISH);
        assertEquals(enWarnings.size(), 1);
        assertTrue(enWarnings.containsKey("ALink[1]/VfsLink[1]"));
        assertTrue(enWarnings.containsValue(org.opencms.xml.content.Messages.get().getBundle().key(
            org.opencms.xml.content.Messages.GUI_XMLCONTENT_CHECK_WARNING_NOT_RELEASED_0)));

        // reset the time to have the 'normal' behaviour again
        cms.getRequestContext().setRequestTime(System.currentTimeMillis());

        // check offline

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get a valid string
        assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

        // get the broken vfs reference
        assertNull(xmlcontent.getValue("ALink", Locale.ENGLISH));
        assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertFalse(errHandler.hasErrors());
        assertFalse(errHandler.hasWarnings());

        // check online, always automatically checked, and includes time window check
        CmsProject project = cms.getRequestContext().getCurrentProject();
        try {
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // read the content again
            xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

            // validate the XML structure
            xmlcontent.validateXmlStructure(resolver);

            // get a valid string
            assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

            // get the broken vfs reference
            assertNull(xmlcontent.getValue("ALink", Locale.ENGLISH));
            assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

            // validate, and assert the result
            errHandler = xmlcontent.validate(cms);
            assertFalse(errHandler.hasErrors());
            assertFalse(errHandler.hasWarnings());
        } finally {
            cms.getRequestContext().setCurrentProject(project);
        }

        // delete the link
        cms.lockResource(FILENAME);
        cms.deleteResource(FILENAME, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline, without link checking.

        // set the flag to prevent link checking
        cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get a valid string
        assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

        // get and validate the vfs reference
        expectedRefLink = getExpected(cms, false);
        refLink = getVfsFileRefLink(cms, xmlcontent, "ALink[1]/VfsLink");
        assertLink(expectedRefLink, refLink, false);

        // get and validate the html link, should be unchanged
        expectedHtmlLink = getExpected(cms, true);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "ALink[1]/Html", "link0"), false);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertEquals(errHandler.getErrors().size(), 1);
        assertTrue(errHandler.getErrors().containsKey(Locale.ENGLISH));
        Map<String, String> enErrors = errHandler.getErrors().get(Locale.ENGLISH);
        assertEquals(enErrors.size(), 1);
        assertTrue(enErrors.containsKey("ALink[1]/VfsLink[1]"));
        assertTrue(enErrors.containsValue(org.opencms.xml.content.Messages.get().getBundle().key(
            org.opencms.xml.content.Messages.GUI_XMLCONTENT_CHECK_ERROR_0)));
        assertTrue(errHandler.getWarnings().isEmpty());

        // reset the time to have the 'normal' behaviour again
        cms.getRequestContext().setRequestTime(System.currentTimeMillis());

        // check offline

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get a valid string
        assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

        // get the broken vfs reference
        assertNull(xmlcontent.getValue("ALink", Locale.ENGLISH));
        assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

        // validate, and assert the result
        errHandler = xmlcontent.validate(cms);
        assertFalse(errHandler.hasErrors());
        assertFalse(errHandler.hasWarnings());

        // check online, always automatically checked, and includes time window check
        project = cms.getRequestContext().getCurrentProject();
        try {
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // read the content again
            xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

            // validate the XML structure
            xmlcontent.validateXmlStructure(resolver);

            // get a valid string
            assertEquals(xmlcontent.getValue("String", Locale.ENGLISH).getStringValue(cms), "test");

            // get the broken vfs reference
            assertNull(xmlcontent.getValue("ALink", Locale.ENGLISH));
            assertEquals(xmlcontent.getNames(Locale.ENGLISH).size(), 1);

            // validate, and assert the result
            errHandler = xmlcontent.validate(cms);
            assertFalse(errHandler.hasErrors());
            assertFalse(errHandler.hasWarnings());
        } finally {
            cms.getRequestContext().setCurrentProject(project);
        }

        // recreate the file for the next test case
        cms.createResource(FILENAME, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests saving XML contents with links from/to various sites.<p>
     * 
     * @throws Exception 
     */
    public void testSiteLinks() throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        cms.createResource("/sites/testsite/", CmsResourceTypeFolder.getStaticTypeId());
        cms.unlockResource("/sites/testsite/");
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.getRequestContext().setSiteRoot("");
        // Site A
        CmsResource a = createTestFile("/sites/testsite/testfile-a.html");

        // Site B
        CmsResource b = createTestFile("/sites/default/folder1/testfile-b.html");

        // (W)orkplace site
        CmsResource w = createTestFile("/sites/default/testfile-w.html");

        // (S)hared
        CmsResource s = createTestFile("/shared/testfile-shared.html");

        // (R)oot
        CmsResource r = createTestFile("/system/testfile-system.html");

        for (String targetMacro : new String[] {"${TARGET}", "${TARGET2}"}) {

            // from default site 
            checkSetLink("/sites/default", w, targetMacro, "/testfile-w.html", w);
            checkSetLink("/sites/default", w, targetMacro, "http://localhost:8080/data/opencms/testfile-w.html", w);
            checkSetLink("/sites/default", w, targetMacro, "/data/opencms/testfile-w.html", w);

            checkSetLink("/sites/default", w, targetMacro, "http://localhost:8082/data/opencms/testfile-a.html", a);

            checkSetLink(
                "/sites/default",
                w,
                targetMacro,
                "http://localhost:8080/data/opencms/system/testfile-system.html",
                r);
            checkSetLink("/sites/default", w, targetMacro, "/data/opencms/system/testfile-system.html", r);
            checkSetLink("/sites/default", w, targetMacro, "/system/testfile-system.html", r);

            checkSetLink("/sites/default", w, targetMacro, "/shared/testfile-shared.html", s);
            checkSetLink(
                "/sites/default",
                w,
                targetMacro,
                "http://localhost:8080/data/opencms/shared/testfile-shared.html",
                s);
            checkSetLink("/sites/default", w, targetMacro, "/data/opencms/shared/testfile-shared.html", s);

            // from shared folder

            checkSetLink("/shared", s, targetMacro, "/testfile-shared.html", s);
            checkSetLink("/shared", s, targetMacro, "http://localhost:8080/data/opencms/testfile-shared.html", s);
            checkSetLink("/shared", s, targetMacro, "http://localhost:8080/data/opencms/shared/testfile-shared.html", s);
            checkSetLink("/shared", s, targetMacro, "/data/opencms/shared/testfile-shared.html", s);

            checkSetLink("/shared", s, targetMacro, "http://localhost:8082/data/opencms/testfile-a.html", a);
            checkSetLink("/shared", s, targetMacro, "http://localhost:8080/data/opencms/testfile-w.html", w);

            checkSetLink("/shared", s, targetMacro, "http://localhost:8080/data/opencms/system/testfile-system.html", r);
            checkSetLink("/shared", s, targetMacro, "/data/opencms/system/testfile-system.html", r);

            // from root site

            checkSetLink("", r, targetMacro, "/shared/testfile-shared.html", s);
            checkSetLink("", r, targetMacro, "http://localhost:8080/data/opencms/shared/testfile-shared.html", s);
            checkSetLink("", r, targetMacro, "/system/testfile-system.html", r);
            checkSetLink("", r, targetMacro, "http://localhost:8080/data/opencms/system/testfile-system.html", r);
            checkSetLink("", r, targetMacro, "/sites/testsite/testfile-a.html", a);
            checkSetLink("", r, targetMacro, "http://localhost:8080/data/opencms/sites/testsite/testfile-a.html", a);

            // From testsite

            checkSetLink("/sites/testsite", a, targetMacro, "http://localhost:8082/data/opencms/testfile-a.html", a);
            checkSetLink("/sites/testsite", a, targetMacro, "/testfile-a.html", a);
            checkSetLink("/sites/testsite", a, targetMacro, "http://localhost:8080/data/opencms/testfile-a.html", a);
            checkSetLink("/sites/testsite", a, targetMacro, "http://localhost:8080/data/opencms/testfile-w.html", w);
            checkSetLink("/sites/testsite", a, targetMacro, "/shared/testfile-shared.html", s);
            checkSetLink(
                "/sites/testsite",
                a,
                targetMacro,
                "http://localhost:8080/data/opencms/shared/testfile-shared.html",
                s);
            checkSetLink(
                "/sites/testsite",
                a,
                targetMacro,
                "http://localhost:8082/data/opencms/shared/testfile-shared.html",
                s);
            checkSetLink("/sites/testsite", a, targetMacro, "/system/testfile-system.html", r);
            checkSetLink(
                "/sites/testsite",
                a,
                targetMacro,
                "http://localhost:8080/data/opencms/system/testfile-system.html",
                r);
            checkSetLink(
                "/sites/testsite",
                a,
                targetMacro,
                "http://localhost:8082/data/opencms/system/testfile-system.html",
                r);
            checkSetLink("/sites/testsite", a, targetMacro, "http://localhost:8081/data/opencms/testfile-b.html", b);
        }
    }

    /**
     * Test updating the id of a moved resource in a broken link.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUpdateId() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the id update of a moved resource in a broken link");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-11.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        CmsLink expectedRefLink = getExpected(cms, false);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink expectedHtmlLink = getExpected(cms, true);
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);

        // delete the link
        cms.lockResource(FILENAME);
        cms.deleteResource(FILENAME, CmsResource.DELETE_REMOVE_SIBLINGS);

        // create a new one in place
        CmsResource res = cms.createResource(FILENAME, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockResource(FILENAME);
        OpenCms.getPublishManager().publishResource(cms, FILENAME);
        OpenCms.getPublishManager().waitWhileRunning();

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        updateLink(cms, expectedRefLink, res);
        assertLink(expectedRefLink, getVfsFileRefLink(cms, xmlcontent, "VfsLink"), true);

        // get and validate the html link
        updateLink(cms, expectedHtmlLink, res);
        assertLink(expectedHtmlLink, getHtmlLink(cms, xmlcontent, "Html", "link0"), true);
    }

    /**
     * Test updating the path of a moved resource in a broken link.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUpdatePath() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the path update of a moved resource in a broken link");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // now read the XML content
        String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-11.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate the vfs reference
        CmsLink expectedRefLink = getExpected(cms, false);
        CmsLink refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);

        // get and validate the html link
        CmsLink expectedHtmlLink = getExpected(cms, true);
        CmsLink htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);

        // store the current content, this is important since the original content had no ids
        content = xmlcontent.toString();

        // move the link
        cms.lockResource(FILENAME);
        cms.moveResource(FILENAME, FILENAME2);
        cms.unlockResource(FILENAME2);
        OpenCms.getPublishManager().publishResource(cms, FILENAME2);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsResource resMoved = cms.readResource(FILENAME2);

        // create a new resource in the original place
        CmsResource resNew = cms.createResource(FILENAME, CmsResourceTypePlain.getStaticTypeId());

        // read the content again
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // get and validate vfs reference
        updateLink(cms, expectedRefLink, resMoved);
        refLink = getVfsFileRefLink(cms, xmlcontent, "VfsLink");
        assertLink(expectedRefLink, refLink, true);
        assertNotSame(resNew.getRootPath(), refLink.getTarget());
        assertNotSame(resNew.getStructureId(), refLink.getStructureId());

        // get and validate html link
        updateLink(cms, expectedHtmlLink, resMoved);
        htmlLink = getHtmlLink(cms, xmlcontent, "Html", "link0");
        assertLink(expectedHtmlLink, htmlLink, true);
        assertNotSame(resNew.getRootPath(), htmlLink.getTarget());
        assertNotSame(resNew.getStructureId(), htmlLink.getStructureId());

        // recreate the file for the next test case
        cms.lockResource(FILENAME2);
        cms.deleteResource(FILENAME2, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(FILENAME2);
        OpenCms.getPublishManager().publishResource(cms, FILENAME2);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Replaces the content of an xmlcontent resource with a new content which contains a given link, and then checks if the
     * link has been correctly created.<p>
     * 
     * @param siteRoot the site root to set 
     * @param source the resource whose content should be written 
     * @param macro the macro to replace with the link 
     * @param targetUri the URI which should be saved 
     * @param expected the resource to which the resource should link after saving 
     * 
     * @throws Exception if something goes wrong 
     */
    private void checkSetLink(String siteRoot, CmsResource source, String macro, String targetUri, CmsResource expected)
    throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot(siteRoot);
        String data = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-15.xml", "UTF-8");
        data = data.replace(macro, targetUri);
        CmsFile file = cms.readFile(source);
        byte[] originalContent = file.getContents();
        file.setContents(data.getBytes());
        cms.writeFile(file);
        file = cms.readFile(source);
        String newData = new String(file.getContents(), "UTF-8");
        assertTrue(
            "[" + newData + "] does not contain " + expected.getStructureId(),
            newData.contains(expected.getStructureId().toString()));
        file.setContents(originalContent);
        cms.writeFile(file);
    }

    /**
     * Creates a test file for the testSiteLinks test.<p>
     * 
     * @param rootPath the test file path 
     * @return the new test file 
     * @throws Exception if something goes wrong 
     */
    private CmsResource createTestFile(String rootPath) throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        int xmlcontentType = OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId();
        byte[] content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-14.xml");
        CmsResource tsl = cms.createResource(rootPath, xmlcontentType, content, Collections.<CmsProperty> emptyList());
        return tsl;
    }
}