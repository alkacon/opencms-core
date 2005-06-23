/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/TestCmsModuleNameGeneration.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.5 $
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.module;

import junit.framework.TestCase;

/**
 * Tests the module name generation.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $
 */
public class TestCmsModuleNameGeneration extends TestCase {
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsModuleNameGeneration(String arg0) {
        super(arg0);
    }

    /**
     * Tests version increment.<p>
     */
    public void testNameGeneration() {
        
        String name;
        name = CmsModuleXmlHandler.makeValidJavaClassName("org.opencms.welcome");
        assertEquals("org.opencms.welcome", name);
        
        name = CmsModuleXmlHandler.makeValidJavaClassName("com.alkacon.documentation.howto-httpd-modproxy");
        assertEquals("com.alkacon.documentation.howto_httpd_modproxy", name);

        name = CmsModuleXmlHandler.makeValidJavaClassName("com.alkacon.documentation.examples-flex-1");
        assertEquals("com.alkacon.documentation.examples_flex_1", name);
    }        
}
