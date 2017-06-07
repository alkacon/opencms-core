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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsStringUtil;

import java.util.Date;

/**
 * Provides filters for getting resources visited by a user in a specified time range.<p>
 *
 * @since 8.0
 */
public class CmsVisitedByFilter {

    /** The date specifying the start point in time from which a resource was visited. */
    private long m_fromDate;

    /** The flag to determine if subfolders should be included to search for visited resources. */
    private boolean m_includeSubFolders;

    /** The parent path to read visited resources from. */
    private String m_parentPath;

    /** The date specifying the end point in time to which a resource was visited. */
    private long m_toDate;

    /** The user to check the visited resources for. */
    private CmsUser m_user;

    /**
     * Constructor, without parameters.<p>
     *
     * It is required to set the user manually if using this constructor.<p>
     */
    public CmsVisitedByFilter() {

        m_fromDate = 0L;
        m_toDate = Long.MAX_VALUE;
    }

    /**
     * Constructor, setting the user to the current user from the context.<p>
     *
     * @param cms the current users context
     */
    public CmsVisitedByFilter(CmsObject cms) {

        this();
        m_user = cms.getRequestContext().getCurrentUser();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsVisitedByFilter filter = new CmsVisitedByFilter();
        filter.m_fromDate = m_fromDate;
        filter.m_includeSubFolders = m_includeSubFolders;
        filter.m_parentPath = m_parentPath;
        filter.m_toDate = m_toDate;
        filter.m_user = m_user;
        return filter;
    }

    /**
     * Returns the date specifying the start point in time from which a resource was visited.<p>
     *
     * @return the date specifying the start point in time from which a resource was visited
     */
    public long getFromDate() {

        return m_fromDate;
    }

    /**
     * Returns the parent root path to read visited resources from.<p>
     *
     * @return the parent root path to read visited resources from
     */
    public String getParentPath() {

        return m_parentPath;
    }

    /**
     * Returns the date specifying the end point in time to which a resource was visited.<p>
     *
     * @return the date specifying the end point in time to which a resource was visited
     */
    public long getToDate() {

        return m_toDate;
    }

    /**
     * Returns the user to check the visited resources for.<p>
     *
     * @return the user to check the visited resources for
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Returns if subfolders should be included to search for visited resources.<p>
     *
     * @return <code>true</code> if subfolders should be included to search for visited resources, otherwise <code>false</code>
     */
    public boolean isIncludeSubFolders() {

        return m_includeSubFolders;
    }

    /**
     * Sets the date specifying the start point in time from which a resource was visited.<p>
     *
     * @param fromDate the date specifying the start point in time from which a resource was visited
     */
    public void setFromDate(long fromDate) {

        m_fromDate = fromDate;
    }

    /**
     * Returns if subfolders should be included to search for visited resources.<p>
     *
     * @param includeSubFolders the flag to determine if subfolders should be included
     */
    public void setIncludeSubfolders(boolean includeSubFolders) {

        m_includeSubFolders = includeSubFolders;
    }

    /**
     * Sets the parent path to read visited resources from.<p>
     *
     * This has to be the root path of the parent resource, not the site path.<p>
     *
     * @param parentPath the parent path to read visited resources from
     */
    public void setParentPath(String parentPath) {

        m_parentPath = parentPath;
    }

    /**
     * Sets the parent path to read visited resources from using the given resource as parent.<p>
     *
     * @param parentResource the resource to use as parent resource
     */
    public void setParentResource(CmsResource parentResource) {

        if (parentResource.isFile()) {
            m_parentPath = CmsResource.getFolderPath(parentResource.getRootPath());
        } else {
            m_parentPath = parentResource.getRootPath();
        }
    }

    /**
     * Sets the date specifying the end point in time to which a resource was visited.<p>
     *
     * @param toDate the date specifying the end point in time to which a resource was visited
     */
    public void setToDate(long toDate) {

        m_toDate = toDate;
    }

    /**
     * Sets the user to check the visited resources for.<p>
     *
     * @param user the user to check the visited resources for
     */
    public void setUser(CmsUser user) {

        m_user = user;
    }

    /**
     * Sets the start and end point in time in which a resource was visited.<p>
     *
     * @param fromDate the date specifying the start point in time from which a resource was visited
     * @param toDate the date specifying the end point in time to which a resource was visited
     */
    public void setVisitedDates(long fromDate, long toDate) {

        setFromDate(fromDate);
        setToDate(toDate);
    }

    /**
     * Returns a user readable representation of the filter.<p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer(256);
        result.append("From: ").append(new Date(getFromDate()).toString());
        result.append(", To: ").append(new Date(getToDate()).toString());
        if (getUser() != null) {
            result.append(", User: ").append(getUser().getName());
        }
        if (CmsStringUtil.isNotEmpty(getParentPath())) {
            result.append(", Parent path: ").append(getParentPath());
            result.append(", Subfolders: ").append(isIncludeSubFolders());
        }
        return result.toString();
    }
}
