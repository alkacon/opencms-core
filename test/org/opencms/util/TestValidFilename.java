/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestValidFilename.java,v $
 * Date   : $Date: 2005/06/23 14:27:27 $
 * Version: $Revision: 1.6 $
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
 * Test cases for file name validation.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 6.0.0
 */
public class TestValidFilename extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestValidFilename(String arg0) {

        super(arg0);
    }

    /**
     * Tests the file name validation method in the class CmsDriverManager.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testValidateResourceName() throws Exception {

        // PLEASE NOTE: This logic is NOT yet used by OpenCms, this is planned for the future

        // according to windows, the following characters are illegal:
        // \ / : * ? " < > |

        // accoring to JSR 170, the following characters are illegal:
        // / : [ ] * ' " |

        // technically, for OpenCms only the following char can not be part of a file: /

        // for HTML URL building, the following chars are "reserved characters" (see RFC 1738)
        // ; / ? : @ = &

        // stupidity tests
        assertFalse(CmsStringUtil.validateResourceName(null));
        assertFalse(CmsStringUtil.validateResourceName(""));

        // all valid chard according to the "old" OpenCms logic
        assertTrue(CmsStringUtil.validateResourceName("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~$"));

        // add some of the new valid chars
        assertTrue(CmsStringUtil.validateResourceName("Copy of abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~$"));
        assertTrue(CmsStringUtil.validateResourceName("Some German umlauts - הצüÄÖÜ‗"));
        assertTrue(CmsStringUtil.validateResourceName("Some more western European special chars - יטפבאûםל"));

        assertTrue(CmsStringUtil.validateResourceName("my File"));
        // Window logic invalid chars
        assertFalse(CmsStringUtil.validateResourceName(" my File"));
        assertFalse(CmsStringUtil.validateResourceName("my File "));
        assertFalse(CmsStringUtil.validateResourceName("\tmy File"));
        assertFalse(CmsStringUtil.validateResourceName("\rmy File"));
        assertFalse(CmsStringUtil.validateResourceName("\nmy File"));
        assertFalse(CmsStringUtil.validateResourceName("my/file"));
        assertFalse(CmsStringUtil.validateResourceName("my\\file"));
        assertFalse(CmsStringUtil.validateResourceName("my:file"));
        assertFalse(CmsStringUtil.validateResourceName("my*file"));
        assertFalse(CmsStringUtil.validateResourceName("my?file"));
        assertFalse(CmsStringUtil.validateResourceName("my\"file"));
        assertFalse(CmsStringUtil.validateResourceName("my<file"));
        assertFalse(CmsStringUtil.validateResourceName("my>file"));
        assertFalse(CmsStringUtil.validateResourceName("my|file"));

        // JSR 170 chars
        assertTrue(CmsStringUtil.validateResourceName("my[file"));
        assertTrue(CmsStringUtil.validateResourceName("my]file"));
        assertTrue(CmsStringUtil.validateResourceName("my'file"));

        // HTML reserved chars 
        assertTrue(CmsStringUtil.validateResourceName("my&file"));
        assertTrue(CmsStringUtil.validateResourceName("my=file"));
        assertTrue(CmsStringUtil.validateResourceName("my@file"));
    }
}
