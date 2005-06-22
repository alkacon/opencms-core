/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/TestXmlUtils.java,v $
 * Date   : $Date: 2005/06/22 10:38:32 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.xml;

import junit.framework.TestCase;

/**
 * @author Alexander Kandzior 
 * @version $Revision: 1.4 $
 * 
 * @since 5.5.4
 */
public class TestXmlUtils extends TestCase {

    /**
     * Test case for the Xpath generation methods.
     * 
     * @throws Exception in case the test fails
     */
    public void testCreateXpath() throws Exception {
        
        assertEquals("Title[1]", CmsXmlUtils.createXpath("Title", 1));
        assertEquals("Title[1]/Test[1]", CmsXmlUtils.createXpath("Title/Test", 1));
        assertEquals("Title[1]/Test[1]/Toast[1]", CmsXmlUtils.createXpath("Title/Test/Toast", 1));
        assertEquals("Title[4]/Test[2]/Toast[1]", CmsXmlUtils.createXpath("Title[4]/Test[2]/Toast[1]", 1));        
        assertEquals("Title[1]/Test[2]/Toast[2]", CmsXmlUtils.createXpath("Title/Test[2]/Toast", 2));
        assertEquals("Title[1]/Test[1]/Toast[1]/Toll[5]", CmsXmlUtils.createXpath("Title/Test/Toast/Toll", 5));
    }
    
    /**
     * Test case for the Xpath simplification.
     * 
     * @throws Exception in case the test fails
     */
    public void testSimplifyXpath() throws Exception {
                
        assertEquals("Title[1]", CmsXmlUtils.simplifyXpath("/Title[1]"));
        assertEquals("Title[1]", CmsXmlUtils.simplifyXpath("Title[1]/"));
        assertEquals("Title[1]", CmsXmlUtils.simplifyXpath("/Title[1]/"));
        
        assertEquals("Title", CmsXmlUtils.simplifyXpath("/Title"));
        assertEquals("Title", CmsXmlUtils.simplifyXpath("Title/"));
        assertEquals("Title", CmsXmlUtils.simplifyXpath("/Title/"));
    }
    
    /**
     * Test case for the Xpath index cut off.
     * 
     * @throws Exception in case the test fails
     */
    public void testGetXpathIndex() throws Exception {
        
        assertEquals("", CmsXmlUtils.getXpathIndex("Title"));
        assertEquals("[1]", CmsXmlUtils.getXpathIndex("Title[1]"));
        assertEquals("[1]", CmsXmlUtils.getXpathIndex("Title[4]/Test[2]/Toast[1]"));        
        assertEquals("", CmsXmlUtils.getXpathIndex("Title/Test[2]/Toast"));
        assertEquals("[5]", CmsXmlUtils.getXpathIndex("Title/Test/Toast/Toll[5]"));
    }
    
    /**
     * Test case for the Xpath remove methods.
     * 
     * @throws Exception in case the test fails
     */
    public void testRemoveXpath() throws Exception {
        
        assertEquals("Title", CmsXmlUtils.removeXpath("Title[1]"));
        assertEquals("Title/Test", CmsXmlUtils.removeXpath("Title[1]/Test[1]"));
        assertEquals("Title/Test/Toast", CmsXmlUtils.removeXpath("Title[1]/Test[1]/Toast"));
        assertEquals("Title/Test/Toast", CmsXmlUtils.removeXpath("Title/Test[2]/Toast[1]"));        
        assertEquals("Title/Test/Toast", CmsXmlUtils.removeXpath("Title/Test[2]/Toast"));
        assertEquals("Title/Test/Toast/Toll", CmsXmlUtils.removeXpath("Title[1]/Test[1]/Toast[1]/Toll[5]"));      
    }
}
