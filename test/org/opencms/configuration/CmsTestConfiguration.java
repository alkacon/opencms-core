/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/configuration/CmsTestConfiguration.java,v $
 * Date   : $Date: 2004/03/06 18:48:38 $
 * Version: $Revision: 1.3 $
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
package org.opencms.configuration;

import org.opencms.main.OpenCms;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;


/**
 * Dummy class for configuration testing.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsTestConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    private String m_content1;
    private String m_content2;
    
    /**
     * The public contructor is hidden to prevent generation of instances of this class.<p> 
     */
    public CmsTestConfiguration() {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Empty constructor called on " + this);
        }  
    }
    
    /**
     * Test method to add a value.<p>
     * 
     * @param name the name of the test
     * @param value the value of the test
     */
    public void addTest(String name, String value) {
        if ("test1".equals(name)) {
            m_content1 = value;
        }
        if ("test2".equals(name)) {
            m_content2 = value;
        }        
    }   

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {
        // add factory create method for "real" instance creation
        digester.addFactoryCreate("*/tests", CmsTestConfiguration.class);
        // add action methods
        digester.addCallMethod("*/tests/test", "addTest", 2);
        digester.addCallParam("*/tests/test", 0, A_NAME);
        digester.addCallParam("*/tests/test", 1);
        // add the configured object to the calling configuration after the node has been processed
        digester.addSetNext("*/tests", "addConfiguration");
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        Element testElement = parent.addElement("tests");
        testElement.addElement("test").addAttribute(A_NAME, "test1").addText(m_content1);
        testElement.addElement("test").addAttribute(A_NAME, "test2").addText(m_content2);
        return testElement;
    }
    
    /**
     * Main executable for testing purposes.<p>
     * 
     * @param args the arguments
     * @throws SAXException in case of XML parsing issues
     * @throws IOException in case of file IO issues
     */
    public static void main(String[] args) throws SAXException, IOException {

        // set "OpenCms" system property to "test" for allowing the logger to be used
        System.setProperty("OpenCmsLog", "opencms_test.log");
        
        // generate registy singleton
        CmsConfigurationManager manager = new CmsConfigurationManager();

        // get URL of test input resource
        URL inputUrl = ClassLoader.getSystemResource("org/opencms/configuration/opencms.xml");
        
        // now digest the XML
        manager.loadXmlConfiguration(inputUrl);
        
        System.out.println("Vfs configuration instance: " + manager.getConfiguration(CmsVfsConfiguration.class));
        System.out.println("Import/export configuration instance: " + manager.getConfiguration(CmsImportExportConfiguration.class));
        
        // gernerate XML document for the configuration
        Document doc = manager.generateXml();
                               
        // output the document
        XMLWriter writer;        
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
        format.setTrimText(false);
        format.setEncoding("UTF-8");
        writer = new XMLWriter( System.out, format );
        writer.write( doc );        
    }       
}
