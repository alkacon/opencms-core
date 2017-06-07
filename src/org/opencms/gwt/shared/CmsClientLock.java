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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client side lock class.<p>
 *
 * @since 8.0.0
 */
public class CmsClientLock implements IsSerializable {

    /**
     * The available lock types. Replace with {@link org.opencms.lock.CmsLockType} as soon that fulfills the serializable convention.
     */
    public enum LockType {
        /**
         * A lock that allows the user to edit the resource's structure record,
         * it's resource record, and its content record.<p>
         *
         * This lock is assigned to files that are locked via the context menu.
         */
        EXCLUSIVE(4),

        /** A lock that is inherited from a locked parent folder. */
        INHERITED(3),

        /** A lock that indicates that the resource is waiting to be published in the publish queue. */
        PUBLISH(7),

        /**
         * A lock that allows the user to edit the resource's structure record only,
         * but not it's resource record nor content record.<p>
         *
         * This lock is assigned to files if a sibling of the resource record has
         * already an exclusive lock.
         */
        SHARED_EXCLUSIVE(2),

        /**
         * A lock that allows the user to edit the resource's structure record only,
         * but not it's resource record nor content record.<p>
         *
         * This lock is assigned to resources that already have a shared exclusive lock,
         * and then inherit a lock because one if it's parent folders gets locked.
         */
        SHARED_INHERITED(1),

        /**
         * A temporary exclisive lock that allows the user to edit the resource's structure record,
         * it's resource record, and its content record.<p>
         *
         * This lock is identical to the {@link #EXCLUSIVE} lock, but it is automatically removed after
         * a user is logged out.<p>
         */
        TEMPORARY(6),

        /** Type of the NULL lock obtained by {@link org.opencms.lock.CmsLock#getNullLock()}. */
        SYSTEM_UNLOCKED(8),

        /** Type of the NULL system lock. */
        UNLOCKED(0);

        /** The lock mode/type. */
        private int m_mode;

        /**
         * Constructor.<p>
         *
         * @param mode the lock mode/type
         */
        LockType(int mode) {

            m_mode = mode;
        }

        /**
         * Returns the lock type according to the given mode.<p>
         *
         * @param mode the lock mode/type int
         *
         * @return the lock type
         */
        public static LockType valueOf(int mode) {

            switch (mode) {
                case 1:
                    return SHARED_INHERITED;
                case 2:
                    return SHARED_EXCLUSIVE;
                case 3:
                    return INHERITED;
                case 4:
                    return EXCLUSIVE;
                case 6:
                    return TEMPORARY;
                case 7:
                    return PUBLISH;
                case 8:
                    return SYSTEM_UNLOCKED;
                default:
                    return UNLOCKED;
            }
        }

        /**
         * Returns <code>true</code> if this lock is in fact unlocked.<p>
         *
         * Only if this is <code>true</code>, the result lock is equal to the <code>NULL</code> lock,
         * which can be obtained by {@link org.opencms.lock.CmsLock#getNullLock()}.<p>
         *
         * @return <code>true</code> if this lock is in fact unlocked
         */
        public boolean isUnlocked() {

            return ((this == UNLOCKED) || (this == SYSTEM_UNLOCKED));
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            switch (m_mode) {
                case 1:
                    return "shared inherited";
                case 2:
                    return "shared exclusive";
                case 3:
                    return "inherited";
                case 4:
                    return "exclusive";
                case 6:
                    return "temporary exclusive";
                case 7:
                    return "publish";
                case 8:
                    return "system unlocked";
                default:
                    return "unlocked";
            }
        }

        /**
         * Return the lock mode/type.<p>
         *
         * @return the lock mode/type
         */
        public int getMode() {

            return m_mode;
        }
    }

    /** Flag to indicate if the current lock is owned by the current user. */
    private boolean m_isOwnedByUser;

    /** The lock owner name. */
    private String m_lockOwner;

    /** The lock type. */
    private LockType m_lockType;

    /**
     * Default constructor for serialization.<p>
     */
    public CmsClientLock() {

        // nothing to do
    }

    /**
     * Returns the lock owner name.<p>
     *
     * @return the lock owner name
     */
    public String getLockOwner() {

        return m_lockOwner;
    }

    /**
     * Returns the lock type.<p>
     *
     * @return the lock type
     */
    public LockType getLockType() {

        return m_lockType;
    }

    /**
     * Returns if the current lock is owned by the current user.<p>
     *
     * @return if the current lock is owned by the current user
     */
    public boolean isOwnedByUser() {

        return m_isOwnedByUser;
    }

    /**
     * Sets the lock owner name.<p>
     *
     * @param lockOwner the lock owner name to set
     */
    public void setLockOwner(String lockOwner) {

        m_lockOwner = lockOwner;
    }

    /**
     * Sets the lock type.<p>
     *
     * @param lockType the lock type to set
     */
    public void setLockType(LockType lockType) {

        m_lockType = lockType;
    }

    /**
     * Sets if the current lock is owned by the current user.<p>
     *
     * @param isOwnedByUser if the current lock is owned by the current user
     */
    public void setOwnedByUser(boolean isOwnedByUser) {

        m_isOwnedByUser = isOwnedByUser;
    }
}
