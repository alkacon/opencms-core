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

package org.opencms.db.urlname;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which contains filter criteria for reading or deleting URL name mapping entries.<p>
 *
 * @since 8.0.0
 */
public class CmsUrlNameMappingFilter {

    /** Base filter which matches all URL name mapping entries. */
    public static final CmsUrlNameMappingFilter ALL = new CmsUrlNameMappingFilter();

    /** The locale which should be matched. */
    private String m_locale;

    /** The name which should be matched. */
    private String m_name;

    /** The name pattern which should be matched. */
    private String m_namePattern;

    /** The structure id which should not be matched. */
    private CmsUUID m_rejectStructureId;

    /** The states which should be matched. */
    private int[] m_states;

    /** The structure id which should be matched. */
    private CmsUUID m_structureId;

    /**
     * The default constructor.<p>
     */
    protected CmsUrlNameMappingFilter() {

        // do nothing
    }

    /**
     * The copy constructor.<p>
     *
     * @param filter the filter to copy
     */
    protected CmsUrlNameMappingFilter(CmsUrlNameMappingFilter filter) {

        m_name = filter.m_name;
        m_structureId = filter.m_structureId;
        m_rejectStructureId = filter.m_rejectStructureId;
        m_states = filter.m_states;
        m_namePattern = filter.m_namePattern;
        m_locale = filter.m_locale;
    }

    /**
     * Returns a new url name mapping filter based on the current one which also has to match a given locale.<p>
     *
     * @param locale the locale to match
     *
     * @return the new filter
     */
    public CmsUrlNameMappingFilter filterLocale(String locale) {

        if (locale == null) {
            throw new IllegalArgumentException();
        }
        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_locale = locale;
        return result;
    }

    /**
     * Creates a new filter from the current filter which also has to match a given name.<p>
     *
     * @param name the name to match
     *
     * @return a new filter
     */
    public CmsUrlNameMappingFilter filterName(String name) {

        if (name == null) {
            throw new IllegalArgumentException();
        }
        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_name = name;
        return result;
    }

    /**
     * Creates a new filter from the current filter which also has to match a given name pattern.<p>
     *
     * @param namePattern the name pattern which should be matched
     *
     * @return a new filter
     */
    public CmsUrlNameMappingFilter filterNamePattern(String namePattern) {

        if (namePattern == null) {
            throw new IllegalArgumentException();
        }
        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_namePattern = namePattern;
        return result;
    }

    /**
     * Creates a new filter from the current filter which also must not match a given structure id.<p>
     *
     * @param id the structure id to not match
     *
     * @return a new filter
     */
    public CmsUrlNameMappingFilter filterRejectStructureId(CmsUUID id) {

        if (id == null) {
            throw new IllegalArgumentException();
        }
        if (m_structureId != null) {
            throw new IllegalStateException();
        }
        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_rejectStructureId = id;
        return result;
    }

    /**
     * Creates a new filter from the current filter which also has to match a given state.<p>
     *
     * @param states the states to match
     *
     * @return the new filter
     */
    public CmsUrlNameMappingFilter filterStates(int... states) {

        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_states = states;
        return result;

    }

    /**
     * Creates a new filter from the current filter which also has to match a given structure id.<p>
     *
     * @param structureId the structure id to match
     *
     * @return the new filter
     */
    public CmsUrlNameMappingFilter filterStructureId(CmsUUID structureId) {

        if (structureId == null) {
            throw new IllegalArgumentException();
        }
        CmsUrlNameMappingFilter result = new CmsUrlNameMappingFilter(this);
        result.m_structureId = structureId;
        return result;
    }

    /**
     * Returns the locale which should be matched by the filter.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the name which should be matched by the filter.<p>
     *
     * @return the name which should be matched by the filter
     */
    public String getName() {

        return m_name;

    }

    /**
     * Returns the name pattern which should be matched by the filter.<p>
     *
     * @return the name pattern which should be matched by the filter
     */
    public String getNamePattern() {

        return m_namePattern;
    }

    /**
     * Returns the structure id which should not be matched by the filter.<p>
     *
     * @return a structure id
     */
    public CmsUUID getRejectStructureId() {

        return m_rejectStructureId;
    }

    /**
     * Returns the state which should be matched by the filter.<p>
     *
     * @return the state which should be matched by the filter
     */
    public int[] getStates() {

        return m_states;
    }

    /**
     * Returns the structure id which should be matched by the filter.<p>
     *
     * @return the structure id which should be matched by the filter
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Checks whether this is a filter which only filters by structure id.<p>
     *
     * @return true if this is a filter which only filters by structure id
     */
    public boolean isIdFilter() {

        return (m_structureId != null)
            && (m_name == null)
            && (m_namePattern == null)
            && (m_states == null)
            && (m_rejectStructureId == null)
            && (m_locale == null);
    }

    /**
     * Checks whether this is a filter which only filters by name.<p>
     *
     * @return true if this is a filter which only filters by name
     */
    public boolean isNameFilter() {

        return (m_structureId == null)
            && (m_name != null)
            && (m_namePattern == null)
            && (m_states == null)
            && (m_rejectStructureId == null)
            && (m_locale == null);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        List<String> resultParts = new ArrayList<String>();
        resultParts.add("[CmsUrlNameMappingFilter:");
        if (m_name != null) {
            resultParts.add("name='" + m_name + "'");
        }
        if (m_structureId != null) {
            resultParts.add("id=" + m_structureId);
        }
        if (m_states != null) {
            resultParts.add("states=" + m_states);
        }
        if (m_namePattern != null) {
            resultParts.add("pattern='" + m_namePattern + "'");
        }
        if (m_rejectStructureId != null) {
            resultParts.add("rejectId=" + m_rejectStructureId);
        }
        resultParts.add("]");
        return CmsStringUtil.listAsString(resultParts, " ");
    }

}
