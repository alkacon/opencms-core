/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/configuration/Attic/CmsConfigurationTest.java,v $
 * Date   : $Date: 2004/04/05 05:42:01 $
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

package org.opencms.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsConfigurationTest extends TestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public CmsConfigurationTest(String arg0) {
        super(arg0);
        
        // set "OpenCms" system property to "test" for allowing the logger to be used
        System.setProperty("OpenCmsLog", "opencms_test.log");
    }
    
    /**
     * Loads the configuration using the configuration manager,
     * if anyting goes wrong an exception is thrown and the test fails.<p>
     * 
     * @throws SAXException if something goes wrong
     * @throws IOException if something goes wrong
     */
    public void testLoadXmlConfiguration() throws SAXException, IOException {
        
        // get URL of test input resource
        URL inputUrl = ClassLoader.getSystemResource("org/opencms/configuration/");
        // generate the configuration manager
        CmsConfigurationManager manager = new CmsConfigurationManager(inputUrl.getFile());
        // now digest the XML
        manager.loadXmlConfiguration();     
        
        
        // generate an output XML format
        List allConfigurations = new ArrayList();
        allConfigurations.add(manager);
        allConfigurations.addAll(manager.getConfigurations());
        
        Iterator i = allConfigurations.iterator();
        while (i.hasNext()) {
            I_CmsXmlConfiguration config = (I_CmsXmlConfiguration)i.next();
            System.out.println("\n\nConfiguration instance: " + config + ":\n");
            
            // gernerate XML document for the configuration
            Document doc = manager.generateXml(config);
                                   
            // output the document
            XMLWriter writer;        
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(4);
            format.setTrimText(false);
            format.setEncoding("UTF-8");
            writer = new XMLWriter(System.out, format);
            writer.write(doc);                             
        }         
    }
}
