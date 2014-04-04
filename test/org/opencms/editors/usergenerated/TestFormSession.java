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

package org.opencms.editors.usergenerated;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import com.google.common.base.Optional;

/**
 * Tests the form session methods.<p>
 */
public class TestFormSession extends OpenCmsTestCase {

    /** Schema id. */
    private static final String SCHEMA_ID_IMAGE = "http://www.opencms.org/image.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_LINK = "http://www.opencms.org/link.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_PARAGRAPH = "http://www.opencms.org/paragraph.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_TEXTBLOCK = "http://www.opencms.org/textblock.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestFormSession(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        Test wrapper = generateSetupTestWrapper(TestFormSession.class, "simpletest", "/");
        return wrapper;
    }

    /**
     * Tests the add values method.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testAddValues() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        CmsXmlContentDefinition definition = unmarshalDefinition(resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_ID_TEXTBLOCK,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        String fileContent = CmsFileUtil.readFile(
            "org/opencms/editors/usergenerated/tb_00001.xml",
            CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(fileContent, CmsEncoder.ENCODING_UTF_8, resolver);
        CmsFormSession session = new CmsFormSession(getCmsObject());
        Locale editLocale = new Locale("en");
        Map<String, String> values = session.getContentValues(xmlContent, editLocale);

        xmlContent.removeLocale(editLocale);
        session.addContentValues(xmlContent, editLocale, values);

        // all content values should be restored
        assertEquals(fileContent, new String(xmlContent.marshal(), CmsEncoder.ENCODING_UTF_8));
    }

    /**
     * Tests creation of new contents.<p>
     * 
     * @throws Exception - 
     */
    public void testCreateContent() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsFormConfiguration config = new CmsFormConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        CmsFormSession session = new CmsFormSession(cms, config);
        CmsResource createdContent = session.createXmlContent();
        assertTrue(
            "The content should be created in the content folder",
            createdContent.getRootPath().startsWith(contentFolder.getRootPath()));
        assertEquals(createdContent.getTypeId(), OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId());

        /* Try with a different resource type. */
        config = new CmsFormConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            contentFolder,
            "n1_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        session = new CmsFormSession(cms, config);
        createdContent = session.createXmlContent();
        assertEquals(createdContent.getTypeId(), OpenCms.getResourceManager().getResourceType("plain").getTypeId());
    }

    /**
     * Tests that editing multiple files in a single form session causes an error.<p>
     * 
     * @throws Exception - 
     */
    public void testFailEditMultipleContents() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsFormConfiguration config = new CmsFormConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());

        cms.createResource("/" + getName() + "/file1.txt", 1);
        cms.createResource("/" + getName() + "/file2.txt", 1);

        CmsFormSession session;

        // case create -> load 
        session = new CmsFormSession(cms, config);
        session.createXmlContent();
        try {

            session.loadXmlContent("file1.txt");
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsIllegalStateException e) {
            // ok 
        }

        // case create -> create 
        session = new CmsFormSession(cms, config);
        session.createXmlContent();
        try {
            session.createXmlContent();
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsIllegalStateException e) {
            // ok 
        }

        // case load -> create 
        session = new CmsFormSession(cms, config);
        session.loadXmlContent("file1.txt");
        try {
            session.createXmlContent();
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsIllegalStateException e) {
            // ok 
        }

        // case load -> load
        session = new CmsFormSession(cms, config);
        session.loadXmlContent("file1.txt");
        try {
            session.loadXmlContent("file2.txt");
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsIllegalStateException e) {
            // ok 
        }

    }

    /**
     * Tests the get values method.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testGetValues() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        CmsXmlContentDefinition definition = unmarshalDefinition(resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_ID_TEXTBLOCK,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        String fileContent = CmsFileUtil.readFile(
            "org/opencms/editors/usergenerated/tb_00001.xml",
            CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(fileContent, CmsEncoder.ENCODING_UTF_8, resolver);
        CmsFormSession session = new CmsFormSession(getCmsObject());
        Map<String, String> values = session.getContentValues(xmlContent, new Locale("en"));
        assertEquals("Full width example", values.get("Title[1]"));
    }

    /**
     * Read the given file and cache it's contents as XML schema with the given system id.
     *
     * @param fileName the file name to read
     * @param systemId the XML schema system id to use
     *
     * @throws IOException in case of errors reading the file
     */
    private void cacheXmlSchema(String fileName, String systemId) throws IOException {

        // read the XML schema
        byte[] schema = CmsFileUtil.readFile(fileName);
        // store the XML schema in the resolver
        CmsXmlEntityResolver.cacheSystemId(systemId, schema);
    }

    /**
     * Unmarshals the content definition used within the tests.<p>
     * 
     * @param resolver the entity resolver
     * 
     * @return the content definition
     * 
     * @throws IOException if reading the files fails
     * @throws CmsXmlException if parsing the schema fails
     */
    private CmsXmlContentDefinition unmarshalDefinition(CmsXmlEntityResolver resolver)
    throws IOException, CmsXmlException {

        // fire "clear cache" event to clear up previously cached schemas
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
        cacheXmlSchema("org/opencms/editors/usergenerated/image.xsd", SCHEMA_ID_IMAGE);
        cacheXmlSchema("org/opencms/editors/usergenerated/link.xsd", SCHEMA_ID_LINK);
        cacheXmlSchema("org/opencms/editors/usergenerated/paragraph.xsd", SCHEMA_ID_PARAGRAPH);

        String schemaContent = CmsFileUtil.readFile(
            "org/opencms/editors/usergenerated/textblock.xsd",
            CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(
            schemaContent,
            SCHEMA_ID_TEXTBLOCK,
            resolver);
        return definition;
    }
}
