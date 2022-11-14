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
import org.opencms.jsp.search.config.parser.simplesearch.Messages;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionRule.MatchType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.util.Arrays;

import org.apache.commons.logging.Log;

/**
 * Parser for restriction rules.
 */
public final class CmsRestrictionRuleParser {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRestrictionRuleParser.class.getName());

    /** Rule prefix for the resource type name. */
    private static String PREFIX_TYPE = "type=";
    /** Rule prefix for the field name. */
    private static String PREFIX_FIELD = "field=";
    /** Rule prefix for the match mode. */
    private static String PREFIX_MATCH = "match=";
    /** Rule prefix for the combination mode. */
    private static String PREFIX_COMBINE = "combine=";

    /**
     * Hide the default constructor.
     */
    private CmsRestrictionRuleParser() {

        // Hide default constructor
    }

    /**
     * Parses the provided restriction rule.
     * @param rule the rule to parse.
     * @return the parsed rule.
     * @throws CmsException thrown if the rule cannot be parsed.
     */
    public static CmsRestrictionRule parseRule(String rule) throws CmsException {

        if ((rule != null) && (rule.length() > 0)) {
            String[] ruleParts = rule.split(",");
            if (ruleParts.length == 1) {
                if (!rule.contains("=")) {
                    return new CmsRestrictionRule(rule);
                } else {
                    if (rule.startsWith(PREFIX_FIELD)) {
                        return new CmsRestrictionRule(rule.substring(PREFIX_FIELD.length()));
                    }
                    throw new CmsException(Messages.get().container(Messages.ERR_WRONG_CONFIGURATION_SYNTAX_1, rule));
                }
            } else {
                String field = null;
                String type = null;
                MatchType match = null;
                CombinationMode modeBetweenFields = null;
                CombinationMode modeInField = null;
                for (String rulePart : Arrays.asList(ruleParts)) {
                    if (rulePart.startsWith(PREFIX_FIELD)) {
                        field = rulePart.substring(PREFIX_FIELD.length());
                    } else if (rulePart.startsWith(PREFIX_COMBINE)) {
                        String modeString = rulePart.substring(PREFIX_COMBINE.length());
                        try {
                            if (modeString.contains("-")) {
                                String[] modes = modeString.split("-");
                                modeBetweenFields = CombinationMode.valueOf(modes[0].toUpperCase());
                                modeInField = CombinationMode.valueOf(modes[1].toUpperCase());
                            }
                            modeBetweenFields = CombinationMode.valueOf(modeString.toUpperCase());
                        } catch (Throwable t) {
                            LOG.info("Invalid combination mode '" + modeString + "' is ignored");
                        }
                    } else if (rulePart.startsWith(PREFIX_MATCH)) {
                        String matchString = rulePart.substring(PREFIX_MATCH.length());
                        try {
                            match = MatchType.valueOf(matchString.toUpperCase());
                        } catch (Throwable t) {
                            LOG.info("Invalid match type \"" + matchString + "\" is ignored.");
                            match = MatchType.DEFAULT;
                        }
                    } else if (rulePart.startsWith(PREFIX_TYPE)) {
                        type = rulePart.substring(PREFIX_TYPE.length());
                    } else {
                        LOG.info("Invalid rule part '" + rulePart + "' is ignored.");
                    }
                }
                if (field != null) {
                    return new CmsRestrictionRule(field, type, match, modeBetweenFields, modeInField);
                } else {
                    throw new CmsException(Messages.get().container(Messages.ERR_WRONG_CONFIGURATION_SYNTAX_1, rule));
                }
            }
        } else {
            throw new CmsException(Messages.get().container(Messages.ERR_WRONG_CONFIGURATION_SYNTAX_1, rule));
        }
    }

}
