/*
 * File   : $Source: /alkacon/cvs/opencms/test/com/opencms/flex/util/Attic/CmsResourceTranslatorTest.java,v $
 * Date   : $Date: 2003/02/11 16:58:43 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2003  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * First created on 11.02.2003
 */
package com.opencms.flex.util;

import com.opencms.flex.util.CmsResourceTranslator;

import junit.framework.TestCase;

/** 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.0
 */
public class CmsResourceTranslatorTest extends TestCase {

    private CmsResourceTranslator translator;

    // default rules (same as in "opencms.properties")
    private static String rules[] = {    
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
        "s#/default/vfs/system/workplace/templates/js/(.*)#/default/vfs/system/workplace/scripts/$1#" 
    };
     
    /**
     * Constructor for CmsResourceTranslatorTest.
     * @param arg0
     */
    public CmsResourceTranslatorTest(String arg0) {
        super(arg0);
        
        translator = new CmsResourceTranslator(rules, false);
    }

    public void testTranslateResource() {
        String test;
       
        test = translator.translateResource("/default/vfs/content/bodys/test/index.html");
        assertEquals(test, "/default/vfs/system/bodies/test/index.html");
        
        test = translator.translateResource("/default/vfs/system/workplace/templates/js/test.js");
        assertEquals(test, "/default/vfs/system/workplace/scripts/test.js");        
    }

}
