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

package org.opencms.ade.configuration;

import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Helper class for computing dynamic function availability based on sitemap configuration settings.
 */
public class CmsFunctionAvailability {

    /** The formatter configuration state. */
    private CmsFormatterConfigurationCacheState m_formatterConfig;

    /** The map of states for dynamic functions - true for explicitly added functions, false for explicitly removed functions (unless there is a whitelist and they are not in it. */
    private Map<CmsUUID, Boolean> m_functionStates = new HashMap<>();

    /** True if whitelisting has been enabled. */
    private boolean m_hasWhitelist;

    /**
     * Create a new instance.
     *
     * @param formatterConfig the formatter configuration state
     */
    public CmsFunctionAvailability(CmsFormatterConfigurationCacheState formatterConfig) {

        m_formatterConfig = formatterConfig;
    }

    /**
     * Adds a dynamic function id and enables whitelisting.
     *
     * @param functionId the function id to add
     */
    public void add(CmsUUID functionId) {

        if (m_hasWhitelist) {
            // If we have a whitelist, and we explicitly add a function,
            // remove all whitelisted functions with the same key.
            I_CmsFormatterBean functionFormatter = m_formatterConfig.getFormatters().get(functionId);
            if ((functionFormatter != null) && (functionFormatter.getKey() != null)) {
                String key = functionFormatter.getKey();
                Iterator<Map.Entry<CmsUUID, Boolean>> iter = m_functionStates.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<CmsUUID, Boolean> entry = iter.next();
                    CmsUUID id = entry.getKey();
                    I_CmsFormatterBean existingBean = m_formatterConfig.getFormatters().get(id);
                    if ((existingBean != null) && key.equals(existingBean.getKey())) {
                        iter.remove();
                    }
                }

            }
        }
        m_hasWhitelist = true;
        m_functionStates.put(functionId, Boolean.TRUE);
    }

    /**
     * Adds all ids from the given collection.
     *
     * @param enabledIds the ids to add
     */
    public void addAll(Collection<CmsUUID> enabledIds) {

        for (CmsUUID id : enabledIds) {
            add(id);
        }
    }

    /**
     * Check if the function with the given id is available with this configuration.
     *
     * @param id the id to check
     * @return true if the function is available
     */
    public boolean checkAvailable(CmsUUID id) {

        Boolean state = m_functionStates.get(id);
        if (state != null) {
            return state.booleanValue();
        } else {
            return !m_hasWhitelist;
        }
    }

    /**
     * Gets the blacklist of explicitly removed functions that are not already covered by not being in a whitelist.
     *
     * @return the list of explicitly removed functions
     */
    public Collection<CmsUUID> getBlacklist() {

        List<CmsUUID> result = new ArrayList<>();
        for (Map.Entry<CmsUUID, Boolean> entry : m_functionStates.entrySet()) {
            if (!entry.getValue().booleanValue()) {
                result.add(entry.getKey());
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Gets the whitelist of explicitly enabled functions.
     *
     * <p>If no functions have been explicitly added, and no "remove all" option has been used, this will return null.
     *
     * @return the whitelist of functions
     */
    public Collection<CmsUUID> getWhitelist() {

        if (!m_hasWhitelist) {
            return null;
        }
        List<CmsUUID> result = new ArrayList<>();
        for (Map.Entry<CmsUUID, Boolean> entry : m_functionStates.entrySet()) {
            if (entry.getValue().booleanValue()) {
                result.add(entry.getKey());
            }
        }
        Collections.sort(result);
        return result;

    }

    /**
     * Checks if this object has any restrictions on functions.
     *
     * @return true if this has any restrictions on functions
     */
    public boolean isDefined() {

        return m_hasWhitelist || (m_functionStates.size() > 0);
    }

    /**
     * Removes a single function.
     *
     * @param functionId the id of the function
     */
    public void remove(CmsUUID functionId) {

        if (m_hasWhitelist) {
            m_functionStates.remove(functionId);
        } else {
            m_functionStates.put(functionId, Boolean.FALSE);
        }
    }

    /**
     * Removes all functions and enables the whitelist.
     */
    public void removeAll() {

        m_hasWhitelist = true;
        m_functionStates.clear();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
