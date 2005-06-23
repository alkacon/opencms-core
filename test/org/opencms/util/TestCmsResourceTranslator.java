/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsResourceTranslator.java,v $
 * Date   : $Date: 2005/06/23 11:11:58 $
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
 
package org.opencms.util;

import junit.framework.TestCase;

/** 
 * @author Alexander Kandzior 
 * @version $Revision: 1.7 $
 * 
 * @since 5.0
 */
public class TestCmsResourceTranslator extends TestCase {

    // default rules (same as in "opencms.properties")
    private static String[] rules = {    
        "s#/default/vfs/content/bodys/(.*)#/default/vfs/system/bodies/$1#", 
        "s#/default/vfs/pics/system/(.*)#/default/vfs/system/workplace/resources/$1#", 
        "s#/default/vfs/pics/(.*)#/default/vfs/system/galleries/pics/$1#",
        "s#/default/vfs/download/(.*)#/default/vfs/system/galleries/download/$1#", 
        "s#/default/vfs/externallinks/(.*)#/default/vfs/system/galleries/externallinks/$1#", 
        "s#/default/vfs/htmlgalleries/(.*)#/default/vfs/system/galleries/htmlgalleries/$1#", 
        "s#/default/vfs/content/(.*)#/default/vfs/system/modules/default/$1#",
        "s#/default/vfs/moduledemos/(.*)#/default/vfs/system/moduledemos/$1#", 
        "s#/default/vfs/system/workplace/config/language/(.*)#/default/vfs/system/workplace/locales/$1#", 
        "s#/default/vfs/system/workplace/css/(.*)#/default/vfs/system/workplace/resources/$1#",
        "s#/default/vfs/system/workplace/templates/js/(.*)#/default/vfs/system/workplace/scripts/$1#",
        "s#[\\s]+#_#g",
        "s#[ä]#ae#g",
        "s#[Ä]#Ae#g",
        "s#[ö]#oe#g",
        "s#[Ö]#Oe#g",
        "s#[ü]#ue#g",
        "s#[Ü]#Ue#g",
        "s#[ß]#ss#g",
        "s#[^0-9a-zA-Z_\\.\\-\\/]#!#g",       
        "s#!+#x#g"        
    };
     
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsResourceTranslator(String arg0) {
        super(arg0);
    }

    /**
     * Tests for the resource name translation.<p>
     */
    public void testTranslateResource() {
        
        CmsResourceTranslator translator = new CmsResourceTranslator(rules, false);
        String test;
       
        test = translator.translateResource("/default/vfs/content/bodys/test/index.html");
        assertEquals(test, "/default/vfs/system/bodies/test/index.html");
        
        test = translator.translateResource("/default/vfs/system/workplace/templates/js/test.js");
        assertEquals(test, "/default/vfs/system/workplace/scripts/test.js"); 
        
        translator = new CmsResourceTranslator(rules, true);
        test = translator.translateResource("Schöne Übung mit Fuß.js");
        assertEquals(test, "Schoene_Uebung_mit_Fuss.js");
    }

}
