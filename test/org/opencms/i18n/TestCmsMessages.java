/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/TestCmsMessages.java,v $
 * Date   : $Date: 2005/06/23 11:11:43 $
 * Version: $Revision: 1.5 $
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
package org.opencms.i18n;

import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for the CmsMessages.<p>
 * 
 * @author Alexander Kandzior 
 * @since 5.3
 */
public class TestCmsMessages extends TestCase {

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

        CmsMessages messages = new CmsMessages(CmsWorkplaceMessages.DEFAULT_WORKPLACE_MESSAGE_BUNDLE, Locale.ENGLISH);
        value = messages.key("name");
        assertFalse(CmsMessages.isUnknownKey(value));
        assertEquals("English", value);

        String defaultValue = "This value does not exist!";
        value = messages.key("idontexist", defaultValue);
        assertFalse(CmsMessages.isUnknownKey(defaultValue));
        assertEquals(defaultValue, value);
    }
    
    /**
     * Tests parameter replacement in messages.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testMessageWithParameters() throws Exception {
        
        String value;
        
        CmsMessages messages = new CmsMessages(CmsWorkplaceMessages.DEFAULT_WORKPLACE_MESSAGE_BUNDLE, Locale.ENGLISH);
        
        value = messages.key("editor.xmlcontent.validation.error");
        assertEquals("Invalid value \"{0}\" according to rule {1}", value);
        
        value = messages.key("editor.xmlcontent.validation.error", new Object[]{"'value'", "'rule'"});
        assertEquals("Invalid value \"'value'\" according to rule 'rule'", value);
        
        value = messages.key("editor.xmlcontent.validation.warning");
        assertEquals("Bad value \"{0}\" according to rule {1}", value);
        
        value = messages.key("editor.xmlcontent.validation.warning", new Object[]{"some value", "the rule"});
        assertEquals("Bad value \"some value\" according to rule the rule", value);
    }    
}
