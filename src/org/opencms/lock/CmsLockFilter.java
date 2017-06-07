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

package org.opencms.lock;

import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to retrieve the locks.<p>
 *
 * @since 6.5.4
 */
public final class CmsLockFilter implements Cloneable {

    /** To filter all locks. */
    public static final CmsLockFilter FILTER_ALL = new CmsLockFilter(true).filterIncludeChildren();

    /** To filter all inherited locks. */
    public static final CmsLockFilter FILTER_INHERITED = new CmsLockFilter(true);

    /** To filter all non inherited locks. */
    public static final CmsLockFilter FILTER_NON_INHERITED = new CmsLockFilter(false);

    /** If set the filter restricts the result excluding locks owned by the given user. */
    private CmsUUID m_notOwnedByUserId;

    /** If set the filter extends the result to non inherited locks. */
    private boolean m_includeChildren;

    /** If set the filter restricts the result including only locks owned by the given user. */
    private CmsUUID m_ownedByUserId;

    /** If set the filter extends the result to inherited locks. */
    private boolean m_includeParents;

    /** If set the filter restricts the result to the given project. */
    private CmsUUID m_projectId;

    /** If set the filter also matches shared exclusive locks. */
    private boolean m_sharedExclusive;

    /** The types to filter. */
    private Set<CmsLockType> m_types = new HashSet<CmsLockType>();

    /** If set the filter restricts the result excluding locks not lockable by the given user. */
    private CmsUser m_notLockableByUser;

    /** If set the filter restricts the result including only locks lockable by the given user. */
    private CmsUser m_lockableByUser;

    /**
     * Private constructor.<p>
     *
     * @param inherited if the this lock filter should checks inherited locks or not
     */
    private CmsLockFilter(boolean inherited) {

        m_includeChildren = !inherited;
        m_includeParents = inherited;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsLockFilter filter = new CmsLockFilter(false);
        filter.m_includeChildren = m_includeChildren;
        filter.m_includeParents = m_includeParents;
        filter.m_types = new HashSet<CmsLockType>(m_types);
        filter.m_ownedByUserId = m_ownedByUserId;
        filter.m_notOwnedByUserId = m_notOwnedByUserId;
        filter.m_projectId = m_projectId;
        filter.m_notLockableByUser = m_notLockableByUser;
        filter.m_lockableByUser = m_lockableByUser;
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param userId the user id to filter
     *
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterNotOwnedByUserId(CmsUUID userId) {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_notOwnedByUserId = userId;
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param user the user to filter
     *
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterNotLockableByUser(CmsUser user) {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_notLockableByUser = user;
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param user the user to filter
     *
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterLockableByUser(CmsUser user) {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_lockableByUser = user;
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its children.<p>
     *
     * @return an extended filter to search the subresources of the given path
     */
    public CmsLockFilter filterIncludeChildren() {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_includeChildren = true;
        return filter;
    }

    /**
     * Returns an extended filter with the given user restriction.<p>
     *
     * @param userId the user id to filter
     *
     * @return an extended filter with the given user restriction
     */
    public CmsLockFilter filterOwnedByUserId(CmsUUID userId) {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_ownedByUserId = userId;
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its parents.<p>
     *
     * @return an extended filter to search the subresources of the given path
     */
    public CmsLockFilter filterIncludeParents() {

        CmsLockFilter filter = (CmsLockFilter)clone();
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
    public CmsLockFilter filterProject(CmsUUID projectId) {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_projectId = projectId;
        return filter;
    }

    /**
     * Returns an extended filter that also matches shared exclusive locks (siblings).<p>
     *
     * @return an extended filter that also matches shared exclusive locks
     */
    public CmsLockFilter filterSharedExclusive() {

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_sharedExclusive = true;
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

        CmsLockFilter filter = (CmsLockFilter)clone();
        filter.m_types.add(type);
        return filter;
    }

    /**
     * Returns the user that has to own the locks.<p>
     *
     * @return the user that has to own the locks
     */
    public CmsUUID getOwnedByUserId() {

        return m_ownedByUserId;
    }

    /**
     * Returns the user that has not to own the locks.<p>
     *
     * @return the user that has not to own the locks
     */
    public CmsUUID getNotOwnedByUserId() {

        return m_notOwnedByUserId;
    }

    /**
     * Returns the user that can overwrite the locks.<p>
     *
     * @return the user that can overwrite the locks
     */
    public CmsUser getLockableByUserId() {

        return m_lockableByUser;
    }

    /**
     * Returns the user that can not overwrite the locks.<p>
     *
     * @return the user that can not overwrite the locks
     */
    public CmsUser getNotLockableByUserId() {

        return m_notLockableByUser;
    }

    /**
     * Returns the project restriction.<p>
     *
     * @return the project restriction
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the types to filter.<p>
     *
     * @return the types to filter
     */
    public Set<CmsLockType> getTypes() {

        return Collections.unmodifiableSet(m_types);
    }

    /**
     * Returns the include children flag.<p>
     *
     * @return if set the filter extends the result to the given path and all its children
     */
    public boolean isIncludeChildren() {

        return m_includeChildren;
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
     * Returns the <code>true</code> if this filter also matches shared exclusive locks.<p>
     *
     * @return the <code>true</code> if this filter also matches shared exclusive locks
     */
    public boolean isSharedExclusive() {

        return m_sharedExclusive;
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
        if (m_includeChildren) {
            // safe since rootPath always ends with slash if a folder
            match = lock.getResourceName().startsWith(rootPath);
        }
        if (!match && m_includeParents) {
            // since parents can only be folders, check it only for folders
            if (lock.getResourceName().endsWith("/")) {
                match = rootPath.startsWith(lock.getResourceName());
            }
        }
        if (match && (m_projectId != null) && !m_projectId.isNullUUID() && (lock.getProjectId() != null)) {
            match = lock.getProjectId().equals(m_projectId);
        }
        if (match && (m_ownedByUserId != null) && !m_ownedByUserId.isNullUUID()) {
            match = lock.getUserId().equals(m_ownedByUserId);
        }
        if (match && (m_notOwnedByUserId != null) && !m_notOwnedByUserId.isNullUUID()) {
            match = !lock.getUserId().equals(m_notOwnedByUserId);
        }
        if (match && (m_lockableByUser != null)) {
            match = lock.isLockableBy(m_lockableByUser);
        }
        if (match && (m_notLockableByUser != null)) {
            match = !lock.isLockableBy(m_notLockableByUser);
        }
        if (match && !m_types.isEmpty()) {
            match = m_types.contains(lock.getType());
            match = match || (m_includeParents && lock.isInherited());
        }
        // check the related lock if available
        if (!match && !lock.getRelatedLock().isNullLock()) {
            match = match(rootPath, lock.getRelatedLock());
        }
        return match;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(128);
        str.append("[");
        str.append("children").append("=").append(m_includeChildren).append(", ");
        str.append("parents").append("=").append(m_includeParents).append(", ");
        str.append("types").append("=").append(m_types).append(", ");
        str.append("includedUser").append("=").append(m_ownedByUserId).append(", ");
        str.append("excludedUser").append("=").append(m_notOwnedByUserId).append(", ");
        str.append("project").append("=").append(m_projectId).append(", ");
        str.append("lockableBy").append("=").append(m_lockableByUser).append(", ");
        str.append("notLockableBy").append("=").append(m_notLockableByUser).append(", ");
        str.append("includeShared").append("=").append(m_sharedExclusive);
        str.append("]");
        return str.toString();
    }
}
