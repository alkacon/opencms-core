/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLock.java,v $
 * Date   : $Date: 2003/07/17 12:00:40 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.lock;

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;

/**
 * Represents the lock state of a VFS resource.<p>
 * 
 * The lock state is combination of by whom, how and in which project
 * a resource is currently locked.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/07/17 12:00:40 $
 * @since 5.1.4
 */
public class CmsLock extends Object implements Serializable, Cloneable {

    public static final int C_LOCK_STATE_DIRECT_LOCKED = 2;

    public static final int C_LOCK_STATE_INDIRECT_LOCKED = 3;

    public static final int C_LOCK_STATE_UNLOCKED = 1;

    private static final CmsLock C_NULL_LOCK = new CmsLock("", CmsUUID.getNullUUID(), I_CmsConstants.C_UNKNOWN_ID);

    /** The ID of the project where the resource is currently locked */
    private int m_projectId;
    
    /** The name of the locked resource */
    private String m_resourceName;

    /** The ID of the user who currently locked the resource */
    private CmsUUID m_userId;

    /**
     * Constructor for a new Cms lock.<p>
     * 
     * @param userId the ID of the user
     * @param projectId the ID of the project
     */
    public CmsLock(String resourceName, CmsUUID userId, int projectId) {
        m_resourceName = resourceName;
        m_userId = userId;
        m_projectId = projectId;
    }

    /**
     * Returnes the shared Null CmsLock.<p>
     * 
     * @return the shared Null CmsLock
     */
    public static CmsLock getNullLock() {
        return CmsLock.C_NULL_LOCK;
    }

    /**
     * Compares this lock to the specified object.<p>
     * 
     * @param object the object to compare to
     * @return true if and only if member values of this CmsLock are the same with the compared CmsLock 
     */
    public boolean equals(Object object) {
        CmsLock otherLock = null;

        if (object instanceof CmsLock) {
            otherLock = (CmsLock) object;
            if (otherLock.getResourceName().equals(getResourceName()) && otherLock.getUserId().equals(getUserId()) && otherLock.getProjectId() == getProjectId()) {
                return true;
            }
        }

        return false;
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
     * Returns the ID of the user who currently locked the resource.<p>
     * 
     * @return the ID of the user
     */
    public CmsUUID getUserId() {
        return m_userId;
    }

    /**
     * Proves if this CmsLock is the Null CmsLock.<p>
     * 
     * @return true if and only if this CmsLock is the Null CmsLock
     */
    public boolean isNullLock() {
        return this.equals(CmsLock.C_NULL_LOCK);
    }

}
