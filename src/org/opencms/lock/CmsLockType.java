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

package org.opencms.lock;

import org.opencms.util.A_CmsModeIntEnumeration;

/**
 * Indicates the different possible lock types.<p>
 *
 * @since 7.0.0
 */
public final class CmsLockType extends A_CmsModeIntEnumeration {

    /**
     * A lock that allows the user to edit the resource's structure record,
     * it's resource record, and its content record.<p>
     *
     * This lock is assigned to files that are locked via the context menu.
     */
    public static final CmsLockType EXCLUSIVE = new CmsLockType(4);

    /** A lock that is inherited from a locked parent folder. */
    public static final CmsLockType INHERITED = new CmsLockType(3);

    /** A lock that indicates that the resource is waiting to be published in the publish queue. */
    public static final CmsLockType PUBLISH = new CmsLockType(7);

    /**
     * A lock that allows the user to edit the resource's structure record only,
     * but not it's resource record nor content record.<p>
     *
     * This lock is assigned to files if a sibling of the resource record has
     * already an exclusive lock.
     */
    public static final CmsLockType SHARED_EXCLUSIVE = new CmsLockType(2);

    /**
     * A lock that allows the user to edit the resource's structure record only,
     * but not it's resource record nor content record.<p>
     *
     * This lock is assigned to resources that already have a shared exclusive lock,
     * and then inherit a lock because one of it's parent folders gets locked.
     */
    public static final CmsLockType SHARED_INHERITED = new CmsLockType(1);

    /**
     * A temporary exclusive lock that allows the user to edit the resource's structure record,
     * it's resource record, and its content record.<p>
     *
     * This lock is identical to the {@link #EXCLUSIVE} lock, but it is automatically removed after
     * a user is logged out.<p>
     */
    public static final CmsLockType TEMPORARY = new CmsLockType(6);

    /** Type of the NULL lock obtained by {@link CmsLock#getNullLock()}. */
    public static final CmsLockType UNLOCKED = new CmsLockType(0);

    /** Type of the NULL system lock. */
    protected static final CmsLockType SYSTEM_UNLOCKED = new CmsLockType(8);

    /** serializable version id. */
    private static final long serialVersionUID = 5333767594124738789L;

    /**
     * Creates a new lock type with the given name.<p>
     *
     * @param type the type id to use
     */
    private CmsLockType(int type) {

        super(type);
    }

    /**
     * Returns the lock type for the given type value.<p>
     *
     * This is used only for serialization and should not be accessed for other purposes.<p>
     *
     * @param type the type value to get the lock type for
     *
     * @return the lock type for the given type value
     */
    public static CmsLockType valueOf(int type) {

        switch (type) {
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
     * Returns <code>true</code> if this is an directly inherited lock.<p>
     *
     * @return <code>true</code> if this is an directly inherited lock
     */
    public boolean isDirectlyInherited() {

        return (this == CmsLockType.INHERITED);
    }

    /**
     * Returns <code>true</code> if this is an exclusive (or temporary exclusive) lock.<p>
     *
     * @return <code>true</code> if this is an exclusive (or temporary exclusive) lock
     */
    public boolean isExclusive() {

        return (this == CmsLockType.EXCLUSIVE) || (this == CmsLockType.TEMPORARY);
    }

    /**
     * Returns <code>true</code> if this is an inherited lock, which may either be directly or shared inherited.<p>
     *
     * @return <code>true</code> if this is an inherited lock, which may either be directly or shared inherited
     */
    public boolean isInherited() {

        return (isDirectlyInherited() || isSharedInherited());
    }

    /**
     * Returns <code>true</code> if this is a persistent lock that should be saved when the systems shuts down.<p>
     *
     * @return <code>true</code> if this is a persistent lock that should be saved when the systems shuts down
     */
    public boolean isPersistent() {

        return (this == CmsLockType.EXCLUSIVE) || isPublish();
    }

    /**
     * Returns <code>true</code> if this is a publish lock.<p>
     *
     * @return <code>true</code> if this is a publish lock
     */
    public boolean isPublish() {

        return (this == CmsLockType.PUBLISH);
    }

    /**
     * Returns <code>true</code> if this is a shared lock.<p>
     *
     * @return <code>true</code> if this is a shared lock
     */
    public boolean isShared() {

        return (isSharedExclusive() || isSharedInherited());
    }

    /**
     * Returns <code>true</code> if this is an shared exclusive lock.<p>
     *
     * @return <code>true</code> if this is an shared exclusive lock
     */
    public boolean isSharedExclusive() {

        return (this == CmsLockType.SHARED_EXCLUSIVE);
    }

    /**
     * Returns <code>true</code> if this is an shared inherited lock.<p>
     *
     * @return <code>true</code> if this is an shared inherited lock
     */
    public boolean isSharedInherited() {

        return (this == CmsLockType.SHARED_INHERITED);
    }

    /**
     * Returns <code>true</code> if this is a system (2nd level) lock.<p>
     *
     * @return <code>true</code> if this is a system (2nd level) lock
     */
    public boolean isSystem() {

        return (isPublish() || (this == CmsLockType.SYSTEM_UNLOCKED));
    }

    /**
     * Returns <code>true</code> if this is a temporary lock.<p>
     *
     * @return <code>true</code> if this is a temporary lock
     */
    public boolean isTemporary() {

        return (this == CmsLockType.TEMPORARY);
    }

    /**
     * Returns <code>true</code> if this lock is in fact unlocked.<p>
     *
     * Only if this is <code>true</code>, the result lock is equal to the <code>NULL</code> lock,
     * which can be obtained by {@link CmsLock#getNullLock()}.<p>
     *
     * @return <code>true</code> if this lock is in fact unlocked
     */
    public boolean isUnlocked() {

        return ((this == CmsLockType.UNLOCKED) || (this == CmsLockType.SYSTEM_UNLOCKED));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        switch (getMode()) {
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
}