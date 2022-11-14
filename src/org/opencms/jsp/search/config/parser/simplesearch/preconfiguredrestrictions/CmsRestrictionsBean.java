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

import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionRule.MatchType;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean.FieldValues.FieldType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.util.ClientUtils;

/** Wraps the preconfigured restrictions. */
public class CmsRestrictionsBean {

    /** The values in one input field. */
    public static class FieldValues {

        /** The type describes how values in that input field should be handled. */
        public static enum FieldType {
            /** Use the value as plain Solr input. */
            PLAIN,
            /** Use the values in the default way, that is according to the rule configuration. */
            DEFAULT
        }

        /** The field type. */
        private FieldType m_type;
        /** The values in the field. */
        private Collection<String> m_values;

        /**
         * Default constructor.
         * @param type the field type.
         * @param values the field values.
         */
        public FieldValues(FieldType type, Collection<String> values) {

            m_type = type;
            m_values = Collections.unmodifiableCollection(values);
        }

        /**
         * Returns the type of the input field.
         * @return the type of the input field.
         */
        public FieldType getFieldType() {

            return m_type;
        }

        /**
         * Returns the values in the input field.
         * @return the values in the input field.
         */
        public Collection<String> getValues() {

            return m_values;
        }
    }

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRestrictionsBean.class.getName());

    /** Constant for the rule to ignore. */
    private static final String CONST_IGNORED_RULE = "none";

    /** Constant for the rule rule prefix, if the rule should be just used as plain solr query. */
    private static final String PREFIX_PLAIN = "plain:";

    /**
     * Map from type to rule to values for the rule.
     * We use this structure to easily generate the solr queries.
     */
    private Map<String, Map<CmsRestrictionRule, Collection<FieldValues>>> m_restrictions = new HashMap<>();

    /**
     * Add a preconfigured restriction.
     * @param ruleString the rule for the restriction.
     * @param values the values for the restriction.
     */
    public void addRestriction(String ruleString, Collection<String> values) {

        // Filter out empty values.
        Collection<String> realValues = values.stream().map(
            v -> (v == null) || (v.trim().length() == 0) ? null : v.trim()).filter(v -> v != null).collect(
                Collectors.toSet());
        if (realValues.size() > 0) {
            CmsRestrictionRule rule = null;
            // This rule has to be ignored - so we do not parse it at all.
            if (CONST_IGNORED_RULE.equals(ruleString)) {
                return;
            }
            try {
                rule = CmsRestrictionRuleParser.parseRule(ruleString);
            } catch (CmsException e) {
                LOG.warn("Ignoring unparsable restriction rule '" + ruleString + "'.", e);
                return;
            }
            // We know the rule is parsed and ok here.
            String type = rule.getType();
            if (null == m_restrictions.get(type)) {
                m_restrictions.put(type, new HashMap<>());
            }
            Map<CmsRestrictionRule, Collection<FieldValues>> mapForType = m_restrictions.get(type);
            Collection<FieldValues> fieldValues = new HashSet<>(realValues.size());
            for (String value : realValues) {
                if (value.startsWith(PREFIX_PLAIN)) {
                    String realValue = value.substring(PREFIX_PLAIN.length()).trim();
                    fieldValues.add(new FieldValues(FieldValues.FieldType.PLAIN, Collections.singleton(realValue)));
                } else if (rule.getMatchType().equals(MatchType.EXACT)) {
                    fieldValues.add(
                        new FieldValues(FieldType.DEFAULT, Collections.singleton(ClientUtils.escapeQueryChars(value))));
                } else {
                    Collection<String> finalValues = (Arrays.asList(value.split(" "))).stream().map(
                        word -> ClientUtils.escapeQueryChars(word.trim())).collect(Collectors.toSet());
                    fieldValues.add(new FieldValues(FieldValues.FieldType.DEFAULT, finalValues));
                }
            }
            mapForType.put(rule, fieldValues);
        } else {
            LOG.debug("Ignoring restriction with rule '" + ruleString + "' since no real values are provided.");
        }
    }

    /**
     * Returns the restrictions for the provided type.
     * @param type the type to get the restrictions for.
     * @return the restrictions for the provided type.
     */
    public Map<CmsRestrictionRule, Collection<FieldValues>> getRestrictionsForType(String type) {

        return m_restrictions.get(type);
    }

    /**
     * Returns a flag, indicating if there are restrictions for the provided type.
     * @param type the type to check.
     * @return <code>true</code> iff there are restrictions for the provided type, <code>false</otherwise>.
     */
    public boolean hasRestrictionForType(String type) {

        // If it's not null, it can't be empty since it can only be filled via addRestriction
        return null != m_restrictions.get(type);
    }

    /**
     * Returns a flag, indicating if there are restrictions at all.
     * @return <code>true</code> iff there are restrictions, <code>false</otherwise>.
     */
    public boolean hasRestrictions() {

        return m_restrictions.size() > 0;
    }
}