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

package org.opencms.widgets;

import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test cases for the parsing of select widget options.<p>
 *
 */
public class TestSelectWidgetOption extends OpenCmsTestCase {

    /**
     * Tests parsing of select widget options.<p>
     *
     * @throws Exception if the test fails
     */
    public void testOptionParser() throws Exception {

        List<CmsSelectWidgetOption> res = CmsSelectWidgetOption.parseOptions(null);
        assertSame(Collections.EMPTY_LIST, res);

        res = CmsSelectWidgetOption.parseOptions("");
        assertSame(Collections.EMPTY_LIST, res);

        res = CmsSelectWidgetOption.parseOptions("        ");
        assertSame(Collections.EMPTY_LIST, res);

        res = CmsSelectWidgetOption.parseOptions("one");
        assertNotNull(res);
        assertEquals(1, res.size());

        CmsSelectWidgetOption opt = res.get(0);
        assertFalse(opt.isDefault());
        assertEquals("one", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());

        // some checks with malformed option values - these are silently ignored
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("default='true'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("option='some'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("help='many'"));
        assertEquals(Collections.EMPTY_LIST, CmsSelectWidgetOption.parseOptions("option='some' default='true'"));
        assertEquals(
            Collections.EMPTY_LIST,
            CmsSelectWidgetOption.parseOptions("help='many' option='some' default='true'"));

        // check the examples frm the JavaDoc to make sure they really work as advertised
        assertEquals(
            CmsSelectWidgetOption.parseOptions("value='some value' default='true'"),
            CmsSelectWidgetOption.parseOptions("some value default='true'"));
        assertEquals(
            CmsSelectWidgetOption.parseOptions("value='some value' default='true'"),
            CmsSelectWidgetOption.parseOptions("some value*"));
        assertEquals(
            CmsSelectWidgetOption.parseOptions("value='some value' option='some option'"),
            CmsSelectWidgetOption.parseOptions("some value:some option"));
        assertEquals(
            CmsSelectWidgetOption.parseOptions("value='some value' default='true' option='some option'"),
            CmsSelectWidgetOption.parseOptions("some value*:some option"));

        assertEquals(
            CmsSelectWidgetOption.parseOptions(""),
            CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(null)));
        assertEquals(
            CmsSelectWidgetOption.parseOptions(null),
            CmsSelectWidgetOption.parseOptions(
                CmsSelectWidgetOption.createConfigurationString(Collections.EMPTY_LIST)));

        // check a first list with "full" syntax
        List<CmsSelectWidgetOption> result1 = CmsSelectWidgetOption.parseOptions(
            "value='1' default='true'|value='2'|value='3'|value='4'|value='5'|value='6'|value='7'|value='8'|value='9'|value='10'");
        assertNotNull(result1);
        assertEquals(10, result1.size());

        opt = result1.get(0);
        assertTrue(opt.isDefault());
        assertEquals("1", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());

        opt = result1.get(4);
        assertFalse(opt.isDefault());
        assertEquals("5", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());

        opt = result1.get(9);
        assertFalse(opt.isDefault());
        assertEquals("10", opt.getValue());
        assertNull(opt.getHelp());
        assertSame(opt.getValue(), opt.getOption());

