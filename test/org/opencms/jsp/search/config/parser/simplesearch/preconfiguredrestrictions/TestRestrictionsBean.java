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

import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean.FieldValues;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean.FieldValues.FieldType;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.util.ClientUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the preconfigured restriction bean. */
public class TestRestrictionsBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestRestrictionsBean(String arg0) {

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
        suite.addTest(new TestRestrictionsBean("testEmptyCheckAndSimpleRestrictions"));
        suite.addTest(new TestRestrictionsBean("testMultipleRestrictions"));
        suite.addTest(new TestRestrictionsBean("testValueHandling"));
        suite.addTest(new TestRestrictionsBean("testIgnoredRule"));
        suite.addTest(new TestRestrictionsBean("testFieldValues"));

        return suite;
    }

    /**
     * Tests empty checks and simple restrictions.
     */
    @org.junit.Test
    public void testEmptyCheckAndSimpleRestrictions() {

        CmsRestrictionsBean bean = new CmsRestrictionsBean();
        assertFalse(bean.hasRestrictions());
        bean.addRestriction("field=test", Collections.emptySet());
        // Restriction should not be added, since no values
        assertFalse(bean.hasRestrictions());
        bean.addRestriction("test,abc,invalid", Collections.singleton("value"));
        // Restriction should not be added, since invalid rule
        assertFalse(bean.hasRestrictions());
        bean.addRestriction("field=test", Collections.singleton("value"));
        // Restriction should be added
        assertTrue(bean.hasRestrictions());
        // The restriction holds without type restriction
        assertTrue(bean.hasRestrictionForType(null));
        // We should get the restriction
        Map<CmsRestrictionRule, Collection<FieldValues>> restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        Collection<FieldValues> values = restrictions.entrySet().iterator().next().getValue();
        assertEquals(1, values.size());
        assertEquals("value", values.iterator().next().getValues().iterator().next());
        // But not for some special type
        assertFalse(bean.hasRestrictionForType("type"));
        assertNull(bean.getRestrictionsForType("type"));
        // Add restriction for type
        bean.addRestriction("field=test,type=type", Collections.singleton("value"));
        assertTrue(bean.hasRestrictionForType("type"));
        assertNotNull(bean.getRestrictionsForType("type"));
    }

    /**
     * Tests field values behavior.
     */
    @org.junit.Test
    public void testFieldValues() {

        // simple rule
        CmsRestrictionsBean bean = new CmsRestrictionsBean();
        Collection<String> values = new HashSet<>(1);
        values.add("v1 v2");
        bean.addRestriction("field=test", values);
        Map<CmsRestrictionRule, Collection<FieldValues>> restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        Collection<FieldValues> storedFieldValues = restrictions.entrySet().iterator().next().getValue();
        assertEquals(1, storedFieldValues.size());
        FieldValues storedFV = storedFieldValues.iterator().next();
        assertEquals(FieldType.DEFAULT, storedFV.getFieldType());
        Collection<String> expectedFieldValues = new HashSet<>(2);
        expectedFieldValues.add("v1");
        expectedFieldValues.add("v2");
        assertEquals(new HashSet<>(expectedFieldValues), new HashSet<>(storedFV.getValues()));

        // rule with exact match
        bean = new CmsRestrictionsBean();
        bean.addRestriction("field=test,match=exact", values);
        restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        storedFieldValues = restrictions.entrySet().iterator().next().getValue();
        assertEquals(1, storedFieldValues.size());
        storedFV = storedFieldValues.iterator().next();
        assertEquals(FieldType.DEFAULT, storedFV.getFieldType());
        expectedFieldValues = new HashSet<>(1);
        expectedFieldValues.add(ClientUtils.escapeQueryChars("v1 v2"));
        assertEquals(new HashSet<>(expectedFieldValues), new HashSet<>(storedFV.getValues()));

        // rule with plain value
        bean = new CmsRestrictionsBean();
        values.clear();
        values.add("plain: (\"some term\" OR test) AND open*");
        bean.addRestriction("field=test,match=exact", values);
        restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        storedFieldValues = restrictions.entrySet().iterator().next().getValue();
        assertEquals(1, storedFieldValues.size());
        storedFV = storedFieldValues.iterator().next();
        assertEquals(FieldType.PLAIN, storedFV.getFieldType());
        expectedFieldValues = new HashSet<>(1);
        expectedFieldValues.add("(\"some term\" OR test) AND open*");
        assertEquals(new HashSet<>(expectedFieldValues), new HashSet<>(storedFV.getValues()));
    }

    /**
     * Tests if rule "none" is ignored.
     */
    @org.junit.Test
    public void testIgnoredRule() {

        // Test escaping of special characters
        CmsRestrictionsBean bean = new CmsRestrictionsBean();
        bean.addRestriction("none", Collections.singleton("v1"));
        assertFalse(bean.hasRestrictions());
    }

    /**
     * Tests multiple restrictions on the same type.
     */
    @org.junit.Test
    public void testMultipleRestrictions() {

        CmsRestrictionsBean bean = new CmsRestrictionsBean();
        Collection<String> values = new HashSet<>(2);
        values.add("v1");
        values.add("v2");
        bean.addRestriction("field=test", values);
        Map<CmsRestrictionRule, Collection<FieldValues>> restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        Collection<FieldValues> storedFieldValues = restrictions.entrySet().iterator().next().getValue();
        Collection<String> storedValues = storedFieldValues.stream().map(
            fv -> fv.getValues().iterator().next()).collect(Collectors.toSet());
        assertEquals(new HashSet<>(values), new HashSet<>(storedValues));
        bean.addRestriction("field=test,type=type", Collections.singleton("v1"));
        bean.addRestriction("field=test,type=type", Collections.singleton("v2"));
        restrictions = bean.getRestrictionsForType("type");

        assertEquals(2, restrictions.size());
        Iterator<Entry<CmsRestrictionRule, Collection<FieldValues>>> it = restrictions.entrySet().iterator();
        Collection<String> vals1 = it.next().getValue().iterator().next().getValues();
        Collection<String> vals2 = it.next().getValue().iterator().next().getValues();
        assertEquals(1, vals1.size());
        assertEquals(1, vals2.size());
        storedValues = new HashSet<>(2);
        storedValues.addAll(vals1);
        storedValues.addAll(vals2);
        assertEquals(new HashSet<>(values), new HashSet<>(storedValues));
    }

    /**
     * Tests value handling for the different match types.
     */
    @org.junit.Test
    public void testValueHandling() {

        // Test escaping of special characters
        CmsRestrictionsBean bean = new CmsRestrictionsBean();
        bean.addRestriction("field=test,match=default", Collections.singleton("v1*\"&"));
        Map<CmsRestrictionRule, Collection<FieldValues>> restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        Collection<String> storedValues = restrictions.entrySet().iterator().next().getValue().iterator().next().getValues();
        Collection<String> values = new HashSet<>(1);
        values.add(ClientUtils.escapeQueryChars("v1*\"&"));
        assertEquals(new HashSet<>(values), new HashSet<>(storedValues));

        // For non-exact match type, the values should be taken apart
        bean = new CmsRestrictionsBean();
        bean.addRestriction("field=test,match=default", Collections.singleton("v1 v2"));
        restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        storedValues = restrictions.entrySet().iterator().next().getValue().iterator().next().getValues();
        values = new HashSet<>(2);
        values.add("v1");
        values.add("v2");
        assertEquals(new HashSet<>(values), new HashSet<>(storedValues));

        // For exact match type, the values should remain together
        bean = new CmsRestrictionsBean();
        bean.addRestriction("field=test,match=exact", Collections.singleton("v1 v2"));
        restrictions = bean.getRestrictionsForType(null);
        assertEquals(1, restrictions.size());
        storedValues = restrictions.entrySet().iterator().next().getValue().iterator().next().getValues();
        values = new HashSet<>(1);
        values.add("v1\\ v2");
        assertEquals(new HashSet<>(values), new HashSet<>(storedValues));
    }
}
