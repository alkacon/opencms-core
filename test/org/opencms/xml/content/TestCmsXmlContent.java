/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/content/TestCmsXmlContent.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
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
 
package org.opencms.xml.content;

import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlStringValue;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for generating an XML content.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.7 $
 */
public class TestCmsXmlContent extends TestCase {

    private static final String C_SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/test1.xsd";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsXmlContent(String arg0) {
        super(arg0);
    }
    
    /**
     * Test unmarshalling an XML page from a String.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshalFromString() throws Exception {
        
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        String content;        
                
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-1.xsd", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_1, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-1.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_1, definition.getSchema().asXML().getBytes(CmsEncoder.C_UTF8_ENCODING));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver); 
                        
        assertTrue(xmlcontent.hasValue("String", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("DateTime", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Locale", Locale.ENGLISH));
        
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());
        
        CmsXmlStringValue stringValue = (CmsXmlStringValue)xmlcontent.getValue("String", Locale.ENGLISH);
        CmsXmlDateTimeValue dateTimeValue = (CmsXmlDateTimeValue)xmlcontent.getValue("DateTime", Locale.ENGLISH);
        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)xmlcontent.getValue("Html", Locale.ENGLISH);
        CmsXmlLocaleValue localeValue = (CmsXmlLocaleValue)xmlcontent.getValue("Locale", Locale.ENGLISH);
        
        assertEquals("Multitest 1", stringValue.getStringValue(null));
        assertEquals("-58254180000", dateTimeValue.getStringValue(null));
        assertEquals("<p>This is some Html</p>", htmlValue.getStringValue(null));
        assertEquals("en_EN", localeValue.getStringValue(null));
    }
}
