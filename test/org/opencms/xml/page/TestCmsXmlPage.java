/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/page/TestCmsXmlPage.java,v $
 * Date   : $Date: 2004/08/03 07:19:03 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.xml.page;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.i18n.CmsEncoder;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlContentTypeManager;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlHtmlValue;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for the XML page that dosen't require a running OpenCms system.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 5.5.0
 */
public class TestCmsXmlPage extends TestCase {

    private static final String C_XMLPAGE_SCHEMA_SYSTEM_ID = CmsConfigurationManager.C_DEFAULT_DTD_PREFIX + "xmlpage.xsd";
        
    private static final String UTF8 = CmsEncoder.C_UTF8_ENCODING;
        
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsXmlPage(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests creating a XMl page (final version) with the API.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageCreateMinimal() throws Exception {
        
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        String pageStr = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        System.out.println("Testing creation of a minimal valid XML page:\n");
        System.out.println(pageStr);
        
        // now compare against stored version of minimal XML page 
        String minimalPageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        // remove windows-style linebreaks
        minimalPageStr = CmsStringUtil.substitute(minimalPageStr, "\r\n", "\n");
        assertEquals(pageStr, minimalPageStr);

        // create a new XML page with this content, marshal it and compare
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);        
        byte[] bytes = page.marshal();
        String newPageStr = new String(bytes, UTF8);                
        assertEquals(pageStr, newPageStr);        
    }
    
    /**
     * Tests writing elements to the "old", pre 5.5.0 version of the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageWriteOldVersion() throws Exception {
                
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        CmsXmlPage page;
        String content; 
        
        // validate "old" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-1.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        page.addValue("body3", Locale.ENGLISH);
        page.setStringValue(null, "body3", Locale.ENGLISH, "English WRITTEN! Image <img src=\"/test/image.gif\" />");        
        assertTrue(page.hasValue("body3", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body3", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English WRITTEN! Image <img src=\"/test/image.gif\" />", page.getStringValue(null, "body3", Locale.ENGLISH));        
    }
    
    /**
     * Tests writing elements to the updated, final version of the XML page.<p> 
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageWriteFinalVersion() throws Exception {
                
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        CmsXmlPage page;
        String content; 
        
        // validate "final" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        page.addValue("body3", Locale.ENGLISH);
        page.setStringValue(null, "body3", Locale.ENGLISH, "English WRITTEN! Image <img src=\"/test/image.gif\" />");        
        assertTrue(page.hasValue("body3", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body3", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English WRITTEN! Image <img src=\"/test/image.gif\" />", page.getStringValue(null, "body3", Locale.ENGLISH));        
    }
        
    /**
     * Tests reading elements from the updated, final version of the XML page.<p> 
     * 
     * @throws Exception  in case something goes wrong
     */    
    public void testXmlPageReadFinalVersion() throws Exception {
        
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        CmsXmlPage page;
        String content; 
        
        // validate "final" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.ENGLISH));        
        
        // validate "final" xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        assertTrue(page.hasValue("body", Locale.GERMAN));
        assertTrue(page.hasValue("body2", Locale.ENGLISH));   
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.ENGLISH));        
        assertEquals("English 2!", page.getStringValue(null, "body2", Locale.ENGLISH));        
        assertEquals("Deutsch! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.GERMAN));
    }
    
    /**
     * Tests reading elements from the "old", pre 5.5.0 version of the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageReadOldVersion() throws Exception {
                
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        CmsXmlPage page;
        String content; 
        
        // validate "old" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-1.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.ENGLISH));        

        // validate "old" xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-2.xml", UTF8);        
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        assertTrue(page.hasValue("body", Locale.GERMAN));
        assertTrue(page.hasValue("body2", Locale.ENGLISH));      
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.ENGLISH));        
        assertEquals("English 2!", page.getStringValue(null, "body2", Locale.ENGLISH));        
        assertEquals("Deutsch! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(null, "body", Locale.GERMAN));        
    }
    
    /**
     * Test validating a XML page with the XML page schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValidateXmlPageWithSchema() throws Exception {
    
        CmsXmlContentTypeManager typeManager = CmsXmlContentTypeManager.getTypeManager();
        typeManager.addContentType(CmsXmlHtmlValue.class);    
        
        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        String content;        
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage.xsd", UTF8);
        
        // store schema in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_XMLPAGE_SCHEMA_SYSTEM_ID, content.getBytes(UTF8));
        
        // validate the minimal xmlpage
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), UTF8, resolver);        

        // validate the xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), UTF8, resolver);        

        // validate the xmlpage 3
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), UTF8, resolver);        
    }    
    
    /**
     * Tests using a XML page with a XML content definition.<p> 
     * 
     * @throws Exception  in case something goes wrong
     */
    public void testXmlPageAsXmlContentDefinition() throws Exception {
        
        CmsXmlContentTypeManager typeManager = CmsXmlContentTypeManager.getTypeManager();
        typeManager.addContentType(CmsXmlHtmlValue.class);        
        
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        String content;        
                
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage.xsd", UTF8);
        CmsXmlContentDefinition cd1 = CmsXmlContentDefinition.unmarshal(content, C_XMLPAGE_SCHEMA_SYSTEM_ID, resolver);
        
        // create new content definition form objects
        CmsXmlContentDefinition cd2 = new CmsXmlContentDefinition("page", C_XMLPAGE_SCHEMA_SYSTEM_ID);
        cd2.addType(new CmsXmlHtmlValue("element", 0, Integer.MAX_VALUE));    
        
        // ensure content definitions are equal
        assertEquals(cd1, cd2);
        
        // obtain content definition from a XML page
        String pageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);    
        CmsXmlContentDefinition cd3 = page.getContentDefinition(resolver);

        // ensure content definitions are equal
        assertEquals(cd1, cd3);    
    }
}
