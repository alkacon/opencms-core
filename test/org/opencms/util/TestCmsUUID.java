/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.test.OpenCmsTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.safehaus.uuid.UUID;

/**
 * Test case for the UUID generator.<p>
 *
 * @since 6.0.0
 */
public class TestCmsUUID extends OpenCmsTestCase {

    /** Map to store serialized objects with a name. */
    private Map m_serializedMap = new HashMap();

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
     * Tests serialization of the CmsUUID.<p>
     *
     * @throws Exception if the test fails
     */
    public void testUUIDSerialization() throws Exception {

        CmsUUID.init(CmsUUID.getDummyEthernetAddress());
        CmsUUID id1 = new CmsUUID();
        serializeObject("id1", id1);
        CmsUUID d_id1 = (CmsUUID)deSerializeObject("id1");
        assertEquals(id1, d_id1);

        // serializeObjectToFile("org/opencms/util/uuid_v535.bmp", id1);
    }

    /**
     * Tests de-serialization of CmsUUIDs from various OpenCms versions.<p>
     *
     * @throws Exception if the test fails
     */
    public void testUUIDDeSerialization() throws Exception {

        CmsUUID uuid_v702 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v702.bmp");
        System.out.println("De-Serialized from version 7.0.2: " + uuid_v702.toString());

        CmsUUID uuid_v701 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v701.bmp");
        System.out.println("De-Serialized from version 7.0.1: " + uuid_v701.toString());

        CmsUUID uuid_v623 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v623.bmp");
        System.out.println("De-Serialized from version 6.2.3: " + uuid_v623.toString());

        CmsUUID uuid_v620 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v620.bmp");
        System.out.println("De-Serialized from version 6.2.0: " + uuid_v620.toString());

        // de-serialization of CmsUUIDs before 6.2.0 is impossible because of changes to the UUID implementation

        //        CmsUUID uuid_v605 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v605.bmp");
        //        System.out.println("De-Serialized from version 6.0.5: " + uuid_v605.toString());
        //
        //        CmsUUID uuid_v600 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v600.bmp");
        //        System.out.println("De-Serialized from version 6.0.0: " + uuid_v600.toString());
        //
        //        CmsUUID uuid_v535 = (CmsUUID)deSerializeObjectFromFile("org/opencms/util/uuid_v535.bmp");
        //        System.out.println("De-Serialized from version 5.3.5: " + uuid_v535.toString());
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

        System.out.println("Generating a lot of UUIDs in a loop very fast - warnings may pe printed to console...");
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
        System.out.println("Time for UUID equals() implementation for " + testSize + " UUIDs : " + time1);
    }

    /**
     * Tests the {@link CmsUUID#isValidUUID(String)} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testUUIDisValid() throws Exception {

        assertTrue(CmsUUID.isValidUUID((new CmsUUID()).toString()));
        assertTrue(CmsUUID.isValidUUID(CmsUUID.getNullUUID().toString()));
        assertFalse(CmsUUID.isValidUUID(CmsUUID.getNullUUID().toString() + "0"));
        assertFalse(CmsUUID.isValidUUID("0" + CmsUUID.getNullUUID().toString()));
        assertFalse(CmsUUID.isValidUUID(null));
        assertFalse(CmsUUID.isValidUUID(""));
        assertFalse(CmsUUID.isValidUUID("kaputt"));
    }

    /**
     * De-Serializes an object with the given name from the internal Map.<p>
     *
     * @param name the name to use
     *
     * @return the de-serialized Object
     *
     * @throws Exception if something goes wrong
     */
    protected Object deSerializeObject(String name) throws Exception {

        byte[] bytes = (byte[])m_serializedMap.get(name);
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return oin.readObject();
    }

    /**
     * De-Serializes an object from an external File with the given name.<p>
     *
     * @param name the name to use
     *
     * @return the de-serialized Object
     *
     * @throws Exception if something goes wrong
     */
    protected Object deSerializeObjectFromFile(String name) throws Exception {

        ObjectInputStream oin = new ObjectInputStream(getClass().getClassLoader().getResourceAsStream(name));
        return oin.readObject();
    }

    /**
     * Serializes an object and stores the result into an internal Map using the given name.<p>
     *
     * @param name the name to use
     * @param o the Object to serialize
     *
     * @throws Exception if something goes wrong
     */
    protected void serializeObject(String name, Object o) throws Exception {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(o);
        oout.close();
        m_serializedMap.put(name, bout.toByteArray());
    }

    /**
     * Serializes an object and stores the result into a file with the given name.<p>
     *
     * @param name the name to use
     * @param o the Object to serialize
     *
     * @throws Exception if something goes wrong
     */
    protected void serializeObjectToFile(String name, Object o) throws Exception {

        File outputFile = new File(new URI(getClass().getClassLoader().getResource(name).toString()));
        // System.out.println(outputFile.getAbsolutePath());
        FileOutputStream fout = new FileOutputStream(outputFile);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(o);
        oout.close();
    }
}