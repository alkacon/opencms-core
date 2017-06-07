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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a filter for the repositories.<p>
 *
 * It filters path names, depending on the configuration made.<p>
 *
 * @since 6.2.4
 */
public class CmsRepositoryFilter {

    /** The exclude type to set for the filer. */
    private static final String TYPE_EXCLUDE = "exclude";

    /** The include type to set for the filer. */
    private static final String TYPE_INCLUDE = "include";

    /** The rules to be used of the filter. */
    private List<Pattern> m_filterRules;

    /** The type of the filter: include or exclude. */
    private String m_type;

    /**
     * Default constructor initializing member variables.
     */
    public CmsRepositoryFilter() {

        m_filterRules = new ArrayList<Pattern>();
    }

    /**
     * Adds a new filter rule (regex) to the filter.<p>
     *
     * @param rule the rule (regex) to add
     */
    public void addFilterRule(String rule) {

        m_filterRules.add(Pattern.compile(rule));
    }

    /**
     * Returns the filterRules.<p>
     *
     * @return the filterRules
     */
    public List<Pattern> getFilterRules() {

        return m_filterRules;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Initializes a configuration after all parameters have been added.<p>
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public void initConfiguration() throws CmsConfigurationException {

        if ((!TYPE_INCLUDE.equals(m_type)) && (!TYPE_EXCLUDE.equals(m_type))) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_INVALID_FILTER_TYPE_1, m_type));
        }

        if (CmsLog.INIT.isInfoEnabled()) {

            Iterator<Pattern> iter = m_filterRules.iterator();
            while (iter.hasNext()) {
                Pattern rule = iter.next();

                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_ADD_FILTER_RULE_2, m_type, rule.pattern()));
            }
        }

        m_filterRules = Collections.unmodifiableList(m_filterRules);
    }

    /**
     * Checks if a path is filtered out of the filter or not.<p>
     *
     * @param path the path of a resource to check
     * @return true if the name matches one of the given filter patterns
     */
    public boolean isFiltered(String path) {

        for (int j = 0; j < m_filterRules.size(); j++) {
            Pattern pattern = m_filterRules.get(j);
            if (isPartialMatch(pattern, path)) {
                return m_type.equals(TYPE_EXCLUDE);
            }
        }

        return m_type.equals(TYPE_INCLUDE);
    }

    /**
     * Sets the filterRules.<p>
     *
     * @param filterRules the filterRules to set
     */
    public void setFilterRules(List<Pattern> filterRules) {

        m_filterRules = filterRules;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Returns if the given path matches or partially matches the pattern.<p>
     *
     * For example the regex "/system/modules/" should match the path "/system/".
     * That's not working with Java 1.4. Starting with Java 1.5 there are possiblities
     * to do that. Until then you have to configure all parent paths as a regex filter.<p>
     *
     * @param pattern the pattern to use
     * @param path the path to test if the pattern matches (partially)
     *
     * @return true if the path matches (partially) the pattern
     */
    private boolean isPartialMatch(Pattern pattern, String path) {

        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            return true;
        }

        if (!path.endsWith("/")) {
            matcher = pattern.matcher(path + "/");
            return matcher.matches();
        }

        return false;
        // return matcher.hitEnd();
    }
}
