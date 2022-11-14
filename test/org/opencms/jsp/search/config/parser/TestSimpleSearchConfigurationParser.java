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

package org.opencms.jsp.search.config.parser;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.solr.client.solrj.util.ClientUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the simple configuration parser used by cms:simplesearch.
 *
 * TODO: We have very low test coverage - improve.
 */
public class TestSimpleSearchConfigurationParser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSimpleSearchConfigurationParser(String arg0) {

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
        suite.addTest(new TestSimpleSearchConfigurationParser("testTypeRestrictionWithoutPreconfiguredRestrictions"));
        suite.addTest(
            new TestSimpleSearchConfigurationParser("testTypeRestrictionWithPreconfiguredRestrictionWithoutType"));
        suite.addTest(
            new TestSimpleSearchConfigurationParser("testTypeRestrictionWithPreconfiguredRestrictionWithType"));
        suite.addTest(
            new TestSimpleSearchConfigurationParser("testTypeRestrictionWithComplexPreconfiguredRestriction"));
        suite.addTest(
            new TestSimpleSearchConfigurationParser("testTypeRestrictionWithComplexExactPreconfiguredRestriction"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testMultipleRestrictionsWithType"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testRuleForUnknownType"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testInfixRule"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testPrefixRule"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testPostfixRule"));
        suite.addTest(new TestSimpleSearchConfigurationParser("testExactRule"));
        return suite;
    }

    /**
     * Test if excact is correctly added.
     */
    @org.junit.Test
    public void testExactRule() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=event,match=exact", Collections.singleton("v1 value"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode(
                "((type:\"event\" AND (field:(\"" + ClientUtils.escapeQueryChars("v1 value") + "\"))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test if infix is correctly added.
     */
    @org.junit.Test
    public void testInfixRule() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=event,match=infix", Collections.singleton("v1"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode("((type:\"event\" AND (field:((v1 OR *v1 OR *v1* OR v1*)))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testMultipleRestrictionsWithType() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        List<String> values = new ArrayList<>(2);
        values.add("v1");
        values.add("v2");
        preconfigurations.addRestriction("field=field,type=event", values);
        preconfigurations.addRestriction("field=field,type=event", Collections.singleton("v3"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode("((type:\"event\" AND (field:(v1 OR v2) AND field:(v3))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test if postfix is correctly added.
     */
    @org.junit.Test
    public void testPostfixRule() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=event,match=postfix", Collections.singleton("v1"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq=" + CmsEncoder.encode("((type:\"event\" AND (field:((v1 OR *v1)))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test if prefix is correctly added.
     */
    @org.junit.Test
    public void testPrefixRule() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=event,match=prefix", Collections.singleton("v1"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq=" + CmsEncoder.encode("((type:\"event\" AND (field:((v1 OR v1*)))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Check, if the restriction is ignored, if it is for a type that is not present.
     */
    @org.junit.Test
    public void testRuleForUnknownType() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=article", Collections.singleton("value"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq=" + CmsEncoder.encode("((type:\"event\"))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testTypeRestrictionWithComplexExactPreconfiguredRestriction() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("article", "event");
        // Add complex preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        Collection<String> values = new HashSet<>(3);
        values.add("v1 v2");
        values.add("plain: v1 OR v2");
        values.add("v3");
        preconfigurations.addRestriction("field=field,match=exact,combine=and-and", values);
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode(
                "(field:(\""
                    + ClientUtils.escapeQueryChars("v1 v2")
                    + "\" AND \""
                    + ClientUtils.escapeQueryChars("v3")
                    + "\" AND (v1 OR v2)))")
            + "&fq="
            + CmsEncoder.encode("((type:\"event\") OR (type:\"article\"))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testTypeRestrictionWithComplexPreconfiguredRestriction() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("article", "event");
        // Add complex preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        Collection<String> values = new HashSet<>(3);
        values.add("v1 v2");
        values.add("plain: v1 OR v2");
        values.add("v3");
        preconfigurations.addRestriction("field=field,combine=or-and", values);
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        Collection<String> expectedResults = new HashSet<>(2);
        String expectedPreconfiguredFilterV1 = "&fq="
            + CmsEncoder.encode("(field:((v1 AND v2) OR (v1 OR v2) OR v3))")
            + "&fq="
            + CmsEncoder.encode("((type:\"event\") OR (type:\"article\"))");
        expectedResults.add(expectedPreconfiguredFilterV1);
        String expectedPreconfiguredFilterV2 = "&fq="
            + CmsEncoder.encode("(field:((v1 OR v2) OR (v1 AND v2) OR v3))")
            + "&fq="
            + CmsEncoder.encode("((type:\"event\") OR (type:\"article\"))");
        expectedResults.add(expectedPreconfiguredFilterV2);
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertTrue(expectedResults.contains(preconfiguredFilter));
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testTypeRestrictionWithoutPreconfiguredRestrictions() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("event", "article");
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String expectedTypeFilter = "&fq=" + CmsEncoder.encode("type:(\"event\" OR \"article\")");
        String typeFilter = parser.getResourceTypeFilter();
        assertEquals(expectedTypeFilter, typeFilter);
        assertEquals("", parser.getPreconfiguredFilterQuery());
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testTypeRestrictionWithPreconfiguredRestrictionWithoutType() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("article", "event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field", Collections.singleton("value"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode("(field:(value))")
            + "&fq="
            + CmsEncoder.encode("((type:\"event\") OR (type:\"article\"))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Test the type restriction if no predefined restriction is present.
     */
    @org.junit.Test
    public void testTypeRestrictionWithPreconfiguredRestrictionWithType() {

        // Set up the configuration bean
        CmsConfigurationBean bean = getBasicBean("article", "event");
        // Add simple preconfiguration
        CmsRestrictionsBean preconfigurations = new CmsRestrictionsBean();
        preconfigurations.addRestriction("field=field,type=event", Collections.singleton("value"));
        bean.setPreconfiguredRestrictions(preconfigurations);
        // Initialize the parser
        CmsSimpleSearchConfigurationParser parser = CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(
            null,
            bean);
        // We explicitly set the locale to prevent using the CmsObject
        parser.setSearchLocale(Locale.ENGLISH);
        String typeFilter = parser.getResourceTypeFilter();
        // Whenever we use the predefined filter, it takes over the role of the type filter and the type filter is empty
        assertEquals("", typeFilter);
        // Our check is not very great, since we deal with hash sets and the order could be different
        String expectedPreconfiguredFilter = "&fq="
            + CmsEncoder.encode("((type:\"article\") OR (type:\"event\" AND (field:(value))))");
        String preconfiguredFilter = parser.getPreconfiguredFilterQuery();
        assertEquals(expectedPreconfiguredFilter, preconfiguredFilter);
    }

    /**
     * Generates a basic bean with types.
     * @param types the configured types.
     * @return the configured bean.
     */
    private CmsConfigurationBean getBasicBean(String... types) {

        CmsConfigurationBean bean = new CmsConfigurationBean();
        List<String> displayTypes = new ArrayList<>(types.length);
        for (String type : Arrays.asList(types)) {
            displayTypes.add(type + ":some-display-formatter-uuid-does-not-matter");
        }
        bean.setDisplayTypes(displayTypes);
        return bean;
    }

}
