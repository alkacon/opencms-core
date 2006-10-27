/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockFilter.java,v $
 * Date   : $Date: 2006/10/27 11:14:07 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.lock;

import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to retrieve the locks.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.4 
 */
public final class CmsLockFilter implements Cloneable {

    /** To filter all locks. */
    public static final CmsLockFilter FILTER_ALL = new CmsLockFilter(true).filterIncludeChilds();

    /** To filter all inherited locks. */
    public static final CmsLockFilter FILTER_INHERITED = new CmsLockFilter(true);

    /** To filter all non inherited locks. */
    public static final CmsLockFilter FILTER_NON_INHERITED = new CmsLockFilter(false);

    /** If set the filter extends the result to non inherited locks. */
    private boolean m_includeChilds = false;

    /** If set the filter extends the result to inherited locks. */
    private boolean m_includeParents = false;

    /** If set the filter restricts the result to the given project. */
    private int m_projectId = 0;

    /** The types to filter. */
    private Set m_types = new HashSet();

    /** If set the filter restricts the result to the given user. */
    private CmsUUID m_includedUserId = null;

    /** If set the filter restricts the result excluding the given user. */
    private CmsUUID m_excludedUserId = null;

    /**
     * Private constructor.<p>
     * 
     * @param inherited if the this lock filter should checks inherited locks or not
     */
    private CmsLockFilter(boolean inherited) {

        m_includeChilds = !inherited;
        m_includeParents = inherited;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsLockFilter filter = new CmsLockFilter(false);
        filter.m_includeChilds = m_includeChilds;
        filter.m_includeParents = m_includeParents;
        filter.m_types = new HashSet(m_types);
        filter.m_includedUserId = m_includedUserId;
        filter.m_excludedUserId = m_excludedUserId;
        filter.m_projectId = m_projectId;
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its childs.<p>
     * 
     * @return an extended filter to search the subresources of the given path
     */
    public CmsLockFilter filterIncludeChilds() {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_includeChilds = true;
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its parents.<p>
     * 
     * @return an extended filter to search the subresources of the given path
     */
    public CmsLockFilter filterIncludeParents() {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_includeParents = true;
        return filter;
    }

    /**
     * Returns an extended filter with the given project restriction.<p>
     * 
     * @param projectId the project to filter the locks with
     *  
     * @return an extended filter with the given project restriction
     */
    public CmsLockFilter filterProject(int projectId) {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_projectId = projectId;
        return filter;
    }

    /**
     * Returns an extended filter with the given type restriction.<p>
     * 
     * @param type the lock type to filter
     *  
     * @return an extended filter with the given type restriction
     */
    public CmsLockFilter filterType(CmsLockType type) {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_types.add(type);
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param userId the user id to filter
     *  
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterIncludedUserId(CmsUUID userId) {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_includedUserId = userId;
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param userId the user id to filter
     *  
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterExcludedUserId(CmsUUID userId) {

        CmsLockFilter filter = (CmsLockFilter)this.clone();
        filter.m_excludedUserId = userId;
        return filter;
    }

    /**
     * Returns the project restriction.<p>
     *
     * @return the project restriction
     */
    public int getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the types to filter.<p>
     *
     * @return the types to filter
     */
    public Set getTypes() {

        return Collections.unmodifiableSet(m_types);
    }

    /**
     * Returns the user restriction.<p>
     *
     * @return the user restriction
     */
    public CmsUUID getIncludedUserId() {

        return m_includedUserId;
    }

    /**
     * Returns the include childs flag.<p>
     * 
     * @return if set the filter extends the result to the given path and all its childs
     */
    public boolean isIncludeChilds() {

        return m_includeChilds;
    }

    /**
     * Returns the include parents flag.<p>
     * 
     * @return if set the filter extends the result to the given path and all its parents
     */
    public boolean isIncludeParent() {

        return m_includeParents;
    }

    /**
     * Matches the given lock against this filter and the given path.<p>
     * 
     * @param rootPath the path to match the lock against
     * @param lock the lock to match
     * 
     * @return <code>true</code> if the given lock matches
     */
    public boolean match(String rootPath, CmsLock lock) {

        boolean match = false;
        if (m_includeChilds) {
            match = lock.getResourceName().startsWith(rootPath);
        }
        if (!match && m_includeParents) {
            match = rootPath.startsWith(lock.getResourceName());
        }
        if (match && m_projectId != 0) {
            match = (lock.getProjectId() == m_projectId);
        }
        if (match && m_includedUserId != null && !m_includedUserId.isNullUUID()) {
            match = lock.getUserId().equals(m_includedUserId);
        }
        if (match && m_excludedUserId != null && !m_excludedUserId.isNullUUID()) {
            match = !lock.getUserId().equals(m_excludedUserId);
        }
        if (match && !m_types.isEmpty()) {
            match = m_types.contains(lock.getType());
            match = match || (m_includeParents && lock.isInherited());
        }
        return match;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer str = new StringBuffer(128);
        str.append("[");
        str.append("childs").append("=").append(m_includeChilds);
        str.append("parents").append("=").append(m_includeParents);
        str.append("types").append("=").append(m_types).append(", ");
        str.append("includedUser").append("=").append(m_includedUserId).append(", ");
        str.append("excludedUser").append("=").append(m_excludedUserId).append(", ");
        str.append("project").append("=").append(m_projectId).append(", ");
        str.append("]");
        return str.toString();
    }
}
