/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLock.java,v $
 * Date   : $Date: 2006/08/19 13:40:55 $
 * Version: $Revision: 1.28.8.1 $
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

import org.opencms.db.CmsDbUtil;
import org.opencms.util.CmsUUID;

/**
 * Represents the lock state of a VFS resource.<p>
 * 
 * The lock state is combination of how, by whom and in which project
 * a resource is currently locked.<p>
 * 
 * Using old-style methods on CmsResource objects to prove the lock
 * state of a resource may result to incorrect lock states. Use 
 * {@link org.opencms.file.CmsObject#getLock(String)} to obtain a
 * CmsLock object that represents the current lock state of a resource.
 * 
 * @author Thomas Weckert  
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.28.8.1 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.file.CmsObject#getLock(org.opencms.file.CmsResource)
 * @see org.opencms.lock.CmsLockManager
 */
public class CmsLock implements Cloneable {

    /** The shared null lock object. */
    private static final CmsLock NULL_LOCK = new CmsLock(
        "",
        CmsUUID.getNullUUID(),
        CmsDbUtil.UNKNOWN_ID,
        CmsLockType.UNLOCKED);

    /** Flag to indicate if the lock is a temporary lock. */
    private int m_mode;

    /** The ID of the project where the resource is locked. */
    private int m_projectId;

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
     * @param projectId the ID of the project where the resource is locked
     * @param type flag indicating how the resource is locked
     */
    public CmsLock(String resourceName, CmsUUID userId, int projectId, CmsLockType type) {

        m_resourceName = resourceName;
        m_userId = userId;
        m_projectId = projectId;
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
     * Compares this lock to the specified object.<p>
     * 
     * @param obj the object to compare to
     * @return true if and only if member values of this CmsLock are the same with the compared CmsLock 
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsLock) {
            CmsLock other = (CmsLock)obj;
            return other.m_resourceName.equals(m_resourceName)
                && other.m_userId.equals(m_userId)
                && (other.m_projectId == m_projectId);
        }
        return false;
    }

    /**
     * Returns the mode of the lock to indicate if the lock is a temporary lock.<p>
     * 
     * @return the temporary mode of the lock
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Returns the ID of the project where the resource is currently locked.<p>
     * 
     * @return the ID of the project
     */
    public int getProjectId() {

        return m_projectId;
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
    public int hashCode() {

        return getResourceName().hashCode();
    }

    /**
     * Returns <code>true</code> if this is an exclusive (or temporary exclusive) lock.<p>
     * 
     * @return <code>true</code> if this is an exclusive (or temporary exclusive) lock
     */
    public boolean isExclusive() {

        return (m_type == CmsLockType.EXCLUSIVE) || (m_type == CmsLockType.TEMPORARY);
    }

    /**
     * Returns <code>true</code> if this is an inherited lock, which may either be directly or shared inherited.<p>
     * 
     * @return <code>true</code> if this is an inherited lock, which may either be directly or shared inherited
     */
    public boolean isInherited() {

        return (m_type == CmsLockType.INHERITED) || (m_type == CmsLockType.SHARED_INHERITED);
    }

    /**
     * Returns <code>true</code> if this is an directly inherited lock.<p>
     * 
     * @return <code>true</code> if this is an directly inherited lock
     */
    public boolean isInheritedDirectly() {

        return m_type == CmsLockType.INHERITED;
    }

    /**
     * Proves if this CmsLock is the Null CmsLock.<p>
     * 
     * @return true if and only if this CmsLock is the Null CmsLock
     */
    public boolean isNullLock() {

        return this.equals(CmsLock.NULL_LOCK);
    }

    /**
     * Returns <code>true</code> if this is a persistant lock that should be saved when the systems shuts down.<p>
     * 
     * @return <code>true</code> if this is a persistant lock that should be saved when the systems shuts down
     */
    public boolean isPersistant() {

        return (m_type == CmsLockType.EXCLUSIVE) || (m_type == CmsLockType.WORKFLOW);
    }

    /**
     * Returns <code>true</code> if this is a shared lock.<p>
     * 
     * @return <code>true</code> if this is a shared lock
     */
    public boolean isShared() {

        return (m_type == CmsLockType.SHARED_EXCLUSIVE) || (m_type == CmsLockType.SHARED_INHERITED);
    }

    /**
     * Returns <code>true</code> if this is a temporary lock.<p>
     * 
     * @return <code>true</code> if this is a temporary lock
     */
    public boolean isTemporary() {

        return m_type == CmsLockType.TEMPORARY;
    }

    /**
     * Returns <code>true</code> if this lock is the <code>NULL</code> lock which can 
     * be obtained by {@link #getNullLock()}.<p>
     * 
     * @return <code>true</code> if this lock is the <code>NULL</code> lock
     */
    public boolean isUnlocked() {

        return m_type == CmsLockType.UNLOCKED;
    }

    /**
     * Returns <code>true</code> if this is a workflow lock.<p>
     * 
     * @return <code>true</code> if this is a workflow lock
     */
    public boolean isWorkflow() {

        return m_type == CmsLockType.WORKFLOW;
    }

    /**
     * Builds a string representation of the current state.<p>
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();

        buf.append("resource: ");
        buf.append(this.getResourceName());
        buf.append(" type: ");
        buf.append(m_type.toString());
        buf.append(" project: ");
        buf.append(this.getProjectId());
        buf.append(" user: ");
        buf.append(this.getUserId());

        return buf.toString();
    }
}