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

import java.util.Locale;

/**
 * A single restriction rule, telling for which field and type what kind of restriction should be enforced.
 */
public class CmsRestrictionRule {

    /** Match type of the restriction, i.e., how the values should be treated. */
    public static enum MatchType {
        /** Quote the value to match */
        EXACT,
        /** Take the values as is. */
        DEFAULT,
        /** Adds * in the beginning of each word */
        POSTFIX,
        /** Adds * in the end of each word */
        PREFIX,
        /** Adds * in the beginning and end of each word */
        INFIX
    }

    /** Placeholder that is replaced by the locale where we want to search in. */
    private static String LOCALE_PLACEHOLDER = "#";

    /** The type the restriction should hold for. */
    private String m_type;
    /** The solr field, the restriction should be placed on */
    private String m_field;
    /** The match type for the restriction. */
    private MatchType m_match;
    /** The combination mode for multiple values in different input fields, either AND or OR - match all or only any of the values. */
    private CombinationMode m_combinationModeBetweenFields;
    /** The combination mode for multiple values in one input field, either AND or OR - match all or only any of the values. */
    private CombinationMode m_combinationModeInField;

    /**
     * Constructs a restriction rule for the provided field that has no type restriction and default combination mode and match type.
     * @param field the field to put the restriction on.
     */
    public CmsRestrictionRule(String field) {

        this(field, null, null, null, null);
    }

    /**
     * Constructs restriction rule with the provided settings.
     * @param field the field the restriction has to be enforced on.
     * @param type the resource type the restriction should be enforced on.
     * @param matchType the way how values should be treated.
     * @param combinationModeBetweenFields the way how values in different fields should be combined.
     * @param combinationModeInField the way how values in different fields in one field should be combined.
     */
    public CmsRestrictionRule(
        String field,
        String type,
        MatchType matchType,
        CombinationMode combinationModeBetweenFields,
        CombinationMode combinationModeInField) {

        m_field = field;
        m_type = type;
        m_match = null == matchType ? MatchType.DEFAULT : matchType;
        m_combinationModeBetweenFields = null == combinationModeBetweenFields
        ? CombinationMode.OR
        : combinationModeBetweenFields;
        m_combinationModeInField = (null == combinationModeInField)
        ? m_combinationModeBetweenFields
        : combinationModeInField;
    }

    /**
     * Returns the way values of different input fields are combined (match any or all).
     * @return the way values of different input fields are combined (match any or all).
     */
    public CombinationMode getCombinationModeBetweenFields() {

        return m_combinationModeBetweenFields;
    }

    /**
     * Returns the way values of different input fields are combined (match any or all).
     * @return the way values of different input fields are combined (match any or all).
     */
    public CombinationMode getCombinationModeInField() {

        return m_combinationModeInField;
    }

    /**
     * Returns the name of the solr field, the restrictions should be enforced on for, dependent on the provided locale.
     * @param l the search locale.
     * @return the name of the solr field, the restrictions should be enforced on for, dependent on the provided locale.
     */
    public String getFieldForLocale(Locale l) {

        return m_field.replace(LOCALE_PLACEHOLDER, l.toString());
    }

    /**
     * Returns the match type for values, e.g., exact, as-is, prefix, ....
     * @return the match type for values, e.g., exact, as-is, prefix, ....
     */
    public MatchType getMatchType() {

        return m_match;
    }

    /**
     * Returns the name of the solr field, the restrictions should be enforced on, but without resolving the locale placeholder.
     * @return the name of the solr field, the restrictions should be enforced on, but without resolving the locale placeholder.
     */
    public String getRawField() {

        return m_field;
    }

    /**
     * Returns the name of the resource type, the restriction should be enforced for.
     * @return the resource type, the restriction should be enforced for.
     *      <code>null</code> is returned iff the restriction should hold for all resource types.
     */
    public String getType() {

        return m_type;
    }
}
