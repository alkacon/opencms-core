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

package org.opencms.crypto;

import static org.junit.Assert.assertNotEquals;

import junit.framework.TestCase;

/**
 * Tests for text en/decryption.
 */
public class TestTextEncryption extends TestCase {

    /**
     * This is mostly an integration test to show that the crypto libraries work.
     */
    public void testAES() throws Exception {
        String key1 = "key1";
        String key2 = "key2";
        CmsAESTextEncryption enc1 = new CmsAESTextEncryption(key1);
        CmsAESTextEncryption enc2 = new CmsAESTextEncryption(key2);
        String plaintext = "foo bar baz";
        String encrypted1 = enc1.encrypt(plaintext);
        String encrypted2 = enc2.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted1);
        assertNotEquals(plaintext, encrypted2);
        assertNotEquals(encrypted1, encrypted2);
        assertEquals(plaintext, enc1.decrypt(encrypted1));
        assertEquals(plaintext, enc2.decrypt(encrypted2));
    }

}
