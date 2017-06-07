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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

/**
 * Represents the lock state of a VFS resource.<p>
 *
 * The lock state is combination of how, by whom and in which project
 * a resource is currently locked.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.file.CmsObject#getLock(org.opencms.file.CmsResource)
 * @see org.opencms.lock.CmsLockManager
 */
public class CmsLock implements Comparable<CmsLock> {

    /** The shared null lock object. */
    private static final CmsLock NULL_LOCK = new CmsLock(
        "",
        CmsUUID.getNullUUID(),
        new CmsProject(),
        CmsLockType.UNLOCKED);

    /** The project where the resource is locked. */
    private CmsProject m_project;

    /** The related lock. */
    private CmsLock m_relatedLock;

    /** The name of the locked resource. */
    private String m_resourceName;

    /** Indicates how the resource is locked. */
    private CmsLockType m_type;

    /** The ID of the user who locked the resource. */
    private CmsUUID m_userId;

    /**
     * Constructor for a new Cms lock.<p>
     *
     * @param resourceName the full resource name including the site root
     * @param userId the ID of the user who locked the resource
     * @param project the project where the resource is locked
     * @param type flag indicating how the resource is locked
     */
    public CmsLock(String resourceName, CmsUUID userId, CmsProject project, CmsLockType type) {

        m_resourceName = resourceName;
        m_userId = userId;
        m_project = project;
        m_type = type;
    }

    /**
     * Returns the shared Null CmsLock.<p>
     *
     * @return the shared Null CmsLock
     */
    public static CmsLock getNullLock() {

        return CmsLock.NULL_LOCK;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsLock other) {

        return m_resourceName.compareTo(other.m_resourceName);
    }

    /**
     * Compares this lock to the specified object.<p>
     *
     * @param obj the object to compare to
     * @return true if and only if member values of this CmsLock are the same with the compared CmsLock
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsLock) {
            CmsLock other = (CmsLock)obj;
            return other.m_resourceName.equals(m_resourceName)
                && other.m_userId.equals(m_userId)
                && other.m_project.equals(m_project)
                && other.m_type.equals(m_type);
        }
        return false;
    }

    /**
     * Returns the edition lock.<p>
     *
     * @return the edition lock
     */
    public CmsLock getEditionLock() {

        if (isSystemLock()) {
            return getRelatedLock();
        }
        return this;
    }

    /**
     * Returns the project where the resource is currently locked.<p>
     *
     * @return the project where the resource is currently locked
     */
    public CmsProject getProject() {

        return m_project;
    }

    /**
     * Returns the ID of the project where the resource is currently locked.<p>
     *
     * @return the ID of the project
     */
    public CmsUUID getProjectId() {

        return m_project.getUuid();
    }

    /**
     * Returns the name of the locked resource.<p>
     *
     * @return the name of the locked resource
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /**
     * Returns the system lock.<p>
     *
     * @return the system lock
     */
    public CmsLock getSystemLock() {

        if (!isSystemLock()) {
            return getRelatedLock();
        }
        return this;
    }

    /**
     * Returns the type about how the resource is locked.<p>
     *
     * @return the type of the lock
     */
    public CmsLockType getType() {

        return m_type;
    }

    /**
     * Returns the ID of the user who currently locked the resource.<p>
     *
     * @return the ID of the user
     */
    public CmsUUID getUserId() {

        return m_userId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_project.hashCode() + m_resourceName.hashCode() + m_userId.hashCode() + m_type.hashCode();
    }

    /**
     * Returns <code>true</code> if this is an directly inherited lock.<p>
     *
     * @return <code>true</code> if this is an directly inherited lock
     */
    public boolean isDirectlyInherited() {

        return m_type.isDirectlyInherited();
    }

    /**
     * Returns <code>true</code> if this is an exclusive, temporary exclusive, or
     * directly inherited lock, and the given user is the owner of this lock.<p>
     *
     * @param user the user to compare to the owner of this lock
     *
     * @return <code>true</code> if this is an exclusive, temporary exclusive, or
     *      directly inherited lock, and the given user is the owner of this lock
     */
    public boolean isDirectlyOwnedBy(CmsUser user) {

        return (isExclusive() || isDirectlyInherited()) && isOwnedBy(user);
    }

