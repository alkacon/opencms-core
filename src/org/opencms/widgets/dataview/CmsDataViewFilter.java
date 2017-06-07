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

package org.opencms.widgets.dataview;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * Represents a filter to narrow down the list of displayed results.<p>
 *
 * A filter has a unique id, a set of options (consisting of a map whose keys are internal identifiers of the
 * options and whose values are the texts to display to the user), a current value and a 'nice name' to display
 * as caption for the filter.
 *
 * Filter options don't by default have a "null" (=neutral, i.e. filter is not applied) option; if you need this,
 * add a corresponding option in your implementation of I_CmsDataView.
 */
public class CmsDataViewFilter {

    /** The filter id. */
    private String m_id;

    /** The ordered map of filter options. */
    private LinkedHashMap<String, String> m_options;

    /** The current filter options. */
    private String m_value;

    /** The user-readable name for the filter. */
    private String m_niceName;

    /** The help text for the filter. */
    private String m_helpText;

    /**
     * Creates a new filter.<p>
     *
     * @param id the filter id
     * @param niceName the nice name
     * @param helpText the help text for the filter
     * @param options the ordered map of options
     * @param value the current value
     */
    public CmsDataViewFilter(
        String id,
        String niceName,
        String helpText,
        LinkedHashMap<String, String> options,
        String value) {
        m_options = new LinkedHashMap<String, String>(options);
        m_id = id;
        m_niceName = niceName;
        m_value = value;
        m_helpText = helpText;
        if (!m_options.containsKey(value)) {
            throw new IllegalArgumentException("Option value " + value + " not found in " + options);
        }
    }

    /**
     * Creates a copy of the filter.<p>
     *
     * @return the copied filter
     */
    public CmsDataViewFilter copy() {

        return new CmsDataViewFilter(m_id, m_niceName, m_helpText, m_options, m_value);
    }

    /**
     * Creates a copy of the filter, but uses a different filter value for the copy.<p>
     *
     * @param value the filter value for the copy
     * @return the copied filter
     */
    public CmsDataViewFilter copyWithValue(String value) {

        return new CmsDataViewFilter(m_id, m_niceName, m_helpText, m_options, value);
    }

    /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
    @Override
    public boolean equals(Object other) {

        if (!(other instanceof CmsDataViewFilter)) {
            return false;
        }
        CmsDataViewFilter otherFilter = (CmsDataViewFilter)other;
        LinkedHashMap<String, String> otherOptions = otherFilter.m_options;
        if (!m_id.equals(otherFilter.m_id)) {
            return false;
        }
        if (!m_value.equals(otherFilter.m_value)) {
            return false;
        }
        if (otherOptions.size() != m_options.size()) {
            return false;
        }
        Iterator<Map.Entry<String, String>> iter1, iter2;
        iter1 = m_options.entrySet().iterator();
        iter2 = otherOptions.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry<String, String> entry1 = iter1.next();
            Map.Entry<String, String> entry2 = iter2.next();
            boolean equalKey = Objects.equal(entry1.getKey(), entry2.getKey());
            boolean equalValue = Objects.equal(entry1.getValue(), entry2.getValue());
            if (!equalKey || !equalValue) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the help text for the filter.<p>
     *
     * @return the help text for the filter
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * Gets the id of the filter.<p>
     *
     * @return the id of the filter
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the user-readable name of the filter.<p>
     *
     * @return the user-readable name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * The map of filter options.<p>
     *
     * Internally, this is stored as a LinkedHashMap, so the order of options is preserved.
     *
     * The keys of the map are internal identifiers for the options, while the values are the texts displayed in the GUI for the options.
     *
     * @return the map of filter options
     */
    public Map<String, String> getOptions() {

        return Collections.unmodifiableMap(m_options);
    }

    /**
     * Gets the current value.<p>
     *
     * @return the filter value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return (31 * m_id.hashCode()) + (31 * 31 * m_options.hashCode()) + m_value.hashCode();
    }
}
