/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsExportFolderMatcher.java,v $
 * Date   : $Date: 2004/07/08 13:52:47 $
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
 
package org.opencms.util;

import org.opencms.staticexport.CmsExportFolderMatcher;

import java.util.ArrayList;

import junit.framework.TestCase;

/** 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.0
 */
public class TestCmsExportFolderMatcher extends TestCase {
   
    private static String checkRes ="/system/opencms.ini";
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsExportFolderMatcher(String arg0) {
        super(arg0);
    }

    /**
     * Tests for the resource name translation.<p>
     */
    public void testTranslateResource() {
        
        /** default folders. */
        ArrayList folders = new ArrayList(); 
        
        folders.add("\\/sites\\/.*");
        folders.add("\\/system\\/galleries\\/.*");
        folders.add("\\/system\\/modules\\/.*\\/resources\\/.*");
        
        CmsExportFolderMatcher matcher = new CmsExportFolderMatcher(folders, checkRes); 
        
        boolean test;
        test = matcher.match("/system/opencms.ini");
        assertEquals(test, true);
        
        test = matcher.match("/sites/default/index.html");
        assertEquals(test, true);
      
        test = matcher.match("/sites/default/folder/index.html");
        assertEquals(test, true);
    
        test = matcher.match("/gibtsnicht/index.html");
        assertEquals(test, false);
        
        test = matcher.match("/system/galleries/pics/demo.gif");
        assertEquals(test, true);
        
        test = matcher.match("/system/modules/org.opencms.welcome/resources/test.gif");
        assertEquals(test, true);
        
        test = matcher.match("/system/modules/org.opencms.welcome/templates/test.jsp");
        assertEquals(test, false);
        
 
    }

}