    /**
     * Returns <code>true</code> if this is an exclusive, temporary exclusive, or
     * directly inherited lock, and the current user is the owner of this lock,
     * checking also the project of the lock.<p>
     *
     * @param cms the CMS context to check
     *
     * @return <code>true</code> if this is an exclusive, temporary exclusive, or
     *      directly inherited lock, and the current user is the owner of this lock
     */
    public boolean isDirectlyOwnedInProjectBy(CmsObject cms) {

        return (isExclusive() || isDirectlyInherited())
            && isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject());
    }

    /**
     * Returns <code>true</code> if this is an exclusive, temporary exclusive, or
     * directly inherited lock, and the given user is the owner of this lock,
     * checking also the project of the lock.<p>
     *
     * @param user the user to compare to the owner of this lock
     * @param project the project to compare to the project of this lock
     *
     * @return <code>true</code> if this is an exclusive, temporary exclusive, or
     *      directly inherited lock, and the given user is the owner of this lock
     */
    public boolean isDirectlyOwnedInProjectBy(CmsUser user, CmsProject project) {

        return (isExclusive() || isDirectlyInherited()) && isOwnedInProjectBy(user, project);
    }

    /**
     * Returns <code>true</code> if this is an exclusive (or temporary exclusive) lock.<p>
     *
     * @return <code>true</code> if this is an exclusive (or temporary exclusive) lock
     */
    public boolean isExclusive() {

        return m_type.isExclusive();
    }

    /**
     * Returns <code>true</code> if this is an exclusive (or temporary exclusive) lock,
     * and the given user is the owner of this lock.<p>
     *
     * @param user the user to compare to the owner of this lock
     *
     * @return <code>true</code> if this is an exclusive (or temporary exclusive) lock,
     *      and the given user is the owner of this lock
     */
    public boolean isExclusiveOwnedBy(CmsUser user) {

        return isExclusive() && isOwnedBy(user);
    }

    /**
     * Returns <code>true</code> if this is an exclusive (or temporary exclusive) lock,
     * and the given user is the owner and the given project is the project of this lock.<p>
     *
     * @param user the user to compare to the owner of this lock
     * @param project the project to compare to the project of this lock
     *
     * @return <code>true</code> if this is an exclusive (or temporary exclusive) lock,
     *      and the given user is the owner and the given project is the project of this lock
     */
    public boolean isExclusiveOwnedInProjectBy(CmsUser user, CmsProject project) {

        return isExclusive() && isOwnedInProjectBy(user, project);
    }

    /**
     * Returns <code>true</code> if this is an inherited lock, which may either be directly or shared inherited.<p>
     *
     * @return <code>true</code> if this is an inherited lock, which may either be directly or shared inherited
     */
    public boolean isInherited() {

        return m_type.isInherited();
    }

    /**
     * Returns <code>true</code> if the given project is the project of this lock.<p>
     *
     * @param project the project to compare to the project of this lock
     *
     * @return <code>true</code> if the given project is the project of this lock
     */
    public boolean isInProject(CmsProject project) {

        return m_project.equals(project);
    }

    /**
     * Checks if a resource can be locked by a user.<p>
     *
     * The resource is not lockable if it already has a lock of type {@link CmsLockType#PUBLISH}.<p>
     *
     * The resource is lockable either
     * - if it is currently unlocked
     * - if it has a lock of another type set and the user is the lock owner
     *
     * @param user the user to test lockeability for
     *
     * @return <code>true</code> if this lock blocks any operation on the locked resource until it is unlocked
     */
    public boolean isLockableBy(CmsUser user) {

        if (getSystemLock().isPublish()) {
            return false;
        }
        if (getEditionLock().isUnlocked() && getSystemLock().isUnlocked()) {
            return true;
        }
        return getEditionLock().isOwnedBy(user);
    }

    /**
     * Returns <code>true</code> if this lock is the <code>NULL</code> lock which can
     * be obtained by {@link #getNullLock()}.<p>
     *
     * Only for the <code>NULL</code> lock, {@link #isUnlocked()} is <code>true</code>.<p>
     *
     * @return <code>true</code> if this lock is the <code>NULL</code> lock
     */
    public boolean isNullLock() {

        return isUnlocked();
    }

    /**
     * Returns <code>true</code> if the given user is the owner of this lock.<p>
     *
     * @param user the user to compare to the owner of this lock
     *
     * @return <code>true</code> if the given user is the owner of this lock
     */
    public boolean isOwnedBy(CmsUser user) {

        return m_userId.equals(user.getId());
    }

    /**
     * Returns <code>true</code> if the given user is the owner of this lock,
     * and this lock belongs to the given project.<p>
     *
     * @param user the user to compare to the owner of this lock
     * @param project the project to compare to the project of this lock
     *
     * @return <code>true</code> if the given user is the owner of this lock,
     *      and this lock belongs to the given project
     */
    public boolean isOwnedInProjectBy(CmsUser user, CmsProject project) {

        return isOwnedBy(user) && isInProject(project);
    }

    /**
     * Returns <code>true</code> if this is a persistent lock that should be saved when the systems shuts down.<p>
     *
     * @return <code>true</code> if this is a persistent lock that should be saved when the systems shuts down
     */
    public boolean isPersistent() {

        return m_type.isPersistent();
    }

    /**
     * Returns <code>true</code> if this is a publish lock.<p>
     *
     * @return <code>true</code> if this is a publish lock
     */
    public boolean isPublish() {

        return m_type.isPublish();
    }

    /**
     * Returns <code>true</code> if this is a shared lock.<p>
     *
     * @return <code>true</code> if this is a shared lock
     */
    public boolean isShared() {

        return m_type.isShared();
    }

    /**
     * Returns <code>true</code> if this is a system (2nd level) lock.<p>
     *
     * @return <code>true</code> if this is a system (2nd level) lock
     */
    public boolean isSystemLock() {

        return m_type.isSystem();
    }

    /**
     * Returns <code>true</code> if this is a temporary lock.<p>
     *
     * @return <code>true</code> if this is a temporary lock
     */
    public boolean isTemporary() {

        return m_type.isTemporary();
    }

    /**
     * Returns <code>true</code> if this lock is in fact unlocked.<p>
     *
     * Only if this is <code>true</code>, the result lock is equal to the <code>NULL</code> lock,
     * which can be obtained by {@link #getNullLock()}.<p>
     *
     * @return <code>true</code> if this lock is in fact unlocked
     */
    public boolean isUnlocked() {

        return m_type.isUnlocked();
    }

    /**
     * Builds a string representation of the current state.<p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();

        buf.append("[CmsLock: resource: ");
        buf.append(getResourceName());
        buf.append(", type: ");
        buf.append(getType());
        buf.append(", project: ");
        buf.append(getProjectId());
        buf.append(", user: ");
        buf.append(getUserId());
        if (getRelatedLock() != null) {
            buf.append(", related lock: ");
            buf.append(getRelatedLock().getType());
        }
        buf.append("]");

        return buf.toString();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() {

        CmsLock lock = new CmsLock(m_resourceName, m_userId, m_project, m_type);
        if ((m_relatedLock != null) && !m_relatedLock.isNullLock()) {
            lock.setRelatedLock(
                new CmsLock(
                    m_relatedLock.m_resourceName,
                    m_relatedLock.m_userId,
                    m_relatedLock.m_project,
                    m_relatedLock.m_type));
        }
        return lock;
    }

    /**
     * Returns the related Lock.<p>
     *
     * @return the related Lock
     */
    protected CmsLock getRelatedLock() {

        if (m_relatedLock == null) {
            CmsLockType type;
            if (isSystemLock()) {
                type = CmsLockType.UNLOCKED;
            } else {
                type = CmsLockType.SYSTEM_UNLOCKED;
            }
            CmsLock lock = new CmsLock(getResourceName(), getUserId(), getProject(), type);
            lock.setRelatedLock(this);
            if (isUnlocked()) {
                // prevent the null lock gets modified
                return lock;
            }
            m_relatedLock = lock;
        }
        return m_relatedLock;
    }

    /**
     * Sets the related Lock.<p>
     *
     * @param relatedLock the related Lock to set
     */
    protected void setRelatedLock(CmsLock relatedLock) {

        if (this == NULL_LOCK) {
            throw new RuntimeException("null lock");
        }
        if ((relatedLock == null) || relatedLock.isUnlocked()) {
            m_relatedLock = null;
        } else {
            m_relatedLock = relatedLock;
            m_relatedLock.m_relatedLock = this;
        }
    }
}