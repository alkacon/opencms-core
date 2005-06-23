/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsUUID.java,v $
 * Date   : $Date: 2005/06/23 14:27:27 $
 * Version: $Revision: 1.8 $
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

import java.util.Random;

import org.doomdark.uuid.UUID;

import junit.framework.TestCase;

/** 
 * @param Please leave a comment here.
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 6.0.0
 */
public class TestCmsUUID extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsUUID(String arg0) {

        super(arg0);
    }

    /**
     * Tests UUID generation.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testUUID() throws Exception {

        CmsUUID.init(CmsUUID.getDummyEthernetAddress());
        CmsUUID id1 = new CmsUUID();
        CmsUUID id2 = new CmsUUID();
        CmsUUID id3 = new CmsUUID();

        assertNotSame(id1, id2);
        assertFalse(id1.equals(id2));
        assertNotSame(id1, id3);
        assertFalse(id1.equals(id3));
        assertNotSame(id3, id2);
        assertFalse(id3.equals(id2));

        CmsUUID id4 = CmsUUID.getNullUUID();
        assertTrue(id4.isNullUUID());
        assertTrue(id4.equals(CmsUUID.getNullUUID()));
    }

    /**
     * Tests UUID equals() method.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testUUIDEquals() throws Exception {

        CmsUUID.init(CmsUUID.getDummyEthernetAddress());
        CmsUUID id1 = new CmsUUID("c300ba5c-01e8-3727-b305-5dcc9ccae1ee");
        CmsUUID id2 = new CmsUUID("c300ba5c-01e8-3727-b305-5dcc9ccae1ee");

        assertNotSame(id1, id2);
        assertEquals(id1, id2);
        CmsUUID id3 = new CmsUUID();
        assertFalse(id1.equals(id3));
        assertFalse(id2.equals(id3));

        UUID uid1 = new UUID("c300ba5c-01e8-3727-b305-5dcc9ccae1ee");
        UUID uid2 = new UUID("c300ba5c-01e8-3727-b305-5dcc9ccae1ee");
        assertNotSame(uid1, uid2);
        assertEquals(uid1, uid2);

        // check behaviour of equals method in JUG UUID class
        UUID uid = new UUID("c300ba5c-01e8-3727-b305-5dcc9ccae1ee");
        byte[] b1 = uid.asByteArray();
        byte[] b2 = uid.toByteArray();
        assertNotSame(b1, b2);
        byte[] b3 = uid.asByteArray();
        assertNotSame(b1, b3);
        byte[] b4 = uid.toByteArray();
        assertNotSame(b2, b4);

        CmsUUID idNull = new CmsUUID("00000000-0000-0000-0000-000000000000");
        assertTrue(idNull.isNullUUID());
        CmsUUID id4 = CmsUUID.getNullUUID();
        assertEquals(idNull, id4);

        int testSize = 100000;
        CmsUUID[] ids = new CmsUUID[testSize];
        for (int i = 0; i < testSize; i++) {
            ids[i] = new CmsUUID();
        }

        int hits1 = 0;
        Random r = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < testSize; i++) {
            int pos1 = r.nextInt(testSize);
            int pos2 = r.nextInt(testSize);
            if (ids[pos1].equals(ids[pos2])) {
                hits1++;
            }
        }
        long time1 = System.currentTimeMillis() - start;
        System.out.println("Time for UUID equals() implementation: " + time1);

        //        
        //        int hits2 = 0;
        //        start = System.currentTimeMillis();
        //        for (int i=0; i<testSize; i++) {            
        //            int pos1 = r.nextInt(testSize);
        //            int pos2 = r.nextInt(testSize);
        //            if (ids[pos1].equals2(ids[pos2])) {
        //                hits2++;
        //            }
        //        }
        //        long time2 = System.currentTimeMillis() - start;
        //        System.out.println("Time 2 for equals(): " + time2);
    }
}