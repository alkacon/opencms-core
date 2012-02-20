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

package org.opencms.ade.contenteditor;

import com.alkacon.acacia.shared.Entity;
import com.alkacon.vie.shared.I_Type;

import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.dom4j.Element;

/**
 * Tests the content service for generating serializable XML content entities and type definitions and persisting those entities.<p>
 */
public class TestCmsContentService extends TestCase {

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/test1.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsContentService(String arg0) {

        super(arg0);
    }

    /**
     * Tests the read types method.<p>
     * 
     * @throws Exception if something fails 
     */
    public void testReadTypes() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        String baseTypeName = "OpenCmsMultitest";
        CmsContentService service = new CmsContentService();
        String content;
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-1.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1, resolver);
        baseTypeName = service.getTypeUri(definition);
        Map<String, I_Type> registeredTypes = service.readTypes(definition, new Locale("en"));

        assertFalse("Registered types should not be empty", registeredTypes.isEmpty());
        assertTrue("Registered types should contain type: " + baseTypeName, registeredTypes.containsKey(baseTypeName));
        assertEquals("Should contain 5 types, the base type and 4 simple types", 5, registeredTypes.size());
    }

    /**
     * Tests the read entity method.<p>
     * 
     * @throws Exception if something fails 
     */
    public void testReadEntity() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        String baseTypeName = "OpenCmsMultitest";
        CmsContentService service = new CmsContentService();
        String content;
        // unmarshal content definition
        Locale locale = new Locale("en");
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-1.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1, resolver);
        baseTypeName = service.getTypeUri(definition);
        Map<String, I_Type> registeredTypes = service.readTypes(definition, locale);

        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-1.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_SYSTEM_ID_1,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        Entity result = null;

        if (xmlcontent.hasLocale(locale)) {
            Element element = xmlcontent.getLocaleNode(locale);
            result = service.readEntity(xmlcontent, element, locale, "myEntity", "", baseTypeName, registeredTypes);
        }
        assertNotNull("Result should not be null", result);
        // TODO: check out the result some more to ensure success
    }
}
