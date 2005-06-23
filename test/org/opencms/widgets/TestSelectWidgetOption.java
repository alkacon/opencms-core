/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/widgets/TestSelectWidgetOption.java,v $
 * Date   : $Date: 2005/06/23 11:12:02 $
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
 
package org.opencms.widgets;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/** 
 * Test cases for the parsing of select widget options.<p>
 * 
 * @author Alexander Kandzior
 * @version $Revision: 1.6 $
 */
public class TestSelectWidgetOption extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestSelectWidgetOption(String arg0) {
        
        super(arg0);
    }
    
    /**
     * Tests parsing of select widget options.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testOptionParser() throws Exception {

        List res = CmsSelectWidgetOption.parseOptions(null);
        assertSame(Collections.EMPTY_LIST, res);

        res = CmsSelectWidgetOption.parseOptions("");
        assertSame(Collections.EMPTY_LIST, res);

        res = CmsSelectWidgetOption.parseOptions("        ");
        assertSame(Collections.EMPTY_LIST, res);
        
        res = CmsSelectWidgetOption.parseOptions("one");
        assertNotNull(res);
        assertEquals(1, res.size());
        
        CmsSelectWidgetOption opt = (CmsSelectWidgetOption)res.get(0);
        assertFalse(opt.isDefault());
        assertEquals("one", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());

        // some checks where no valid value is present - these are silently ignored
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("default='true'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("option='some'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("help='many'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("option='some' default='true'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("help='many' option='some' default='true'"));
                
        // check the examples frm the JavaDoc to make sure they really work as advertised 
        assertEquals(CmsSelectWidgetOption.parseOptions("value='some value' default='true'"), CmsSelectWidgetOption.parseOptions("some value default='true'"));       
        assertEquals(CmsSelectWidgetOption.parseOptions("value='some value' default='true'"), CmsSelectWidgetOption.parseOptions("some value*"));
        assertEquals(CmsSelectWidgetOption.parseOptions("value='some value' option='some option'"), CmsSelectWidgetOption.parseOptions("some value:some option"));
        assertEquals(CmsSelectWidgetOption.parseOptions("value='some value' default='true' option='some option'"), CmsSelectWidgetOption.parseOptions("some value*:some option"));                        

        assertEquals(CmsSelectWidgetOption.parseOptions(""), CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(null)));                        
        assertEquals(CmsSelectWidgetOption.parseOptions(null), CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(Collections.EMPTY_LIST)));                        
        
        // check a first list with "full" syntax
        List result1 = CmsSelectWidgetOption.parseOptions("value='1' default='true'|value='2'|value='3'|value='4'|value='5'|value='6'|value='7'|value='8'|value='9'|value='10'");
        assertNotNull(result1);
        assertEquals(10, result1.size());

        opt = (CmsSelectWidgetOption)result1.get(0);
        assertTrue(opt.isDefault());
        assertEquals("1", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());
        
        opt = (CmsSelectWidgetOption)result1.get(4);
        assertFalse(opt.isDefault());
        assertEquals("5", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());
        
        opt = (CmsSelectWidgetOption)result1.get(9);
        assertFalse(opt.isDefault());
        assertEquals("10", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());
        
        // check "round trip" creation if a String from the parsed options
        assertEquals(result1, CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(result1)));        
        
        // check a second list with "shortcut" syntax
        List result2 = CmsSelectWidgetOption.parseOptions("1 default='true'|2|3|4|5|6|7|8|9|10");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // check a third list with "legacy" syntax
        result2 = CmsSelectWidgetOption.parseOptions("1*|2|3|4|5|6|7|8|9|10");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }
        
        // now a different input list
        result1 = CmsSelectWidgetOption.parseOptions("value='accessible' default='true' option='${key.layout.version.accessible}'|value='common' option='${key.layout.version.classic}'");
        assertNotNull(result1);
        assertEquals(2, result1.size());
        
        opt = (CmsSelectWidgetOption)result1.get(0);
        assertTrue(opt.isDefault());
        assertEquals("accessible", opt.getValue());
        assertEquals("${key.layout.version.accessible}", opt.getOption());
        assertNull(opt.getHelp());
        
        opt = (CmsSelectWidgetOption)result1.get(1);
        assertFalse(opt.isDefault());
        assertEquals("common", opt.getValue());
        assertEquals("${key.layout.version.classic}", opt.getOption());
        assertNull(opt.getHelp());
        
        // check "round trip" creation if a String from the parsed options
        assertEquals(result1, CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(result1)));        
        
        // variation of the element order
        result2 = CmsSelectWidgetOption.parseOptions("default='true' value='accessible' option='${key.layout.version.accessible}'|option='${key.layout.version.classic}' value='common'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());     
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }
        
        // shortcut syntax
        result2 = CmsSelectWidgetOption.parseOptions("accessible default='true' option='${key.layout.version.accessible}'|common option='${key.layout.version.classic}'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());     
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }
        
        // shortcut syntax 2
        result2 = CmsSelectWidgetOption.parseOptions("accessible* option='${key.layout.version.accessible}'|common option='${key.layout.version.classic}'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());     
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }
        
        // legacy syntax
        result2 = CmsSelectWidgetOption.parseOptions("accessible*:${key.layout.version.accessible}|common:${key.layout.version.classic}");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());     
        
        for (int i=0; i<result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }
    }
    
    public void testCastFloatToInt() {
        
        float f = -0.99f;        
        assertTrue(0 == (int)f);
    }
}