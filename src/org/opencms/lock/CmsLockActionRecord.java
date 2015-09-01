/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

/**
 * Bean class which keeps track of a locking action performed on a resource.<p>
 */
public class CmsLockActionRecord {

    /** A type for the different locking actions. */
    public enum LockChange {
        /** The lock was changed. */
        changed, /** The resource was locked. */
        locked, /** The lock state was not modified. */
        unchanged;
    }

    /** The lock state after the action. */
    private CmsLock m_lock;

    /** The action type. */
    private LockChange m_change;

    /**
     * Creates a new instance.<p>
     *
     * @param lock the lock state after the action
     * @param change the action type
     */
    public CmsLockActionRecord(CmsLock lock, LockChange change) {

        m_lock = lock;
        m_change = change;
    }

    /**
     * Gets the change type.<p>
     *
     * @return the change type
     */
    public LockChange getChange() {

        return m_change;
    }

    /**
     * Gets the lock state after the action.<p>
     *
     * @return the lock state after the action
     */
    public CmsLock getLock() {

        return m_lock;
    }

}
