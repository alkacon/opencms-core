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

package org.opencms.i18n;

import org.opencms.test.OpenCmsTestCase;

import java.util.Locale;

/**
 * Tests for the CmsMessages.<p>
 *
 * @since 6.0.0
 */
public class TestCmsMessages extends OpenCmsTestCase {

    /**
     * Tests if message will be returned in the correct locale.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLocale() throws Exception {

        CmsMessages messages = new CmsMessages("org.opencms.i18n.messages", Locale.GERMANY);
        String value = messages.key("LOG_LOCALE_MANAGER_FLUSH_CACHE_1", new Object[] {"TestParam"});
        assertEquals("Locale manager leerte die Caches nachdem Event TestParam empfangen wurde.", value);
    }

    /**
     * Tests parameter replacement in messages.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMessageWithParameters() throws Exception {

        String value;

        CmsMessages messages = new CmsMessages(org.opencms.xml.content.Messages.get().getBundleName(), Locale.ENGLISH);

        value = messages.key("GUI_EDITOR_XMLCONTENT_VALIDATION_WARNING_2");
        assertEquals("Bad value \"{0}\" according to rule {1}", value);

        value = messages.key("GUI_EDITOR_XMLCONTENT_VALIDATION_WARNING_2", new Object[] {"some value", "the rule"});
        assertEquals("Bad value \"some value\" according to rule the rule", value);
    }

    /**
     * Tests for for missing localized keys.<p>
     *
     * @throws Exception if the test fails
     */
    public void testUnknownKeys() throws Exception {

        String value = null;

        // check for null value
        assertTrue(CmsMessages.isUnknownKey(value));

        // test key formatted as unknown
        value = CmsMessages.formatUnknownKey("somekey");
        assertTrue(CmsMessages.isUnknownKey(value));

        // check a value certainly NOT unknown
        value = "Title";
        assertFalse(CmsMessages.isUnknownKey(value));

        // the empty String is also NOT an unknown key
        value = "";
        assertFalse(CmsMessages.isUnknownKey(value));

        CmsMessages messages = new CmsMessages(org.opencms.workplace.Messages.get().getBundleName(), Locale.ENGLISH);
        value = messages.key("GUI_LOGIN_BUTTON_0");
        assertFalse(CmsMessages.isUnknownKey(value));
        assertEquals("Login", value);

        String defaultValue = "This value does not exist!";
        value = messages.keyDefault("idontexist", defaultValue);
        assertFalse(CmsMessages.isUnknownKey(defaultValue));
        assertEquals(defaultValue, value);
    }
}