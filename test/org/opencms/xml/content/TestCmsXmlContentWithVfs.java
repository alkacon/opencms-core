/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/content/TestCmsXmlContentWithVfs.java,v $
 * Date   : $Date: 2004/11/08 15:06:43 $
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
 
package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;

import java.util.Iterator;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the link resolver for XML contents.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class TestCmsXmlContentWithVfs extends OpenCmsTestCase {

    private static final String C_SCHEMA_SYSTEM_ID_2 = "http://www.opencms.org/test2.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_3 = "http://www.opencms.org/test3.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_4 = "http://www.opencms.org/test4.xsd";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsXmlContentWithVfs(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestCmsXmlContentWithVfs.class.getName());
                
        suite.addTest(new TestCmsXmlContentWithVfs("testContentHandler"));
        suite.addTest(new TestCmsXmlContentWithVfs("testSchemaCascade"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLinkResolver"));

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
     * Test using a cascaded XML content schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testSchemaCascade() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing cascading XML content schema");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        
        String content;        
                
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-4.xsd", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_4, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_4, definition.getSchema().asXML().getBytes(CmsEncoder.C_UTF8_ENCODING));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver); 

        assertTrue(xmlcontent.hasValue("Title", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Title[0]", Locale.ENGLISH));        
        assertTrue(xmlcontent.hasValue("Cascade[0]/Html[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[0]/VfsLink[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[0]/VfsLink[1]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[1]/Html[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[1]/Html", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink", Locale.ENGLISH));      

//        assertTrue(xmlcontent.hasValue("Cascade", Locale.ENGLISH));
//        assertTrue(xmlcontent.hasValue("Cascade[0]", Locale.ENGLISH));
//        assertTrue(xmlcontent.hasValue("Cascade[1]", Locale.ENGLISH));
        
        assertTrue(xmlcontent.hasValue("Cascade/Html", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade/Html[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[0]", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[1]", Locale.ENGLISH));          
        
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
    }    
    
    /**
     * Test using a different XML content handler then the default handler.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testContentHandler() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing individual content handler for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        
        String content;        
                
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-3.xsd", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_3, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-3.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_3, definition.getSchema().asXML().getBytes(CmsEncoder.C_UTF8_ENCODING));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver); 

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));      
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
    }
    
    /**
     * Test resolving the links from an XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testLinkResolver() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing link resolver for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        
        String content;        
                
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-2.xsd", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_2, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-2.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_2, definition.getSchema().asXML().getBytes(CmsEncoder.C_UTF8_ENCODING));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver); 

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));        
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());
        
        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)xmlcontent.getValue("Html", Locale.ENGLISH);
        CmsXmlVfsFileValue vfsValue = (CmsXmlVfsFileValue)xmlcontent.getValue("VfsLink", Locale.ENGLISH);

        // must set the value again to ensure link table is properly initialized
        htmlValue.setStringValue(cms, xmlcontent, htmlValue.getStringValue(cms, xmlcontent));
        vfsValue.setStringValue(cms, xmlcontent, vfsValue.getStringValue(cms, xmlcontent));
        
        Iterator i;        
        CmsLinkTable table;
        
        table = htmlValue.getLinkTable();
        assertEquals(3, table.size());

        i = table.iterator();
        int result = 0;
        while (i.hasNext()) {
            // iterate all links and check if the required values are found
            CmsLink link = (CmsLink)i.next();
            if (link.getTarget().equals("/sites/default/index.html") && link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("http://www.alkacon.com") && !link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("/sites/default/folder1/index.html") 
            && link.getQuery().equals("a=b&c=d")
            && link.getAnchor().equals("anchor")
            && link.isInternal()) {
                result++;
            }
        }
        assertEquals(3, result);
      
        table = vfsValue.getLinkTable();
        assertEquals(1, table.size());
        CmsLink link = (CmsLink)table.iterator().next();
        assertEquals("/sites/default/index.html", link.getTarget());
        assertTrue(link.isInternal());
        assertEquals("/index.html", vfsValue.getStringValue(cms, xmlcontent));
    }    
}
