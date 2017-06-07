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

import org.opencms.file.CmsResource;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.test.OpenCmsTestCase;

/**
 * Test cases for file name validation.<p>
 *
 * @since 6.0.0
 */
public class TestValidFilename extends OpenCmsTestCase {

    /**
     * Tests the file name validation method in the class CmsDriverManager.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCheckNameForResource() throws Exception {

        // according to windows, the following characters are illegal:
        // \ / : * ? " < > |

        // accoring to JSR 170, the following characters are illegal:
        // / : [ ] * ' " |

        // technically, for OpenCms only the following char can not be part of a file: /

        // for HTML URL building, the following chars are "reserved characters" (see RFC 1738)
        // ; / ? : @ = &

        // stupidity tests
        assertFalse(checkName(null));
        assertFalse(checkName(""));

        // all valid chars according to the OpenCms logic
        assertTrue(checkName("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~$"));

        // add some of the new valid chars
        assertFalse(checkName("Copy of abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~$"));
        assertFalse(checkName("Some German umlauts - \u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF"));
        assertFalse(
            checkName("Some more western European special chars - \u00E9\u00E8\u00F4\u00E1\u00E0\u00FB\u00ED\u00EC"));

        assertFalse(checkName("my File"));
        // Window logic invalid chars
        assertFalse(checkName(" my File"));
        assertFalse(checkName("my File "));
        assertFalse(checkName("\tmy File"));
        assertFalse(checkName("\rmy File"));
        assertFalse(checkName("\nmy File"));
        assertFalse(checkName("my/file"));
        assertFalse(checkName("my\\file"));
        assertFalse(checkName("my:file"));
        assertFalse(checkName("my*file"));
        assertFalse(checkName("my?file"));
        assertFalse(checkName("my\"file"));
        assertFalse(checkName("my<file"));
        assertFalse(checkName("my>file"));
        assertFalse(checkName("my|file"));

        // JSR 170 chars
        assertFalse(checkName("my[file"));
        assertFalse(checkName("my]file"));
        assertFalse(checkName("my'file"));

        // HTML reserved chars
        assertFalse(checkName("my&file"));
        assertFalse(checkName("my=file"));
        assertFalse(checkName("my@file"));
    }

    private boolean checkName(String name) {

        try {
            CmsResource.checkResourceName(name);
            return true;
        } catch (CmsIllegalArgumentException e) {
            return false;
        }
    }
}