        // check "round trip" creation if a String from the parsed options
        assertEquals(
            result1,
            CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(result1)));

        // check a second list with "shortcut" syntax
        List<CmsSelectWidgetOption> result2 = CmsSelectWidgetOption.parseOptions("1 default='true'|2|3|4|5|6|7|8|9|10");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // check a third list with "legacy" syntax
        result2 = CmsSelectWidgetOption.parseOptions("1*|2|3|4|5|6|7|8|9|10");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // now a different input list
        result1 = CmsSelectWidgetOption.parseOptions(
            "value='accessible' default='true' option='${key.layout.version.accessible}'|value='common' option='${key.layout.version.classic}'");
        assertNotNull(result1);
        assertEquals(2, result1.size());

        opt = result1.get(0);
        assertTrue(opt.isDefault());
        assertEquals("accessible", opt.getValue());
        assertEquals("${key.layout.version.accessible}", opt.getOption());
        assertNull(opt.getHelp());

        opt = result1.get(1);
        assertFalse(opt.isDefault());
        assertEquals("common", opt.getValue());
        assertEquals("${key.layout.version.classic}", opt.getOption());
        assertNull(opt.getHelp());

        // check "round trip" creation if a String from the parsed options
        assertEquals(
            result1,
            CmsSelectWidgetOption.parseOptions(CmsSelectWidgetOption.createConfigurationString(result1)));

        // variation of the element order
        result2 = CmsSelectWidgetOption.parseOptions(
            "default='true' value='accessible' option='${key.layout.version.accessible}'|option='${key.layout.version.classic}' value='common'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // shortcut syntax
        result2 = CmsSelectWidgetOption.parseOptions(
            "accessible default='true' option='${key.layout.version.accessible}'|common option='${key.layout.version.classic}'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // shortcut syntax 2
        result2 = CmsSelectWidgetOption.parseOptions(
            "accessible* option='${key.layout.version.accessible}'|common option='${key.layout.version.classic}'");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // legacy syntax
        result2 = CmsSelectWidgetOption.parseOptions(
            "accessible*:${key.layout.version.accessible}|common:${key.layout.version.classic}");
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());

        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i), result2.get(i));
        }

        // check for realistic example that currently can't be expressed in short syntax
        String options3 = "value='beginn_de_dt: [* TO NOW]' option='vergangen'|value='beginn_de_dt: [NOW TO *]' option='zukünftig'|value='beginn_de_dt: NOW/YEAR' option='dieses Jahr'";
        List<CmsSelectWidgetOption> result3 = CmsSelectWidgetOption.parseOptions(options3);
        List<CmsSelectWidgetOption> expected3 = new ArrayList<>(3);
        expected3.add(new CmsSelectWidgetOption("beginn_de_dt: [* TO NOW]", false, "vergangen"));
        expected3.add(new CmsSelectWidgetOption("beginn_de_dt: [NOW TO *]", false, "zukünftig"));
        expected3.add(new CmsSelectWidgetOption("beginn_de_dt: NOW/YEAR", false, "dieses Jahr"));
        assertEquals(expected3, result3);

        // check for example with delimiters in the values
        String options4 = "a\\|b|c\\||\\|d";
        List<CmsSelectWidgetOption> result4 = CmsSelectWidgetOption.parseOptions(options4);
        List<CmsSelectWidgetOption> expected4 = new ArrayList<>(3);
        expected4.add(new CmsSelectWidgetOption("a|b"));
        expected4.add(new CmsSelectWidgetOption("c|"));
        expected4.add(new CmsSelectWidgetOption("|d"));
        assertEquals(expected4, result4);

        // check for options/values starting with spaces
        String options5 = "value=' a ' option=' b ' help=' '";
        List<CmsSelectWidgetOption> result5 = CmsSelectWidgetOption.parseOptions(options5);
        assertEquals(1, result5.size());
        CmsSelectWidgetOption option = result5.get(0);
        assertEquals(" a ", option.getValue());
        assertEquals(" b ", option.getOption());
        assertEquals(" ", option.getHelp());

    }

    /**
     * Tests if options are split correctly.
     */
    public void testOptionSplitter() {

        String testString = "a|b|c";
        String[] expectedResult = new String[] {"a", "b", "c"};
        String[] splitResult = CmsSelectWidgetOption.splitOptions(testString);
        assertEquals(3, splitResult.length);
        assertEquals(Arrays.asList(expectedResult), Arrays.asList(splitResult));

        testString = "a\\|b|c\\||\\|d";
        expectedResult = new String[] {"a\\|b", "c\\|", "\\|d"};
        splitResult = CmsSelectWidgetOption.splitOptions(testString);
        assertEquals(3, splitResult.length);
        assertEquals(Arrays.asList(expectedResult), Arrays.asList(splitResult));

    }

    /**
     * Incomplete test for the creation of the options string from a list of options, just checking if escapings are added as necessary.
     */
    public void testOptionStringCreation() {

        String expected = "value='a\\|b'|value='c\\|'|value='\\|d'";
        List<CmsSelectWidgetOption> options = new ArrayList<>(3);
        options.add(new CmsSelectWidgetOption("a|b"));
        options.add(new CmsSelectWidgetOption("c|"));
        options.add(new CmsSelectWidgetOption("|d"));
        assertEquals(expected, CmsSelectWidgetOption.createConfigurationString(options));
    }
}