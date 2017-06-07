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

package org.opencms.db;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides filters for getting resources subscribed by a user or group in a specified time range.<p>
 *
 * @since 8.0
 */
public class CmsSubscriptionFilter extends CmsVisitedByFilter {

    /** The groups to check subscribed resources for. */
    private List<CmsGroup> m_groups;

    /** The mode to read subscribed resources. */
    private CmsSubscriptionReadMode m_mode;

    /**
     * Constructor, without parameters.<p>
     *
     * It is required to set the user or group manually if using this constructor.<p>
     */
    public CmsSubscriptionFilter() {

        super();
        m_groups = new ArrayList<CmsGroup>();
        m_mode = CmsSubscriptionReadMode.UNVISITED;
    }

    /**
     * Constructor, setting the user to the current user from the context.<p>
     *
     * @param cms the current users context
     */
    public CmsSubscriptionFilter(CmsObject cms) {

        this(cms, false);
    }

    /**
     * Constructor, setting the user to the current user from the context.<p>
     *
     * @param cms the current users context
     * @param addUserGroups determines if the groups of the current user should be added to the list of groups of the filter
     */
    public CmsSubscriptionFilter(CmsObject cms, boolean addUserGroups) {

        super(cms);
        m_groups = new ArrayList<CmsGroup>();
        if (addUserGroups) {
            try {
                m_groups = cms.getGroupsOfUser(getUser().getName(), false);
            } catch (CmsException e) {
                // failed to set user groups
            }
        }
        m_mode = CmsSubscriptionReadMode.UNVISITED;
    }

    /**
     * Adds a group to the list of groups to check subscribed resources for.<p>
     *
     * @param group the group to add
     */
    public void addGroup(CmsGroup group) {

        m_groups.add(group);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsSubscriptionFilter filter = new CmsSubscriptionFilter();
        filter.setFromDate(getFromDate());
        filter.setParentPath(getParentPath());
        filter.setToDate(getToDate());
        if (getUser() != null) {
            filter.setUser(getUser());
        }
        filter.setGroups(getGroups());
        filter.setMode(getMode());
        return filter;
    }

    /**
     * Returns the groups to check subscribed resources for.<p>
     *
     * @return the groups to check subscribed resources for
     */
    public List<CmsGroup> getGroups() {

        return m_groups;
    }

    /**
     * Returns the mode to read subscribed resources.<p>
     *
     * @return the mode to read subscribed resources
     */
    public CmsSubscriptionReadMode getMode() {

        return m_mode;
    }

    /**
     * Sets the groups to check subscribed resources for.<p>
     *
     * @param groups the groups to check subscribed resources for
     */
    public void setGroups(List<CmsGroup> groups) {

        m_groups = groups;
    }

    /**
     * Sets the mode to read subscribed resources.<p>
     *
     * @param mode the mode to read subscribed resources
     */
    public void setMode(CmsSubscriptionReadMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the groups of the user currently set in this filter as the list of groups to check subscribed resources for.<p>
     *
     * @param cms the current users context
     */
    public void setUserGroups(CmsObject cms) {

        if (getUser() != null) {
            try {
                m_groups = cms.getGroupsOfUser(getUser().getName(), false);
            } catch (CmsException e) {
                // failed to set user groups
            }
        }
    }

    /**
     * Returns a user readable representation of the filter.<p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.toString());
        if (!getGroups().isEmpty()) {
            result.append(", Groups: ").append(CmsStringUtil.collectionAsString(getGroups(), ";"));
        }
        result.append(", Mode: ").append(getMode().toString());
        return result.toString();
    }
}
