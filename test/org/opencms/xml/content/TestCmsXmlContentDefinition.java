/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/content/TestCmsXmlContentDefinition.java,v $
 * Date   : $Date: 2004/11/08 15:06:43 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlStringValue;

import java.io.StringWriter;
import java.util.Locale;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Tests for generating an XML content definition.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestCmsXmlContentDefinition extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentDefinition(String arg0) {
        super(arg0);
    } 
    
    /**
     * Basic test for xml content definitions.<p> 
     * 
     * @throws Exception in case an error occured
     */
    public void testCmsXmlContentDefiniton() throws Exception {
        CmsXmlContentDefinition cd1 = new CmsXmlContentDefinition("Article", null);         
        
        cd1.addType(new CmsXmlStringValue("Author"));
        cd1.addType(new CmsXmlStringValue("Teaser", 0, 1));
        cd1.addType(new CmsXmlStringValue("Toaster", 0, Integer.MAX_VALUE));
        cd1.addType(new CmsXmlStringValue("Rollercoaster", 3, 10));
        
        Document schema;
        StringWriter out;
        
        out = new StringWriter();        
        schema = cd1.getSchema();                
        
        XMLWriter writer;
        
        // output the schema XML        
        writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
        writer.write(schema);
        writer.flush();
            
        System.out.println(out.toString());
        
        CmsXmlContentDefinition cd2 = CmsXmlContentDefinition.unmarshal(out.toString().getBytes(), null, null);
        
        out = new StringWriter();
        schema = cd2.getSchema();                
        
        // output the schema XML
        writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
        writer.write(schema);
        writer.flush();
        
        System.out.println(out.toString());
        
        assertEquals(cd1, cd2);
        
        cd1.addType(new CmsXmlStringValue("AddedLater"));
        assertFalse(cd1.equals(cd2));
    }
    
    /** 
     * Tests creation of an XML content from a XML content definition.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateXmlContent() throws Throwable {
        
        String schemaUri = "http://www.opencms.org/test.xsd";
        CmsXmlContentDefinition cd1 = new CmsXmlContentDefinition("Article", schemaUri);         
        
        cd1.addType(new CmsXmlStringValue("Author"));
        cd1.addType(new CmsXmlStringValue("Teaser"));        
        cd1.addType(new CmsXmlStringValue("Text"));
        cd1.addType(new CmsXmlDateTimeValue("Date"));
        cd1.addType(new CmsXmlStringValue("Option", 0, 1)); 
        
        CmsXmlEntityResolver.cacheSystemId(schemaUri, cd1.getSchema().asXML().getBytes("UTF-8"));
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        
        Locale locale = Locale.ENGLISH;
        
        CmsXmlContent content = new CmsXmlContent(cd1, locale, "UTF-8");
        content.validateXmlStructure(resolver);

        // change cd to break validation
        cd1.addType(new CmsXmlStringValue("Kaputt"));
        CmsXmlEntityResolver.cacheSystemId(schemaUri, cd1.getSchema().asXML().getBytes("UTF-8"));

        try {
            content.validateXmlStructure(resolver);
            fail("Validation wrongly works with modified cd");          
        } catch (CmsXmlException e) {
            // this is expected, so no error
        }
        
        // output the schema XML
        System.out.println(content.toString());
    }
}
