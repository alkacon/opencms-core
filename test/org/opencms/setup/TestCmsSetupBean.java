/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/setup/TestCmsSetupBean.java,v $
 * Date   : $Date: 2005/06/23 10:47:27 $
 * Version: $Revision: 1.6 $
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
 
package org.opencms.setup;

import org.opencms.test.OpenCmsTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/** 
 * @author Alexander Kandzior 
 * @version $Revision: 1.6 $
 * 
 * @since 5.0
 */
public class TestCmsSetupBean extends OpenCmsTestCase {

    // DEBUG flag
    // private static final boolean DEBUG = true;
     
    // private static final String PROPERTIES = "/opencms/etc/config/opencms.properties";
    // private static final String PROPERTIES = "/../OpenCms6-Setup/webapp/WEB-INF/config/opencms.properties";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSetupBean(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests the method saveProperties.<p>
     * 
     * @throws IOException if something goes wrong
     */
    public void testSaveProperties() throws IOException {
        CmsSetupBean bean = new CmsSetupBean();
        bean.init("", null, null);

        String base = getTestDataPath(File.separator + "WEB-INF" + File.separator + "config" +  File.separator);
        String inputFile = base + "opencms.properties";
        String outputFile = base + "output.properties";
               
        System.out.println("Reading properties from " + inputFile);
        ExtendedProperties oldProperties = bean.loadProperties(inputFile);
        
        System.out.println("Writing properties to " + outputFile);
        bean.copyFile(inputFile, outputFile);
        bean.saveProperties(oldProperties, outputFile, false);
        
        System.out.println("Checking properties from " + outputFile);
        ExtendedProperties newProperties = bean.loadProperties(outputFile);
        
        for (Iterator i = oldProperties.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            String oldValue = "", newValue = "";
            Object obj = oldProperties.get(key);
            
            if (obj instanceof Vector) {
                StringBuffer buf;
                
                buf = new StringBuffer();
                for (Iterator j = ((Vector)obj).iterator(); j.hasNext();) {
                    buf.append("[" + (String)j.next() + "]");
                }
                oldValue = buf.toString();
                
                buf = new StringBuffer();
                for (Iterator j = ((Vector)newProperties.get(key)).iterator(); j.hasNext();) {
                    buf.append("[" + (String)j.next() + "]");
                }
                newValue = buf.toString();

            } else {
                oldValue = (String)obj;
                newValue = (String)newProperties.get(key);
            }
            System.out.println(key);
            System.out.println(oldValue);
            System.out.println(newValue);
            System.out.println("---");
            assertEquals(oldValue, newValue);   
        }        
        
        // clean up - remvove generated file
        File output = new File(outputFile);
        output.delete();
    }
}
