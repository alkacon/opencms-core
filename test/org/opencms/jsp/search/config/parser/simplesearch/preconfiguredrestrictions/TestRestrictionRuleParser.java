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

package org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions;

import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean.CombinationMode;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionRule.MatchType;
import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the restriction rule parser. */
public class TestRestrictionRuleParser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestRestrictionRuleParser(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.addTest(new TestRestrictionRuleParser("testComplexRules"));
        suite.addTest(new TestRestrictionRuleParser("testLocalePlaceholder"));
        suite.addTest(new TestRestrictionRuleParser("testSimpleRule"));

        return suite;
    }

    /**
     * Tests complex rule
     * @throws CmsException thrown when rule parsing fails
     */
    @org.junit.Test
    public void testComplexRules() throws CmsException {

        // Only field
        CmsRestrictionRule rule = CmsRestrictionRuleParser.parseRule("field=test");
        assertEquals(CombinationMode.OR, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.OR, rule.getCombinationModeInField());
        assertNull(rule.getType());
        assertEquals(MatchType.DEFAULT, rule.getMatchType());
        assertEquals("test", rule.getRawField());

        // all possible combinations
        rule = CmsRestrictionRuleParser.parseRule("field=test,type=type,match=exact,combine=AND");
        assertEquals(CombinationMode.AND, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.AND, rule.getCombinationModeInField());
        assertEquals("type", rule.getType());
        assertEquals(MatchType.EXACT, rule.getMatchType());
        assertEquals("test", rule.getRawField());
        assertEquals("test", rule.getFieldForLocale(Locale.ENGLISH));

        // different combination modes
        rule = CmsRestrictionRuleParser.parseRule("field=test,type=type,match=exact,combine=AND-or");
        assertEquals(CombinationMode.AND, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.OR, rule.getCombinationModeInField());
        assertEquals("type", rule.getType());
        assertEquals(MatchType.EXACT, rule.getMatchType());
        assertEquals("test", rule.getRawField());
        assertEquals("test", rule.getFieldForLocale(Locale.ENGLISH));

        // different order
        rule = CmsRestrictionRuleParser.parseRule("type=type,match=PREFIX,field=test,combine=or");
        assertEquals(CombinationMode.OR, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.OR, rule.getCombinationModeInField());
        assertEquals("type", rule.getType());
        assertEquals(MatchType.PREFIX, rule.getMatchType());
        assertEquals("test", rule.getRawField());
        assertEquals("test", rule.getFieldForLocale(Locale.ENGLISH));

        // invalid with defaults as fallback
        rule = CmsRestrictionRuleParser.parseRule("type=type,match=InValiD,combine=UnKnown,field=test");
        assertEquals(CombinationMode.OR, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.OR, rule.getCombinationModeInField());
        assertEquals("type", rule.getType());
        assertEquals(MatchType.DEFAULT, rule.getMatchType());
        assertEquals("test", rule.getRawField());
        assertEquals("test", rule.getFieldForLocale(Locale.ENGLISH));

    }

    /**
     * Tests locale placeholder
     * @throws CmsException thrown when rule parsing fails
     */
    @org.junit.Test
    public void testLocalePlaceholder() throws CmsException {

        CmsRestrictionRule rule = CmsRestrictionRuleParser.parseRule("test_#");
        assertEquals("test_de", rule.getFieldForLocale(Locale.GERMAN));
        assertEquals("test_de_DE", rule.getFieldForLocale(Locale.GERMANY));
        assertEquals("test_en", rule.getFieldForLocale(Locale.ENGLISH));
    }

    /**
     * Tests simple rule
     * @throws CmsException thrown when rule parsing fails
     */
    @org.junit.Test
    public void testSimpleRule() throws CmsException {

        CmsRestrictionRule rule = CmsRestrictionRuleParser.parseRule("test");
        assertEquals(CombinationMode.OR, rule.getCombinationModeBetweenFields());
        assertEquals(CombinationMode.OR, rule.getCombinationModeInField());
        assertNull(rule.getType());
        assertEquals(MatchType.DEFAULT, rule.getMatchType());
        assertEquals("test", rule.getRawField());
        assertEquals("test", rule.getFieldForLocale(Locale.ENGLISH));

    }
}
