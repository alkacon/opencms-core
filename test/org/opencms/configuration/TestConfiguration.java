/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/configuration/TestConfiguration.java,v $
 * Date   : $Date: 2004/10/28 09:11:17 $
 * Version: $Revision: 1.4 $
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

import org.opencms.test.OpenCmsTestCase;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.xml.sax.InputSource;



/**
 * Tests for the OpenCms configuration handling.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class TestConfiguration extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestConfiguration(String arg0) {
        super(arg0, false);
    }
    
    /**
     * Loads the configuration using the configuration manager,
     * if anyting goes wrong an exception is thrown and the test fails.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testLoadXmlConfiguration() throws Exception {

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
            String xmlOrigFile = inputUrl.getFile() + config.getXmlFileName();
            System.out.println("\n\nConfiguration instance: " + config + ":\n");

            // gernerate XML document for the configuration
            Document outputDoc = manager.generateXml(config);
            outputDoc.setName(config.getXmlFileName());

            // load XML from original file and compare to generated document
            InputSource source = new InputSource(new FileInputStream(xmlOrigFile));
            Document inputDoc = CmsXmlUtils.unmarshalHelper(source, new CmsXmlEntityResolver(null));

            // output the document
            System.out.println("---");
            System.out.println(CmsXmlUtils.marshal(outputDoc, "UTF-8"));
            System.out.println("---");
            
            assertEquals(outputDoc, inputDoc);
        }
    }    
}
