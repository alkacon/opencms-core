/*
 * File   : $Source: /alkacon/cvs/opencms/test/com/opencms/flex/util/Attic/CmsUUIDTest.java,v $
 * Date   : $Date: 2003/03/19 19:40:05 $
 * Version: $Revision: 1.1 $
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
 
package com.opencms.flex.util;

import com.opencms.core.CmsException;

import junit.framework.TestCase;

/** 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.0
 */
public class CmsUUIDTest extends TestCase {

    // DEBUG flag
    private static final boolean DEBUG = true;
     
    /**
     * Constructor for CmsResourceTranslatorTest.
     * @param arg0
     */
    public CmsUUIDTest(String arg0) {
        super(arg0);
    }

    public void testUUID() {
        try {
            CmsUUID.init(CmsUUID.getDummyEthernetAddress());
        } catch (CmsException e) {
            // will not happen as the dummy address is always valid
        }        
        CmsUUID id1 = new CmsUUID();
        CmsUUID id2 = new CmsUUID();
        CmsUUID id3 = new CmsUUID();    
        if (DEBUG) {
            System.out.println("UUID 1: " + id1);
            System.out.println("UUID 2: " + id2);
            System.out.println("UUID 3: " + id3);
        }
        assertNotSame(id1, id2);
        assertNotSame(id1, id3);
        assertNotSame(id3, id2);        
    }
}
