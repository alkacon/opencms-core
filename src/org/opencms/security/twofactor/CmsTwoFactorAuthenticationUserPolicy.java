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

package org.opencms.security.twofactor;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/**
 * A policy that determines which users should use two-factor authentication.<p>
 *
 * A policy consists of two lists of rules: an include list, and an exclude list.
 * A user should use two-factor authentification if they match at least one rule in
 * the include list, and no rule in the exclude list. However, if the include list
 * does not contain any rules, only the exclude list is checked.
 */
public class CmsTwoFactorAuthenticationUserPolicy {

    /**
     * The rule type.
     */
    public enum CheckType {
        /** Checks if the user belongs to a group. */
        group,

        /** Checks if the full user name (including OU) matches a pattern. */
        pattern,

        /** Checks if the user belongs to an organization unit (or its sub-units). */
        orgunit;

    }

    /**
     * Represents a single rule configured for a policy.
     */
    public static class Rule {

        /** The match type, which determines what the name is compared to. */
        private CheckType m_type;

        /** The value to match. */
        private String m_value;

        /** Regex pattern used for matching user names. */
        private Pattern m_pattern;

        /**
         * Creates a new rule.
         *
         * @param type the rule type
         * @param value the rule value
         */
        public Rule(CheckType type, String value) {

            super();
            m_type = type;
            m_value = value;
            if (type == CheckType.pattern) {
                m_pattern = Pattern.compile(m_value);
            }
        }

        /**
         * Gets the pattern (only used for check type 'pattern').
         *
         * @return pattern
         */
        public Pattern getPattern() {

            return m_pattern;
        }

        /**
         * Gets the match type, which determines what the name is compared to.
         *
         * @return the match type
         */
        public CheckType getType() {

            return m_type;
        }

        /**
         * Gets the name that is used for comparison.
         *
         * @return the name
         */
        public String getValue() {

            return m_value;
        }

    }

    /**
     * A context object used to keep user-related data around which may be needed by multiple rules, so we only need read it once
     * (e.g. the list of groups of a user).
     */
    protected static class UserCheckContext {

        /** The CMS context. */
        private CmsObject m_cms;

        /** The cached set of group names (lazily initialized). */
        private Set<String> m_groupNames;

        /** The user. */
        private CmsUser m_user;

        /**
         * Creates a new instance.
         *
         * @param cms the CMS context
         * @param user the user
         */
        public UserCheckContext(CmsObject cms, CmsUser user) {

            m_cms = cms;
            m_user = user;

        }

        /**
         * Gets the set of names of the groups the user belongs to.
         *
         * @return the set of group names
         * @throws CmsException if initializing the groups fails
         */
        public Set<String> getGroupNames() throws CmsException {

            if (m_groupNames == null) {
                m_groupNames = m_cms.getGroupsOfUser(m_user.getName(), false).stream().map(
                    group -> group.getName()).collect(Collectors.toSet());
            }
            return m_groupNames;
        }

        /**
         * Gets the user.
         *
         * @return the user
         */
        public CmsUser getUser() {

            return m_user;
        }

    }

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTwoFactorAuthenticationUserPolicy.class);

    /** The exclude rules. */
    private List<Rule> m_excludes = new ArrayList<>();

    /** The include rules. */
    private List<Rule> m_includes = new ArrayList<>();

    /**
     * Creates a new policy object.
     *
     * @param include the list of include rules
     * @param exclude the list of exclude rules
     */
    public CmsTwoFactorAuthenticationUserPolicy(List<Rule> include, List<Rule> exclude) {

        m_includes = new ArrayList<>(include);
        m_excludes = new ArrayList<>(exclude);
    }

    /**
     * Checks whether the given user should use two-factor-authentication according to this policy.
     *
     * @param cms the current CMS context
     * @param user the user to check
     * @return true if the user should use two-factor authentication
     */
    public boolean shouldUseTwoFactorAuthentication(CmsObject cms, CmsUser user) {

        UserCheckContext context = new UserCheckContext(cms, user);
        return checkIncluded(context) && !checkExcluded(context);
    }

    /**
     * Checks if a user (given in a user check context) matches the given rule entry.
     *
     * @param context the user check context
     * @param entry the entry
     * @return true if the user matches the check entry
     */
    private boolean check(UserCheckContext context, Rule entry) {

        if (entry.getType() == CheckType.orgunit) {
            String entryOu = normalizeOu(entry.getValue());
            String ou = context.getUser().getOuFqn();
            while (ou != null) {
                if (entryOu.equals(normalizeOu(ou))) {
                    return true;
                }
                ou = CmsOrganizationalUnit.getParentFqn(ou);
            }
        } else if (entry.getType() == CheckType.group) {
            try {
                return context.getGroupNames().contains(entry.getValue());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }
        } else if (entry.getType() == CheckType.pattern) {
            return entry.getPattern().matcher(context.getUser().getName()).matches();
        }
        return false;
    }

    /**
     * Checks if the user from the given context matches the exclude rules.
     *
     * @param context the user check context
     * @return true if the user matches the exclude rules
     */
    private boolean checkExcluded(UserCheckContext context) {

        return m_excludes.stream().anyMatch(exclude -> check(context, exclude));
    }

    /**
     * Checks if the user from the given context matches the include rules.
     *
     * @param context the user check context
     * @return true if the user matches the include rules
     */
    private boolean checkIncluded(UserCheckContext context) {

        if (m_includes.size() == 0) {
            return true;
        }
        return m_includes.stream().anyMatch(include -> check(context, include));
    }

    /**
     * Normalizes the OU name with regard to leading / trailing slashes, for comparison purposes.
     *
     * @param name the OU name
     * @return the normalized OU name
     */
    private String normalizeOu(String name) {

        return CmsStringUtil.joinPaths("/", name, "/");
    }

}
