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

import org.opencms.test.OpenCmsTestCase;

import org.htmlparser.util.ParserException;

/**
 * Tests the HTML validator.<p>
 */
public class TestCmsHtmlValidator extends OpenCmsTestCase {

    /**
     * Test HTML validation.<p>
     *
     * @throws ParserException in case parsing fails
     */
    public void testValidation() throws ParserException {

        CmsHtmlValidator validator = new CmsHtmlValidator();
        validator.validate("<div></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><p></p></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><img src='' /></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><br></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><div /></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><header><br></header><img ></div>");
        assertTrue(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><p ></div>");
        assertFalse(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());

        validator.validate("<div><header><br></header><img ></div><div />");
        assertTrue(validator.isBalanced());
        assertEquals(2, validator.getRootElementCount());

        validator.validate(
            "<div><header><br></header><img ></div><div><header><br></header><img ></div><div><header><br></header><img ></div>");
        assertTrue(validator.isBalanced());
        assertEquals(3, validator.getRootElementCount());

        validator.validate("<!-- --><div><p ></div>");
        assertFalse(validator.isBalanced());
        assertEquals(1, validator.getRootElementCount());
    }

}
