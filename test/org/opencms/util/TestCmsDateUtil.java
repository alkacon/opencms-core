/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsDateUtil.java,v $
 * Date   : $Date: 2005/02/17 12:46:01 $
 * Version: $Revision: 1.2 $
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

package org.opencms.util;

import junit.framework.TestCase;

/** 
 * Test cases for the class "CmsDateUtil".<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.0
 */
public class TestCmsDateUtil extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsDateUtil(String arg0) {
        super(arg0);
    }

    /**
     * Tests HTTP-Header date format generation.<p>
     * 
     * Issue: 
     * Http headers generated with bad formatting according to http spec.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testHttpDateGeneration() throws Exception {

        String dateString = "Mon, 12 Jul 2004 10:00:00 GMT";
        long dateLong = CmsDateUtil.parseHeaderDate(dateString);                
        String result = CmsDateUtil.getHeaderDate(dateLong);        
        assertEquals(dateString, result);
    }
}
